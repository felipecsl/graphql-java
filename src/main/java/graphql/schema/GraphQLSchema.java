package graphql.schema;

import graphql.Assert;
import graphql.Directives;

import java.util.*;

import static graphql.Assert.assertNotNull;

public class GraphQLSchema {
  private final GraphQLObjectType queryType;
  private final GraphQLObjectType mutationType;
  private final Map<String, GraphQLType> typeMap;
  private final Set<GraphQLType> dictionary;

  public GraphQLSchema(GraphQLObjectType queryType) {
    this(queryType, null, Collections.<GraphQLType>emptySet());
  }

  public GraphQLSchema(GraphQLObjectType queryType, GraphQLObjectType mutationType,
      Set<GraphQLType> dictionary) {
    assertNotNull(dictionary, "dictionary can't be null");
    assertNotNull(queryType, "queryType can't be null");
    this.queryType = queryType;
    this.mutationType = mutationType;
    this.dictionary = dictionary;
    typeMap = SchemaUtil.allTypes(this, dictionary);
  }

  Set<GraphQLType> getDictionary() {
    return dictionary;
  }

  public GraphQLType getType(String typeName) {
    return typeMap.get(typeName);
  }

  public List<GraphQLType> getAllTypesAsList() {
    return new ArrayList<>(typeMap.values());
  }

  public GraphQLObjectType getQueryType() {
    return queryType;
  }

  public GraphQLObjectType getMutationType() {
    return mutationType;
  }

  public List<GraphQLDirective> getDirectives() {
    return Arrays.asList(Directives.IncludeDirective, Directives.SkipDirective);
  }

  public GraphQLDirective getDirective(String name) {
    for (GraphQLDirective directive : getDirectives()) {
      if (directive.getName().equals(name)) return directive;
    }
    return null;
  }

  boolean isSupportingMutations() {
    return mutationType != null;
  }

  public static Builder newSchema() {
    return new Builder();
  }

  public static class Builder {
    private GraphQLObjectType queryType;
    private GraphQLObjectType mutationType;

    public Builder query(GraphQLObjectType.Builder builder) {
      return query(builder.build());
    }

    public Builder query(GraphQLObjectType queryType) {
      this.queryType = queryType;
      return this;
    }

    public Builder mutation(GraphQLObjectType.Builder builder) {
      return mutation(builder.build());
    }

    public Builder mutation(GraphQLObjectType mutationType) {
      this.mutationType = mutationType;
      return this;
    }

    public GraphQLSchema build() {
      return build(Collections.<GraphQLType>emptySet());
    }

    public GraphQLSchema build(Set<GraphQLType> dictionary) {
      Assert.assertNotNull(dictionary, "dictionary can't be null");
      GraphQLSchema graphQLSchema = new GraphQLSchema(queryType, mutationType, dictionary);
      SchemaUtil.replaceTypeReferences(graphQLSchema);
      return graphQLSchema;
    }
  }

}
