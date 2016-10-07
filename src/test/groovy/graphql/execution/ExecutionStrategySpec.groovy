package graphql.execution

import graphql.ExecutionResult
import graphql.Scalars
import graphql.language.Field
import graphql.schema.GraphQLList
import graphql.schema.GraphQLObjectType
import spock.lang.Specification

class ExecutionStrategySpec extends Specification {
  ExecutionStrategy executionStrategy

  def setup() {
    executionStrategy = new ExecutionStrategy(null) {
      @Override
      ExecutionResult execute(GraphQLObjectType parentType, Field parentField,
                              Object source, Map<String, List<Field>> fields) {
        return null
      }
    }
  }

  def "completes value for a java.util.List"() {
    given:
    Field field = new Field()
    def fieldType = new GraphQLList(Scalars.GraphQLString)
    def result = Arrays.asList("test")
    when:
    def executionResult = executionStrategy.completeValue(fieldType, [field], result)

    then:
    executionResult.data == ["test"]
  }

  def "completes value for an array"() {
    given:
    Field field = new Field()
    def fieldType = new GraphQLList(Scalars.GraphQLString)
    String[] result = ["test"]
    when:
    def executionResult = executionStrategy.completeValue(fieldType, [field], result)

    then:
    executionResult.data == ["test"]
  }
}
