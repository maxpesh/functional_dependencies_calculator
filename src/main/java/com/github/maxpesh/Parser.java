package com.github.maxpesh;

import java.util.List;

import static com.github.maxpesh.TokenType.*;

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
        Expr expr = attributes();
        Token operator = consume(SEMICOLON, "Expect ';' before the list of functional dependencies");
        Expr right = funcDependencies();
        return new Binary(expr, operator, right);
    }

    private Expr attributes() {
        consume(LEFT_BRACE, "Expect '{'");
        Expr expr = literal();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = literal();
            expr = new Binary(expr, operator, right);
        }
        consume(RIGHT_BRACE, "Expect '}'");
        return expr;
    }

    private Expr funcDependencies() {
        Expr expr = funcDependency();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = funcDependency();
            expr = new Binary(expr, operator, right);
        }
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

    private boolean isLowerCaseAlpha(char c) {
        return (c >= 'a' && c <= 'z') || c == '_';
    }

    private static class ParseError extends RuntimeException {
    }
}
