/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.Serializable;
import java.nio.ByteBuffer;

public class PluginCompat {
  static class Base {
    int b = 5;

    Base() {
      b = 99;
    }
  }

  static class Sub extends Base implements Serializable {
    int s = 7;

    Sub(int x) {
      s = x;
    }
  }

  static class NoDefault {
    final int v;

    NoDefault(int v) {
      this.v = v;
      throw new AssertionError("constructor must not run");
    }
  }

  private static void check(boolean ok, String what) {
    if (!ok) {
      throw new AssertionError(what);
    }
  }

  public static void main(String[] args) throws Exception {
    int[] hits = {0};
    sun.misc.Signal usr2 = new sun.misc.Signal("USR2");
    check(usr2.getNumber() == 12 && usr2.toString().equals("SIGUSR2"), "signal identity");
    sun.misc.Signal.handle(usr2, s -> hits[0]++);
    sun.misc.Signal.raise(usr2);
    check(hits[0] == 1, "signal raise");

    sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
    Sub sub = (Sub) rf.newConstructorForSerialization(Sub.class, Base.class.getDeclaredConstructor()).newInstance();
    check(sub.b == 99 && sub.s == 0, "serialization constructor runs the base only");
    check(((Sub) rf.newConstructorForSerialization(Sub.class).newInstance()).s == 0, "serializable lookup");
    NoDefault nd = (NoDefault) rf.newConstructorForSerialization(NoDefault.class, Object.class.getDeclaredConstructor())
        .newInstance();
    check(nd.v == 0, "objenesis style instantiation");

    Class<?> direct = Class.forName("sun.nio.ch.DirectBuffer");
    ByteBuffer bb = ByteBuffer.allocateDirect(16);
    check(direct.isInstance(bb), "direct buffer interface");
    check((Long) direct.getMethod("address").invoke(bb) != 0L, "direct buffer address");
    System.out.println("PluginCompat ok");
  }
}
