/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleScriptContext implements ScriptContext {
  protected Writer writer;
  protected Writer errorWriter;
  protected Reader reader;
  protected Bindings engineScope;
  protected Bindings globalScope;

  public SimpleScriptContext() {
    this(new InputStreamReader(System.in), new PrintWriter(System.out, true), new PrintWriter(System.err, true));
    engineScope = new SimpleBindings();
    globalScope = null;
  }

  SimpleScriptContext(Reader in, Writer out, Writer err) {
    reader = in;
    writer = out;
    errorWriter = err;
  }

  @Override
  public void setBindings(Bindings bindings, int scope) {
    switch (scope) {
      case ENGINE_SCOPE:
        if (bindings == null) {
          throw new NullPointerException("Engine scope cannot be null.");
        }
        engineScope = bindings;
        break;
      case GLOBAL_SCOPE:
        globalScope = bindings;
        break;
      default:
        throw new IllegalArgumentException("Invalid scope value.");
    }
  }

  @Override
  public Object getAttribute(String name) {
    checkName(name);
    if (engineScope.containsKey(name)) {
      return getAttribute(name, ENGINE_SCOPE);
    } else if (globalScope != null && globalScope.containsKey(name)) {
      return getAttribute(name, GLOBAL_SCOPE);
    }
    return null;
  }

  @Override
  public Object getAttribute(String name, int scope) {
    checkName(name);
    switch (scope) {
      case ENGINE_SCOPE:
        return engineScope.get(name);
      case GLOBAL_SCOPE:
        if (globalScope != null) {
          return globalScope.get(name);
        }
        return null;
      default:
        throw new IllegalArgumentException("Illegal scope value.");
    }
  }

  @Override
  public Object removeAttribute(String name, int scope) {
    checkName(name);
    switch (scope) {
      case ENGINE_SCOPE:
        if (getBindings(ENGINE_SCOPE) != null) {
          return getBindings(ENGINE_SCOPE).remove(name);
        }
        return null;
      case GLOBAL_SCOPE:
        if (getBindings(GLOBAL_SCOPE) != null) {
          return getBindings(GLOBAL_SCOPE).remove(name);
        }
        return null;
      default:
        throw new IllegalArgumentException("Illegal scope value.");
    }
  }

  @Override
  public void setAttribute(String name, Object value, int scope) {
    checkName(name);
    switch (scope) {
      case ENGINE_SCOPE:
        engineScope.put(name, value);
        return;
      case GLOBAL_SCOPE:
        if (globalScope != null) {
          globalScope.put(name, value);
        }
        return;
      default:
        throw new IllegalArgumentException("Illegal scope value.");
    }
  }

  @Override
  public Writer getWriter() {
    return writer;
  }

  @Override
  public Reader getReader() {
    return reader;
  }

  @Override
  public void setReader(Reader reader) {
    this.reader = reader;
  }

  @Override
  public void setWriter(Writer writer) {
    this.writer = writer;
  }

  @Override
  public Writer getErrorWriter() {
    return errorWriter;
  }

  @Override
  public void setErrorWriter(Writer writer) {
    this.errorWriter = writer;
  }

  @Override
  public int getAttributesScope(String name) {
    checkName(name);
    if (engineScope.containsKey(name)) {
      return ENGINE_SCOPE;
    } else if (globalScope != null && globalScope.containsKey(name)) {
      return GLOBAL_SCOPE;
    } else {
      return -1;
    }
  }

  @Override
  public Bindings getBindings(int scope) {
    if (scope == ENGINE_SCOPE) {
      return engineScope;
    } else if (scope == GLOBAL_SCOPE) {
      return globalScope;
    } else {
      throw new IllegalArgumentException("Illegal scope value.");
    }
  }

  @Override
  public List<Integer> getScopes() {
    return kScopes;
  }

  private void checkName(String name) {
    java.util.Objects.requireNonNull(name);
    if (name.isEmpty()) {
      throw new IllegalArgumentException("name cannot be empty");
    }
  }

  private static final List<Integer> kScopes;

  static {
    List<Integer> scopes = new ArrayList<Integer>(2);
    scopes.add(ENGINE_SCOPE);
    scopes.add(GLOBAL_SCOPE);
    kScopes = Collections.unmodifiableList(scopes);
  }
}
