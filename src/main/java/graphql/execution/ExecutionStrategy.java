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

  public abstract ExecutionResult execute(ExecutionContext executionContext,
      GraphQLObjectType parentType, @Nullable Object source, Map<String, List<Field>> fields);

  @Nullable ExecutionResult resolveField(ExecutionContext executionContext,
      GraphQLObjectType parentType, @Nullable Object source, List<Field> fields) {
    GraphQLFieldDefinition fieldDef = getFieldDef(executionContext.getGraphQLSchema(), parentType,
        fields.get(0));
    Map<String, Object> argumentValues = valuesResolver.getArgumentValues(
        fieldDef.getArguments(), fields.get(0).getArguments(), executionContext.getVariables());
    DataFetchingEnvironment environment = new DataFetchingEnvironment(source, argumentValues,
        executionContext.getRoot(), fields, fieldDef.getType(), parentType,
        executionContext.getGraphQLSchema());

    Object resolvedValue = null;
    try {
      resolvedValue = fieldDef.getDataFetcher().get(environment);
    } catch (Exception e) {
      log.info("Exception while fetching data", e);
      executionContext.addError(new ExceptionWhileDataFetching(e));
    }

    return completeValue(executionContext, fieldDef.getType(), fields, resolvedValue);
  }

  @Nullable protected ExecutionResult completeValue(ExecutionContext executionContext, GraphQLType
      fieldType, List<Field> fields, @Nullable Object result) {
    GraphQLObjectType resolvedType;
    if (fieldType instanceof GraphQLNonNull) {
      return completeValueForNonNull(executionContext, (GraphQLNonNull) fieldType, fields, result);
    } else if (result == null) {
      return null;
    } else if (fieldType instanceof GraphQLList) {
      return completeValueForList(executionContext, (GraphQLList) fieldType, fields, result);
    } else if (fieldType instanceof GraphQLScalarType) {
      return completeValueForScalar((GraphQLScalarType) fieldType, result);
    } else if (fieldType instanceof GraphQLEnumType) {
      return completeValueForEnum((GraphQLEnumType) fieldType, result);
    } else if (fieldType instanceof GraphQLInterfaceType) {
      resolvedType = resolveType((GraphQLInterfaceType) fieldType, result);
    } else if (fieldType instanceof GraphQLUnionType) {
      resolvedType = resolveType((GraphQLUnionType) fieldType, result);
    } else {
      resolvedType = (GraphQLObjectType) fieldType;
    }

    Map<String, List<Field>> subFields = new LinkedHashMap<>();
    List<String> visitedFragments = new ArrayList<>();
    for (Field field : fields) {
      if (field.getSelectionSet() == null) continue;
      fieldCollector.collectFields(executionContext, resolvedType, field.getSelectionSet(),
          visitedFragments, subFields);
    }

    // Calling this from the executionContext so that you can shift from the simple execution
    // strategy for mutations back to the desired strategy.
    return executionContext.getExecutionStrategy()
        .execute(executionContext, resolvedType, result, subFields);
  }

  private ExecutionResult completeValueForNonNull(ExecutionContext executionContext,
      GraphQLNonNull fieldType, List<Field> fields, @Nullable Object result) {
    ExecutionResult completed = completeValue(executionContext, fieldType.getWrappedType(),
        fields, result);
    if (completed == null) {
      throw new GraphQLException("Cannot return null for non-nullable type: " + fields);
    } else {
      return completed;
    }
  }

  private ExecutionResult completeValueForList(ExecutionContext executionContext,
      GraphQLList fieldType, List<Field> fields, Object result) {
    if (result.getClass().isArray()) {
      result = Arrays.asList((Object[]) result);
    }
    return completeValueForList(executionContext, fieldType, fields, (Iterable<Object>) result);
  }

  protected GraphQLObjectType resolveType(GraphQLInterfaceType graphQLInterfaceType, Object value) {
    GraphQLObjectType result = graphQLInterfaceType.getTypeResolver().getType(value);
    if (result == null) {
      throw new GraphQLException("could not determine type");
    }
    return result;
  }

  protected GraphQLObjectType resolveType(GraphQLUnionType graphQLUnionType, Object value) {
    GraphQLObjectType result = graphQLUnionType.getTypeResolver().getType(value);
    if (result == null) {
      throw new GraphQLException("could not determine type");
    }
    return result;
  }

  private ExecutionResult completeValueForEnum(GraphQLEnumType enumType, Object result) {
    return new ExecutionResultImpl(enumType.getCoercing().serialize(result), null);
  }

  private ExecutionResult completeValueForScalar(GraphQLScalarType scalarType, Object result) {
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
      ExecutionResult completedValue = completeValue(executionContext, fieldType.getWrappedType(),
          fields, item);
      completedResults.add(completedValue != null ? completedValue.getData() : null);
    }
    return new ExecutionResultImpl(completedResults, null);
  }

  protected GraphQLFieldDefinition getFieldDef(GraphQLSchema schema, GraphQLObjectType parentType,
      Field field) {
    if (schema.getQueryType() == parentType) {
      if (field.getName().equals(SchemaMetaFieldDef.getName())) {
        return SchemaMetaFieldDef;
      }
      if (field.getName().equals(TypeMetaFieldDef.getName())) {
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
