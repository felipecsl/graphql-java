package graphql.language;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Field extends AbstractNode implements Selection {
  private final String name;
  @Nullable private String alias;
  @Nullable private List<Argument> arguments = new ArrayList<>();
  @Nullable private List<Directive> directives = new ArrayList<>();
  @Nullable private SelectionSet selectionSet;

  public Field() {
    this(null);
  }

  public Field(String name) {
    this.name = name;
  }

  public Field(String name, @Nullable SelectionSet selectionSet) {
    this.name = name;
    this.selectionSet = selectionSet;
  }


  public Field(String name, @Nullable List<Argument> arguments) {
    this.name = name;
    this.arguments = arguments;
  }

  public Field(String name, @Nullable List<Argument> arguments,
      @Nullable List<Directive> directives) {
    this.name = name;
    this.arguments = arguments;
    this.directives = directives;
  }

  public Field(String name, @Nullable List<Argument> arguments,
      @Nullable SelectionSet selectionSet) {
    this.name = name;
    this.arguments = arguments;
    this.selectionSet = selectionSet;
  }

  @Override
  public List<Node> getChildren() {
    List<Node> result = new ArrayList<>();
    if (arguments != null) {
      result.addAll(arguments);
    }
    if (directives != null) {
      result.addAll(directives);
    }
    if (selectionSet != null) result.add(selectionSet);
    return result;
  }

  public String getName() {
    return name;
  }

  @Nullable public String getAlias() {
    return alias;
  }

  public void setAlias(@Nullable String alias) {
    this.alias = alias;
  }

  @Nullable public List<Argument> getArguments() {
    return arguments;
  }

  public void setArguments(@Nullable List<Argument> arguments) {
    this.arguments = arguments;
  }

  @Nullable public List<Directive> getDirectives() {
    return directives;
  }

  @Nullable public SelectionSet getSelectionSet() {
    return selectionSet;
  }

  public void setSelectionSet(@Nullable SelectionSet selectionSet) {
    this.selectionSet = selectionSet;
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Field field = (Field) o;

    if (name != null ? !name.equals(field.name) : field.name != null) return false;
    return !(alias != null ? !alias.equals(field.alias) : field.alias != null);

  }

  @Override
  public String toString() {
    return "Field{" + "name='" + name + '\'' + ", alias='" + alias + '\'' + ", arguments=" +
        arguments + ", directives=" + directives + ", selectionSet=" + selectionSet + '}';
  }
}
