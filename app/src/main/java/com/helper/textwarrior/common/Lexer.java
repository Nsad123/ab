/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * Modified for standalone CppEditor (C/C++ focus) - 2026
 */
package com.helper.textwarrior.common;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexical analyzer for C-like languages.
 * For C/C++ we use the reliable generic tokenizer.
 * Lua-specific full lexer is kept only for compatibility.
 */
public class Lexer {
    public final static int UNKNOWN = -1;
    public final static int NORMAL = 0;
    public final static int KEYWORD = 1;
    public final static int OPERATOR = 2;
    public final static int NAME = 3;
    public final static int LITERAL = 4;

    public final static int SINGLE_SYMBOL_WORD = 10;
    public final static int SINGLE_SYMBOL_LINE_A = 20;
    public final static int SINGLE_SYMBOL_LINE_B = 21;
    public final static int DOUBLE_SYMBOL_LINE = 30;
    public final static int DOUBLE_SYMBOL_DELIMITED_MULTILINE = 40;
    public final static int SINGLE_SYMBOL_DELIMITED_A = 50;
    public final static int SINGLE_SYMBOL_DELIMITED_B = 51;

    private final static int MAX_KEYWORD_LENGTH = 127;

    private static Language _globalLanguage = LanguageNonProg.getInstance();

    LexCallback _callback = null;
    private DocumentProvider _hDoc;
    private LexThread _workerThread = null;

    public Lexer(LexCallback callback) {
        _callback = callback;
    }

    synchronized public static Language getLanguage() {
        return _globalLanguage;
    }

    synchronized public static void setLanguage(Language lang) {
        _globalLanguage = lang;
    }

    public void tokenize(DocumentProvider hDoc) {
        if (!Lexer.getLanguage().isProgLang()) return;

        setDocument(new DocumentProvider(hDoc));

        if (_workerThread == null) {
            _workerThread = new LexThread(this);
            _workerThread.start();
        } else {
            _workerThread.restart();
        }
    }

    void tokenizeDone(List result) {
        if (_callback != null) _callback.lexDone(result);
        _workerThread = null;
    }

    public void cancelTokenize() {
        if (_workerThread != null) {
            _workerThread.abort();
            _workerThread = null;
        }
    }

    public synchronized DocumentProvider getDocument() {
        return _hDoc;
    }

    public synchronized void setDocument(DocumentProvider hDoc) {
        _hDoc = hDoc;
    }

    public interface LexCallback {
        void lexDone(List results);
    }

    private static ArrayList mLines = new ArrayList<>();

    public static ArrayList getLines() {
        return mLines;
    }

    private class LexThread extends Thread {
        private final Lexer _lexManager;
        private final Flag _abort;
        private boolean rescan = false;
        private ArrayList _tokens;

        public LexThread(Lexer p) {
            _lexManager = p;
            _abort = new Flag();
        }

        @Override
        public void run() {
            do {
                rescan = false;
                _abort.clear();

                // For C/C++ we use the generic reliable tokenizer.
                // Lua mode is kept for compatibility but falls back to generic.
                if (Lexer.getLanguage() instanceof LanguageLua) {
                    tokenizeLua();
                } else {
                    tokenizeGeneric();   // ← This is what CppEditor uses
                }
            } while (rescan);

            if (!_abort.isSet()) {
                _lexManager.tokenizeDone(_tokens);
            }
        }

        public void restart() {
            rescan = true;
            _abort.set();
        }

        public void abort() {
            _abort.set();
        }

