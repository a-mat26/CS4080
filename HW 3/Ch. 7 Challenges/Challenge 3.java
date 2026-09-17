case SLASH:
  checkNumberOperands(expr.operator, left, right);

  if ((double)right == 0) {
    throw new RuntimeError(expr.operator, "Division by zero.");
  }

  return (double)left / (double)right;
