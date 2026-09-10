package com.craftinginterpreters.lox;
/**
 * Chapter 5, Challenge 3:
 *
 *   (1 + 2) * (4 - 3)   becomes   1 2 + 4 3 - *
 *
 */
class RpnPrinter implements Expr.Visitor<Void> {
  private final StringBuilder output = new StringBuilder();

  String print(Expr expr) {
    output.setLength(0);
    expr.accept(this);
    return output.toString();
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    expr.left.accept(this);
    expr.right.accept(this);
    emit(expr.operator.lexeme);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    return expr.expression.accept(this);
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) {
      emit("nil");
    } else {
      emit(expr.value.toString());
    }
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    expr.right.accept(this);

    if (expr.operator.type == TokenType.MINUS) {
      emit("neg");
    } else {
      emit(expr.operator.lexeme);
    }
    return null;
  }

  private void emit(String token) {
    if (output.length() > 0) output.append(" ");
    output.append(token);
  }

  public static void main(String[] args) {
    // (1 + 2) * (4 - 3)
    Expr expression = new Expr.Binary(
        new Expr.Grouping(
            new Expr.Binary(
                new Expr.Literal(1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Expr.Literal(2))),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Binary(
                new Expr.Literal(4),
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(3))));

    System.out.println(new RpnPrinter().print(expression));
  }
}