package no.fargekritt.lox;

import java.util.ArrayList;
import java.util.List;

import static no.fargekritt.lox.TokenType.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList();

    //  start string index
    private int start = 0;
    // Current index
    private int current = 0;
    // Line we at
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '<' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '>' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '/' -> {
                if (match('/')) {
                    // Comments the whole line
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\t', '\r' -> {
                // Skip white space.
            }
            case '\n' -> line++;
            default -> Lox.error(line, "Unexpected character.");
        }
    }

    // Checks if the character is what we expect, consumes if it does.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // Look at the next character without consuming it.
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Consumes a character and returns it
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
