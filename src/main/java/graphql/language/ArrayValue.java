package graphql.language;

import java.util.ArrayList;
import java.util.List;

public class ArrayValue extends AbstractNode implements Value {
  private final List<Value> values;

  public ArrayValue() {
    this(new ArrayList<Value>());
  }

  public ArrayValue(List<Value> values) {
    this.values = values;
  }

  public List<Value> getValues() {
    return values;
  }

  @Override
  public List<Node> getChildren() {
    return new ArrayList<Node>(values);
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    return true;
  }

  @Override
  public String toString() {
    return "ArrayValue{" + "values=" + values + '}';
  }
}
