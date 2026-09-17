// Goes in Parser, between comma() and equality().
// Needs the QUESTION and COLON tokens from Scanner, and the
// "Ternary : Expr condition, Expr ifTrue, Expr ifFalse" node in GenerateAst.

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
