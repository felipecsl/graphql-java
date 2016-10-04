package graphql.execution;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleExecutionStrategy extends ExecutionStrategy {
  @Override public ExecutionResult execute(ExecutionContext executionContext,
      GraphQLObjectType parentType, @Nullable Object source, Map<String, List<Field>> fields) {
    Map<String, Object> results = new LinkedHashMap<>();
    for (Map.Entry<String, List<Field>> entry : fields.entrySet()) {
      ExecutionResult resolvedResult = resolveField(executionContext, parentType, source,
          entry.getValue());
      results.put(entry.getKey(), resolvedResult != null ? resolvedResult.getData() : null);
    }
    return new ExecutionResultImpl(results, executionContext.getErrors());
  }
}
