package graphql.parser;

import graphql.language.Document;
import graphql.parser.antlr.GraphqlLexer;
import graphql.parser.antlr.GraphqlParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

public class Parser {
  public Document parseDocument(String input) {
    GraphqlLexer lexer = new GraphqlLexer(new ANTLRInputStream(input));
    GraphqlParser parser = new GraphqlParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
    parser.setErrorHandler(new BailErrorStrategy());
    GraphqlAntlrToLanguage antlrToLanguage = new GraphqlAntlrToLanguage();
    antlrToLanguage.visitDocument(parser.document());
    return antlrToLanguage.getResult();
  }
}
