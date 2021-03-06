package graphql.execution;

import graphql.ExecutionResult;
import graphql.GraphQLException;
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
  private final ExecutionStrategy strategy;

  public Execution(@Nullable ExecutionStrategy strategy) {
    this.strategy = strategy != null ? strategy : new SimpleExecutionStrategy();
  }

  public ExecutionResult execute(GraphQLSchema graphQLSchema, @Nullable Object root,
      Document document, @Nullable String operationName, Map<String, Object> args) {
    ExecutionContext executionContext = new ExecutionContextBuilder(new ValuesResolver(),
        graphQLSchema).build(strategy, root, document, operationName, args);
    return executeOperation(executionContext, root, executionContext.getOperationDefinition());
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

  private ExecutionResult executeOperation(ExecutionContext executionContext, @Nullable Object root,
      OperationDefinition operationDefinition) {
    GraphQLObjectType operationRootType =
        getOperationRootType(executionContext.getGraphQLSchema(), operationDefinition);

    Map<String, List<Field>> fields = new LinkedHashMap<>();
    fieldCollector.collectFields(executionContext, operationRootType,
        operationDefinition.getSelectionSet(), new ArrayList<String>(), fields);

    if (operationDefinition.getOperation() == OperationDefinition.Operation.MUTATION) {
      return new SimpleExecutionStrategy()
          .execute(executionContext, operationRootType, root, fields);
    } else {
      return strategy.execute(executionContext, operationRootType, root, fields);
    }
  }
}
