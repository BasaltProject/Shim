/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.Hashtable;
import java.util.concurrent.Callable;
import javax.naming.*;
import javax.naming.spi.InitialContextFactory;

public class NamingModern {
  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e);
    }
  }

  public static class Factory implements InitialContextFactory {
    public Context getInitialContext(Hashtable<?, ?> env) {
      return null;
    }
  }

  public static void main(String[] args) {
    t("lookup-none", () -> new InitialContext().lookup("java:comp/env"));
    t("lookup-none-name", () -> new InitialContext().lookup(new CompositeName("a/b")));
    t("dolookup-none", () -> InitialContext.doLookup("x"));
    t("bind-none", () -> {
      new InitialContext().bind("x", "y");
      return "ok";
    });
    t("env-none", () -> new InitialContext().getEnvironment());
    t("bad-factory", () -> {
      Hashtable<String, String> env = new Hashtable<>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "does.not.Exist");
      return new InitialContext(env);
    });
    t("factory-null", () -> {
      Hashtable<String, String> env = new Hashtable<>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, Factory.class.getName());
      return new InitialContext(env);
    });
    t("lazy", () -> new InitialContext(true) { }.getClass().getSuperclass().getName());
    t("exc-hier", () -> new AuthenticationException() instanceof NamingSecurityException);
    t("exc-str", () -> {
      NamingException e = new NameNotFoundException("missing");
      e.setRemainingName(new CompositeName("a/b"));
      e.setRootCause(new RuntimeException("root"));
      e.setResolvedObj("obj");
      return e.toString() + " | " + e.toString(true) + " | " + e.getExplanation() + " | " + e.getCause();
    });
    t("exc-append", () -> {
      NamingException e = new NamingException("x");
      e.appendRemainingComponent("c1");
      e.appendRemainingName(new CompositeName("c2/c3"));
      return e.getRemainingName();
    });
    t("cname", () -> {
      CompositeName n = new CompositeName("a/b/c");
      return n + " " + n.size() + " " + n.get(1) + " " + n.getPrefix(2) + " " + n.getSuffix(1) + " " + n.startsWith(new CompositeName("a/b")) + " " + n.endsWith(new CompositeName("c"));
    });
    t("cname-empty", () -> new CompositeName("").size() + " " + new CompositeName("/").size() + " [" + new CompositeName("/") + "]");
    t("cname-escape", () -> {
      CompositeName n = new CompositeName("a\\/b/c");
      return n.size() + " " + n.get(0) + " " + n;
    });
    t("cname-quote", () -> {
      CompositeName n = new CompositeName("\"a/b\"/c");
      return n.size() + " " + n.get(0);
    });
    t("cname-badquote", () -> new CompositeName("\"abc"));
    t("cname-ops", () -> {
      CompositeName n = new CompositeName("a/b");
      n.add("c");
      n.add(0, "z");
      Object r = n.remove(1);
      return n + " " + r + " " + n.compareTo(new CompositeName("z/b/c")) + " " + n.equals(new CompositeName("z/b/c"));
    });
    t("cname-add-empty", () -> {
      CompositeName n = new CompositeName();
      n.add("");
      return n.size() + " [" + n + "]";
    });
    t("binding", () -> {
      Binding b = new Binding("n", "obj");
      NameClassPair p = new NameClassPair("m", "cls", false);
      return b + " | " + b.getClassName() + " | " + p;
    });
    t("pair-ns", () -> new NameClassPair("a", "b").getNameInNamespace());
  }
}