        // ==================== GENERIC TOKENIZER (Recommended for C/C++) ====================
        ArrayList<Pair> typedTokens = new ArrayList<>();
        private void tokenizeGeneric() {
            DocumentProvider hDoc = getDocument();
            Language language = Lexer.getLanguage();
            ArrayList tokens = new ArrayList();

            if (!language.isProgLang()) {
                tokens.add(new Pair(0, NORMAL));
                _tokens = tokens;
                return;
            }

            char[] candidateWord = new char[MAX_KEYWORD_LENGTH];
            int currentCharInWord = 0;
            int spanStartPosition = 0;
            int workingPosition = 0;
            int state = UNKNOWN;
            char prevChar = 0;

            hDoc.seekChar(0);
            while (hDoc.hasNext() && !_abort.isSet()) {
                char currentChar = hDoc.next();

                switch (state) {
                    case UNKNOWN:
                    case NORMAL:
                    case KEYWORD:
                    case NAME:
                    case SINGLE_SYMBOL_WORD:
                        int pendingState = state;
                        boolean stateChanged = false;

                        if (language.isLineStart(prevChar, currentChar)) {
                            pendingState = DOUBLE_SYMBOL_LINE;
                            stateChanged = true;
                        } else if (language.isMultilineStartDelimiter(prevChar, currentChar)) {
                            pendingState = DOUBLE_SYMBOL_DELIMITED_MULTILINE;
                            stateChanged = true;
                        } else if (language.isDelimiterA(currentChar)) {
                            pendingState = SINGLE_SYMBOL_DELIMITED_A;
                            stateChanged = true;
                        } else if (language.isDelimiterB(currentChar)) {
                            pendingState = SINGLE_SYMBOL_DELIMITED_B;
                            stateChanged = true;
                        } else if (language.isLineAStart(currentChar)) {
                            pendingState = SINGLE_SYMBOL_LINE_A;
                            stateChanged = true;
                        } else if (language.isLineBStart(currentChar)) {
                            pendingState = SINGLE_SYMBOL_LINE_B;
                            stateChanged = true;
                        }

                        if (stateChanged) {
                            if (pendingState == DOUBLE_SYMBOL_LINE || pendingState == DOUBLE_SYMBOL_DELIMITED_MULTILINE) {
                                spanStartPosition = workingPosition - 1;
                                if (tokens.size() > 0 && ((Pair) tokens.get(tokens.size() - 1)).getFirst() == spanStartPosition) {
                                    tokens.remove(tokens.size() - 1);
                                }
                            } else {
                                spanStartPosition = workingPosition;
                            }

                            if (currentCharInWord > 0 && state != NORMAL) {
                                tokens.add(new Pair(workingPosition - currentCharInWord, NORMAL));
                            }

                            state = pendingState;
                            tokens.add(new Pair(spanStartPosition, state));
                            currentCharInWord = 0;
                        } else if (language.isWhitespace(currentChar) || language.isOperator(currentChar)) {
                            if (currentCharInWord > 0) {
                                String word = new String(candidateWord, 0, currentCharInWord);
                                if (language.isWordStart(candidateWord[0])) {
                                    tokens.add(new Pair(workingPosition - currentCharInWord, SINGLE_SYMBOL_WORD));
                                } else if (language.isKeyword(word)) {
                                    tokens.add(new Pair(workingPosition - currentCharInWord, KEYWORD));
                                } else if (language.isName(word)) {
                                    tokens.add(new Pair(workingPosition - currentCharInWord, NAME));
                                } else if (state != NORMAL) {
                                    tokens.add(new Pair(workingPosition - currentCharInWord, NORMAL));
                                }
                                currentCharInWord = 0;
                            }

                            if (state != NORMAL && language.isOperator(currentChar)) {
                                state = NORMAL;
                                tokens.add(new Pair(workingPosition, state));
                            }
                        } else if (currentCharInWord < MAX_KEYWORD_LENGTH) {
                            candidateWord[currentCharInWord] = currentChar;
                            currentCharInWord++;
                        }
                        break;

                    case DOUBLE_SYMBOL_LINE:
                    case SINGLE_SYMBOL_LINE_A:
                    case SINGLE_SYMBOL_LINE_B:
                        if (language.isMultilineStartDelimiter(prevChar, currentChar)) {
                            state = DOUBLE_SYMBOL_DELIMITED_MULTILINE;
                        } else if (currentChar == '\n') {
                            state = UNKNOWN;
                        }
                        break;

                    case SINGLE_SYMBOL_DELIMITED_A:
                        if ((language.isDelimiterA(currentChar) || currentChar == '\n') && !language.isEscapeChar(prevChar)) {
                            state = UNKNOWN;
                        } else if (language.isEscapeChar(currentChar) && language.isEscapeChar(prevChar)) {
                            currentChar = ' ';
                        }
                        break;

                    case SINGLE_SYMBOL_DELIMITED_B:
                        if ((language.isDelimiterB(currentChar) || currentChar == '\n') && !language.isEscapeChar(prevChar)) {
                            state = UNKNOWN;
                        } else if (language.isEscapeChar(currentChar) && language.isEscapeChar(prevChar)) {
                            currentChar = ' ';
                        }
                        break;

                    case DOUBLE_SYMBOL_DELIMITED_MULTILINE:
                        if (language.isMultilineEndDelimiter(prevChar, currentChar)) {
                            state = UNKNOWN;
                        }
                        break;

                    default:
                        TextWarriorException.fail("Invalid state in TokenScanner");
                        break;
                }
                ++workingPosition;
                prevChar = currentChar;
            }

            if (tokens.isEmpty()) {
                tokens.add(new Pair(0, NORMAL));
            }
            _tokens = tokens;
        }

        // Lua tokenizer kept for compatibility (falls back to generic)
        private void tokenizeLua() {
            tokenizeGeneric();
        }
    }
}
