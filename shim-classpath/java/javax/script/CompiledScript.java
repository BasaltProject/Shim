/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.script;

public abstract class CompiledScript {
  public CompiledScript() {
  }

  public abstract Object eval(ScriptContext context) throws ScriptException;

  public Object eval(Bindings bindings) throws ScriptException {
    ScriptContext ctxt = getEngine().getContext();

    if (bindings != null) {
      SimpleScriptContext tempctxt = new SimpleScriptContext();
      tempctxt.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
      tempctxt.setBindings(ctxt.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
      tempctxt.setWriter(ctxt.getWriter());
      tempctxt.setReader(ctxt.getReader());
      tempctxt.setErrorWriter(ctxt.getErrorWriter());

      ctxt = tempctxt;
    }

    return eval(ctxt);
  }

  public Object eval() throws ScriptException {
    return eval(getEngine().getContext());
  }

  public abstract ScriptEngine getEngine();
}
