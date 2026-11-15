/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.ref;

public class Cleaner {
  private final Runnable thunk;

  private Cleaner(Runnable thunk) {
    this.thunk = thunk;
  }

  public static Cleaner create(Object ob, Runnable thunk) {
    return new Cleaner(thunk);
  }

  public void clean() {
    if (thunk != null) {
      thunk.run();
    }
  }
}
