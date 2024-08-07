package no.fargekritt.lox;

import java.util.*;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<VariableManager> scopes = new Stack<>();

    private FunctionType currentFunction = FunctionType.NONE;
    private boolean inLoop = false;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expression) {
        expression.accept(this);
    }

    private void beginScope() {
        scopes.push(new VariableManager());
    }

    private void endScope() {
        VariableManager scope = scopes.pop();
        List<Variable> unusedVars = scope.getUnusedVars();
        for (Variable unusedVar : unusedVars) {
            Lox.warn(unusedVar.name, "Local variable not used.");
        }

    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().declare(name);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().define(name);
    }

    private void resolveLocal(Expr expr, Token name, boolean isRead){
        for (int i = scopes.size() - 1; i >= 0; i--){
           if (scopes.get(i).containsVar(name.lexeme)) {
               int jumps = scopes.size() - 1 - i;
               interpreter.resolve(expr, jumps);
               if(isRead) {
                   scopes.get(i).addUsage(name.lexeme);
               }
               return;
           }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token parameter : function.function.parameters) {
            declare(parameter);
            define(parameter);
        }

        resolve(function.function.body);
        endScope();
        currentFunction = enclosingFunction;

    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name, false);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        resolve(expr);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().varReady(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name, "Can't read local variable in its own initializer");
        }

        resolveLocal(expr, expr.name, true);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (!inLoop) Lox.error(stmt.keyword, "Can't use break outside of a loop");
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE){
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        inLoop = true;
        resolve(stmt.body);
        inLoop = false;
        return null;
    }
}
