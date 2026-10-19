/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

public interface ScriptContext {
  int ENGINE_SCOPE = 100;
  int GLOBAL_SCOPE = 200;

  void setBindings(Bindings bindings, int scope);

  Bindings getBindings(int scope);

  void setAttribute(String name, Object value, int scope);

  Object getAttribute(String name, int scope);

  Object removeAttribute(String name, int scope);

  Object getAttribute(String name);

  int getAttributesScope(String name);

  Writer getWriter();

  Writer getErrorWriter();

  void setWriter(Writer writer);

  void setErrorWriter(Writer writer);

  Reader getReader();

  void setReader(Reader reader);

  List<Integer> getScopes();
}
