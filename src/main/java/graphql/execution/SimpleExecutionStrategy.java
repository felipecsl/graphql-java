package graphql.execution;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SimpleExecutionStrategy extends ExecutionStrategy {
  SimpleExecutionStrategy(ExecutionContext executionContext) {
    super(executionContext);
  }

  SimpleExecutionStrategy(ExecutionContext executionContext, ExecutionStrategy strategy) {
    super(executionContext, strategy);
  }

  @Override public ExecutionResult execute(GraphQLObjectType parentType, @Nullable Field parentField,
      @Nullable Object source, Map<String, List<Field>> fields) {
    Map<String, Object> results = new LinkedHashMap<>();
    for (Map.Entry<String, List<Field>> entry : fields.entrySet()) {
      ExecutionResult resolvedResult = resolveField(parentType, source, entry.getValue());
      results.put(entry.getKey(), resolvedResult != null ? resolvedResult.getData() : null);
    }
    return new ExecutionResultImpl(results, executionContext.getErrors());
  }
}
