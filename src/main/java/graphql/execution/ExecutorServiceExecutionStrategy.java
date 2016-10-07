package graphql.execution;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLException;
import graphql.language.Field;
import graphql.schema.GraphQLObjectType;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>ExecutorServiceExecutionStrategy uses an {@link ExecutorService} to parallelize the
 * resolve.</p> <p> Due to the nature of {@link #execute(GraphQLObjectType, Object, Map)}
 * implementation, {@link ExecutorService} MUST have the following 2 characteristics: <ul> <li>1.
 * The underlying {@link java.util.concurrent.ThreadPoolExecutor} MUST have a reasonable {@code
 * maximumPoolSize} <li>2. The underlying {@link java.util.concurrent.ThreadPoolExecutor} SHALL NOT
 * use its task queue. </ul> <p> <p>Failure to follow 1. and 2. can result in a very large number of
 * threads created or hanging. (deadlock)</p>
 * <p>
 * See {@code graphql.execution.ExecutorServiceExecutionStrategyTest} for example usage.
 */
class ExecutorServiceExecutionStrategy extends ExecutionStrategy {
  private final ExecutorService executorService;

  private ExecutorServiceExecutionStrategy(ExecutorService executorService,
      ExecutionContext executionContext) {
    super(executionContext);
    this.executorService = executorService;
  }

  ExecutorServiceExecutionStrategy(ExecutionContext executionContext) {
    this(Executors.newCachedThreadPool(), executionContext);
  }

  @Override public ExecutionResult execute(final GraphQLObjectType parentType, @Nullable Field
      parentField, @Nullable final Object source, final Map<String, List<Field>> fields) {
    if (executorService == null) {
      return new SimpleExecutionStrategy(executionContext).execute(parentType, parentField,
          source, fields);
    }

    Map<String, Future<ExecutionResult>> futures = new LinkedHashMap<>();
    for (String fieldName : fields.keySet()) {
      final List<Field> fieldList = fields.get(fieldName);
      Callable<ExecutionResult> resolveField = new Callable<ExecutionResult>() {
        @Override
        public ExecutionResult call() throws Exception {
          return resolveField(parentType, source, fieldList);
        }
      };
      futures.put(fieldName, executorService.submit(resolveField));
    }
    try {
      Map<String, Object> results = new LinkedHashMap<>();
      for (String fieldName : futures.keySet()) {
        ExecutionResult executionResult = futures.get(fieldName).get();
        results.put(fieldName, executionResult != null ? executionResult.getData() : null);
      }
      return new ExecutionResultImpl(results, executionContext.getErrors());
    } catch (InterruptedException e) {
      throw new GraphQLException(e);
    } catch (ExecutionException e) {
      throw new GraphQLException(e);
    }
  }
}
