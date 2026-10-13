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

public class Finalizers {
  private static final Object lock = new Object();
  private static boolean finalized = false;

  private static void expect(boolean v) {
    if (! v) throw new RuntimeException();
  }

  protected void finalize() {
    synchronized (lock) {
      finalized = true;
      lock.notifyAll();
    }
  }

  public static void main(String[] args) throws Exception {
    new Finalizers();

    expect(! finalized);
    
    synchronized (lock) {
      System.gc();
      lock.wait(5000);
    }

    expect(finalized);

    new Finalizers2();
    
    finalized = false;

    expect(! finalized);
    
    synchronized (lock) {
      System.gc();
      lock.wait(5000);
    }

    expect(finalized);
  }

  private static class Finalizers2 extends Finalizers { }

}
