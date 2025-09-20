/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package p;

public class Target {
  private int privateField = 1;
  int packageField = 2;
  protected int protectedField = 3;
  public int publicField = 4;
  private static int privateStatic() { return 5; }
  int packageMethod() { return 6; }
  protected int protectedMethod() { return 7; }
  public int publicMethod() { return 8; }
  public static int nested() { return new Inner().read(new Target()) + Inner.call(); }
  static class Inner {
    int read(Target target) { return target.privateField; }
    static int call() { return privateStatic(); }
  }
}
