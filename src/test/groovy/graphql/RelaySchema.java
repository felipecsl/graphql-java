package graphql;

import graphql.relay.Relay;
import graphql.schema.*;

import java.util.ArrayList;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newBuilder;

public class RelaySchema {
  public static Relay relay = new Relay();
  public static GraphQLObjectType StuffType = GraphQLObjectType.newBuilder()
      .name("Stuff")
      .field(newBuilder()
          .name("id")
          .type(GraphQLString)
          .fetchField())
      .build();

  public static GraphQLInterfaceType NodeInterface = relay.nodeInterface(new TypeResolver() {
    @Override
    public GraphQLObjectType getType(Object object) {
      Relay.ResolvedGlobalId resolvedGlobalId = relay.fromGlobalId((String) object);
      //TODO: implement
      return null;
    }
  });

  public static GraphQLObjectType StuffEdgeType =
      relay.edgeType("Stuff", StuffType, NodeInterface, new ArrayList<GraphQLFieldDefinition>());

  public static GraphQLObjectType StuffConnectionType =
      relay.connectionType("Stuff", StuffEdgeType, new ArrayList<GraphQLFieldDefinition>());

  public static GraphQLObjectType ThingType = GraphQLObjectType.newBuilder()
      .name("Thing")
      .field(newBuilder()
          .name("id")
          .type(GraphQLString)
          .fetchField())
      .field(newBuilder()
          .name("stuffs")
          .type(StuffConnectionType))
      .build();


  public static GraphQLObjectType RelayQueryType = GraphQLObjectType.newBuilder()
      .name("RelayQuery")
      .field(relay.nodeField(NodeInterface, new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
          //TODO: implement
          return null;
        }
      }))
      .field(newBuilder()
          .name("thing")
          .type(ThingType)
          .argument(newArgument()
              .name("id")
              .description("id of the thing")
              .type(new GraphQLNonNull(GraphQLString)))
          .dataFetcher(new DataFetcher() {
            @Override
            public Object get(DataFetchingEnvironment environment) {
              //TODO: implement
              return null;
            }
          }))
      .build();


  public static GraphQLSchema Schema = GraphQLSchema.newSchema()
      .query(RelayQueryType)
      .build();
}
