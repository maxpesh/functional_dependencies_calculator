package com.github.maxpesh;

import java.util.*;

class Interpreter implements ExprVisitor {
    void interpret(Expr expression) {
        Set<String> closure = (Set<String>) evaluate(expression);
        System.out.printf("{%s}\n", String.join(",", closure));
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinary(Binary expr) {
        switch (expr.operator().type()) {
            case SEMICOLON -> {
                Set<String> attrs = (Set<String>) evaluate(expr.left());
                List<FuncDep> funcDeps = (List<FuncDep>) evaluate(expr.right());
                Set<String> closure = closure(attrs, funcDeps);
                return closure;
            }
            case COMMA -> {
                Object left = evaluate(expr.left());
                if (left instanceof Set<?>) {
                    Set<String> attrs = (Set<String>) left;
                    attrs.addAll((Set<String>) evaluate(expr.right()));
                    return attrs;
                } else if (left instanceof List<?>) {
                    List<FuncDep> funcDeps = (List<FuncDep>) left;
                    funcDeps.addAll((Collection<FuncDep>) evaluate(expr.right()));
                    return funcDeps;
                } else {
                    throw new RuntimeException("unknown type of the operand");
                }
            }
            case ARROW -> {
                ArrayList<FuncDep> list = new ArrayList<>();
                Set<String> lhs = (Set<String>) evaluate(expr.left());
                Set<String> rhs = (Set<String>) evaluate(expr.right());
                list.add(new FuncDep(lhs, rhs));
                return list;
            }
            default -> {
                throw new RuntimeException("unknown operator");
            }
        }
    }

    private Set<String> closure(Set<String> attrs, List<FuncDep> funcDeps) {
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

    private boolean isSubset(FuncDep funcDep, Set<String> attrs) {
        return attrs.containsAll(funcDep.lhs);
    }

    @Override
    public Set<String> visitLiteral(Literal expr) {
        HashSet<String> set = new HashSet<>();
        set.add(expr.value());
        return set;
    }

    private record FuncDep(Set<String> lhs, Set<String> rhs) {
        @Override
        public String toString() {
            return "{lhs=%s, rhs=%s}".formatted(lhs, rhs);
        }
    }
}
