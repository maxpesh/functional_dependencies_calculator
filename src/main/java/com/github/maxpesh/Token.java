package com.github.maxpesh;

public record Token(TokenType type, String lexeme, int line, int startIndex) {
}

enum TokenType {
    // Single-character tokens
    LEFT_BRACE, RIGHT_BRACE, COMMA, SEMICOLON,
    // two character tokens.
    ARROW,
    // Literals
    STRING, ATTRIBUTE,
    EOF
}
