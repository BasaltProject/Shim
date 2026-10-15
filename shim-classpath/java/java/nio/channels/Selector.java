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

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Selector implements Closeable {
  protected final Set<SelectionKey> keys = new HashSet();
  protected final Set<SelectionKey> selectedKeys = new HashSet();

  public static Selector open() throws IOException {
    return new SocketSelector();
  }
  
  public void add(SelectionKey key) {
    keys.add(key);
  }

  public void remove(SelectionKey key) {
    keys.remove(key);
  }

  public Set<SelectionKey> keys() {
    return keys;
  }

  public Set<SelectionKey> selectedKeys() {
    return selectedKeys;
  }

  public abstract boolean isOpen();

  public abstract Selector wakeup();

  public abstract int selectNow() throws IOException;

  public abstract int select(long interval) throws IOException;

  public abstract int select() throws IOException;

  public int select(Consumer<SelectionKey> action, long timeout) throws IOException {
    Objects.requireNonNull(action, "action");
    int n = select(timeout);
    for (Iterator<SelectionKey> it = selectedKeys.iterator(); it.hasNext();) {
      SelectionKey k = it.next();
      action.accept(k);
      it.remove();
    }
    return n;
  }

  public int select(Consumer<SelectionKey> action) throws IOException {
    return select(action, 0);
  }

  public int selectNow(Consumer<SelectionKey> action) throws IOException {
    Objects.requireNonNull(action, "action");
    int n = selectNow();
    for (Iterator<SelectionKey> it = selectedKeys.iterator(); it.hasNext();) {
      SelectionKey k = it.next();
      action.accept(k);
      it.remove();
    }
    return n;
  }

  @Override
  public abstract void close() throws IOException;
}

