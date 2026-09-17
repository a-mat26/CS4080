private Expr ternary() {
  Expr condition = equality();

  if (match(QUESTION)) {
    Expr ifTrue = expression();
    consume(COLON, "Expect ':' in ternary expression.");
    Expr ifFalse = ternary();
    return new Expr.Ternary(condition, ifTrue, ifFalse);
  }

  return condition;
}
