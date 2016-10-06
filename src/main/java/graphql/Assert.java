package graphql;

import java.util.Collection;

public class Assert {
  public static <T> T assertNotNull(T object, String errorMessage) {
    if (object != null) return object;
    throw new AssertException(errorMessage);
  }

  public static void assertNotEmpty(Collection<?> c, String errorMessage) {
    if (c == null || c.isEmpty()) throw new AssertException(errorMessage);
  }
}
