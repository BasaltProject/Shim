/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Selector implements Closeable {
  protected Selector() { }

  public static Selector open() throws IOException {
    return SelectorProvider.provider().openSelector();
  }

  public abstract boolean isOpen();

  public abstract SelectorProvider provider();

  public abstract Set<SelectionKey> keys();

  public abstract Set<SelectionKey> selectedKeys();

  public abstract int selectNow() throws IOException;

  public abstract int select(long timeout) throws IOException;

  public abstract int select() throws IOException;

  private int processSelected(Consumer<SelectionKey> action, int n) {
    int count = 0;
    Set<SelectionKey> selected = selectedKeys();
    for (Iterator<SelectionKey> it = selected.iterator(); it.hasNext();) {
      SelectionKey k = it.next();
      it.remove();
      action.accept(k);
      count++;
    }
    return count;
  }

  public int select(Consumer<SelectionKey> action, long timeout) throws IOException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Negative timeout");
    }
    Objects.requireNonNull(action);
    int n = select(timeout);
    return processSelected(action, n);
  }

  public int select(Consumer<SelectionKey> action) throws IOException {
    Objects.requireNonNull(action);
    int n = select();
    return processSelected(action, n);
  }

  public int selectNow(Consumer<SelectionKey> action) throws IOException {
    Objects.requireNonNull(action);
    int n = selectNow();
    return processSelected(action, n);
  }

  public abstract Selector wakeup();

  public abstract void close() throws IOException;
}
