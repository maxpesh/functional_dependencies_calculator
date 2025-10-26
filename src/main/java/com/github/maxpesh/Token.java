package com.github.maxpesh;

public record Token(TokenType type, String lexeme, int line, int startIndex) {
}

enum TokenType {
    // Single-character tokens
    LEFT_BRACE, RIGHT_BRACE, COMMA, SEMICOLON, PLUS, QUESTION,
    // two character tokens.
    ARROW, FOLLOWS, COMMA_SPACE,
    // Literals
    STRING, ATTRIBUTE,
    EOF
}
