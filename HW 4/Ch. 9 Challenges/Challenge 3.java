// TokenType.java
AND, BREAK, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,

// Scanner.java
keywords.put("break",  BREAK);

//GenerateAst.java
"Break      : Token keyword",

// Parser.java

private boolean inLoop = false;

if (match(BREAK)) return breakStatement();

private Stmt breakStatement() {
  Token keyword = previous();

  if (!inLoop) {
    error(keyword, "Can't use 'break' outside of a loop.");
  }

  consume(SEMICOLON, "Expect ';' after 'break'.");
  return new Stmt.Break(keyword);
}

boolean enclosingLoop = inLoop;
inLoop = true;
Stmt body;
try {
  body = statement();
} finally {
  inLoop = enclosingLoop;
}

// Interpreter.java
private boolean breaking = false;

@Override
public Void visitBreakStmt(Stmt.Break stmt) {
  breaking = true;
  return null;
}

for (Stmt statement : statements) {
  execute(statement);

  if (breaking) break;
}

@Override
public Void visitWhileStmt(Stmt.While stmt) {
  while (isTruthy(evaluate(stmt.condition))) {
    execute(stmt.body);

    if (breaking) {
      breaking = false;
      break;
    }
  }
  return null;
}