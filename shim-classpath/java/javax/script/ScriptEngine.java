/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.io.Reader;

public interface ScriptEngine {
  String ARGV = "javax.script.argv";
  String FILENAME = "javax.script.filename";
  String ENGINE = "javax.script.engine";
  String ENGINE_VERSION = "javax.script.engine_version";
  String NAME = "javax.script.name";
  String LANGUAGE = "javax.script.language";
  String LANGUAGE_VERSION = "javax.script.language_version";

  Object eval(String script, ScriptContext context) throws ScriptException;

  Object eval(Reader reader, ScriptContext context) throws ScriptException;

  Object eval(String script) throws ScriptException;

  Object eval(Reader reader) throws ScriptException;

  Object eval(String script, Bindings n) throws ScriptException;

  Object eval(Reader reader, Bindings n) throws ScriptException;

  void put(String key, Object value);

  Object get(String key);

  Bindings getBindings(int scope);

  void setBindings(Bindings bindings, int scope);

  Bindings createBindings();

  ScriptContext getContext();

  void setContext(ScriptContext context);

  ScriptEngineFactory getFactory();
}
