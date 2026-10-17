/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.ThreadInfo;
import java.lang.reflect.Constructor;

final class ThreadInfoAccess {
  private static Constructor<ThreadInfo> constructor;

  private ThreadInfoAccess() {
  }

  static synchronized ThreadInfo make(Thread t, StackTraceElement[] stack) {
    try {
      if (constructor == null) {
        constructor = ThreadInfo.class.getDeclaredConstructor(Thread.class, StackTraceElement[].class);
        constructor.setAccessible(true);
      }
      return constructor.newInstance(t, stack);
    } catch (Exception e) {
      throw new InternalError(e);
    }
  }
}
