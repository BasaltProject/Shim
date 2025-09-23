/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.IOException;
import java.net.SocketException;

public final class NetHandle {
  private final boolean datagram;
  private long handle = -1;
  private int operations;
  private boolean closed;

  public NetHandle(boolean datagram) {
    this.datagram = datagram;
  }

  public synchronized boolean isClosed() {
    return closed;
  }

  public synchronized long peek() {
    return handle;
  }

  public synchronized boolean isCreated() {
    return handle >= 0;
  }

  public synchronized long begin() throws IOException {
    if (closed) {
      throw new SocketException("Socket closed");
    }
    if (handle < 0) {
      NetNative.init();
      handle = NetNative.socket(true, datagram);
    }
    operations++;
    return handle;
  }

  public synchronized void end() {
    operations--;
    if (closed && operations == 0 && handle >= 0) {
      NetNative.close(handle);
      handle = -2;
    }
  }

  public synchronized void adopt(long existing) {
    handle = existing;
  }

  public void close() {
    long toShutdown = -1;
    synchronized (this) {
      if (closed) {
        return;
      }
      closed = true;
      if (handle >= 0) {
        toShutdown = handle;
        if (operations == 0) {
          NetNative.close(handle);
          handle = -2;
          return;
        }
      }
    }
    if (toShutdown >= 0) {
      NetNative.shutdown(toShutdown, 2);
    }
  }
}
