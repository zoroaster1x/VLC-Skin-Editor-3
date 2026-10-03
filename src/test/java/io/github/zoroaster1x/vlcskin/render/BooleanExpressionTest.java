package io.github.zoroaster1x.vlcskin.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class BooleanExpressionTest {

    @Test
    void evaluatesVlcSyntax() {
        assertThat(BooleanExpression.evaluate("true", name -> false)).isTrue();
        assertThat(BooleanExpression.evaluate("not false", name -> false)).isTrue();
        assertThat(BooleanExpression.evaluate("true and true", name -> false)).isTrue();
        assertThat(BooleanExpression.evaluate("true and false", name -> false)).isFalse();
        assertThat(BooleanExpression.evaluate("true or false", name -> false)).isTrue();
        assertThat(BooleanExpression.evaluate("not (true and false)", name -> false)).isTrue();
        assertThat(BooleanExpression.evaluate("(true or false) and false", name -> false)).isFalse();
    }

    @Test
    void unknownIdentifiersAreFalse() {
        assertThat(BooleanExpression.evaluate("mystery.variable", name -> false)).isFalse();
        assertThat(BooleanExpression.evaluate("mystery.variable or true", name -> false)).isTrue();
    }

    @Test
    void malformedExpressionsAreFalse() {
        assertThat(BooleanExpression.evaluate("and true", name -> false)).isFalse();
        assertThat(BooleanExpression.evaluate("(true", name -> false)).isFalse();
        assertThat(BooleanExpression.evaluate("", name -> false)).isFalse();
        assertThat(BooleanExpression.evaluate(null, name -> false)).isFalse();
    }

    @Test
    void variablesSubstituteText() {
        PreviewVariables variables = new PreviewVariables();
        assertThat(variables.substitute("Time: $T left $L")).isEqualTo("Time: 0:55:55 left 0:44:44");
        assertThat(variables.evaluate("vlc.isPaused")).isTrue();
        assertThat(variables.evaluate("vlc.isPlaying")).isFalse();
        variables.setBoolean("vlc.isPlaying", true);
        variables.setBoolean("vlc.isPaused", false);
        assertThat(variables.evaluate("vlc.isPlaying and not vlc.isPaused")).isTrue();
    }

    @Test
    void tokenizerSplitsParentheses() {
        List<String> tokens = BooleanExpression.tokenize("(a and (b or c))");
        assertThat(tokens).containsExactly("(", "a", "and", "(", "b", "or", "c", ")", ")");
    }
}
