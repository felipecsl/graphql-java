package graphql.execution.batched;

class GraphQLExecutionNodeValue {
  private final GraphQLExecutionResultContainer resultContainer;
  private final Object value;

  GraphQLExecutionNodeValue(GraphQLExecutionResultContainer resultContainer, /*Nullable*/
      Object value) {
    this.resultContainer = resultContainer;
    this.value = value;
  }

  GraphQLExecutionResultContainer getResultContainer() {
    return resultContainer;
  }

  /*Nullable*/
  public Object getValue() {
    return value;
  }
}
