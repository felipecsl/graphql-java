package graphql.execution;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.language.Field;
import graphql.schema.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class IntrospectionExecutionStrategy extends SimpleExecutionStrategy {
  IntrospectionExecutionStrategy(ExecutionContext executionContext) {
    super(executionContext);
  }

  @Override public ExecutionResult execute(GraphQLObjectType parentType, @Nullable Field
      parentField, @Nullable Object source, Map<String, List<Field>> fields) {
    Map<String, Object> results = new LinkedHashMap<>(1);
    List<Object> fieldResults = collectFields(parentType, source, fields);
    String nameKey = parentField != null ? "name" : "operationName";
    Object nameValue = parentField != null
        ? parentField.getAlias() : executionContext.getOperationDefinition().getName();
    results.put(nameKey, nameValue);
    results.put("type", parentType.getName());
    results.put("fields", fieldResults);
    return new ExecutionResultImpl(results, executionContext.getErrors());
  }

  private List<Object> collectFields(GraphQLObjectType parentType, @Nullable Object source,
      Map<String, List<Field>> fields) {
    List<Object> fieldResults = new ArrayList<>(fields.keySet().size());
    for (Map.Entry<String, List<Field>> entry : fields.entrySet()) {
      ExecutionResult resolvedResult = resolveField(parentType, source, entry.getValue());
      if (resolvedResult != null) {
        fieldResults.add(resolvedResult.getData());
      }
    }
    return fieldResults;
  }

  @Override protected Object resolveValue(GraphQLFieldDefinition fieldDefinition,
      DataFetchingEnvironment environment) {
    Map<String, String> map = new LinkedHashMap<>(2);
    map.put("name", fieldDefinition.getName());
    map.put("type", fieldDefinition.getType().getName());
    return map;
  }

  @Override ExecutionResult completeValueForScalar(GraphQLScalarType scalarType, Object value) {
    // Prevent us from calling toString() on a Map because the actual field is of Scalar type
    return new ExecutionResultImpl(value, null);
  }
}
