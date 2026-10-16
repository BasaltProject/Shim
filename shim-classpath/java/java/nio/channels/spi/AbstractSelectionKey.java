/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels.spi;

import java.nio.channels.SelectionKey;

public abstract class AbstractSelectionKey extends SelectionKey {
  protected AbstractSelectionKey() { }

  private volatile boolean valid = true;

  public final boolean isValid() {
    return valid;
  }

  void invalidate() {
    valid = false;
  }

  public final void cancel() {
    synchronized (this) {
      if (valid) {
        valid = false;
        ((AbstractSelector) selector()).cancel(this);
      }
    }
  }
}
