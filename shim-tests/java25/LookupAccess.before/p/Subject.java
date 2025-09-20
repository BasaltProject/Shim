/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package p;

public class Subject {
  public Subject() { }
  public Subject(int ignored) { }
  public int publicField = 1;
  public int privateField = 2;
  public int packageField = 3;
  public int protectedField = 4;
  public int publicMethod() { return 5; }
  public int privateMethod() { return 6; }
  public int packageMethod() { return 7; }
  public int protectedMethod() { return 8; }
  public static int privateStatic() { return 9; }
  public static int publicStatic() { return 10; }
}
