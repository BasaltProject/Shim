/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

interface LiveStackFrame extends StackWalker.StackFrame {
  Object[] getMonitors();

  Object[] getLocals();

  Object[] getStack();

  abstract class PrimitiveSlot {
    PrimitiveSlot() {}

    public abstract int size();

    public int intValue() {
      throw new UnsupportedOperationException();
    }

    public long longValue() {
      throw new UnsupportedOperationException();
    }
  }
}
