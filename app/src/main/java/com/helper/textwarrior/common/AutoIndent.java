package com.helper.textwarrior.common;

import java.io.IOException;

/**
 * Auto-indent and formatting for C/C++.
 * Ported from the original Lua version to support C/C++ syntax while
 * keeping the same public API and behavior.
 */
public class AutoIndent {

    public static int createAutoIndent(CharSequence text) {
        int idt = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        String lower = text.toString().toLowerCase();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : '\0';

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                continue;
            }
            if (inString) {
                if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) inString = false;
                continue;
            }
            if (inChar) {
                if (c == '\'' && (i == 0 || text.charAt(i - 1) != '\\')) inChar = false;
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }

            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '\'') {
                inChar = true;
                continue;
            }

            if (c == '{') {
                idt++;
            } else if (c == '}') {
                idt--;
            } else if (i + 6 < text.length() && lower.startsWith("switch", i)) {
                // Special switch handling like the original Lua version
                idt += 1;
                i += 5;
            }
        }
        return Math.max(0, idt);
    }

    private static int indent(String word) {
        switch (word) {
            case "for":
            case "while":
            case "if":
            case "switch":
            case "else":
            case "else if":
            case "do":
            case "try":
            case "catch":
            case "class":
            case "struct":
            case "namespace":
            case "{":
                return 1;

            case "until":   // kept for compatibility with old calls
            case "end":     // kept for compatibility
            case "}":
                return -1;

            default:
                return 0;
        }
    }

    public static CharSequence format(CharSequence text, int width) {
        StringBuilder builder = new StringBuilder();
        boolean isNewLine = true;

        String[] lines = text.toString().split("\n", -1);
        int idt = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.length() == 0) {
                builder.append('\n');
                continue;
            }

            String lower = trimmed.toLowerCase();

            if (isNewLine) {
                switch (getFirstTokenType(lower)) {
                    case WHITE_SPACE:
                        break;

                    case ELSE:
                    case ELSEIF:
                    case CASE:
                    case DEFAULT:
                        // Half-indent like the original
                        builder.append(createIndent(idt * width - width / 2));
                        builder.append(trimmed);
                        isNewLine = false;
                        break;

                    case DOUBLE_COLON:
                    case AT:
                        builder.append(trimmed);
                        isNewLine = false;
                        break;

                    case END:
                    case UNTIL:
                    case RCURLY:
                        idt = Math.max(0, idt - 1);
                        builder.append(createIndent(idt * width));
                        builder.append(trimmed);
                        isNewLine = false;
                        break;

                    default:
                        builder.append(createIndent(idt * width));
                        builder.append(trimmed);
                        idt += getIndentDelta(trimmed);
                        isNewLine = false;
                }
            } else if (trimmed.length() > 0) {
                builder.append(' ').append(trimmed);
            }

            if (i < lines.length - 1) {
                builder.append('\n');
            }
        }

        return builder;
    }

    // Helper to simulate the old token switch for C/C++
    private static int getIndentDelta(String line) {
        String lower = line.toLowerCase().trim();

        if (lower.endsWith("{")) return 1;
        if (lower.endsWith("else") || lower.endsWith("else if")) return 1;
        if (lower.endsWith("case") || lower.endsWith("default")) return 1;
        if (lower.endsWith("do") || lower.endsWith("try")) return 1;
        if (lower.endsWith("public:") || lower.endsWith("private:") || lower.endsWith("protected:")) return 1;

        if (lower.startsWith("}") || lower.endsWith("}")) return -1;
        if (lower.startsWith("case ") || lower.startsWith("default:")) return -1;

        return 0;
    }

    private static TokenType getFirstTokenType(String lowerLine) {
        if (lowerLine.startsWith("else if")) return TokenType.ELSEIF;
        if (lowerLine.startsWith("else")) return TokenType.ELSE;
        if (lowerLine.startsWith("case ")) return TokenType.CASE;
        if (lowerLine.startsWith("default:")) return TokenType.DEFAULT;
        if (lowerLine.startsWith("}")) return TokenType.RCURLY;
        if (lowerLine.startsWith("end")) return TokenType.END;
        if (lowerLine.startsWith("until")) return TokenType.UNTIL;
        if (lowerLine.startsWith("::")) return TokenType.DOUBLE_COLON;
        if (lowerLine.startsWith("@")) return TokenType.AT;
        if (lowerLine.length() == 0) return TokenType.WHITE_SPACE;
        return TokenType.OTHER;
    }

    private enum TokenType {
        WHITE_SPACE, ELSE, ELSEIF, CASE, DEFAULT, DOUBLE_COLON, AT,
        END, UNTIL, RCURLY, OTHER
    }

    private static char[] createIndent(int n) {
        if (n < 0) return new char[0];
        char[] idts = new char[n];
        for (int i = 0; i < n; i++) idts[i] = ' ';
        return idts;
    }

    // Compatibility overload
    public static int createAutoIndent(String text) {
        return createAutoIndent((CharSequence) text);
    }
}
