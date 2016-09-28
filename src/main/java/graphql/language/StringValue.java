package graphql.language;

import java.util.ArrayList;
import java.util.List;

public class StringValue extends AbstractNode implements Value {
  private final String value;

  public StringValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public List<Node> getChildren() {
    return new ArrayList<>();
  }

  @Override
  public String toString() {
    return "StringValue{" + "value='" + value + '\'' + '}';
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StringValue that = (StringValue) o;

    return !(value != null ? !value.equals(that.value) : that.value != null);
  }
}
