package com.github.maxpesh;

import java.util.ArrayList;
import java.util.List;

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start; // first character in the lexeme being scanned
    private int current; // character currently being considered
    private int line = 1; // tracks what source line `current` is on

    public Lexer(String source) {
        this.source = source;

    }

    List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, start));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> {
                if (peek() == ' ') {
                    addToken(TokenType.COMMA_SPACE);
                    advance();
                } else {
                    addToken(TokenType.COMMA);
                }
            }
            case '-' -> {
                if (match('>'))
                    addToken(TokenType.ARROW);
                else
                    Main.error(line, current, "expect >");
            }
            case '=' -> {
                if (match('>'))
                    addToken(TokenType.FOLLOWS);
                else
                    Main.error(line, current, "expect >");
            }
            case ';' -> addToken(TokenType.SEMICOLON);
            case '+' -> addToken(TokenType.PLUS);
            case '?' -> addToken(TokenType.QUESTION);
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> line++;
            default -> {
                if (isLowerCaseAlpha(c)) {
                    attribute();
                } else if (isAlphaNumeric(c)) {
                    string();
                } else {
                    Main.error(line, current - 1, "unexpected character");
                }
            }
        }
    }

    private void attribute() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        addToken(TokenType.ATTRIBUTE);
    }

    private void string() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        addToken(TokenType.STRING);
    }

    private void addToken(TokenType type) {
        var lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, line, start));
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                c == '_';
    }

    private boolean isLowerCaseAlpha(char c) {
        return (c >= 'a' && c <= 'z') || c == '_';
    }
}
