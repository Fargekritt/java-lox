package no.fargekritt.lox;

public class RPNPrinter implements Expr.Visitor<String> {
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
//        return Rpninfy(expr.operator.lexeme, expr.left, expr.right);
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme;
    }


    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return "";
    }

    private String print(Expr expression) {
        return expression.accept(this);
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(new Expr.Grouping(
                new Expr.Binary(new Expr.Literal(1), new Token(TokenType.PLUS, "+", null, 1), new Expr.Literal(2))),
                new Token(TokenType.STAR, "*", null, 1), new Expr.Grouping(
                new Expr.Binary(new Expr.Literal(3), new Token(TokenType.PLUS, "-", null, 1), new Expr.Literal(4))));

        System.out.println(new RPNPrinter().print(expression));
    }


}
