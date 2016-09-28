package graphql.validation.rules;

import graphql.schema.*;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newBuilder;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLSchema.newSchema;
import static graphql.schema.GraphQLUnionType.newUnionType;


public class Harness {

  private static TypeResolver dummyTypeResolve = new TypeResolver() {
    @Override
    public GraphQLObjectType getType(Object object) {
      return null;
    }
  };


  public static GraphQLInterfaceType Being = newInterface()
      .name("Being")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .typeResolver(dummyTypeResolve)
      .build();

  public static GraphQLInterfaceType Pet = newInterface()
      .name("Pet")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .typeResolver(dummyTypeResolve)
      .build();

  public static GraphQLEnumType DogCommand = newEnum()
      .name("DogCommand")
      .value("SIT")
      .value("HEEL")
      .value("DOWN")
      .build();

  public static GraphQLObjectType Dog = GraphQLObjectType.newBuilder()
      .name("Dog")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .field(newBuilder()
          .name("nickName")
          .type(GraphQLString))
      .field(newBuilder()
          .name("barkVolume")
          .type(GraphQLInt))
      .field(newBuilder()
          .name("barks")
          .type(GraphQLBoolean))
      .field(newBuilder()
          .name("doesKnowCommand")
          .type(GraphQLBoolean)
          .argument(newArgument()
              .name("dogCommand")
              .type(DogCommand)))
      .field(newBuilder()
          .name("isHousetrained")
          .type(GraphQLBoolean)
          .argument(newArgument()
              .name("atOtherHomes")
              .type(GraphQLBoolean)
              .defaultValue(true)))
      .field(newBuilder()
          .name("isAtLocation")
          .type(GraphQLBoolean)
          .argument(newArgument()
              .name("x")
              .type(GraphQLInt))
          .argument(newArgument()
              .name("y")
              .type(GraphQLInt)))
      .withInterface(Being)
      .withInterface(Pet)
      .build();

  public static GraphQLEnumType FurColor = newEnum()
      .name("FurColor")
      .value("BROWN")
      .value("BLACK")
      .value("TAN")
      .value("SPOTTED")
      .build();


  public static GraphQLObjectType Cat = GraphQLObjectType.newBuilder()
      .name("Cat")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .field(newBuilder()
          .name("nickName")
          .type(GraphQLString))
      .field(newBuilder()
          .name("meows")
          .type(GraphQLBoolean))
      .field(newBuilder()
          .name("meowVolume")
          .type(GraphQLInt))
      .field(newBuilder()
          .name("furColor")
          .type(FurColor))
      .withInterfaces(Being, Pet)
      .build();

  public static GraphQLUnionType CatOrDog = newUnionType()
      .name("CatOrDog")
      .possibleTypes(Dog, Cat)
      .typeResolver(new TypeResolver() {
        @Override
        public GraphQLObjectType getType(Object object) {
          return null;
        }
      })
      .build();

  public static GraphQLInterfaceType Intelligent = newInterface()
      .name("Intelligent")
      .field(newBuilder()
          .name("iq")
          .type(GraphQLInt))
      .typeResolver(dummyTypeResolve)
      .build();

  public static GraphQLObjectType Human = GraphQLObjectType.newBuilder()
      .name("Human")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString)
          .argument(newArgument()
              .name("surname")
              .type(GraphQLBoolean)))
      .field(newBuilder()
          .name("pets")
          .type(new GraphQLList(Pet)))
      .field(newBuilder()
          .name("relatives")
          .type(new GraphQLList(new GraphQLTypeReference("Human"))))
      .field(newBuilder()
          .name("iq")
          .type(GraphQLInt))
      .withInterfaces(Being, Intelligent)
      .build();

  public static GraphQLObjectType Alien = GraphQLObjectType.newBuilder()
      .name("Alien")
      .field(newBuilder()
          .name("numEyes")
          .type(GraphQLInt))
      .field(newBuilder()
          .name("iq")
          .type(GraphQLInt))
      .withInterfaces(Being, Intelligent)
      .build();

  public static GraphQLUnionType DogOrHuman = newUnionType()
      .name("DogOrHuman")
      .possibleTypes(Dog, Human)
      .typeResolver(dummyTypeResolve)
      .build();

  public static GraphQLUnionType HumanOrAlien = newUnionType()
      .name("HumanOrAlien")
      .possibleTypes(Alien, Human)
      .typeResolver(dummyTypeResolve)
      .build();
//    public static GraphQLInputObjectType ComplexInput = newInputObject()
//            .field(newInputObjectField()
//                    .name("requiredField")
//                    .type(new GraphQLNonNull(GraphQLBoolean))
//                    .build())
//            .field(newInputObjectField()
//                    .name("intField")
//                    .type(GraphQLInt)
//                    .build())
//            .field(newInputObjectField()
//                    .name("stringField")
//                    .type(GraphQLString)
//                    .build())
//            .field(newInputObjectField()
//                    .name("booleanField")
//                    .type(GraphQLBoolean)
//                    .build())
//            .field(newInputObjectField()
//                    .name("stringListField")
//                    .type(new GraphQLList(GraphQLString))
//                    .build())
//            .build();


  public static GraphQLObjectType QueryRoot = GraphQLObjectType.newBuilder()
      .name("QueryRoot")
      .field(newBuilder()
          .name("alien")
          .type(Alien))
      .field(newBuilder()
          .name("dog")
          .type(Dog))
      .field(newBuilder()
          .name("cat")
          .type(Cat))
      .field(newBuilder()
          .name("pet")
          .type(Pet))
      .field(newBuilder()
          .name("catOrDog")
          .type(CatOrDog))

      .field(newBuilder()
          .name("dogOrHuman")
          .type(DogOrHuman))
      .field(newBuilder()
          .name("humanOrAlien")
          .type(HumanOrAlien))
      .build();

  public static GraphQLSchema Schema = newSchema()
      .query(QueryRoot)
      .build();


}

