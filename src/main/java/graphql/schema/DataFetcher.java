package graphql.schema;

import javax.annotation.Nullable;

public interface DataFetcher {
  @Nullable Object get(DataFetchingEnvironment environment);
}
