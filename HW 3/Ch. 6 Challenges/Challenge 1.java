// expression -> comma ;
private Expr expression() {
  return comma();
}

// comma -> ternary ( "," ternary )* ;
private Expr comma() {
  Expr expr = ternary();

  while (match(COMMA)) {
    Token operator = previous();
    Expr right = ternary();
    expr = new Expr.Binary(expr, operator, right);
  }

  return expr;
}
