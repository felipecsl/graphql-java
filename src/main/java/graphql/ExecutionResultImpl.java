package graphql;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExecutionResultImpl implements ExecutionResult {
  private final List<GraphQLError> errors = new ArrayList<>();
  @Nullable private final Object data;

  ExecutionResultImpl(List<? extends GraphQLError> errors) {
    this.errors.addAll(errors);
    this.data = null;
  }

  public ExecutionResultImpl(@Nullable Object data, @Nullable List<? extends GraphQLError> errors) {
    this.data = data;
    if (errors != null) {
      this.errors.addAll(errors);
    }
  }

  @Override @Nullable public Object getData() {
    return data;
  }

  @Override
  public List<GraphQLError> getErrors() {
    return new ArrayList<>(errors);
  }
}
