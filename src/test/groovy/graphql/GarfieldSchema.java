package graphql;


import graphql.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newBuilder;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLUnionType.newUnionType;

public class GarfieldSchema {

  public interface Named {
    String getName();
  }

  public static class Dog implements Named {
    private final String name;


    private final boolean barks;

    public Dog(String name, boolean barks) {
      this.name = name;
      this.barks = barks;
    }

    public boolean isBarks() {
      return barks;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  public static class Cat implements Named {
    private final String name;

    private final boolean meows;

    public Cat(String name, boolean meows) {
      this.name = name;
      this.meows = meows;
    }

    public boolean isMeows() {
      return meows;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  public static class Person implements Named {
    private final String name;
    private final List<Dog> dogs;
    private final List<Cat> cats;
    private final List<Named> friends;

    public Person(String name) {
      this(name, Collections.<Cat>emptyList(), Collections.<Dog>emptyList(),
          Collections.<Named>emptyList());
    }

    public Person(String name, List<Cat> cats, List<Dog> dogs, List<Named> friends) {
      this.name = name;
      this.dogs = dogs;
      this.cats = cats;
      this.friends = friends;
    }

    public List<Object> getPets() {
      List<Object> pets = new ArrayList<>();
      pets.addAll(cats);
      pets.addAll(dogs);
      return pets;
    }

    @Override
    public String getName() {
      return name;
    }

    public List<Named> getFriends() {
      return friends;
    }
  }

  public static Cat garfield = new Cat("Garfield", false);
  public static Dog odie = new Dog("Odie", true);
  public static Person liz = new Person("Liz");
  public static Person john =
      new Person("John", Arrays.asList(garfield), Arrays.asList(odie), Arrays.asList(liz, odie));

  public static GraphQLInterfaceType NamedType = newInterface()
      .name("Named")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .typeResolver(new TypeResolver() {
        @Override
        public GraphQLObjectType getType(Object object) {
          if (object instanceof Dog) {
            return DogType;
          }
          if (object instanceof Person) {
            return PersonType;
          }
          if (object instanceof Cat) {
            return CatType;
          }
          return null;
        }
      })
      .build();

  public static GraphQLObjectType DogType = GraphQLObjectType.newBuilder()
      .name("Dog")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .field(newBuilder()
          .name("barks")
          .type(GraphQLBoolean))
      .withInterface(NamedType)
      .build();

  public static GraphQLObjectType CatType = GraphQLObjectType.newBuilder()
      .name("Cat")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .field(newBuilder()
          .name("meows")
          .type(GraphQLBoolean))
      .withInterface(NamedType)
      .build();

  public static GraphQLUnionType PetType = newUnionType()
      .name("Pet")
      .possibleType(CatType)
      .possibleType(DogType)
      .typeResolver(new TypeResolver() {
        @Override
        public GraphQLObjectType getType(Object object) {
          if (object instanceof Cat) {
            return CatType;
          }
          if (object instanceof Dog) {
            return DogType;
          }
          return null;
        }
      })
      .build();

  public static GraphQLObjectType PersonType = GraphQLObjectType.newBuilder()
      .name("Person")
      .field(newBuilder()
          .name("name")
          .type(GraphQLString))
      .field(newBuilder()
          .name("pets")
          .type(new GraphQLList(PetType)))
      .field(newBuilder()
          .name("friends")
          .type(new GraphQLList(NamedType)))
      .withInterface(NamedType)
      .build();

  public static GraphQLSchema GarfieldSchema = GraphQLSchema.newSchema()
      .query(PersonType)
      .build();


}
