package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * Chapter 6, Challenges 1-3:
 *
 *   1, 2, 3                    becomes   (, (, 1.0 2.0) 3.0)
 *   true ? 1 : false ? 2 : 3   becomes   (?: true 1.0 (?: false 2.0 3.0))
 *   * 2                        reports   [line 1] Error at '*': ...
 *
 */
class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }

  // expression -> comma ;
  private Expr expression() {
    return comma();
  }

  // comma -> ternary ( "," ternary )* ;
  // Same loop as equality(), so it's left-associative. No new node,
  // it's just an Expr.Binary with the ',' token.
  private Expr comma() {
    Expr expr = ternary();

    while (match(COMMA)) {
      Token operator = previous();
      Expr right = ternary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  // ternary -> equality ( "?" expression ":" ternary )? ;
  // The middle is a full expression since '?' and ':' wrap it like
  // parens. Recursing after the ':' makes it right-associative.
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

  private Expr equality() {
    Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  // unary -> ( "!" | "-" ) unary
  //        | ( "!=" | "==" ) comparison          (error)
  //        | ( ">" | ">=" | "<" | "<=" ) term    (error)
  //        | "+" factor                         (error)
  //        | ( "/" | "*" ) unary                 (error)
  //        | primary ;
  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    // Error productions. '-' is left out since it's real negation.
    if (match(BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL,
              LESS, LESS_EQUAL, PLUS, SLASH, STAR)) {
      Token operator = previous();
      // Report without throwing so the parser keeps going.
      error(operator, "Binary operator is missing a left operand.");

      // Parse and drop the right operand the normal rule would use.
      switch (operator.type) {
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

      // No usable tree. hadError is set, so Lox never prints it.
      return null;
    }

    return primary();
  }

  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
}
