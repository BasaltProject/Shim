/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSelector extends Selector {
  private boolean closed;
  private final SelectorProvider provider;
  private final Set<SelectionKey> cancelledKeys = new HashSet<SelectionKey>();

  protected AbstractSelector(SelectorProvider provider) {
    this.provider = provider;
  }

  void cancel(SelectionKey k) {
    synchronized (cancelledKeys) {
      cancelledKeys.add(k);
    }
  }

  public final void close() throws IOException {
    boolean doClose = false;
    synchronized (this) {
      if (!closed) {
        closed = true;
        doClose = true;
      }
    }
    if (doClose) {
      implCloseSelector();
    }
  }

  protected abstract void implCloseSelector() throws IOException;

  public final boolean isOpen() {
    synchronized (this) {
      return !closed;
    }
  }

  public final SelectorProvider provider() {
    return provider;
  }

  protected final Set<SelectionKey> cancelledKeys() {
    return cancelledKeys;
  }

  protected abstract SelectionKey register(AbstractSelectableChannel ch, int ops, Object att);

  protected final void deregister(AbstractSelectionKey key) {
    ((AbstractSelectableChannel) key.channel()).removeKey(key);
  }

  protected final void begin() { }

  protected final void end() { }
}
