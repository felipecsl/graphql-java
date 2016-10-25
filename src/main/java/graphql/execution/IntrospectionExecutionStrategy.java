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
  // A list of name/type pairs of all variables used for this query
  private final List<Map<String, String>> allVariables = new ArrayList<>();

  IntrospectionExecutionStrategy(ExecutionContext executionContext) {
    super(executionContext);
  }

  @Override public ExecutionResult execute(GraphQLObjectType parentType, @Nullable Field
      parentField, @Nullable Object source, Map<String, List<Field>> fields) {
    Map<String, Object> results = new LinkedHashMap<>(1);
    List<Object> fieldResults = collectFields(parentType, source, fields);
    results.put(parentField != null ? "name" : "operationName", resolveName(parentField));
    if (parentField != null) {
      results.put("type", parentType.getName());
    } else {
      results.put("variables", allVariables);
    }
    results.put("fields", fieldResults);
    return new ExecutionResultImpl(results, executionContext.getErrors());
  }

  private String resolveName(@Nullable Field parentField) {
    if (parentField != null) {
      return parentField.getAlias() != null ? parentField.getAlias() : parentField.getName();
    } else {
      // We're in the root of the query, so just use the operation name, if any
      return executionContext.getOperationDefinition().getName();
    }
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
    Map<String, Object> map = new LinkedHashMap<>(2);
    map.put("name", fieldDefinition.getName());
    map.put("type", fieldDefinition.getType().getName());
    addFieldVariables(fieldDefinition);
    return map;
  }

  private void addFieldVariables(GraphQLFieldDefinition fieldDefinition) {
    if (!fieldDefinition.getArguments().isEmpty()) {
      List<Map<String, String>> variables = new ArrayList<>();
      for (GraphQLArgument argument : fieldDefinition.getArguments()) {
        String name = argument.getName();
        String type = argument.getType().getName();
        if (!hasVariable(name, type)) {
          Map<String, String> variable = new LinkedHashMap<>(2);
          variable.put("name", name);
          variable.put("type", type);
          variables.add(variable);
        }
      }
      if (!variables.isEmpty()) {
        allVariables.addAll(variables);
      }
    }
  }

  private boolean hasVariable(String name, String type) {
    for (Map<String, String> variable : allVariables) {
      if (variable.get("name").equals(name) && variable.get("type").equals(type)) {
        return true;
      }
    }
    return false;
  }

  @Override ExecutionResult completeValueForScalar(GraphQLScalarType scalarType,
      Object value) {
    // Prevent us from calling toString() on a Map because the actual field is of Scalar type
    return new ExecutionResultImpl(value, null);
  }

  @Override protected ExecutionResult completeValueForList(GraphQLList fieldType,
      List<Field> fields, Object result) {
    return new ExecutionResultImpl(result, null);
  }
}
