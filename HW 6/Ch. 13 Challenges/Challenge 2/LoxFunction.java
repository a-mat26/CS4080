package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  private final boolean isInitializer;

  LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.isInitializer = isInitializer;
    this.closure = closure;
    this.declaration = declaration;
  }

  LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);


    LoxFunction inner = null;
    LoxClass klass = instance.klass;
    while (klass != null) {
      LoxFunction method = klass.methods.get(declaration.name.lexeme);
      if (method == this) break;
      if (method != null) inner = method;
      klass = klass.superclass;
    }

    if (inner != null) {
      environment.define("inner", inner.bind(instance));
    } else {

      environment.define("inner", new LoxCallable() {
        @Override
        public int arity() {
          return declaration.params.size();
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
          return null;
        }

        @Override
        public String toString() {
          return "<native fn>";
        }
      });
    }

    return new LoxFunction(declaration, environment, isInitializer);
  }

  @Override
  public String toString() {
    if (declaration.name == null) return "<fn>";
    return "<fn " + declaration.name.lexeme + ">";
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme,
          arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      if (isInitializer) return closure.getAt(0, "this");

      return returnValue.value;
    }

    if (isInitializer) return closure.getAt(0, "this");
    return null;
  }
}