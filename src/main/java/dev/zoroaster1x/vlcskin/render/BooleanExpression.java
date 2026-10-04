package dev.zoroaster1x.vlcskin.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Evaluates the boolean expressions VLC uses for visible and state attributes.
 */
public final class BooleanExpression {

    private BooleanExpression() {
    }

    /**
     * The outcome of an expression: the value plus whether every identifier
     * resolved. VLC returns no variable at all when any token is unknown, and
     * callers differ in what they do about it (a control stays visible, a
     * checkbox is dropped), so the distinction has to survive evaluation.
     */
    public record Result(boolean value, boolean resolved) {
    }

    /**
     * Evaluates an expression, treating an identifier the predicate rejects as
     * unknown and therefore false. This is the permissive form used by tests
     * and by callers that do not care to distinguish false from unresolved.
     */
    public static boolean evaluate(String expression, Predicate<String> identifier) {
        return resolve(expression, name -> identifier.test(name)).value();
    }

    /**
     * Evaluates an expression with a resolver that returns null for an unknown
     * identifier. The result reports whether every identifier was known, which
     * VLC callers treat differently per attribute.
     */
    public static Result resolve(String expression, Function<String, Boolean> identifier) {
        if (expression == null || expression.isBlank()) {
            return new Result(false, false);
        }
        List<String> tokens = tokenize(expression);
        List<String> rpn = toRpn(tokens);
        if (rpn == null) {
            return new Result(false, false);
        }
        Deque<Boolean> stack = new ArrayDeque<>();
        boolean resolved = true;
        for (String token : rpn) {
            switch (token.toLowerCase(Locale.ROOT)) {
                case "not" -> {
                    if (stack.isEmpty()) {
                        return new Result(false, false);
                    }
                    stack.push(!stack.pop());
                }
                case "and" -> {
                    if (stack.size() < 2) {
                        return new Result(false, false);
                    }
                    boolean right = stack.pop();
                    boolean left = stack.pop();
                    stack.push(left && right);
                }
                case "or" -> {
                    if (stack.size() < 2) {
                        return new Result(false, false);
                    }
                    boolean right = stack.pop();
                    boolean left = stack.pop();
                    stack.push(left || right);
                }
                default -> {
                    if ("true".equalsIgnoreCase(token)) {
                        stack.push(true);
                    } else if ("false".equalsIgnoreCase(token)) {
                        stack.push(false);
                    } else {
                        Boolean known = identifier.apply(token);
                        if (known == null) {
                            resolved = false;
                            stack.push(false);
                        } else {
                            stack.push(known);
                        }
                    }
                }
            }
        }
        boolean value = stack.size() == 1 && stack.pop();
        return new Result(value, resolved);
    }

    static List<String> tokenize(String expression) {
        String padded = expression.replace("(", " ( ").replace(")", " ) ");
        List<String> tokens = new ArrayList<>();
        for (String token : padded.trim().split("\\s+")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Shunting yard; returns null on unbalanced parentheses or bad operators.
     */
    static List<String> toRpn(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();
        for (String token : tokens) {
            switch (token.toLowerCase(Locale.ROOT)) {
                case "not", "and", "or" -> {
                    while (!operators.isEmpty() && !"(".equals(operators.peek())
                            && precedence(operators.peek()) >= precedence(token)) {
                        output.add(operators.pop());
                    }
                    operators.push(token.toLowerCase(Locale.ROOT));
                }
                case "(" -> operators.push(token);
                case ")" -> {
                    boolean found = false;
                    while (!operators.isEmpty()) {
                        String op = operators.pop();
                        if ("(".equals(op)) {
                            found = true;
                            break;
                        }
                        output.add(op);
                    }
                    if (!found) {
                        return null;
                    }
                }
                default -> output.add(token);
            }
        }
        while (!operators.isEmpty()) {
            String op = operators.pop();
            if ("(".equals(op) || ")".equals(op)) {
                return null;
            }
            output.add(op);
        }
        return output;
    }

    private static int precedence(String operator) {
        return switch (operator.toLowerCase(Locale.ROOT)) {
            case "not" -> 3;
            case "and" -> 2;
            case "or" -> 1;
            default -> 0;
        };
    }
}
