package graphql

import graphql.language.SourceLocation
import graphql.schema.*
import graphql.validation.ValidationErrorType
import spock.lang.Specification

import static graphql.Scalars.GraphQLString
import static graphql.schema.GraphQLArgument.newArgument
import static graphql.schema.GraphQLObjectType.newBuilder
import static graphql.schema.GraphQLSchema.newSchema

class GraphQLTest extends Specification {
  def "simple query"() {
    given:
    GraphQLFieldDefinition.Builder fieldDefinition = GraphQLFieldDefinition.newBuilder()
        .name("hello")
        .type(GraphQLString)
        .staticValue("world")
    GraphQLSchema schema = newSchema().query(
        newBuilder()
            .name("RootQueryType")
            .field(fieldDefinition)
            .build()
    ).build()

    when:
    def result = new GraphQL(schema).execute('{ hello }').data

    then:
    result == [hello: 'world']

  }

  def "query with sub-fields"() {
    given:
    GraphQLObjectType heroType = GraphQLObjectType.newBuilder()
        .name("heroType")
        .field(GraphQLFieldDefinition.newBuilder()
        .name("id")
        .type(GraphQLString))
        .field(GraphQLFieldDefinition.newBuilder()
        .name("name")
        .type(GraphQLString))
        .build()

    GraphQLFieldDefinition.Builder simpsonField = GraphQLFieldDefinition.newBuilder()
        .name("simpson")
        .type(heroType)
        .staticValue([id: '123', name: 'homer'])

    GraphQLSchema graphQLSchema = newSchema()
        .query(newBuilder()
        .name("RootQueryType")
        .field(simpsonField)
        .build()
    ).build();

    when:
    def result = new GraphQL(graphQLSchema).execute('{ simpson { id, name } }').data

    then:
    result == [simpson: [id: '123', name: 'homer']]
  }

  def "query with validation errors"() {
    given:
    GraphQLFieldDefinition.Builder fieldDefinition = GraphQLFieldDefinition.newBuilder()
        .name("hello")
        .type(GraphQLString)
        .argument(newArgument().name("arg").type(GraphQLString))
        .staticValue("world")
    GraphQLSchema schema = newSchema().query(
        newBuilder()
            .name("RootQueryType")
            .field(fieldDefinition)
            .build()
    ).build()

    when:
    def errors = new GraphQL(schema).execute('{ hello(arg:11) }').errors

    then:
    errors.size() == 1
  }

  def "query with invalid syntax"() {
    given:
    GraphQLSchema schema = newSchema().query(
        newBuilder()
            .name("RootQueryType")
            .build()
    ).build()

    when:
    def errors = new GraphQL(schema).execute('{ hello(() }').errors

    then:
    errors.size() == 1
    errors[0].errorType == ErrorType.InvalidSyntax
    errors[0].sourceLocations == [new SourceLocation(1, 8)]
  }

  def "query with invalid syntax 2"() {
    given:
    GraphQLSchema schema = newSchema().query(
        newBuilder()
            .name("RootQueryType")
            .build()
    ).build()

    when:
    def errors = new GraphQL(schema).execute('{ hello[](() }').errors

    then:
    errors.size() == 1
    errors[0].errorType == ErrorType.InvalidSyntax
    errors[0].sourceLocations == [new SourceLocation(1, 7)]
  }

  def "non null argument is missing"() {
    given:
    GraphQLSchema schema = newSchema().query(
        newBuilder()
            .name("RootQueryType")
            .field(GraphQLFieldDefinition.newBuilder()
            .name("field")
            .type(GraphQLString)
            .argument(newArgument()
            .name("arg")
            .type(new GraphQLNonNull(GraphQLString))))
            .build()
    ).build()

    when:
    def errors = new GraphQL(schema).execute('{ field }').errors

    then:
    errors.size() == 1
    errors[0].errorType == ErrorType.ValidationError
    errors[0].validationErrorType == ValidationErrorType.MissingFieldArgument
    errors[0].sourceLocations == [new SourceLocation(1, 3)]
  }

  def "`Iterable` can be used as a `GraphQLList` field result"() {
    given:
    def set = new HashSet<String>()
    set.add("One")
    set.add("Two")

    def schema = GraphQLSchema.newSchema()
        .query(GraphQLObjectType.newBuilder()
        .name("QueryType")
        .field(GraphQLFieldDefinition.newBuilder()
        .name("set")
        .type(new GraphQLList(GraphQLString))
        .dataFetcher({ set })))
        .build()

    when:
    def data = new GraphQL(schema).execute("query { set }").data

    then:
    data == [set: ['One', 'Two']]
  }
}
