package graphql.schema;

interface GraphQLModifiedType extends GraphQLType {
  GraphQLType getWrappedType();
}
