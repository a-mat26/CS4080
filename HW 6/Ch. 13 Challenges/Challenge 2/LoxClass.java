package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

class LoxClass extends LoxInstance implements LoxCallable {
  final String name;
  final LoxClass superclass;
  final Map<String, LoxFunction> methods;
  final Map<String, LoxFunction> getters;

  LoxClass(LoxClass metaclass, String name, LoxClass superclass, Map<String, LoxFunction> methods, Map<String, LoxFunction> getters) {
    super(metaclass);
    this.superclass = superclass;
    this.name = name;
    this.methods = methods;
    this.getters = getters;
  }

  LoxFunction findMethod(String name) {
    //check the superclass first so the highest method wins.
    if (superclass != null) {
      LoxFunction method = superclass.findMethod(name);
      if (method != null) return method;
    }

    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    return null;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LoxInstance instance = new LoxInstance(this);
    LoxFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public int arity() {
    LoxFunction initializer = findMethod("init");
    if (initializer == null) return 0;
    return initializer.arity();
  }
}