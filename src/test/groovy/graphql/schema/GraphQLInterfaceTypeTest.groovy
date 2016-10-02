package graphql.schema

import graphql.AssertException
import spock.lang.Specification

import static graphql.Scalars.GraphQLString
import static graphql.schema.GraphQLFieldDefinition.newBuilder
import static graphql.schema.GraphQLInterfaceType.newInterface

class GraphQLInterfaceTest extends Specification {

  def "duplicate field definition fails"() {
    when:
    newInterface().name("TestInterfaceType")
        .typeResolver(new TypeResolverProxy())
        .field(newBuilder().name("NAME").type(GraphQLString))
        .field(newBuilder().name("NAME").type(GraphQLString))
        .build();
    then:
    thrown(AssertException)
  }
}
