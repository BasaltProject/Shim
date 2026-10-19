/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

public class ScriptApiModern {
  static class Echo extends AbstractScriptEngine implements Compilable {
    final ScriptEngineFactory factory;

    Echo(ScriptEngineFactory factory) {
      this.factory = factory;
    }

    public Object eval(String script, ScriptContext ctx) throws ScriptException {
      if (script.equals("fail")) {
        throw new ScriptException("failed", "echo.txt", 3, 7);
      }
      return script + ":" + ctx.getAttribute("x");
    }

    public Object eval(Reader reader, ScriptContext ctx) throws ScriptException {
      try {
        char[] buffer = new char[64];
        int n = reader.read(buffer);
        return eval(new String(buffer, 0, Math.max(n, 0)), ctx);
      } catch (java.io.IOException e) {
        throw new ScriptException(e);
      }
    }

    public Bindings createBindings() {
      return new SimpleBindings();
    }

    public ScriptEngineFactory getFactory() {
      return factory;
    }

    public CompiledScript compile(String script) {
      Echo engine = this;
      return new CompiledScript() {
        public Object eval(ScriptContext context) throws ScriptException {
          return engine.eval(script, context);
        }

        public ScriptEngine getEngine() {
          return engine;
        }
      };
    }

    public CompiledScript compile(Reader script) {
      return compile("reader");
    }
  }

  static class EchoFactory implements ScriptEngineFactory {
    public String getEngineName() {
      return "echo";
    }

    public String getEngineVersion() {
      return "1";
    }

    public List<String> getExtensions() {
      return List.of("echo");
    }

    public List<String> getMimeTypes() {
      return List.of("text/echo");
    }

    public List<String> getNames() {
      return List.of("echo", "Echo");
    }

    public String getLanguageName() {
      return "echo";
    }

    public String getLanguageVersion() {
      return "1";
    }

    public Object getParameter(String key) {
      return key.equals(ScriptEngine.NAME) ? "echo" : null;
    }

    public String getMethodCallSyntax(String obj, String m, String... args) {
      return obj + "." + m + String.join(",", args);
    }

    public String getOutputStatement(String toDisplay) {
      return "print " + toDisplay;
    }

    public String getProgram(String... statements) {
      return String.join(";", statements);
    }

    public ScriptEngine getScriptEngine() {
      return new Echo(this);
    }
  }

  interface Op { Object run() throws Exception; }

  static void t(String name, Op op) {
    try {
      System.out.println(name + " = " + op.run());
    } catch (Exception e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    ScriptEngineManager manager = new ScriptEngineManager();
    EchoFactory factory = new EchoFactory();
    t("engines", () -> manager.getEngineFactories().size());
    t("missing", () -> manager.getEngineByName("echo"));
    manager.registerEngineName("echo", factory);
    manager.registerEngineExtension("echo", factory);
    manager.registerEngineMimeType("text/echo", factory);
    t("byName", () -> manager.getEngineByName("echo").getFactory().getEngineName());
    t("byExtension", () -> manager.getEngineByExtension("echo").getFactory().getLanguageName());
    t("byMime", () -> manager.getEngineByMimeType("text/echo").getFactory().getEngineVersion());
    t("unknown", () -> manager.getEngineByName("nope"));
    t("null-name", () -> manager.getEngineByName(null));
    t("null-register", () -> {
      manager.registerEngineName(null, factory);
      return "ok";
    });
    t("null-bindings", () -> {
      manager.setBindings(null);
      return "ok";
    });
    manager.put("x", 42);
    ScriptEngine engine = manager.getEngineByName("echo");
    t("global-visible", () -> engine.getBindings(ScriptContext.GLOBAL_SCOPE).get("x"));
    t("eval", () -> engine.eval("hello"));
    engine.put("x", "engine");
    t("eval-engine-scope", () -> engine.eval("hello"));
    t("eval-bindings", () -> {
      Bindings bindings = new SimpleBindings();
      bindings.put("x", "custom");
      return engine.eval("script", bindings);
    });
    t("eval-reader", () -> engine.eval(new StringReader("from reader")));
    t("eval-fail", () -> engine.eval("fail"));
    t("compile", () -> ((Compilable) engine).compile("compiled").eval());
    t("compile-bindings", () -> {
      Bindings bindings = engine.createBindings();
      bindings.put("x", 7);
      return ((Compilable) engine).compile("compiled").eval(bindings);
    });
    t("eval-null-bindings", () -> engine.eval("x", (Bindings) null));
    t("bindings-empty-key", () -> new SimpleBindings().put("", 1));
    t("bindings-null-key", () -> new SimpleBindings().put(null, 1));
    t("bindings-class-key", () -> new SimpleBindings().get(1));
    t("bindings-putall-null", () -> {
      new SimpleBindings().putAll(null);
      return "ok";
    });
    t("exception", () -> new ScriptException("m", "f.js", 4, 5).getMessage());
    t("exception-line", () -> new ScriptException("m", "f.js", 4).getMessage());
    t("exception-plain", () -> new ScriptException("m").getMessage());
    t("exception-cause", () -> new ScriptException(new RuntimeException("boom")).getMessage());
    SimpleScriptContext context = new SimpleScriptContext();
    t("context-scopes", () -> context.getScopes());
    t("context-attribute", () -> {
      context.setAttribute("a", 1, ScriptContext.ENGINE_SCOPE);
      return context.getAttribute("a") + " " + context.getAttributesScope("a") + " " + context.getAttributesScope("b");
    });
    t("context-global-null", () -> context.getAttribute("a", ScriptContext.GLOBAL_SCOPE));
    t("context-bad-scope", () -> context.getAttribute("a", 7));
    t("context-bad-set", () -> {
      context.setBindings(new SimpleBindings(), 7);
      return "ok";
    });
    t("context-null-engine", () -> {
      context.setBindings(null, ScriptContext.ENGINE_SCOPE);
      return "ok";
    });
    t("context-empty-name", () -> context.getAttribute(""));
    t("context-null-name", () -> context.getAttribute(null));
    t("context-remove", () -> context.removeAttribute("a", ScriptContext.ENGINE_SCOPE));
    StringWriter writer = new StringWriter();
    context.setWriter(writer);
    t("context-writer", () -> context.getWriter() == writer);
    t("abstract-null-context", () -> {
      engine.setContext(null);
      return "ok";
    });
    t("abstract-bad-scope", () -> engine.getBindings(1));
  }
}
