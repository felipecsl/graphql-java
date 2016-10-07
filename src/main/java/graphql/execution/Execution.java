package graphql.execution;

import graphql.ExecutionResult;
import graphql.GraphQLException;
import graphql.execution.batched.BatchedExecutionStrategy;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Execution {
  private final FieldCollector fieldCollector = new FieldCollector();
  private final ExecutionStrategy.Type strategyType;

  public Execution(ExecutionStrategy.Type strategyType) {
    this.strategyType = strategyType;
  }

  public ExecutionResult execute(GraphQLSchema graphQLSchema, @Nullable Object root,
      Document document, @Nullable String operationName, Map<String, Object> args) {
    ExecutionContext executionContext = new ExecutionContextBuilder(new ValuesResolver(),
        graphQLSchema).build(root, document, operationName, args);
    return executeOperation(executionContext, root);
  }

  private GraphQLObjectType getOperationRootType(GraphQLSchema graphQLSchema,
      OperationDefinition operationDefinition) {
    if (operationDefinition.getOperation() == OperationDefinition.Operation.MUTATION) {
      return graphQLSchema.getMutationType();
    } else if (operationDefinition.getOperation() == OperationDefinition.Operation.QUERY) {
      return graphQLSchema.getQueryType();
    } else {
      throw new GraphQLException();
    }
  }

  private ExecutionResult executeOperation(ExecutionContext executionContext,
      @Nullable Object root) {
    OperationDefinition operationDefinition = executionContext.getOperationDefinition();
    GraphQLObjectType operationRootType = getOperationRootType(executionContext.getGraphQLSchema(),
        operationDefinition);
    Map<String, List<Field>> fields = new LinkedHashMap<>();
    fieldCollector.collectFields(executionContext, operationRootType,
        operationDefinition.getSelectionSet(), new ArrayList<String>(), fields);
    ExecutionStrategy strategy = strategyForType(executionContext);
    if (operationDefinition.getOperation() == OperationDefinition.Operation.MUTATION) {
      return new SimpleExecutionStrategy(executionContext, strategy)
          .execute(operationRootType, null, root, fields);
    } else {
      return strategy.execute(operationRootType, null, root, fields);
    }
  }

  private ExecutionStrategy strategyForType(ExecutionContext executionContext) {
    switch (strategyType) {
      case Simple:
        return new SimpleExecutionStrategy(executionContext);
      case Batched:
        return new BatchedExecutionStrategy(executionContext);
      case ExecutorService:
        return new ExecutorServiceExecutionStrategy(executionContext);
      case Introspection:
        return new IntrospectionExecutionStrategy(executionContext);
      default:
        throw new IllegalArgumentException("strategyType");
    }
  }
}
