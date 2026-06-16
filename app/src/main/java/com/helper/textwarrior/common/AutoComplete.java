package com.helper.textwarrior.common;

import java.util.ArrayList;

/**
 * Auto-indent and formatting helper for C/C++ (no Lua dependencies).
 */
public class AutoComplete {

    public static int createAutoIndent(CharSequence text) {
        int indent = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

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
                indent++;
            } else if (c == '}') {
                indent--;
            }
        }
        return Math.max(0, indent);
    }

    public static CharSequence format(CharSequence text, int width) {
        StringBuilder builder = new StringBuilder();
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

            if (lower.startsWith("}") || lower.startsWith("case ") || lower.startsWith("default:") ||
                lower.startsWith("public:") || lower.startsWith("private:") || lower.startsWith("protected:")) {
                idt = Math.max(0, idt - 1);
            }

            for (int j = 0; j < idt * width; j++) builder.append(' ');
            builder.append(trimmed);

            if (trimmed.endsWith("{")) {
                idt++;
            } else if (lower.endsWith("else") || lower.endsWith("else if") ||
                       lower.endsWith("case") || lower.endsWith("default") ||
                       lower.endsWith("public:") || lower.endsWith("private:") || lower.endsWith("protected:")) {
                idt++;
            }

            if (trimmed.endsWith("}")) {
                idt = Math.max(0, idt - 1);
            }

            if (i < lines.length - 1) builder.append('\n');
        }
        return builder;
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
