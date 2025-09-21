/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class DefaultMethodSpecificity {
  interface A { default String m() { return "A"; } }
  interface B extends A { default String m() { return "B"; } }
  interface D extends B { }
  interface X { default String m() { return "X"; } }
  interface Y extends X { }
  static abstract class Base implements A { }
  static class ViaSub extends Base implements B { }
  static class ViaDeep extends Base implements D { }
  static class ClassWins extends ViaSub { public String m() { return "class"; } }
  static class Mixed implements Y, X { }
  static class Reordered implements A, D { }

  public static void main(String[] args) {
    System.out.println(new ViaSub().m() + new ViaDeep().m() + new ClassWins().m() + new Mixed().m() + new Reordered().m());
    A a = new ViaDeep();
    System.out.println(a.m());
  }
}
