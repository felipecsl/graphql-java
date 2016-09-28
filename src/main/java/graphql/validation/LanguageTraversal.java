package graphql.validation;

import graphql.language.Node;

import java.util.ArrayList;
import java.util.List;

public class LanguageTraversal {
  public void traverse(Node root, QueryLanguageVisitor queryLanguageVisitor) {
    traverseImpl(root, queryLanguageVisitor, new ArrayList<Node>());
  }

  private void traverseImpl(Node root, QueryLanguageVisitor queryLanguageVisitor, List<Node> path) {
    queryLanguageVisitor.enter(root, path);
    path.add(root);
    for (Node child : root.getChildren()) {
      traverseImpl(child, queryLanguageVisitor, path);
    }
    path.remove(path.size() - 1);
    queryLanguageVisitor.leave(root, path);
  }
}
