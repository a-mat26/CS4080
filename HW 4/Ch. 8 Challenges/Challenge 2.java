// Environment.java
static final Object UNINITIALIZED = new Object();

Object get(Token name) {
  if (values.containsKey(name.lexeme)) {
    Object value = values.get(name.lexeme);

    if (value == UNINITIALIZED) {
      throw new RuntimeError(name,
          "Variable '" + name.lexeme + "' has not been initialized.");
    }

    return value;
  }

  if (enclosing != null) return enclosing.get(name);

  throw new RuntimeError(name,
      "Undefined variable '" + name.lexeme + "'.");
}

// Interpreter.java
@Override
public Void visitVarStmt(Stmt.Var stmt) {
  Object value = Environment.UNINITIALIZED;
  if (stmt.initializer != null) {
    value = evaluate(stmt.initializer);
  }

  environment.define(stmt.name.lexeme, value);
  return null;
}