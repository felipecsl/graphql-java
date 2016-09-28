package graphql.execution.batched;

import java.util.Map;

class GraphQLExecutionNodeDatum extends GraphQLExecutionResultContainer {
  private final Map<String, Object> parentResult;
  private final Object source;

  GraphQLExecutionNodeDatum(Map<String, Object> parentResult, Object source) {
    this.parentResult = parentResult;
    this.source = source;
  }

  Map<String, Object> getParentResult() {
    return parentResult;
  }

  @Override
  public void putResult(String fieldName, Object value) {
    parentResult.put(fieldName, value);
  }

  public Object getSource() {
    return source;
  }
}
