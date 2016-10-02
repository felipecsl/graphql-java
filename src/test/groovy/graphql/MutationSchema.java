package graphql;


import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import static graphql.Scalars.GraphQLInt;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newBuilder;
import static graphql.schema.GraphQLSchema.newSchema;

public class MutationSchema {

  public static class NumberHolder {
    int theNumber;

    public NumberHolder(int theNumber) {
      this.theNumber = theNumber;
    }

    public int getTheNumber() {
      return theNumber;
    }

    public void setTheNumber(int theNumber) {
      this.theNumber = theNumber;
    }


  }

  public static class Root {
    NumberHolder numberHolder;

    public Root(int number) {
      this.numberHolder = new NumberHolder(number);
    }

    public NumberHolder changeNumber(int newNumber) {
      this.numberHolder.theNumber = newNumber;
      return this.numberHolder;
    }


    public NumberHolder failToChangeTheNumber(int newNumber) {
      throw new RuntimeException("Cannot change the number");
    }


  }

  public static GraphQLObjectType numberHolderType = GraphQLObjectType.newBuilder()
      .name("NumberHolder")
      .field(newBuilder()
          .name("theNumber")
          .type(GraphQLInt))
      .build();

  public static GraphQLObjectType queryType = GraphQLObjectType.newBuilder()
      .name("queryType")
      .field(newBuilder()
          .name("numberHolder")
          .type(numberHolderType))
      .build();

  public static GraphQLObjectType mutationType = GraphQLObjectType.newBuilder()
      .name("mutationType")
      .field(newBuilder()
          .name("changeTheNumber")
          .type(numberHolderType)
          .argument(newArgument()
              .name("newNumber")
              .type(GraphQLInt))
          .dataFetcher(new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment environment) {
              Integer newNumber = environment.getArgument("newNumber");
              Root root = (Root) environment.getSource();
              return root.changeNumber(newNumber);
            }
          }))
      .field(newBuilder()
          .name("failToChangeTheNumber")
          .type(numberHolderType)
          .argument(newArgument()
              .name("newNumber")
              .type(GraphQLInt))
          .dataFetcher(new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment environment) {
              Integer newNumber = environment.getArgument("newNumber");
              Root root = (Root) environment.getSource();
              return root.failToChangeTheNumber(newNumber);
            }
          }))
      .build();

  public static GraphQLSchema schema = newSchema()
      .query(queryType)
      .mutation(mutationType)
      .build();

}
