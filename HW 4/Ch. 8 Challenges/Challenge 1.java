// Lox.java

run(new String(bytes, Charset.defaultCharset()), false);
run(line, true);

private static void run(String source, boolean repl) {
  Scanner scanner = new Scanner(source);
  List<Token> tokens = scanner.scanTokens();
  Parser parser = new Parser(tokens, repl);
  List<Stmt> statements = parser.parse();
  if (hadError) return;

  interpreter.interpret(statements);
}

// Parser.java
private final List<Token> tokens;
private final boolean repl;
private int current = 0;

Parser(List<Token> tokens, boolean repl) {
  this.tokens = tokens;
  this.repl = repl;
}

private Stmt expressionStatement() {
  Expr expr = expression();

  // Challenge 1
  if (repl && isAtEnd()) {
    return new Stmt.Print(expr);
  }

  consume(SEMICOLON, "Expect ';' after expression.");
  return new Stmt.Expression(expr);
}