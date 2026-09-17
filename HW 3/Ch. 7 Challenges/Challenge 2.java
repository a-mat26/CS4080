case PLUS:
  if (left instanceof Double && right instanceof Double) {
    return (double)left + (double)right;
  }

  if (left instanceof String || right instanceof String) {
    return stringify(left) + stringify(right);
  }

  throw new RuntimeError(expr.operator,
      "Operands must be two numbers or at least one string.");