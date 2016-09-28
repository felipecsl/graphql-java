package graphql;


import graphql.schema.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newBuilder;

public class ScalarsQuerySchema {

  public static final DataFetcher inputDF = new DataFetcher() {
    @Override
    public Object get(DataFetchingEnvironment environment) {
      return environment.getArgument("input");
    }
  };

  public static final GraphQLObjectType queryType = GraphQLObjectType.newBuilder()
      .name("QueryType")
      /** Static Scalars */
      .field(newBuilder()
          .name("bigInteger")
          .type(Scalars.GraphQLBigInteger)
          .staticValue(BigInteger.valueOf(9999)))
      .field(newBuilder()
          .name("bigDecimal")
          .type(Scalars.GraphQLBigDecimal)
          .staticValue(BigDecimal.valueOf(1234.0)))
      .field(newBuilder()
          .name("floatNaN")
          .type(Scalars.GraphQLFloat)
          .staticValue(Double.NaN))

      /** Scalars with input of same type, value echoed back */
      .field(newBuilder()
          .name("bigIntegerInput")
          .type(Scalars.GraphQLBigInteger)
          .argument(newArgument()
              .name("input")
              .type(new GraphQLNonNull(Scalars.GraphQLBigInteger)))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("bigDecimalInput")
          .type(Scalars.GraphQLBigDecimal)
          .argument(newArgument()
              .name("input")
              .type(new GraphQLNonNull(Scalars.GraphQLBigDecimal)))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("floatNaNInput")
          .type(Scalars.GraphQLFloat)
          .argument(newArgument()
              .name("input")
              .type(new GraphQLNonNull(Scalars.GraphQLFloat)))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("stringInput")
          .type(Scalars.GraphQLString)
          .argument(newArgument()
              .name("input")
              .type(new GraphQLNonNull(Scalars.GraphQLString)))
          .dataFetcher(inputDF))


      /** Scalars with input of String, cast to scalar */
      .field(newBuilder()
          .name("bigIntegerString")
          .type(Scalars.GraphQLBigInteger)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("bigDecimalString")
          .type(Scalars.GraphQLBigDecimal)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("floatString")
          .type(Scalars.GraphQLFloat)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("longString")
          .type(Scalars.GraphQLLong)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("intString")
          .type(Scalars.GraphQLInt)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("shortString")
          .type(Scalars.GraphQLShort)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .field(newBuilder()
          .name("byteString")
          .type(Scalars.GraphQLByte)
          .argument(newArgument()
              .name("input")
              .type(Scalars.GraphQLString))
          .dataFetcher(inputDF))
      .build();


  public static final GraphQLSchema scalarsQuerySchema = GraphQLSchema.newSchema()
      .query(queryType)
      .build();
}
