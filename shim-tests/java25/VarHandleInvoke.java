/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 9
 */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleInvoke {
  interface Action { Object run() throws Throwable; }

  int count = 1;
  long wide = 10L;
  String name = "a";
  static int shared = 5;
  int[] values = {1, 2, 3};

  static void probe(String label, Action action) {
    try {
      System.out.println(label + ":" + action.run());
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName());
    }
  }

  public static void main(String[] args) throws Throwable {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    VarHandle count = lookup.findVarHandle(VarHandleInvoke.class, "count", int.class);
    VarHandle wide = lookup.findVarHandle(VarHandleInvoke.class, "wide", long.class);
    VarHandle name = lookup.findVarHandle(VarHandleInvoke.class, "name", String.class);
    VarHandle shared = lookup.findStaticVarHandle(VarHandleInvoke.class, "shared", int.class);
    VarHandle array = MethodHandles.arrayElementVarHandle(int[].class);
    VarHandleInvoke owner = new VarHandleInvoke();

    probe("get", () -> (int) count.get(owner));
    probe("set", () -> { count.set(owner, 7); return (int) count.get(owner); });
    probe("volatile", () -> { count.setVolatile(owner, 8); return (int) count.getVolatile(owner); });
    probe("release-acquire", () -> { count.setRelease(owner, 9); return (int) count.getAcquire(owner); });
    probe("cas-hit", () -> (boolean) count.compareAndSet(owner, 9, 10));
    probe("cas-miss", () -> (boolean) count.compareAndSet(owner, 9, 11));
    probe("cae", () -> (int) count.compareAndExchange(owner, 10, 12));
    probe("get-and-set", () -> (int) count.getAndSet(owner, 13));
    probe("get-and-add", () -> (int) count.getAndAdd(owner, 2));
    probe("after", () -> (int) count.get(owner));
    probe("long", () -> { wide.set(owner, 99L); return (long) wide.getAndAdd(owner, 1L); });
    probe("reference", () -> { name.set(owner, "b"); return (String) name.getAndSet(owner, "c"); });
    probe("static", () -> { shared.set(6); return (int) shared.getAndAdd(4); });
    probe("static-after", () -> (int) shared.get());
    probe("array", () -> { array.set(owner.values, 1, 20); return (int) array.get(owner.values, 1); });
    probe("array-cas", () -> (boolean) array.compareAndSet(owner.values, 2, 3, 30));
    probe("array-oob", () -> (int) array.get(owner.values, 9));
    probe("null-handle", () -> (int) ((VarHandle) null).get(owner));
  }
}
