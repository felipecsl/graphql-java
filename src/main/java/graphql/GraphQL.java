package graphql;

import graphql.execution.Execution;
import graphql.execution.ExecutionStrategy;
import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static graphql.Assert.assertNotNull;

public class GraphQL {
  private static final Logger log = LoggerFactory.getLogger(GraphQL.class);
  private final GraphQLSchema graphQLSchema;
  private final ExecutionStrategy.Type executionStrategyType;

  public GraphQL(GraphQLSchema graphQLSchema) {
    this(graphQLSchema, ExecutionStrategy.Type.Simple);
  }

  public GraphQL(GraphQLSchema graphQLSchema, ExecutionStrategy.Type executionStrategyType) {
    this.graphQLSchema = graphQLSchema;
    this.executionStrategyType = executionStrategyType;
  }

  public ExecutionResult execute(String requestString) {
    return execute(requestString, null);
  }

  public ExecutionResult execute(String requestString, @Nullable Object context) {
    return execute(requestString, context, Collections.<String, Object>emptyMap());
  }

  public ExecutionResult execute(String requestString, @Nullable String operationName,
      @Nullable Object context) {
    return execute(requestString, operationName, context, Collections.<String, Object>emptyMap());
  }

  public ExecutionResult execute(String requestString, @Nullable Object context,
      Map<String, Object> arguments) {
    return execute(requestString, null, context, arguments);
  }

  public ExecutionResult execute(String requestString, @Nullable String operationName,
      @Nullable Object context, Map<String, Object> arguments) {
    assertNotNull(arguments, "arguments can't be null");
    log.debug("Executing request. operation name: {}. Request: {} ", operationName, requestString);
    Parser parser = new Parser();
    Document document;
    try {
      document = parser.parseDocument(requestString);
    } catch (ParseCancellationException e) {
      RecognitionException recognitionException = (RecognitionException) e.getCause();
      SourceLocation sourceLocation =
          new SourceLocation(recognitionException.getOffendingToken().getLine(),
              recognitionException.getOffendingToken().getCharPositionInLine());
      InvalidSyntaxError invalidSyntaxError = new InvalidSyntaxError(sourceLocation);
      return new ExecutionResultImpl(Collections.singletonList(invalidSyntaxError));
    }

    Validator validator = new Validator(graphQLSchema);
    List<ValidationError> validationErrors = validator.validate(document);
    if (!validationErrors.isEmpty()) {
      return new ExecutionResultImpl(validationErrors);
    } else {
      Execution execution = new Execution(executionStrategyType);
      return execution.execute(graphQLSchema, context, document, operationName, arguments);
    }
  }
}
