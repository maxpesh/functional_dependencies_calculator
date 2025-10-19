package com.github.maxpesh;

interface Expr {
}

record Binary(Expr left, Token operator, Expr right) implements Expr {
}

record Literal(Object value) implements Expr {
}