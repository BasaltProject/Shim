/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

final class SelectorImpl extends AbstractSelector {
  private final Set<SelectionKey> keys = new HashSet<SelectionKey>();
  private final Set<SelectionKey> selectedKeys = new HashSet<SelectionKey>();
  private final Set<SelectionKey> publicKeys = Collections.unmodifiableSet(keys);
  private final Set<SelectionKey> publicSelectedKeys = new SelectedSet(selectedKeys);
  private final long wakeRead;
  private final long wakeWrite;
  private final Object selectLock = new Object();

  private final class SelectedSet extends java.util.AbstractSet<SelectionKey> {
    private final Set<SelectionKey> delegate;

    SelectedSet(Set<SelectionKey> delegate) {
      this.delegate = delegate;
    }

    @Override
    public Iterator<SelectionKey> iterator() {
      return delegate.iterator();
    }

    @Override
    public int size() {
      return delegate.size();
    }

    @Override
    public boolean contains(Object o) {
      return delegate.contains(o);
    }

    @Override
    public boolean remove(Object o) {
      return delegate.remove(o);
    }

    @Override
    public void clear() {
      delegate.clear();
    }

    @Override
    public boolean add(SelectionKey k) {
      throw new UnsupportedOperationException();
    }
  }

  SelectorImpl(SelectorProvider provider) throws IOException {
    super(provider);
    NetNative.init();
    long[] pair = new long[2];
    NetNative.socketPair(pair);
    wakeRead = pair[0];
    wakeWrite = pair[1];
    NetNative.setBlocking(wakeRead, false);
    NetNative.setBlocking(wakeWrite, false);
  }

  private void ensureOpen() {
    if (!isOpen()) {
      throw new ClosedSelectorException();
    }
  }

  @Override
  public Set<SelectionKey> keys() {
    ensureOpen();
    return publicKeys;
  }

  @Override
  public Set<SelectionKey> selectedKeys() {
    ensureOpen();
    return publicSelectedKeys;
  }

  @Override
  protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att) {
    ensureOpen();
    SelectionKeyImpl k = new SelectionKeyImpl(ch, this, ops, att);
    synchronized (keys) {
      keys.add(k);
    }
    return k;
  }

  private void processCancelled() {
    synchronized (cancelledKeys()) {
      for (SelectionKey k : cancelledKeys()) {
        synchronized (keys) {
          keys.remove(k);
        }
        selectedKeys.remove(k);
        deregister((SelectionKeyImpl) k);
      }
      cancelledKeys().clear();
    }
  }

  private static long handleOf(SelectionKeyImpl k) {
    java.nio.channels.SelectableChannel ch = k.channel();
    if (ch instanceof HasHandle) {
      return ((HasHandle) ch).rawHandle();
    }
    return -1;
  }

  interface HasHandle {
    long rawHandle();
  }

  private static int toPoll(int ops) {
    int ev = 0;
    if ((ops & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0) {
      ev |= 1;
    }
    if ((ops & (SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT)) != 0) {
      ev |= 4;
    }
    return ev;
  }

  private int doSelect(long timeout) throws IOException {
    synchronized (selectLock) {
      ensureOpen();
      processCancelled();
      List<SelectionKeyImpl> snapshot;
      synchronized (keys) {
        snapshot = new ArrayList<SelectionKeyImpl>(keys.size());
        for (SelectionKey k : keys) {
          snapshot.add((SelectionKeyImpl) k);
        }
      }
      int n = snapshot.size() + 1;
      long[] handles = new long[n];
      int[] events = new int[n];
      int[] revents = new int[n];
      int count = 0;
      SelectionKeyImpl[] polled = new SelectionKeyImpl[n];
      handles[count] = wakeRead;
      events[count] = 1;
      count++;
      for (SelectionKeyImpl k : snapshot) {
        if (!k.isValid()) {
          continue;
        }
        long h = handleOf(k);
        int ev = toPoll(k.rawInterest());
        if (h < 0 || ev == 0) {
          continue;
        }
        polled[count] = k;
        handles[count] = h;
        events[count] = ev;
        count++;
      }
      int timeoutMs = timeout < 0 ? 0 : (timeout == 0 ? -1 : (int) Math.min(Integer.MAX_VALUE, timeout));
      int ready = NetNative.poll(handles, events, revents, count, timeoutMs);
      int updated = 0;
      if (ready > 0) {
        if ((revents[0] & 1) != 0) {
          byte[] drain = new byte[64];
          while (NetNative.readNb(wakeRead, drain, 0, drain.length) > 0) {
            continue;
          }
        }
        for (int i = 1; i < count; i++) {
          if (revents[i] == 0) {
            continue;
          }
          SelectionKeyImpl k = polled[i];
          if (!k.isValid()) {
            continue;
          }
          int interest = k.rawInterest();
          int rev = revents[i];
          int ops = 0;
          if ((rev & (1 | 8)) != 0) {
            ops |= interest & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT);
          }
          if ((rev & (4 | 8)) != 0) {
            ops |= interest & (SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
          }
          if (ops == 0) {
            continue;
          }
          if (selectedKeys.contains(k)) {
            if ((k.readyOps | ops) != k.readyOps) {
              k.readyOps |= ops;
              updated++;
            }
          } else {
            k.readyOps = ops;
            selectedKeys.add(k);
            updated++;
          }
        }
      }
      processCancelled();
      return updated;
    }
  }

  @Override
  public int selectNow() throws IOException {
    return doSelect(-1);
  }

  @Override
  public int select(long timeout) throws IOException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Negative timeout");
    }
    return doSelect(timeout);
  }

  @Override
  public int select() throws IOException {
    return doSelect(0);
  }

  @Override
  public java.nio.channels.Selector wakeup() {
    byte[] one = {1};
    try {
      NetNative.writeNb(wakeWrite, one, 0, 1);
    } catch (IOException ignore) { }
    return this;
  }

  @Override
  protected void implCloseSelector() throws IOException {
    wakeup();
    synchronized (selectLock) {
      List<SelectionKey> copy;
      synchronized (keys) {
        copy = new ArrayList<SelectionKey>(keys);
      }
      for (SelectionKey k : copy) {
        k.cancel();
      }
      processCancelled();
      NetNative.close(wakeRead);
      NetNative.close(wakeWrite);
    }
  }
}
