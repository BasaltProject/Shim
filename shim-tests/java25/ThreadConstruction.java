/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadConstruction {
  private static int childCalls;
  private static final InheritableThreadLocal<String> LOCAL = new InheritableThreadLocal<String>() {
    protected String childValue(String value) {
      ++childCalls;
      return value + ":child";
    }
  };

  private static void require(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }

  private static void check(String label, ThreadFactory factory, boolean virtual, boolean inherit) throws Exception {
    Thread parent = Thread.currentThread();
    ClassLoader original = parent.getContextClassLoader();
    ClassLoader marker = new ClassLoader(original) { };
    AtomicReference<String> result = new AtomicReference<String>();
    LOCAL.set("parent");
    parent.setContextClassLoader(marker);
    try {
      int before = childCalls;
      Thread child = factory.newThread(() -> result.set(LOCAL.get()));
      require(child.isVirtual() == virtual, label + " thread kind");
      require(childCalls == before + (inherit ? 1 : 0), label + " childValue calls");
      require(child.getContextClassLoader() == (inherit ? marker : ClassLoader.getSystemClassLoader()),
          label + " context class loader");
      LOCAL.set("changed");
      child.start();
      child.join(3000);
      require(!child.isAlive(), label + " did not terminate");
      require(inherit ? "parent:child".equals(result.get()) : result.get() == null, label + " local value");
      System.out.println(label + " verified");
    } finally {
      LOCAL.remove();
      parent.setContextClassLoader(original);
    }
  }

  private static void parentAttributes() throws Exception {
    AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
    Thread parent = new Thread(() -> {
      try {
        Thread child = new Thread();
        Thread built = Thread.ofPlatform().unstarted(() -> { });
        require(child.isDaemon() && built.isDaemon(), "daemon inheritance");
        require(child.getPriority() == 7 && built.getPriority() == 7, "priority inheritance");
        Thread explicit = Thread.ofPlatform().daemon(false).priority(3).unstarted(() -> { });
        require(!explicit.isDaemon() && explicit.getPriority() == 3, "explicit attributes");
      } catch (Throwable error) {
        failure.set(error);
      }
    });
    parent.setDaemon(true);
    parent.setPriority(7);
    parent.start();
    parent.join(3000);
    require(!parent.isAlive(), "parent did not terminate");
    if (failure.get() != null) throw new AssertionError(failure.get());
    System.out.println("parent attributes verified");
  }

  public static void main(String[] args) throws Exception {
    check("constructor inherit", r -> new Thread(null, r, "child", 0, true), false, true);
    check("constructor suppress", r -> new Thread(null, r, "child", 0, false), false, false);
    check("default constructor", Thread::new, false, true);
    check("group constructor", r -> new Thread((ThreadGroup) null, r), false, true);
    check("platform inherit", Thread.ofPlatform()::unstarted, false, true);
    check("platform suppress", Thread.ofPlatform().inheritInheritableThreadLocals(false)::unstarted, false, false);
    check("virtual inherit", Thread.ofVirtual()::unstarted, true, true);
    check("virtual suppress", Thread.ofVirtual().inheritInheritableThreadLocals(false)::unstarted, true, false);
    check("platform factory", Thread.ofPlatform().inheritInheritableThreadLocals(false).factory(), false, false);
    check("virtual factory", Thread.ofVirtual().inheritInheritableThreadLocals(false).factory(), true, false);
    parentAttributes();
    Runnable[] nullNames = {
      () -> new Thread((String) null),
      () -> new Thread((Runnable) null, null),
      () -> new Thread((ThreadGroup) null, (String) null),
      () -> new Thread(null, null, null),
      () -> new Thread(null, null, null, 0),
      () -> new Thread(null, null, null, 0, true)
    };
    for (Runnable create : nullNames) {
      try {
        create.run();
        throw new AssertionError("null name accepted");
      } catch (NullPointerException expected) {
      }
    }
    System.out.println("null names rejected");
  }
}
