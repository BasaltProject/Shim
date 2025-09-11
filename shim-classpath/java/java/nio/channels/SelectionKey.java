/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio.channels;

public class SelectionKey {
  public static final int OP_READ = 1 << 0;
  public static final int OP_WRITE = 1 << 2;
  public static final int OP_CONNECT = 1 << 3;
  public static final int OP_ACCEPT = 1 << 4;

  private final SelectableChannel channel;
  private final Selector selector;
  private int interestOps;
  private int readyOps;
  private Object attachment;
  private volatile boolean valid = true;

  public SelectionKey(SelectableChannel channel, Selector selector,
                      int interestOps, Object attachment)
  {
    this.channel = channel;
    this.selector = selector;
    this.interestOps = interestOps;
    this.attachment = attachment;
    this.readyOps = 0;
  }

  public int interestOps() {
    return interestOps;
  }

  public SelectionKey interestOps(int v) {
    this.interestOps = v;
    return this;
  }

  public int interestOpsOr(int ops) {
    synchronized (this) {
      int old = interestOps();
      interestOps(old | ops);
      return old;
    }
  }

  public int interestOpsAnd(int ops) {
    synchronized (this) {
      int old = interestOps();
      interestOps(old & ops);
      return old;
    }
  }

  public int readyOps() {
    return readyOps;
  }

  public void readyOps(int v) {
    this.readyOps = v;
  }

  public boolean isReadable() {
    return (readyOps & OP_READ) != 0;
  }

  public boolean isWritable() {
    return (readyOps & OP_WRITE) != 0;
  }

  public boolean isConnectable() {
    return (readyOps & OP_CONNECT) != 0;
  }

  public boolean isAcceptable() {
    return (readyOps & OP_ACCEPT) != 0;
  }

  public boolean isValid() {
    return valid && channel.isOpen() && selector.isOpen();
  }

  public void cancel() {
    if (valid) {
      valid = false;
      selector.remove(this);
    }
  }

  public SelectableChannel channel() {
    return channel;
  }

  public Selector selector() {
    return selector;
  }

  public Object attachment() {
    return attachment;
  }

  public final Object attach(Object ob) {
    Object old = attachment;
    attachment = ob;
    return old;
  }
}
