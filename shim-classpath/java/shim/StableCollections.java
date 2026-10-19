/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class StableCollections {
  private StableCollections() { }

  private static void checkSize(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("size can not be negative: " + size);
    }
  }

  private static <T> StableValueImpl<T>[] cells(int size) {
    @SuppressWarnings("unchecked")
    StableValueImpl<T>[] cells = (StableValueImpl<T>[]) new StableValueImpl<?>[size];
    for (int i = 0; i < size; i++) {
      cells[i] = StableValueImpl.newInstance();
    }
    return cells;
  }

  public static <T> Supplier<T> supplier(Supplier<? extends T> underlying) {
    Objects.requireNonNull(underlying);
    return new StableSupplier<T>(underlying);
  }

  public static <R> IntFunction<R> intFunction(int size, IntFunction<? extends R> underlying) {
    checkSize(size);
    Objects.requireNonNull(underlying);
    return new StableIntFunction<R>(size, underlying);
  }

  public static <T, R> Function<T, R> function(Set<? extends T> inputs, Function<? super T, ? extends R> underlying) {
    Objects.requireNonNull(inputs);
    Objects.requireNonNull(underlying);
    return new StableFunction<T, R>(inputs, underlying);
  }

  public static <E> List<E> list(int size, IntFunction<? extends E> mapper) {
    checkSize(size);
    Objects.requireNonNull(mapper);
    StableValueImpl<E>[] cells = cells(size);
    return new StableList<E>(cells, mapper, 0, size, false);
  }

  public static <K, V> Map<K, V> map(Set<K> keys, Function<? super K, ? extends V> mapper) {
    Objects.requireNonNull(keys);
    Objects.requireNonNull(mapper);
    return new StableMap<K, V>(keys, mapper);
  }

  private static String listString(StableValueImpl<?>[] cells, int from, int size, boolean reversed) {
    if (size == 0) {
      return "[]";
    }
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(cells[reversed ? from + size - 1 - i : from + i]);
    }
    return sb.append(']').toString();
  }

  private static final class StableSupplier<T> implements Supplier<T> {
    private final StableValueImpl<T> cell = StableValueImpl.newInstance();
    private final Supplier<? extends T> underlying;

    StableSupplier(Supplier<? extends T> underlying) {
      this.underlying = underlying;
    }

    @Override
    public T get() {
      return cell.orElseSet(underlying);
    }

    @Override
    public String toString() {
      return cell.toString();
    }
  }

  private static final class StableIntFunction<R> implements IntFunction<R> {
    private final StableValueImpl<R>[] cells;
    private final IntFunction<? extends R> underlying;

    StableIntFunction(int size, IntFunction<? extends R> underlying) {
      this.cells = cells(size);
      this.underlying = underlying;
    }

    @Override
    public R apply(int value) {
      if (value < 0 || value >= cells.length) {
        throw new IllegalArgumentException("Input not allowed: " + value);
      }
      return cells[value].orElseSet(() -> underlying.apply(value));
    }

    @Override
    public String toString() {
      return listString(cells, 0, cells.length, false);
    }
  }

  private static String mapString(Map<?, ? extends StableValueImpl<?>> cells) {
    if (cells.isEmpty()) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<?, ? extends StableValueImpl<?>> e : cells.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append(e.getKey()).append('=').append(e.getValue());
    }
    return sb.append('}').toString();
  }

  private static final class StableFunction<T, R> implements Function<T, R> {
    private final Map<T, StableValueImpl<R>> cells = new LinkedHashMap<T, StableValueImpl<R>>();
    private final Function<? super T, ? extends R> underlying;

    StableFunction(Set<? extends T> inputs, Function<? super T, ? extends R> underlying) {
      for (T key : Set.copyOf(inputs)) {
        cells.put(key, StableValueImpl.<R>newInstance());
      }
      this.underlying = underlying;
    }

    @Override
    public R apply(T value) {
      StableValueImpl<R> cell = cells.get(Objects.requireNonNull(value));
      if (cell == null) {
        throw new IllegalArgumentException("Input not allowed: " + value);
      }
      return cell.orElseSet(() -> underlying.apply(value));
    }

    @Override
    public String toString() {
      return mapString(cells);
    }
  }

  private static final class StableList<E> extends AbstractList<E> implements RandomAccess {
    private final StableValueImpl<E>[] cells;
    private final IntFunction<? extends E> mapper;
    private final int from;
    private final int size;
    private final boolean reversed;

    StableList(StableValueImpl<E>[] cells, IntFunction<? extends E> mapper, int from, int size, boolean reversed) {
      this.cells = cells;
      this.mapper = mapper;
      this.from = from;
      this.size = size;
      this.reversed = reversed;
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public E get(int index) {
      if (index < 0 || index >= size) {
        throw new IndexOutOfBoundsException("Index out of range: " + index);
      }
      int slot = reversed ? from + size - 1 - index : from + index;
      return cells[slot].orElseSet(() -> mapper.apply(slot));
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
      Objects.checkFromToIndex(fromIndex, toIndex, size);
      int base = reversed ? from + size - toIndex : from + fromIndex;
      return new StableList<E>(cells, mapper, base, toIndex - fromIndex, reversed);
    }

    @Override
    public List<E> reversed() {
      return new StableList<E>(cells, mapper, from, size, !reversed);
    }

    @Override
    public String toString() {
      return listString(cells, from, size, reversed);
    }
  }

  private static final class StableMap<K, V> extends AbstractMap<K, V> {
    private final Map<K, StableValueImpl<V>> cells = new LinkedHashMap<K, StableValueImpl<V>>();
    private final Function<? super K, ? extends V> mapper;

    StableMap(Set<K> keys, Function<? super K, ? extends V> mapper) {
      for (K key : Set.copyOf(keys)) {
        cells.put(key, StableValueImpl.<V>newInstance());
      }
      this.mapper = mapper;
    }

    @Override
    public int size() {
      return cells.size();
    }

    @Override
    public boolean containsKey(Object key) {
      return cells.containsKey(Objects.requireNonNull(key));
    }

    @Override
    public V get(Object key) {
      StableValueImpl<V> cell = cells.get(Objects.requireNonNull(key));
      if (cell == null) {
        return null;
      }
      @SuppressWarnings("unchecked")
      K k = (K) key;
      return cell.orElseSet(() -> mapper.apply(k));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
      return containsKey(key) ? get(key) : defaultValue;
    }

    @Override
    public Set<K> keySet() {
      return java.util.Collections.unmodifiableSet(cells.keySet());
    }

    @Override
    public Collection<V> values() {
      return new AbstractCollection<V>() {
        @Override
        public Iterator<V> iterator() {
          Iterator<K> keys = cells.keySet().iterator();
          return new Iterator<V>() {
            public boolean hasNext() {
              return keys.hasNext();
            }

            public V next() {
              return get(keys.next());
            }
          };
        }

        @Override
        public int size() {
          return cells.size();
        }

        @Override
        public String toString() {
          if (cells.isEmpty()) {
            return "[]";
          }
          StringBuilder sb = new StringBuilder("[");
          boolean first = true;
          for (StableValueImpl<V> cell : cells.values()) {
            if (!first) {
              sb.append(", ");
            }
            first = false;
            sb.append(cell);
          }
          return sb.append(']').toString();
        }
      };
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
      return new AbstractSet<Map.Entry<K, V>>() {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
          Iterator<K> keys = cells.keySet().iterator();
          return new Iterator<Map.Entry<K, V>>() {
            public boolean hasNext() {
              return keys.hasNext();
            }

            public Map.Entry<K, V> next() {
              K key = keys.next();
              return new Map.Entry<K, V>() {
                public K getKey() {
                  return key;
                }

                public V getValue() {
                  return get(key);
                }

                public V setValue(V value) {
                  throw new UnsupportedOperationException();
                }

                public boolean equals(Object o) {
                  if (!(o instanceof Map.Entry)) {
                    return false;
                  }
                  Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                  return Objects.equals(key, e.getKey()) && Objects.equals(getValue(), e.getValue());
                }

                public int hashCode() {
                  return Objects.hashCode(key) ^ Objects.hashCode(getValue());
                }

                public String toString() {
                  return key + "=" + cells.get(key);
                }
              };
            }
          };
        }

        @Override
        public int size() {
          return cells.size();
        }

        @Override
        public String toString() {
          if (cells.isEmpty()) {
            return "[]";
          }
          StringBuilder sb = new StringBuilder("[");
          boolean first = true;
          for (Map.Entry<K, StableValueImpl<V>> e : cells.entrySet()) {
            if (!first) {
              sb.append(", ");
            }
            first = false;
            sb.append(e.getKey()).append('=').append(e.getValue());
          }
          return sb.append(']').toString();
        }
      };
    }

    @Override
    public String toString() {
      return mapString(cells);
    }
  }
}
