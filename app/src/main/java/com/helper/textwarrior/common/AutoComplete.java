package com.helper.textwarrior.common;

import java.util.ArrayList;

/**
 * Auto-indent and formatting helper for C/C++.
 * Completely rewritten to remove all Lua dependencies.
 * Uses simple brace counting + common C/C++ keyword rules.
 */
public class AutoComplete {

    /**
     * Calculate how much the indent level should change based on the line.
     * Positive = increase indent, negative = decrease.
     */
    public static int computeIndentDelta(String line) {
        int delta = 0;
        String trimmed = line.trim().toLowerCase();

        // Increase indent
        if (trimmed.endsWith("{")) {
            delta++;
        } else if (trimmed.endsWith("else") ||
                   trimmed.endsWith("else if") ||
                   trimmed.endsWith("case") ||
                   trimmed.endsWith("default") ||
                   trimmed.endsWith("public:") ||
                   trimmed.endsWith("private:") ||
                   trimmed.endsWith("protected:")) {
            delta++;
        }

        // Decrease indent
        if (trimmed.startsWith("}") || trimmed.endsWith("}")) {
            delta--;
        } else if (trimmed.startsWith("case ") ||
                   trimmed.startsWith("default:")) {
            delta--; // case/default usually de-indent relative to switch
        }

        return delta;
    }

    /**
     * Creates auto-indent for a new line (simple version).
     * Counts net open braces in the whole text.
     */
    public static int createAutoIndent(CharSequence text) {
        int indent = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean inComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = (i + 1 < text.length()) ? text.charAt(i + 1) : 0;

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }
            if (inComment) {
                if (c == '\n') inComment = false;
                continue;
            }
            if (inString) {
                if (c == '"' && text.charAt(i - 1) != '\\') inString = false;
                continue;
            }
            if (inChar) {
                if (c == '\'' && text.charAt(i - 1) != '\\') inChar = false;
                continue;
            }

            if (c == '/' && next == '/') {
                inComment = true;
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
                indent++;
            } else if (c == '}') {
                indent--;
            }
        }

        return Math.max(0, indent);
    }

    /**
     * Format the entire text with proper indentation.
     */
    public static CharSequence format(CharSequence text, int width) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.toString().split("\n", -1);

        int indentLevel = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                result.append("\n");
                continue;
            }

            // Decrease indent before printing lines that start with }
            if (trimmed.startsWith("}") || trimmed.startsWith("case ") || trimmed.startsWith("default:")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            // Add indentation
            for (int j = 0; j < indentLevel * width; j++) {
                result.append(' ');
            }

            result.append(trimmed);

            // Increase indent after lines that end with {
            if (trimmed.endsWith("{")) {
                indentLevel++;
            } else if (trimmed.endsWith("else") ||
                       trimmed.endsWith("else if") ||
                       trimmed.endsWith("case") ||
                       trimmed.endsWith("default") ||
                       trimmed.endsWith("public:") ||
                       trimmed.endsWith("private:") ||
                       trimmed.endsWith("protected:")) {
                indentLevel++;
            }

            // Handle closing brace on same line
            if (trimmed.endsWith("}")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result;
    }

    /**
     * Simple helper to create spaces for indentation.
     */
    private static char[] createIndent(int n) {
        if (n < 0) return new char[0];
        char[] idts = new char[n];
        for (int i = 0; i < n; i++) {
            idts[i] = ' ';
        }
        return idts;
    }

    // For compatibility with old callers (if any)
    public static int createAutoIndent(String text) {
        return createAutoIndent((CharSequence) text);
    }
}
