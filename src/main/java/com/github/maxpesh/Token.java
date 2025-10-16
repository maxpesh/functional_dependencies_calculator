package com.github.maxpesh;

public record Token(TokenType type, String lexeme, int line) {
}

enum TokenType {
    // Single-character tokens
    LEFT_BRACE, RIGHT_BRACE, COMMA,

    // two character tokens.
    ARROW,

    // Literals
    ATTRIBUTE,

    EOF
}
