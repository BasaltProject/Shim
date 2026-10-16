/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSelectableChannel extends SelectableChannel {
  private final SelectorProvider provider;
  private final List<SelectionKey> keys = new ArrayList<SelectionKey>();
  private final Object keyLock = new Object();
  private final Object regLock = new Object();
  private volatile boolean blocking = true;

  protected AbstractSelectableChannel(SelectorProvider provider) {
    this.provider = provider;
  }

  public final SelectorProvider provider() {
    return provider;
  }

  private void addKey(SelectionKey k) {
    synchronized (keyLock) {
      keys.add(k);
    }
  }

  public final boolean isRegistered() {
    synchronized (keyLock) {
      for (SelectionKey k : keys) {
        if (k.isValid()) {
          return true;
        }
      }
      return false;
    }
  }

  public final SelectionKey keyFor(Selector sel) {
    synchronized (keyLock) {
      for (SelectionKey k : keys) {
        if (k.selector() == sel) {
          return k;
        }
      }
      return null;
    }
  }

  public final SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException {
    if ((ops & ~validOps()) != 0) {
      throw new IllegalArgumentException();
    }
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
    synchronized (regLock) {
      if (isBlocking()) {
        throw new IllegalBlockingModeException();
      }
      if (!(sel instanceof AbstractSelector) || ((AbstractSelector) sel).provider() != provider) {
        throw new IllegalSelectorException();
      }
      synchronized (keyLock) {
        if (!isOpen()) {
          throw new ClosedChannelException();
        }
        SelectionKey k = keyFor(sel);
        if (k != null) {
          if (!k.isValid()) {
            throw new CancelledKeyException();
          }
          k.attach(att);
          k.interestOps(ops);
          return k;
        }
        k = ((AbstractSelector) sel).register(this, ops, att);
        addKey(k);
        return k;
      }
    }
  }

  void removeKey(SelectionKey k) {
    synchronized (keyLock) {
      keys.remove(k);
    }
  }

  protected final void implCloseChannel() throws IOException {
    implCloseSelectableChannel();
    List<SelectionKey> copy;
    synchronized (keyLock) {
      copy = new ArrayList<SelectionKey>(keys);
    }
    for (SelectionKey k : copy) {
      k.cancel();
    }
  }

  protected abstract void implCloseSelectableChannel() throws IOException;

  public final boolean isBlocking() {
    return blocking;
  }

  public final Object blockingLock() {
    return regLock;
  }

  public final SelectableChannel configureBlocking(boolean block) throws IOException {
    synchronized (regLock) {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      if (blocking == block) {
        return this;
      }
      if (block && isRegistered()) {
        throw new IllegalBlockingModeException();
      }
      implConfigureBlocking(block);
      blocking = block;
    }
    return this;
  }

  protected abstract void implConfigureBlocking(boolean block) throws IOException;
}
