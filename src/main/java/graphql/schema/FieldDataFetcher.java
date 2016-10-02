package graphql.schema;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

/** Fetches data directly from a field. */
public class FieldDataFetcher implements DataFetcher {

  /** The name of the field. */
  private final String fieldName;

  /** @param fieldName The name of the field. */
  public FieldDataFetcher(String fieldName) {
    this.fieldName = fieldName;
  }

  @Nullable @Override public Object get(DataFetchingEnvironment environment) {
    Object source = environment.getSource();
    if (source == null) {
      return null;
    } else if (source instanceof Map) {
      return ((Map<?, ?>) source).get(fieldName);
    } else {
      return getFieldValue(source);
    }
  }

  /**
   * Uses introspection to get the field value.
   *
   * @param object     The object being acted on.
   * @return An object, or null.
   */
  private Object getFieldValue(Object object) {
    try {
      Field field = object.getClass().getField(fieldName);
      return field.get(object);
    } catch (NoSuchFieldException e) {
      return null;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
