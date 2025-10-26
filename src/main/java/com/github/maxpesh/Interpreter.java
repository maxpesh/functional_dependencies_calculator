package com.github.maxpesh;

import java.util.*;

class Interpreter implements ExprVisitor {
    void interpret(Expr expression) {
        Object value = evaluate(expression);
        System.out.println(stringify(value));
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinary(Binary expr) {
        switch (expr.operator().type()) {
            case SEMICOLON -> { // <commandline> ::= <funcdependencies> ";" "{" <attributes> "}" "+" "?"
                List<FuncDep> funcDeps = (List<FuncDep>) evaluate(expr.left());
                Set<String> attrs = (Set<String>) evaluate(expr.right());
                return closure(funcDeps, attrs);
            }
            case FOLLOWS -> { // <commandline> ::= <funcdependencies> "=>" <funcdependency> "?"
                List<FuncDep> funcDeps = (List<FuncDep>) evaluate(expr.left());
                FuncDep funcDep = (FuncDep) evaluate(expr.right());
                return follows(funcDeps, funcDep);
            }
            case COMMA -> { // <attributes> ::= <literal> ("," <literal>)*
                Object left = evaluate(expr.left());
                if (left instanceof Set<?>) {
                    Set<String> attrs = (Set<String>) left;
                    attrs.addAll((Set<String>) evaluate(expr.right()));
                    return attrs;
                } else {
                    throw new RuntimeException("unknown type of the operand");
                }
            }
            case COMMA_SPACE -> { // <funcdependencies> ::= "{" <funcdependency> (", " <funcdependency>)* "}"
                List<FuncDep> funcDeps;
                Object left = evaluate(expr.left());
                if (left instanceof FuncDep) {
                    funcDeps = new ArrayList<>();
                    funcDeps.add((FuncDep) left);
                } else {
                    funcDeps = (List<FuncDep>) left;
                }
                funcDeps.add((FuncDep) evaluate(expr.right()));
                return funcDeps;
            }
            case ARROW -> { // <funcdependency> ::= <attributes> "->" <attributes>
                Set<String> lhs = (Set<String>) evaluate(expr.left());
                Set<String> rhs = (Set<String>) evaluate(expr.right());
                return new FuncDep(lhs, rhs);
            }
            default -> throw new RuntimeException("unknown operator: %s".formatted(expr.operator().lexeme()));
        }
    }

    private Set<String> closure(List<FuncDep> funcDeps, Set<String> attrs) {
        Set<String> closure = new HashSet<>(attrs);
        int prevLen = 0;

        while (closure.size() != prevLen) {
            prevLen = closure.size();
            for (FuncDep funcDep : funcDeps) {
                if (isSubset(funcDep, closure)) {
                    closure.addAll(funcDep.rhs);
                }
            }
        }
        return closure;
    }

    private Object follows(List<FuncDep> funcDeps, FuncDep funcDep) {
        Set<String> closure = closure(funcDeps, funcDep.lhs);
        return closure.containsAll(funcDep.rhs);
    }

    private boolean isSubset(FuncDep funcDep, Set<String> attrs) {
        return attrs.containsAll(funcDep.lhs);
    }

    @Override
    public Set<String> visitLiteral(Literal expr) {
        HashSet<String> set = new HashSet<>();
        set.add(expr.value());
        return set;
    }

    private String stringify(Object value) {
        if (value instanceof Set<?>) {
            Set<String> closure = (Set<String>) value;
            return String.join(",", closure);
        } else if (value instanceof Boolean isFollows) {
            return isFollows.toString();
        }
        return value.toString();
    }

    private record FuncDep(Set<String> lhs, Set<String> rhs) {
        @Override
        public String toString() {
            return "{lhs=%s, rhs=%s}".formatted(lhs, rhs);
        }
    }
}
