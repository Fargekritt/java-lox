package no.fargekritt.lox;

import java.util.ArrayList;
import java.util.List;

import static no.fargekritt.lox.Lox.error;
import static no.fargekritt.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration(){
        try {
            if(match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(EQUAL)) initializer = expression();

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);

    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }


    private Expr expression() {
        return assignment();
    }

    private Expr assignment(){
        Expr expr = equality();

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name,value);
            }

            error(equals, "Invalid assignment target.");


        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {

        Expr expr = term();

        while (match(GREATER_EQUAL, GREATER, LESS_EQUAL, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }


    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
        if (match(IDENTIFIER)) return new Expr.Variable(previous());


        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }


    /**
     * Takes a list of TokenTypes, and if current is that token type advance the counter by 1
     * @param types List of TokenTypes to match
     * @return True if match was found
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;

    }

    // Enter panic mode!
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * Checks if the current Token is of a specific TokenType
     * @param type TokenType to check
     * @return True if current Token is same as param
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    /**
     *
     * @return The previous token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    // Go forward till we find the start of a new statement
    private void synchronize() {
        advance();


        while (!isAtEnd()) {

            // If we are at the end of a statement
            if (previous().type == SEMICOLON) return;

            // If we are at the start of a statement
            switch (peek().type) {
                case CLASS,
                     FUN,
                     VAR,
                     FOR,
                     IF,
                     WHILE,
                     PRINT,
                     RETURN:
                    return;
            }
            advance();
        }
    }


}
