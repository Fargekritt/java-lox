package no.fargekritt.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.fargekritt.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private int loopDepth = 0;
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

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (check(FUN) && checkNext(IDENTIFIER)) {
                advance();
                return function("function");
            }
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration(){
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body");

        return new Stmt.Class(name, methods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        return new Stmt.Function(name, functionBody(kind));
    }

    private Expr.Function functionBody(String kind) {
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters");
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Expr.Function(parameters, body);

    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) initializer = expression();

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(IF)) return ifStatement();
        if (match(FOR)) return forStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        if (match(BREAK)) return breakStatement();

        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt breakStatement() {
        Token keyword = previous();
        if (loopDepth == 0) {
            throw error(previous(), "'break' cant be used outside loop");
        }
        consume(SEMICOLON, "Expect ';' after 'break'");
        return new Stmt.Break(keyword);

    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'.");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for clauses.");

        try {
            loopDepth++;
            Stmt body = statement();

            if (increment != null) {
                body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
            }

            if (condition == null) {
                condition = new Expr.Literal(true);
            }
            body = new Stmt.While(condition, body);

            if (initializer != null) {
                body = new Stmt.Block(Arrays.asList(initializer, body));
            }

            return body;
        } finally {
            loopDepth--;
        }

    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expected ')' after expression");
        try {
            loopDepth++;
            Stmt body = statement();
            return new Stmt.While(expr, body);
        } finally {
            loopDepth--;
        }
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expected ')' after expression");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(expr, thenBranch, elseBranch);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }


    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        if (match(PLUS_PLUS, MINUS_MINUS)) {
            Token assigment = previous();
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                Token operator;
                if (assigment.type == MINUS_MINUS) {
                    operator = new Token(MINUS, "-", null, name.line);
                } else {
                    operator = new Token(PLUS, "+", null, name.line);

                }
                Expr desugarAssignment = new Expr.Binary(expr, operator, new Expr.Literal(1.0));
                return new Expr.Assign(name, desugarAssignment);
            }
            throw error(assigment, "Invalid assignment target.");

        }
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get get){
                return new Expr.Set(get.object, get.name, value);
            }

            throw error(equals, "Invalid assignment target.");


        }
        return expr;
    }

    private Expr or() {
        Expr left = and();
        if (match(OR)) {
            Expr right = and();
            Token operator = previous();
            return new Expr.Logical(left, operator, right);
        }
        return left;
    }

    private Expr and() {
        Expr left = equality();
        if (match(AND)) {
            Expr right = equality();
            Token operator = previous();
            return new Expr.Logical(left, operator, right);
        }
        return left;
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

        while (match(GREATER_EQUAL, GREATER, LESS_EQUAL, LESS)) {
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)){
                Token name = consume(IDENTIFIER, "Expect property name after '.'");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }
        return expr;
    }


    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }


    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(FUN)) {
            return functionBody("function");
        }

        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);
        if (match(THIS)) return new Expr.This(previous());
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
     *
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
     *
     * @param type TokenType to check
     * @return True if current Token is same as param
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (isAtEnd()) return false;
        return tokens.get(current + 1).type == type;
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
        // NOTE:
        // This should not be needed.
        // Fucks up when the start of statement is the next token
        // its needs to be there anyway!
        // My bad
        advance();


        while (!isAtEnd()) {

            // If we are at the end of a statement
            if (previous().type == SEMICOLON) return;

            // If we are at the start of a statement
            switch (peek().type) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, RETURN:
                    return;
            }
            advance();
        }
    }


}
