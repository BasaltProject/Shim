/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;

public abstract class SelectionKey {
  protected SelectionKey() { }

  public abstract SelectableChannel channel();

  public abstract Selector selector();

  public abstract boolean isValid();

  public abstract void cancel();

  public abstract int interestOps();

  public abstract SelectionKey interestOps(int ops);

  public int interestOpsOr(int ops) {
    synchronized (this) {
      int oldVal = interestOps();
      interestOps(oldVal | ops);
      return oldVal;
    }
  }

  public int interestOpsAnd(int ops) {
    synchronized (this) {
      int oldVal = interestOps();
      interestOps(oldVal & ops);
      return oldVal;
    }
  }

  public abstract int readyOps();

  public static final int OP_READ = 1 << 0;
  public static final int OP_WRITE = 1 << 2;
  public static final int OP_CONNECT = 1 << 3;
  public static final int OP_ACCEPT = 1 << 4;

  public final boolean isReadable() {
    return (readyOps() & OP_READ) != 0;
  }

  public final boolean isWritable() {
    return (readyOps() & OP_WRITE) != 0;
  }

  public final boolean isConnectable() {
    return (readyOps() & OP_CONNECT) != 0;
  }

  public final boolean isAcceptable() {
    return (readyOps() & OP_ACCEPT) != 0;
  }

  private volatile Object attachment;

  public final Object attach(Object ob) {
    Object old = attachment;
    attachment = ob;
    return old;
  }

  public final Object attachment() {
    return attachment;
  }
}
