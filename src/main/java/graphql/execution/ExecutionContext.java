package graphql.execution;

import graphql.GraphQLError;
import graphql.language.FragmentDefinition;
import graphql.language.OperationDefinition;
import graphql.schema.GraphQLSchema;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static graphql.Assert.assertNotNull;

public class ExecutionContext {
  private final GraphQLSchema graphQLSchema;
  private final Map<String, FragmentDefinition> fragmentsByName;
  private final OperationDefinition operationDefinition;
  private final Map<String, Object> variables;
  private final List<GraphQLError> errors = new ArrayList<>();
  @Nullable private final Object root;

  public ExecutionContext(GraphQLSchema graphQLSchema,
      Map<String, FragmentDefinition> fragmentsByName, OperationDefinition operationDefinition,
      Map<String, Object> variables, @Nullable Object root) {
    this.graphQLSchema = assertNotNull(graphQLSchema, "graphQLSchema == null");
    this.fragmentsByName = assertNotNull(fragmentsByName, "fragmentsByName == null");
    this.operationDefinition = assertNotNull(operationDefinition, "operationDefinition == null");
    this.variables = assertNotNull(variables, "variable == null");
    this.root = root;
  }

  public GraphQLSchema getGraphQLSchema() {
    return graphQLSchema;
  }

  public OperationDefinition getOperationDefinition() {
    return operationDefinition;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  @Nullable public Object getRoot() {
    return root;
  }

  FragmentDefinition getFragment(String name) {
    return fragmentsByName.get(name);
  }

  public void addError(GraphQLError error) {
    this.errors.add(error);
  }

  public List<GraphQLError> getErrors() {
    return errors;
  }
}
