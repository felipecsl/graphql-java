package graphql.execution.batched;

import java.util.List;

class GraphQLExecutionResultList extends GraphQLExecutionResultContainer {
  private final List<Object> results;

  GraphQLExecutionResultList(List<Object> results) {
    this.results = results;
  }

  @Override
  public void putResult(String fieldName, Object value) {
    results.add(value);
  }
}
