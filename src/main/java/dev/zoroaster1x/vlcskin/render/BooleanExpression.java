package dev.zoroaster1x.vlcskin.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Evaluates the boolean expressions VLC uses for visible and state attributes.
 */
public final class BooleanExpression {

    private BooleanExpression() {
    }

    public static boolean evaluate(String expression, Predicate<String> identifier) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        List<String> tokens = tokenize(expression);
        List<String> rpn = toRpn(tokens);
        if (rpn == null) {
            return false;
        }
        Deque<Boolean> stack = new ArrayDeque<>();
        for (String token : rpn) {
            switch (token.toLowerCase(Locale.ROOT)) {
                case "not" -> {
                    if (stack.isEmpty()) {
                        return false;
                    }
                    stack.push(!stack.pop());
                }
                case "and" -> {
                    if (stack.size() < 2) {
                        return false;
                    }
                    boolean right = stack.pop();
                    boolean left = stack.pop();
                    stack.push(left && right);
                }
                case "or" -> {
                    if (stack.size() < 2) {
                        return false;
                    }
                    boolean right = stack.pop();
                    boolean left = stack.pop();
                    stack.push(left || right);
                }
                default -> stack.push(lookup(token, identifier));
            }
        }
        return stack.size() == 1 && stack.pop();
    }

    private static boolean lookup(String token, Predicate<String> identifier) {
        if ("true".equalsIgnoreCase(token)) {
            return true;
        }
        if ("false".equalsIgnoreCase(token)) {
            return false;
        }
        return identifier.test(token);
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
