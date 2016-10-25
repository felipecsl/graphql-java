package graphql.schema;

import javax.annotation.Nullable;

public interface TypeResolver {
  @Nullable GraphQLObjectType getType(Object object);
}
