package com.github.maxpesh;

import java.util.List;

import static com.github.maxpesh.TokenType.*;

/*
  <commandline> ::= <funcdependencies> ((";" "{" <attributes> "}" "+" "?") | ("=>" <funcdependency> "?"))
  <attributes> ::= <literal> ("," <literal>)*
  <funcdependencies> ::= "{" <funcdependency> (", " <funcdependency>)* "}"
  <funcdependency> ::= <attributes> "->" <attributes>
  <literal> ::= <attribute> | <string>
  <attribute> ::= ([a-z] | "_") ([a-z] | [A-Z] | [0-9] | "_")*
  -- error production
  <string> ::= ([a-z] | [A-Z] | [0-9] | "_")+
*/
class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return commandLine();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr commandLine() {
        Expr expr = funcDependencies();
        if (match(SEMICOLON)) {
            Token operator = previous();
            consume(LEFT_BRACE, "Expect '{'");
            Expr right = attributes();
            consume(RIGHT_BRACE, "Expect '}'");
            consume(PLUS, "Expect '+'");
            consume(QUESTION, "Expect '?'");
            return new Binary(expr, operator, right);
        } else if (match(TokenType.FOLLOWS)) {
            Token operator = previous();
            Expr right = funcDependency();
            consume(QUESTION, "Expect '?'");
            return new Binary(expr, operator, right);
        } else {
            throw error(peek(), "Expect ';' or '=>' after the list of attributes");
        }
    }

    private Expr attributes() {
        Expr expr = literal();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = literal();
            expr = new Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr funcDependencies() {
        consume(LEFT_BRACE, "Expect '{'");
        Expr expr = funcDependency();
        while (match(COMMA_SPACE)) {
            Token operator = previous();
            Expr right = funcDependency();
            expr = new Binary(expr, operator, right);
        }
        consume(RIGHT_BRACE, "Expect '}'");
        return expr;
    }

    private Expr funcDependency() {
        Expr expr = attributes();
        Token operator = consume(ARROW, "Expect '->'");
        Expr right = attributes();
        return new Binary(expr, operator, right);
    }

    private Expr literal() {
        if (match(ATTRIBUTE)) {
            return attributeName();
        } else if (match(STRING)) {
            return string(); /* error production */
        }
        throw error(peek(), "expect literal");
    }

    private Expr string() {
        error(previous(), "attribute name must begin with a letter (a-z) or underscore (_). " +
                "Subsequent characters in a name can be letters, digits (0-9), or underscores");
        return new Literal(previous().lexeme());
    }

    private Expr attributeName() {
        return new Literal(previous().lexeme());
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Main.error(token, token.startIndex(), message);
        return new ParseError();
    }

    private static class ParseError extends RuntimeException {
    }
}
