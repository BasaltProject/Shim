/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.locks;

public final class SerialSupport {
  private SerialSupport() {}

  public static Condition condition(ReentrantLock lock) {
    return ReentrantLock.serialCondition(lock);
  }
}
