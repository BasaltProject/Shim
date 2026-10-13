/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

public class Initializers {
  private static class Static2 {
    public static String foo = "Static2.foo";

    static {
      System.gc();
      new Exception().printStackTrace();
    }
  }

  private static class Static1 {
    public static String foo = "Static1.foo";

    static {
      System.out.println(Static2.foo);
    }
  }

  public static void main(String[] args) {
    Object x = new Object();
    System.out.println(Static1.foo);
    x.toString();
  }
}
