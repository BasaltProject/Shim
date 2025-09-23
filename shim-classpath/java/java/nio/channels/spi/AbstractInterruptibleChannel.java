/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.InterruptibleChannel;

public abstract class AbstractInterruptibleChannel implements Channel, InterruptibleChannel {
  private final Object closeLock = new Object();
  private volatile boolean closed;

  protected AbstractInterruptibleChannel() { }

  public final void close() throws IOException {
    synchronized (closeLock) {
      if (closed) {
        return;
      }
      closed = true;
      implCloseChannel();
    }
  }

  protected abstract void implCloseChannel() throws IOException;

  public final boolean isOpen() {
    return !closed;
  }

  protected final void begin() { }

  protected final void end(boolean completed) throws AsynchronousCloseException {
    if (!completed && closed) {
      throw new AsynchronousCloseException();
    }
  }
}
