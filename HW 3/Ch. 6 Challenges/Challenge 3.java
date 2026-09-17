// Goes in Parser, replacing unary(). The second if is the error production.

private Expr unary() {
  if (match(BANG, MINUS)) {
    Token operator = previous();
    Expr right = unary();
    return new Expr.Unary(operator, right);
  }

  if (match(COMMA, BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL,
            LESS, LESS_EQUAL, PLUS, SLASH, STAR)) {
    Token operator = previous();
    error(operator, "Binary operator is missing a left operand.");

    switch (operator.type) {
      case COMMA:
        ternary();
        break;
      case BANG_EQUAL:
      case EQUAL_EQUAL:
        comparison();
        break;
      case GREATER:
      case GREATER_EQUAL:
      case LESS:
      case LESS_EQUAL:
        term();
        break;
      case PLUS:
        factor();
        break;
      case SLASH:
      case STAR:
        unary();
        break;
    }

    return null;
  }

  return primary();
}
