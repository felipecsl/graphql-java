package graphql.execution;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLException;
import graphql.language.Field;
import graphql.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static graphql.introspection.Introspection.*;

public abstract class ExecutionStrategy {
  private static final Logger log = LoggerFactory.getLogger(ExecutionStrategy.class);
  protected final ValuesResolver valuesResolver = new ValuesResolver();
  protected final FieldCollector fieldCollector = new FieldCollector();
  protected final ExecutionContext executionContext;
  protected final ExecutionStrategy originalStrategy;

  public enum Type {
    Simple, Batched, ExecutorService, Introspection
  }

  protected ExecutionStrategy(ExecutionContext executionContext) {
    this.executionContext = executionContext;
    this.originalStrategy = this;
  }

  protected ExecutionStrategy(ExecutionContext executionContext, ExecutionStrategy
      originalStrategy) {
    this.executionContext = executionContext;
    this.originalStrategy = originalStrategy;
  }

  public abstract ExecutionResult execute(GraphQLObjectType parentType, @Nullable Field parentField,
      @Nullable Object source, Map<String, List<Field>> fields);

  @Nullable ExecutionResult resolveField(GraphQLObjectType parentType, @Nullable Object source,
      List<Field> fields) {
    Field field = fields.get(0);
    GraphQLFieldDefinition fieldDefinition =
        getFieldDefinition(executionContext.getGraphQLSchema(), parentType, field);
    Map<String, Object> argumentValues = valuesResolver.getArgumentValues(
        fieldDefinition.getArguments(), field.getArguments(), executionContext.getVariables());
    DataFetchingEnvironment environment = new DataFetchingEnvironment(source, argumentValues,
        executionContext.getRoot(), fields, fieldDefinition.getType(), parentType,
        executionContext.getGraphQLSchema());
    Object resolvedValue = resolveValue(fieldDefinition, environment);
    return completeValue(fieldDefinition.getType(), fields, resolvedValue);
  }

  Object resolveValue(GraphQLFieldDefinition fieldDef, DataFetchingEnvironment
      environment) {
    try {
      return fieldDef.getDataFetcher().get(environment);
    } catch (Exception e) {
      log.info("Exception while fetching data", e);
      executionContext.addError(new ExceptionWhileDataFetching(e));
      return null;
    }
  }

  @Nullable protected ExecutionResult completeValue(GraphQLType fieldType, List<Field> fields,
      @Nullable Object result) {
    if (fieldType instanceof GraphQLNonNull) {
      return completeValueForNonNull((GraphQLNonNull) fieldType, fields, result);
    } else if (result == null) {
      return null;
    } else if (fieldType instanceof GraphQLList) {
      return completeValueForList((GraphQLList) fieldType, fields, result);
    } else if (fieldType instanceof GraphQLScalarType) {
      return completeValueForScalar((GraphQLScalarType) fieldType, result);
    } else if (fieldType instanceof GraphQLEnumType) {
      return completeValueForEnum((GraphQLEnumType) fieldType, result);
    } else {
      return completeValueForObject(fieldType, fields, result);
    }
  }

  protected ExecutionResult completeValueForObject(GraphQLType fieldType, List<Field> fields,
      @Nullable Object result) {
    GraphQLObjectType resolvedType = resolveType(fieldType, result);
    Map<String, List<Field>> subFields = collectSubFields(fields, resolvedType);
    // Calling this from the executionContext so that you can shift from the simple execution
    // strategy for mutations back to the desired strategy.
    return originalStrategy.execute(resolvedType, fields.get(0), result, subFields);
  }

  Map<String, List<Field>> collectSubFields(List<Field> fields, GraphQLObjectType resolvedType) {
    Map<String, List<Field>> subFields = new LinkedHashMap<>();
    List<String> visitedFragments = new ArrayList<>();
    for (Field field : fields) {
      if (field.getSelectionSet() != null) {
        fieldCollector.collectFields(executionContext, resolvedType, field.getSelectionSet(),
            visitedFragments, subFields);
      }
    }
    return subFields;
  }

  GraphQLObjectType resolveType(GraphQLType fieldType, @Nullable Object result) {
    if (fieldType instanceof GraphQLInterfaceType) {
      return resolveType((GraphQLInterfaceType) fieldType, result);
    } else if (fieldType instanceof GraphQLUnionType) {
      return resolveType((GraphQLUnionType) fieldType, result);
    } else {
      return (GraphQLObjectType) fieldType;
    }
  }

  private ExecutionResult completeValueForNonNull(GraphQLNonNull fieldType, List<Field> fields,
      @Nullable Object result) {
    ExecutionResult completed = completeValue(fieldType.getWrappedType(), fields, result);
    if (completed == null) {
      throw new GraphQLException("Cannot return null for non-nullable type: " + fields);
    } else {
      return completed;
    }
  }

  private ExecutionResult completeValueForList(GraphQLList fieldType, List<Field> fields,
      Object result) {
    if (result.getClass().isArray()) {
      result = Arrays.asList((Object[]) result);
    }
    return completeValueForList(executionContext, fieldType, fields, (Iterable<Object>) result);
  }

  protected GraphQLObjectType resolveType(GraphQLInterfaceType graphQLInterfaceType,
      Object value) {
    GraphQLObjectType result = graphQLInterfaceType.getTypeResolver().getType(value);
    if (result != null) {
      return result;
    } else {
      throw new GraphQLException("could not determine type");
    }
  }

  protected GraphQLObjectType resolveType(GraphQLUnionType graphQLUnionType,
      Object value) {
    GraphQLObjectType result = graphQLUnionType.getTypeResolver().getType(value);
    if (result != null) {
      return result;
    } else {
      throw new GraphQLException("could not determine type");
    }
  }

  private ExecutionResult completeValueForEnum(GraphQLEnumType enumType,
      Object result) {
    return new ExecutionResultImpl(enumType.getCoercing().serialize(result), null);
  }

  ExecutionResult completeValueForScalar(GraphQLScalarType scalarType,
      Object result) {
    Object serialized = scalarType.getCoercing().serialize(result);
    //6.6.1 http://facebook.github.io/graphql/#sec-Field-entries
    if (serialized instanceof Double && ((Double) serialized).isNaN()) {
      serialized = null;
    }
    return new ExecutionResultImpl(serialized, null);
  }

  private ExecutionResult completeValueForList(ExecutionContext executionContext,
      GraphQLList fieldType, List<Field> fields, Iterable<Object> result) {
    List<Object> completedResults = new ArrayList<>();
    for (Object item : result) {
      ExecutionResult completedValue = completeValue(fieldType.getWrappedType(), fields, item);
      completedResults.add(completedValue != null ? completedValue.getData() : null);
    }
    return new ExecutionResultImpl(completedResults, null);
  }

  protected static GraphQLFieldDefinition getFieldDefinition(GraphQLSchema schema, GraphQLObjectType
      parentType, Field field) {
    if (schema.getQueryType() == parentType) {
      if (field.getName().equals(SchemaMetaFieldDef.getName())) {
        return SchemaMetaFieldDef;
      } else if (field.getName().equals(TypeMetaFieldDef.getName())) {
        return TypeMetaFieldDef;
      }
    }
    if (field.getName().equals(TypeNameMetaFieldDef.getName())) {
      return TypeNameMetaFieldDef;
    }
    GraphQLFieldDefinition fieldDefinition = parentType.getFieldDefinition(field.getName());
    if (fieldDefinition == null) {
      throw new GraphQLException("unknown field " + field.getName());
    }
    return fieldDefinition;
  }
}
