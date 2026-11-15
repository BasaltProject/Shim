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

  private sun.nio.ch.Interruptible interruptor;
  private volatile Thread interrupted;

  protected final void begin() {
    if (interruptor == null) {
      interruptor = new sun.nio.ch.Interruptible() {
        public void interrupt(Thread target) {
          synchronized (closeLock) {
            if (closed) {
              return;
            }
            closed = true;
            interrupted = target;
            try {
              implCloseChannel();
            } catch (IOException e) {
              throw new Error(e);
            }
          }
        }
      };
    }
    Thread.shimBlockedOn(interruptor);
  }

  protected final void end(boolean completed) throws AsynchronousCloseException {
    Thread.shimBlockedOn(null);
    Thread target = interrupted;
    if (target != null && target == Thread.currentThread()) {
      interrupted = null;
      throw new java.nio.channels.ClosedByInterruptException();
    }
    if (!completed && closed) {
      throw new AsynchronousCloseException();
    }
  }
}
