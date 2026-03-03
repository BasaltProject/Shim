/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class PolymorphicHiddenTypeModern {
  static final class Hidden {
    final int value;

    Hidden(int value) {
      this.value = value;
    }

    static Hidden make(int v) {
      return new Hidden(v);
    }

    static int read(Hidden h) {
      return h.value;
    }
  }

  public static void main(String[] args) throws Throwable {
    MethodHandles.Lookup l = MethodHandles.lookup();
    MethodHandle make = l.findStatic(Hidden.class, "make", MethodType.methodType(Hidden.class, int.class));
    MethodHandle read = l.findStatic(Hidden.class, "read", MethodType.methodType(int.class, Hidden.class));
    Hidden h = (Hidden) make.invokeExact(7);
    System.out.println(h.value);
    Hidden g = (Hidden) make.invoke(8);
    System.out.println(g.value + " " + (int) read.invokeExact(g));
    Object o = make.invoke(9);
    System.out.println(((Hidden) o).value);
    try {
      String s = (String) make.invoke(1);
      System.out.println(s);
    } catch (ClassCastException e) {
      System.out.println("cce");
    }
  }
}
