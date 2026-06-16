/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * Enhanced for better C/C++ support (2026).
 */
package com.helper.textwarrior.common;

/**
 * Singleton class containing the symbols and operators of the C/C++ language.
 * Enhanced version with many common keywords and standard library names for better autocomplete.
 */
public class LanguageC extends Language {
    private static Language _theOne = null;

    // Core C/C++ keywords
    private final static String[] keywords = {
        // C keywords
        "char", "double", "float", "int", "long", "short", "void",
        "auto", "const", "extern", "register", "static", "volatile",
        "signed", "unsigned", "sizeof", "typedef",
        "enum", "struct", "union",
        "break", "case", "continue", "default", "do", "else", "for",
        "goto", "if", "return", "switch", "while",

        // C++ keywords
        "class", "namespace", "template", "typename", "public", "private", "protected",
        "virtual", "override", "final", "friend", "inline", "explicit",
        "try", "catch", "throw", "new", "delete", "this", "nullptr",
        "bool", "true", "false", "mutable", "constexpr", "noexcept",
        "using", "namespace", "decltype", "static_assert", "alignas", "alignof"
    };

    // Common C/C++ standard library names + popular functions (for autocomplete)
    private final static String[] names = {
        // Common headers / namespaces
        "std", "iostream", "vector", "string", "map", "unordered_map",
        "set", "unordered_set", "algorithm", "memory", "utility",
        "fstream", "sstream", "iomanip", "cmath", "cstdlib", "cstdio",
        "cstring", "cassert", "chrono", "thread", "mutex", "condition_variable",

        // Common std functions / objects
        "cout", "cin", "endl", "cerr", "clog",
        "printf", "scanf", "malloc", "free", "memcpy", "memset",
        "strlen", "strcpy", "strcmp", "atoi", "atof",
        "vector", "string", "map", "pair", "make_pair",
        "push_back", "pop_back", "size", "empty", "clear",
        "begin", "end", "find", "sort", "reverse",
        "unique", "erase", "insert", "emplace", "emplace_back",
        "std::cout", "std::cin", "std::endl", "std::vector", "std::string"
    };

    // Base packages for autocomplete (like Lua style)
    private final static String std = "cout|cin|endl|cerr|clog|vector|string|map|unordered_map|set|pair|make_pair|unique_ptr|shared_ptr|move|forward";
    private final static String iostream = "cout|cin|endl|cerr|clog|ios|ios_base|streambuf";
    private final static String vector = "push_back|pop_back|size|empty|clear|begin|end|at|front|back|insert|erase|emplace|emplace_back|resize|reserve";
    private final static String string = "length|size|empty|clear|substr|find|replace|append|push_back|pop_back|compare|data|c_str";
    private final static String algorithm = "sort|reverse|find|count|fill|copy|move|transform|for_each|min|max|min_element|max_element|swap|accumulate";
    private final static String cmath = "abs|acos|asin|atan|atan2|ceil|cos|cosh|exp|floor|fmod|log|log10|pow|sin|sinh|sqrt|tan|tanh|round|trunc";
    private final static String cstdio = "printf|scanf|fprintf|fscanf|sprintf|sscanf|fopen|fclose|fread|fwrite|feof|ferror|fflush|rewind|ftell|fseek";

    private final static char[] CPP_OPERATORS = {
        '(', ')', '{', '}', '[', ']', ',', ';', '=', '+', '-',
        '/', '*', '&', '!', '|', ':', '<', '>', '?', '~', '%', '^',
        '.'
    };

    public static Language getInstance() {
        if (_theOne == null) {
            _theOne = new LanguageC();
        }
        return _theOne;
    }

    private LanguageC() {
        super.setKeywords(keywords);
        super.setNames(names);
        super.setOperators(CPP_OPERATORS);

        // Add base packages for rich autocomplete (similar to LanguageLua)
        super.addBasePackage("std", std.split("\\|"));
        super.addBasePackage("iostream", iostream.split("\\|"));
        super.addBasePackage("vector", vector.split("\\|"));
        super.addBasePackage("string", string.split("\\|"));
        super.addBasePackage("algorithm", algorithm.split("\\|"));
        super.addBasePackage("cmath", cmath.split("\\|"));
        super.addBasePackage("cstdio", cstdio.split("\\|"));
    }
}
