package graphql

import graphql.schema.*

import static graphql.Scalars.GraphQLString
import static graphql.schema.GraphQLArgument.newArgument

class TestUtil {


  static GraphQLSchema schemaWithInputType(GraphQLInputType inputType) {
    GraphQLArgument.Builder fieldArgument = newArgument().name("arg").type(inputType)
    GraphQLFieldDefinition.Builder name = GraphQLFieldDefinition.newBuilder()
        .name("name").type(GraphQLString).argument(fieldArgument)
    GraphQLObjectType queryType = GraphQLObjectType.newBuilder().name("query").field(name).build()
    new GraphQLSchema(queryType)
  }

  static dummySchema = GraphQLSchema.newSchema()
      .query(GraphQLObjectType.newBuilder()
      .name("QueryType")
      .build())
      .build()
}
