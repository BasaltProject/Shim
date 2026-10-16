/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent.locks;

import java.io.Serializable;

public abstract class AbstractOwnableSynchronizer implements Serializable {
  private static final long serialVersionUID = 3737899427754241961L;

  private transient Thread exclusiveOwnerThread;

  protected AbstractOwnableSynchronizer() { }

  protected final void setExclusiveOwnerThread(Thread thread) {
    exclusiveOwnerThread = thread;
  }

  protected final Thread getExclusiveOwnerThread() {
    return exclusiveOwnerThread;
  }
}
