/*
 * Copyright 2026 Bob Hablutzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source: https://github.com/bobhablutzel/cadette
 */

package app.cadette.command;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates arithmetic expressions with $variable substitution.
 * Supports: numbers, $var, +, -, *, /, parentheses.
 *
 * Usage:
 *   evaluate("$width - 2 * $thickness", Map.of("width", 600.0, "thickness", 19.05))
 *   → 561.9
 */
public class ExpressionEvaluator {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$([a-zA-Z_][a-zA-Z0-9_]*)");

    /** Substitute $variables and evaluate the arithmetic expression. */
    public static double evaluate(String expr, Map<String, Double> vars) {
        String substituted = substituteVars(expr, vars);
        Parser parser = new Parser(substituted.trim());
        double result = parser.parseExpression();
        if (parser.pos < parser.input.length()) {
            throw new IllegalArgumentException(
                    "Unexpected character at position " + parser.pos + ": '" + parser.input.charAt(parser.pos) + "'");
        }
        return result;
    }

    /** Replace $var tokens with their numeric values. */
    static String substituteVars(String expr, Map<String, Double> vars) {
        Matcher m = VAR_PATTERN.matcher(expr);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String varName = m.group(1);
            Double val = vars.get(varName);
            if (val == null) {
                throw new IllegalArgumentException("Undefined variable: $" + varName);
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Substitute and evaluate all $-expressions in a command line.
     * Finds tokens that contain $ or arithmetic operators in numeric positions
     * and evaluates them, leaving the rest of the line intact.
     *
     * Strategy: find contiguous runs of characters that could be expressions
     * (containing $, digits, operators, parens, spaces between them) and evaluate them.
     */
    /**
     * Substitute $variables and evaluate all expressions in a command line.
     * Splits the line into tokens separated by commas and whitespace-delimited
     * keyword boundaries. Any token that looks like an expression (contains
     * operators, parens, or function calls) is evaluated.
     */
    public static String substituteInLine(String line, Map<String, Double> vars) {
        // First: substitute all $var references
        String substituted = substituteVars(line, vars);

        // Second: find and evaluate expression tokens.
        // An expression token is a comma-separated value that contains arithmetic
        // (operators, parens, function calls). We split on commas in numeric
        // positions and evaluate each piece.
        return evaluateExpressionsInLine(substituted);
    }

    /**
     * Scan the line for tokens that could be expressions and evaluate them.
     * Strategy: find contiguous runs that look like math (digits, operators,
     * parens, function names like min/max) bounded by command keywords or commas.
     */
    private static String evaluateExpressionsInLine(String line) {
        // Match tokens that look like expressions:
        // - start with a digit, minus, opening paren, or function name (min/max)
        // - contain arithmetic characters
        // - bounded by whitespace, comma, or line boundaries
        // This pattern captures function calls like min(400.0, 100)
        // as well as arithmetic like 600.0 - 2.0 * 19.05
        Pattern exprPattern = Pattern.compile(
                "(?<=^|[\\s,])" +                                          // preceded by start/space/comma
                "((?:min|max)\\s*\\([^)]+\\)" +                            // function call: min(...) or max(...)
                "|-?[0-9]*\\.?[0-9]+(?:\\s*[+\\-*/]\\s*(?:(?:min|max)\\s*\\([^)]+\\)|-?[0-9]*\\.?[0-9]+|\\([^)]+\\)))+" + // arithmetic chain
                "|-?\\([^)]+\\)" +                                         // parenthesized expression
                ")(?=[\\s,]|$)");                                          // followed by space/comma/end

        Matcher m = exprPattern.matcher(line);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String expr = m.group(0).trim();
            try {
                double val = evaluateSimple(expr);
                m.appendReplacement(sb, Matcher.quoteReplacement(formatNumber(val)));
            } catch (Exception ignored) {
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Evaluate a pure arithmetic string (no $variables). */
    private static double evaluateSimple(String expr) {
        Parser parser = new Parser(expr.trim());
        double result = parser.parseExpression();
        return result;
    }

    private static String formatNumber(double val) {
        if (val == Math.floor(val) && !Double.isInfinite(val)) {
            return String.valueOf((long) val);
        }
        // Use enough precision but trim trailing zeros
        String s = String.format("%.4f", val);
        s = s.contains(".") ? s.replaceAll("0+$", "").replaceAll("\\.$", "") : s;
        return s;
    }

    /** Simple recursive-descent parser for arithmetic expressions. */
    private static class Parser {
        final String input;
        int pos = 0;

        Parser(String input) {
            this.input = input;
        }

        // expression = term (('+' | '-') term)*
        double parseExpression() {
            double result = parseTerm();
            while (pos < input.length()) {
                skipSpaces();
                if (pos < input.length() && input.charAt(pos) == '+') {
                    pos++;
                    result += parseTerm();
                } else if (pos < input.length() && input.charAt(pos) == '-') {
                    pos++;
                    result -= parseTerm();
                } else {
                    break;
                }
            }
            return result;
        }

        // term = factor (('*' | '/') factor)*
        double parseTerm() {
            double result = parseFactor();
            while (pos < input.length()) {
                skipSpaces();
                if (pos < input.length() && input.charAt(pos) == '*') {
                    pos++;
                    result *= parseFactor();
                } else if (pos < input.length() && input.charAt(pos) == '/') {
                    pos++;
                    double divisor = parseFactor();
                    if (divisor == 0) throw new ArithmeticException("Division by zero");
                    result /= divisor;
                } else {
                    break;
                }
            }
            return result;
        }

        // factor = number | '(' expression ')' | '-' factor | min(expr, expr) | max(expr, expr)
        double parseFactor() {
            skipSpaces();
            if (pos >= input.length()) {
                throw new IllegalArgumentException("Unexpected end of expression");
            }

            char c = input.charAt(pos);

            // Function calls: min(a, b), max(a, b)
            if (Character.isLetter(c)) {
                int start = pos;
                while (pos < input.length() && Character.isLetter(input.charAt(pos))) pos++;
                String funcName = input.substring(start, pos).toLowerCase();
                skipSpaces();
                if (pos < input.length() && input.charAt(pos) == '(') {
                    pos++; // skip '('
                    double a = parseExpression();
                    skipSpaces();
                    if (pos < input.length() && input.charAt(pos) == ',') pos++; // skip ','
                    double b = parseExpression();
                    skipSpaces();
                    if (pos < input.length() && input.charAt(pos) == ')') {
                        pos++; // skip ')'
                    } else {
                        throw new IllegalArgumentException("Missing closing ')' for " + funcName);
                    }
                    return switch (funcName) {
                        case "min" -> Math.min(a, b);
                        case "max" -> Math.max(a, b);
                        default -> throw new IllegalArgumentException("Unknown function: " + funcName);
                    };
                }
                throw new IllegalArgumentException("Expected '(' after function name: " + funcName);
            }

            // Parenthesized expression
            if (c == '(') {
                pos++; // skip '('
                double result = parseExpression();
                skipSpaces();
                if (pos < input.length() && input.charAt(pos) == ')') {
                    pos++; // skip ')'
                } else {
                    throw new IllegalArgumentException("Missing closing parenthesis");
                }
                return result;
            }

            // Unary minus
            if (c == '-') {
                pos++;
                return -parseFactor();
            }

            // Number
            return parseNumber();
        }

        double parseNumber() {
            skipSpaces();
            int start = pos;
            if (pos < input.length() && input.charAt(pos) == '-') pos++;
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            if (pos == start) {
                throw new IllegalArgumentException(
                        "Expected number at position " + pos +
                        (pos < input.length() ? ": '" + input.charAt(pos) + "'" : " (end of input)"));
            }
            return Double.parseDouble(input.substring(start, pos));
        }

        void skipSpaces() {
            while (pos < input.length() && input.charAt(pos) == ' ') pos++;
        }
    }
}
