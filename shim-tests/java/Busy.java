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

public class Busy {
  private static volatile int foo = 0;
  private static volatile boolean go;

  public static void main(String[] args) {
    final Object lock = new Object();

    synchronized (lock) {
      new Thread() {
        public void run() {
          while (foo < 100) {
            go = true;
          }
        }
      }.start();

      while (foo < 100) {
        while (! go) { }
        go = false;
        byte[] array = new byte[256 * 1024];
        ++ foo;
      }
    }
  }
}