/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

public final class ScopedValue<T> {
  private static final ThreadLocal<BindingNode> CURRENT_BINDINGS = new ThreadLocal<BindingNode>();

  private ScopedValue() {}

  public static <T> ScopedValue<T> newInstance() {
    return new ScopedValue<T>();
  }

  public boolean isBound() {
    for (BindingNode n = CURRENT_BINDINGS.get(); n != null; n = n.prev) {
      if (n.key == this) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public T get() {
    for (BindingNode n = CURRENT_BINDINGS.get(); n != null; n = n.prev) {
      if (n.key == this) {
        return (T) n.value;
      }
    }
    throw new NoSuchElementException("ScopedValue not bound");
  }

  @SuppressWarnings("unchecked")
  public T orElse(T other) {
    for (BindingNode n = CURRENT_BINDINGS.get(); n != null; n = n.prev) {
      if (n.key == this) {
        return (T) n.value;
      }
    }
    return other;
  }

  @SuppressWarnings("unchecked")
  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    Objects.requireNonNull(exceptionSupplier, "exceptionSupplier");
    for (BindingNode n = CURRENT_BINDINGS.get(); n != null; n = n.prev) {
      if (n.key == this) {
        return (T) n.value;
      }
    }
    throw exceptionSupplier.get();
  }

  public static <T> Carrier where(ScopedValue<T> key, T value) {
    Objects.requireNonNull(key, "key");
    return new Carrier(new BindingNode(key, value, null));
  }

  @FunctionalInterface
  public interface CallableOp<T, X extends Throwable> {
    T call() throws X;
  }

  private static final class BindingNode {
    final ScopedValue<?> key;
    final Object value;
    final BindingNode prev;

    BindingNode(ScopedValue<?> key, Object value, BindingNode prev) {
      this.key = key;
      this.value = value;
      this.prev = prev;
    }
  }

  public static final class Carrier {
    private final BindingNode head;

    private Carrier(BindingNode head) {
      this.head = head;
    }

    public <T> Carrier where(ScopedValue<T> key, T value) {
      Objects.requireNonNull(key, "key");
      return new Carrier(new BindingNode(key, value, head));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ScopedValue<T> key) {
      Objects.requireNonNull(key, "key");
      for (BindingNode n = head; n != null; n = n.prev) {
        if (n.key == key) {
          return (T) n.value;
        }
      }
      throw new NoSuchElementException("ScopedValue not bound in carrier");
    }

    private BindingNode composeWith(BindingNode parent) {
      BindingNode acc = parent;
      // Reverse link our nodes onto parent so latest 'where' wins
      BindingNode[] nodes = new BindingNode[countNodes(head)];
      int idx = 0;
      for (BindingNode n = head; n != null; n = n.prev) {
        nodes[idx++] = n;
      }
      for (int i = nodes.length - 1; i >= 0; --i) {
        acc = new BindingNode(nodes[i].key, nodes[i].value, acc);
      }
      return acc;
    }

    private static int countNodes(BindingNode n) {
      int c = 0;
      for (BindingNode cur = n; cur != null; cur = cur.prev) {
        c++;
      }
      return c;
    }

    public void run(Runnable op) {
      Objects.requireNonNull(op, "op");
      BindingNode prev = CURRENT_BINDINGS.get();
      BindingNode next = composeWith(prev);
      CURRENT_BINDINGS.set(next);
      try {
        op.run();
      } finally {
        CURRENT_BINDINGS.set(prev);
      }
    }

    public <R, X extends Throwable> R call(CallableOp<? extends R, X> op) throws X {
      Objects.requireNonNull(op, "op");
      BindingNode prev = CURRENT_BINDINGS.get();
      BindingNode next = composeWith(prev);
      CURRENT_BINDINGS.set(next);
      try {
        return op.call();
      } finally {
        CURRENT_BINDINGS.set(prev);
      }
    }
  }
}
