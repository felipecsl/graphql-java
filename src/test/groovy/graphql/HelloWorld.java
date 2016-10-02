package graphql;


import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.junit.Test;

import java.util.Map;

import static graphql.Scalars.GraphQLString;
import static org.junit.Assert.assertEquals;

public class HelloWorld {

  public static void main(String[] args) {
    GraphQLObjectType queryType = GraphQLObjectType.newBuilder()
        .name("helloWorldQuery")
        .field(GraphQLFieldDefinition.newBuilder()
            .type(GraphQLString)
            .name("hello")
            .staticValue("world"))
        .build();

    GraphQLSchema schema = GraphQLSchema.newSchema()
        .query(queryType)
        .build();
    Map<String, Object> result =
        (Map<String, Object>) new GraphQL(schema).execute("{hello}").getData();
    System.out.println(result);
  }

  @Test
  public void helloWorldTest() {
    GraphQLObjectType queryType = GraphQLObjectType.newBuilder()
        .name("helloWorldQuery")
        .field(GraphQLFieldDefinition.newBuilder()
            .type(GraphQLString)
            .name("hello")
            .staticValue("world"))
        .build();

    GraphQLSchema schema = GraphQLSchema.newSchema()
        .query(queryType)
        .build();
    Map<String, Object> result =
        (Map<String, Object>) new GraphQL(schema).execute("{hello}").getData();
    assertEquals("world", result.get("hello"));
  }
}
