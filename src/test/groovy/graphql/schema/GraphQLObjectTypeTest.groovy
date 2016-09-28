package graphql.schema

import graphql.AssertException
import spock.lang.Specification

import static graphql.Scalars.GraphQLString

class GraphQLObjectTypeTest extends Specification {

  def "duplicate field definition fails"() {
    when:
    GraphQLObjectType.newBuilder()
        .name("TestObjectType")
        .field(GraphQLFieldDefinition.newBuilder()
        .name("NAME")
        .type(GraphQLString))
        .field(GraphQLFieldDefinition.newBuilder()
        .name("NAME")
        .type(GraphQLString))
        .build();
    then:
    thrown(AssertException)
  }
}
