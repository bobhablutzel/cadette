package com.jigger;

import com.jigger.command.ExpressionEvaluator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionEvaluatorTest {

    @Test
    void testSimpleNumber() {
        assertEquals(42.0, ExpressionEvaluator.evaluate("42", Map.of()));
    }

    @Test
    void testDecimalNumber() {
        assertEquals(3.14, ExpressionEvaluator.evaluate("3.14", Map.of()), 0.001);
    }

    @Test
    void testVariable() {
        assertEquals(600.0, ExpressionEvaluator.evaluate("$width", Map.of("width", 600.0)));
    }

    @Test
    void testSubtraction() {
        assertEquals(561.9, ExpressionEvaluator.evaluate("$width - 2 * $thickness",
                Map.of("width", 600.0, "thickness", 19.05)), 0.001);
    }

    @Test
    void testParentheses() {
        assertEquals(30.0, ExpressionEvaluator.evaluate("($a + $b) * $c",
                Map.of("a", 2.0, "b", 3.0, "c", 6.0)));
    }

    @Test
    void testNegation() {
        assertEquals(-400.0, ExpressionEvaluator.evaluate("-$depth",
                Map.of("depth", 400.0)));
    }

    @Test
    void testDivision() {
        assertEquals(300.0, ExpressionEvaluator.evaluate("$width / 2",
                Map.of("width", 600.0)));
    }

    @Test
    void testComplexExpression() {
        // $width - 2 * $thickness with operator precedence
        double result = ExpressionEvaluator.evaluate("$width - 2 * $thickness",
                Map.of("width", 500.0, "thickness", 18.0));
        assertEquals(464.0, result, 0.001);
    }

    @Test
    void testMin() {
        assertEquals(100.0, ExpressionEvaluator.evaluate("min($depth, 100)",
                Map.of("depth", 400.0)));
        assertEquals(50.0, ExpressionEvaluator.evaluate("min($depth, 100)",
                Map.of("depth", 50.0)));
    }

    @Test
    void testMax() {
        assertEquals(400.0, ExpressionEvaluator.evaluate("max($depth, 100)",
                Map.of("depth", 400.0)));
    }

    @Test
    void testUndefinedVariable() {
        assertThrows(IllegalArgumentException.class, () ->
                ExpressionEvaluator.evaluate("$missing", Map.of()));
    }

    @Test
    void testSubstituteInLine() {
        String result = ExpressionEvaluator.substituteInLine(
                "create part \"side\" size $width, $height at $x, 0, 0",
                Map.of("width", 600.0, "height", 900.0, "x", 18.0));
        // Variables should be substituted
        assertFalse(result.contains("$"), "All variables should be substituted: " + result);
        assertTrue(result.contains("600"), "Width should be 600: " + result);
    }
}
