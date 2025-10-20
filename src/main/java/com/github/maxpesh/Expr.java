package com.github.maxpesh;

import java.util.Set;

interface Expr {
    Object accept(ExprVisitor exprVisitor);
}

interface ExprVisitor {
    Object visitBinary(Binary expr);

    Set<String> visitLiteral(Literal expr);
}

record Binary(Expr left, Token operator, Expr right) implements Expr {
    @Override
    public Object accept(ExprVisitor exprVisitor) {
        return exprVisitor.visitBinary(this);
    }
}

record Literal(String value) implements Expr {
    @Override
    public Object accept(ExprVisitor exprVisitor) {
        return exprVisitor.visitLiteral(this);
    }
}