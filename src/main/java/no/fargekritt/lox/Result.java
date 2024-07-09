package no.fargekritt.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Result {
    private final List<Stmt> stmt;
    private final Expr expr;

    public Result(List<Stmt> stmt) {
        this.stmt = stmt;
        this.expr = null;
    }

    public Result(Expr expr) {
        this.expr = expr;
        this.stmt = new ArrayList<>();
    }

    public boolean isStmt(){
        return !stmt.isEmpty() && stmt.getFirst() != null;
    }

    public boolean isExpr(){
        return expr != null;
    }

    public Expr getExpr() {
        return expr;
    }

    public List<Stmt> getStmt(){
        return stmt;
    }
}
