package graphql.validation;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.validation.rules.*;

import java.util.Arrays;
import java.util.List;

public class Validator {
  private final GraphQLSchema schema;

  public Validator(GraphQLSchema schema) {
    this.schema = schema;
  }

  public List<ValidationError> validate(Document document) {
    ValidationContext validationContext = new ValidationContext(schema, document);
    ValidationErrorCollector validationErrorCollector = new ValidationErrorCollector();
    LanguageTraversal languageTraversal = new LanguageTraversal();
    languageTraversal.traverse(document, new RulesVisitor(validationContext,
        createRules(validationContext, validationErrorCollector)));
    return validationErrorCollector.getErrors();
  }

  private static List<AbstractRule> createRules(ValidationContext validationContext,
      ValidationErrorCollector validationErrorCollector) {
    return Arrays.asList(new ArgumentsOfCorrectType(validationContext, validationErrorCollector),
        new FieldsOnCorrectType(validationContext, validationErrorCollector),
        new FragmentsOnCompositeType(validationContext, validationErrorCollector),
        new KnownArgumentNames(validationContext, validationErrorCollector),
        new KnownDirectives(validationContext, validationErrorCollector),
        new KnownFragmentNames(validationContext, validationErrorCollector),
        new KnownTypeNames(validationContext, validationErrorCollector),
        new NoFragmentCycles(validationContext, validationErrorCollector),
        new NoUndefinedVariables(validationContext, validationErrorCollector),
        new NoUnusedFragments(validationContext, validationErrorCollector),
        new NoUnusedVariables(validationContext, validationErrorCollector),
        new OverlappingFieldsCanBeMerged(validationContext, validationErrorCollector),
        new PossibleFragmentSpreads(validationContext, validationErrorCollector),
        new ProvidedNonNullArguments(validationContext, validationErrorCollector),
        new ScalarLeafs(validationContext, validationErrorCollector),
        new VariableDefaultValuesOfCorrectType(validationContext, validationErrorCollector),
        new VariablesAreInputTypes(validationContext, validationErrorCollector),
        new VariableTypesMatchRule(validationContext, validationErrorCollector));
  }
}
