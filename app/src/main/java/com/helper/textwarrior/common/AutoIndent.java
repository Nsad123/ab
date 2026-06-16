package com.helper.textwarrior.common;

import java.io.IOException;

/**
 * Auto-indent and formatting for C/C++.
 * Ported from the original Lua AutoIndent while removing all Lua dependencies.
 * Keeps the same public method signatures.
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
                idt += 1;   // special switch handling from original
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
                return 1;
            case "}":
            case "end":      // kept for compatibility
            case "until":
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
                if (lower.startsWith("else") || lower.startsWith("else if") ||
                    lower.startsWith("case ") || lower.startsWith("default:") ||
                    lower.startsWith("public:") || lower.startsWith("private:") || lower.startsWith("protected:")) {
                    builder.append(createIndent(idt * width - width / 2));
                    builder.append(trimmed);
                    isNewLine = false;
                } else if (lower.startsWith("}") || lower.startsWith("end") || lower.startsWith("until")) {
                    idt = Math.max(0, idt - 1);
                    builder.append(createIndent(idt * width));
                    builder.append(trimmed);
                    isNewLine = false;
                } else {
                    builder.append(createIndent(idt * width));
                    builder.append(trimmed);
                    idt += getIndentDelta(trimmed);
                    isNewLine = false;
                }
            } else {
                builder.append(' ').append(trimmed);
            }

            if (i < lines.length - 1) builder.append('\n');
        }
        return builder;
    }

    private static int getIndentDelta(String line) {
        String lower = line.toLowerCase().trim();
        if (lower.endsWith("{")) return 1;
        if (lower.endsWith("else") || lower.endsWith("else if") || lower.endsWith("case") || lower.endsWith("default")) return 1;
        if (lower.endsWith("}")) return -1;
        return 0;
    }

    private static char[] createIndent(int n) {
        if (n < 0) return new char[0];
        char[] idts = new char[n];
        for (int i = 0; i < n; i++) idts[i] = ' ';
        return idts;
    }

    public static int createAutoIndent(String text) {
        return createAutoIndent((CharSequence) text);
    }
}
