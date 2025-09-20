/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package p;

public class Target {
  public int privateField = 1;
  public int packageField = 2;
  public int protectedField = 3;
  public int publicField = 4;
  public static int privateStatic() { return 5; }
  public int packageMethod() { return 6; }
  public int protectedMethod() { return 7; }
  public int publicMethod() { return 8; }
  public static int nested() { return 9; }
}
