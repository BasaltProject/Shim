/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

final class SelectionKeyImpl extends AbstractSelectionKey {
  private final SelectableChannel channel;
  private final SelectorImpl selector;
  private volatile int interestOps;
  volatile int readyOps;

  SelectionKeyImpl(SelectableChannel channel, SelectorImpl selector, int ops, Object att) {
    this.channel = channel;
    this.selector = selector;
    this.interestOps = ops;
    attach(att);
  }

  @Override
  public SelectableChannel channel() {
    return channel;
  }

  @Override
  public Selector selector() {
    return selector;
  }

  private void ensureValid() {
    if (!isValid()) {
      throw new CancelledKeyException();
    }
  }

  @Override
  public int interestOps() {
    ensureValid();
    return interestOps;
  }

  @Override
  public SelectionKey interestOps(int ops) {
    ensureValid();
    if ((ops & ~channel.validOps()) != 0) {
      throw new IllegalArgumentException();
    }
    interestOps = ops;
    return this;
  }

  @Override
  public int readyOps() {
    ensureValid();
    return readyOps;
  }

  int rawInterest() {
    return interestOps;
  }

  @Override
  public String toString() {
    return "channel=" + channel + ", selector=" + selector + (isValid() ? ", interestOps=" + interestOps
        + ", readyOps=" + readyOps : ", invalid");
  }
}
