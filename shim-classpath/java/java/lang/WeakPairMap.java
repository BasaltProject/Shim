/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

final class WeakPairMap<K1, K2, V> {
  private final ConcurrentHashMap<Pair<K1, K2>, V> map = new ConcurrentHashMap<Pair<K1, K2>, V>();
  private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

  boolean containsKeyPair(K1 k1, K2 k2) {
    expungeStaleEntries();
    return map.containsKey(Pair.lookup(k1, k2));
  }

  V get(K1 k1, K2 k2) {
    expungeStaleEntries();
    return map.get(Pair.lookup(k1, k2));
  }

  V put(K1 k1, K2 k2, V v) {
    expungeStaleEntries();
    return map.put(Pair.weak(k1, k2, queue), Objects.requireNonNull(v));
  }

  V putIfAbsent(K1 k1, K2 k2, V v) {
    expungeStaleEntries();
    return map.putIfAbsent(Pair.weak(k1, k2, queue), Objects.requireNonNull(v));
  }

  V computeIfAbsent(K1 k1, K2 k2, BiFunction<? super K1, ? super K2, ? extends V> mappingFunction) {
    expungeStaleEntries();
    Pair<K1, K2> pair = Pair.lookup(k1, k2);
    V v = map.get(pair);
    if (v == null) {
      v = Objects.requireNonNull(mappingFunction.apply(k1, k2));
      V prev = map.putIfAbsent(Pair.weak(k1, k2, queue), v);
      if (prev != null) {
        v = prev;
      }
    }
    return v;
  }

  V remove(K1 k1, K2 k2) {
    expungeStaleEntries();
    return map.remove(Pair.lookup(k1, k2));
  }

  private void expungeStaleEntries() {
    Object ref;
    while ((ref = queue.poll()) != null) {
      map.remove(ref);
    }
  }

  private interface Pair<K1, K2> {
    K1 first();

    K2 second();

    static <K1, K2> Pair<K1, K2> weak(K1 k1, K2 k2, ReferenceQueue<Object> queue) {
      return new Weak<K1, K2>(k1, k2, queue);
    }

    static <K1, K2> Pair<K1, K2> lookup(K1 k1, K2 k2) {
      return new Lookup<K1, K2>(k1, k2);
    }

    final class Weak<K1, K2> extends WeakRefPeer<K1> implements Pair<K1, K2> {
      private final int hash;
      private final WeakRefPeer<K2> secondPeer;

      Weak(K1 k1, K2 k2, ReferenceQueue<Object> queue) {
        super(Objects.requireNonNull(k1), queue);
        hash = 31 * System.identityHashCode(k1) + System.identityHashCode(Objects.requireNonNull(k2));
        secondPeer = new WeakRefPeer<K2>(k2, queue) {
          @Override
          public boolean equals(Object obj) {
            return Weak.this.equals(obj);
          }

          @Override
          public int hashCode() {
            return Weak.this.hashCode();
          }
        };
      }

      @Override
      public K1 first() {
        return get();
      }

      @Override
      public K2 second() {
        return secondPeer.get();
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (!(obj instanceof Pair)) {
          return false;
        }
        Pair<?, ?> that = (Pair<?, ?>) obj;
        K1 k1 = first();
        K2 k2 = second();
        return k1 != null && k2 != null && k1 == that.first() && k2 == that.second();
      }

      @Override
      public int hashCode() {
        return hash;
      }
    }

    final class Lookup<K1, K2> implements Pair<K1, K2> {
      private final K1 k1;
      private final K2 k2;

      Lookup(K1 k1, K2 k2) {
        this.k1 = Objects.requireNonNull(k1);
        this.k2 = Objects.requireNonNull(k2);
      }

      @Override
      public K1 first() {
        return k1;
      }

      @Override
      public K2 second() {
        return k2;
      }

      @Override
      public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
          return false;
        }
        Pair<?, ?> that = (Pair<?, ?>) obj;
        return k1 == that.first() && k2 == that.second();
      }

      @Override
      public int hashCode() {
        return 31 * System.identityHashCode(k1) + System.identityHashCode(k2);
      }
    }
  }

  private abstract static class WeakRefPeer<T> extends WeakReference<T> {
    WeakRefPeer(T referent, ReferenceQueue<Object> queue) {
      super(referent, queue);
    }
  }
}
