package graphql;

import java.util.ArrayList;
import java.util.List;

public class ExecutionResultImpl implements ExecutionResult {
  private final List<GraphQLError> errors = new ArrayList<>();
  private final Object data;

  ExecutionResultImpl(List<? extends GraphQLError> errors) {
    this.errors.addAll(errors);
    this.data = null;
  }

  public ExecutionResultImpl(Object data, List<? extends GraphQLError> errors) {
    this.data = data;

    if (errors != null) {
      this.errors.addAll(errors);
    }
  }

  @Override
  public Object getData() {
    return data;
  }

  @Override
  public List<GraphQLError> getErrors() {
    return new ArrayList<>(errors);
  }
}
