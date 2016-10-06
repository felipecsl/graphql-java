package graphql.execution;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntrospectionExecutionStrategy extends SimpleExecutionStrategy {
  public IntrospectionExecutionStrategy(ExecutionContext executionContext) {
    super(executionContext);
  }

  @Override
  public ExecutionResult execute(GraphQLObjectType parentType, @Nullable Object source,
      Map<String, List<Field>> fields) {
    Map<String, Object> results = new LinkedHashMap<>(1);
    List<Object> fieldResults = new ArrayList<>(fields.keySet().size());
    for (Map.Entry<String, List<Field>> entry : fields.entrySet()) {
      ExecutionResult resolvedResult = resolveField(parentType, source, entry.getValue());
      if (resolvedResult != null) {
        fieldResults.add(resolvedResult.getData());
      }
    }
    results.put("name", source);
    results.put("type", parentType.getName());
    results.put("fields", fieldResults);
    return new ExecutionResultImpl(results, executionContext.getErrors());
  }

  @Override protected Object resolveValue(GraphQLFieldDefinition fieldDef,
      DataFetchingEnvironment environment) {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("name", fieldDef.getName());
    map.put("type", fieldDef.getType().getName());
    return map;
  }
}
