package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Environment {
  final Environment enclosing;
  private final Map<String, Object> values = new HashMap<>();
  //locals live in an array, globals stay in the map.
  private final List<Object> localValues = new ArrayList<>();

  static final Object UNINITIALIZED = new Object();

  Environment() {
    enclosing = null;
  }

  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

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

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) {
    // locals go in the array in resolver order.
    if (enclosing != null) {
      localValues.add(value);
      return;
    }

    values.put(name, value);
  }

  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
  }

  //look up a local by index instead of by name.
  Object getAt(int distance, int index) {
    return ancestor(distance).localValues.get(index);
  }

  void assignAt(int distance, int index, Object value) {
    ancestor(distance).localValues.set(index, value);
  }
}