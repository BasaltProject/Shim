/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Serializable;

final class ImmutableCollections {
  private ImmutableCollections() {
  }

  static final List<?> EMPTY_LIST = new ListN<Object>(new Object[0]);
  static final Set<?> EMPTY_SET = new SetN<Object>(new Object[0]);
  static final Map<?, ?> EMPTY_MAP = new MapN<Object, Object>(new Object[0]);

  @SuppressWarnings("unchecked")
  static <E> List<E> emptyList() {
    return (List<E>) EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  static <E> Set<E> emptySet() {
    return (Set<E>) EMPTY_SET;
  }

  @SuppressWarnings("unchecked")
  static <K, V> Map<K, V> emptyMap() {
    return (Map<K, V>) EMPTY_MAP;
  }

  static abstract class AbstractImmutableCollection<E> extends AbstractCollection<E> {
    @Override
    public boolean add(E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }
  }

  static final class ListN<E> extends AbstractImmutableCollection<E>
      implements List<E>, RandomAccess, Serializable {
    private final Object[] elements;

    ListN(Object[] input) {
      this.elements = new Object[input.length];
      for (int i = 0; i < input.length; ++i) {
        this.elements[i] = Objects.requireNonNull(input[i]);
      }
    }

    @Override
    public int size() {
      return elements.length;
    }

    @Override
    public boolean isEmpty() {
      return elements.length == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
      Objects.checkIndex(index, elements.length);
      return (E) elements[index];
    }

    @Override
    public boolean contains(Object o) {
      Objects.requireNonNull(o);
      return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
      Objects.requireNonNull(o);
      for (int i = 0; i < elements.length; ++i) {
        if (elements[i].equals(o)) {
          return i;
        }
      }
      return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
      Objects.requireNonNull(o);
      for (int i = elements.length - 1; i >= 0; --i) {
        if (elements[i].equals(o)) {
          return i;
        }
      }
      return -1;
    }

    @Override
    public E set(int index, E value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int startIndex, Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
      Object[] copy = new Object[elements.length];
      System.arraycopy(elements, 0, copy, 0, elements.length);
      return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
      if (a.length < elements.length) {
        a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), elements.length);
      }
      System.arraycopy(elements, 0, a, 0, elements.length);
      if (a.length > elements.length) {
        a[elements.length] = null;
      }
      return a;
    }

    @Override
    public Iterator<E> iterator() {
      return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator() {
      return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
      Objects.checkIndex(index, elements.length + 1);
      return new ListIterator<E>() {
        private int cursor = index;

        @Override
        public boolean hasNext() {
          return cursor < elements.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          return (E) elements[cursor++];
        }

        @Override
        public boolean hasPrevious() {
          return cursor > 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E previous() {
          if (!hasPrevious()) {
            throw new NoSuchElementException();
          }
          return (E) elements[--cursor];
        }

        @Override
        public int nextIndex() {
          return cursor;
        }

        @Override
        public int previousIndex() {
          return cursor - 1;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
      Objects.checkFromToIndex(fromIndex, toIndex, elements.length);
      int subLen = toIndex - fromIndex;
      Object[] sub = new Object[subLen];
      System.arraycopy(elements, fromIndex, sub, 0, subLen);
      return new ListN<E>(sub);
    }

    @Override
    public List<E> reversed() {
      return new ReverseOrderListView<E>(this);
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof List)) {
        return false;
      }
      List<?> other = (List<?>) o;
      if (other.size() != elements.length) {
        return false;
      }
      for (int i = 0; i < elements.length; ++i) {
        if (!elements[i].equals(other.get(i))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      int hash = 1;
      for (Object e : elements) {
        hash = 31 * hash + e.hashCode();
      }
      return hash;
    }

    @Override
    public String toString() {
      if (elements.length == 0) {
        return "[]";
      }
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (int i = 0; i < elements.length; ++i) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(elements[i]);
      }
      sb.append(']');
      return sb.toString();
    }
  }

  static final class SetN<E> extends AbstractImmutableCollection<E>
      implements Set<E>, Serializable {
    private final Object[] elements;

    SetN(Object[] input) {
      this.elements = new Object[input.length];
      for (int i = 0; i < input.length; ++i) {
        Objects.requireNonNull(input[i]);
        for (int j = 0; j < i; ++j) {
          if (elements[j].equals(input[i])) {
            throw new IllegalArgumentException("duplicate element: " + input[i]);
          }
        }
        this.elements[i] = input[i];
      }
    }

    @Override
    public int size() {
      return elements.length;
    }

    @Override
    public boolean isEmpty() {
      return elements.length == 0;
    }

    @Override
    public boolean contains(Object o) {
      Objects.requireNonNull(o);
      for (Object e : elements) {
        if (e.equals(o)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Object[] toArray() {
      Object[] copy = new Object[elements.length];
      System.arraycopy(elements, 0, copy, 0, elements.length);
      return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
      if (a.length < elements.length) {
        a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), elements.length);
      }
      System.arraycopy(elements, 0, a, 0, elements.length);
      if (a.length > elements.length) {
        a[elements.length] = null;
      }
      return a;
    }

    @Override
    public Iterator<E> iterator() {
      return new Iterator<E>() {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
          return cursor < elements.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          return (E) elements[cursor++];
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Set)) {
        return false;
      }
      Set<?> other = (Set<?>) o;
      if (other.size() != elements.length) {
        return false;
      }
      try {
        return containsAll(other);
      } catch (ClassCastException | NullPointerException unused) {
        return false;
      }
    }

    @Override
    public int hashCode() {
      int hash = 0;
      for (Object e : elements) {
        hash += e.hashCode();
      }
      return hash;
    }

    @Override
    public String toString() {
      if (elements.length == 0) {
        return "[]";
      }
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (int i = 0; i < elements.length; ++i) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(elements[i]);
      }
      sb.append(']');
      return sb.toString();
    }
  }

  static final class MapEntry<K, V> implements Map.Entry<K, V>, Serializable {
    private final K key;
    private final V value;

    MapEntry(K key, V value) {
      this.key = Objects.requireNonNull(key);
      this.value = Objects.requireNonNull(value);
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public V setValue(V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
      return key.equals(other.getKey()) && value.equals(other.getValue());
    }

    @Override
    public int hashCode() {
      return key.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }

  static final class MapN<K, V> implements Map<K, V>, Serializable {
    private final Object[] table;
    private final int size;

    MapN(Object[] input) {
      if ((input.length & 1) != 0) {
        throw new IllegalArgumentException("table length must be even");
      }
      this.size = input.length >> 1;
      this.table = new Object[input.length];
      for (int i = 0; i < input.length; i += 2) {
        Object k = Objects.requireNonNull(input[i]);
        Object v = Objects.requireNonNull(input[i + 1]);
        for (int j = 0; j < i; j += 2) {
          if (table[j].equals(k)) {
            throw new IllegalArgumentException("duplicate key: " + k);
          }
        }
        this.table[i] = k;
        this.table[i + 1] = v;
      }
    }

    @Override
    public int size() {
      return size;
    }

    @Override
    public boolean isEmpty() {
      return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
      Objects.requireNonNull(key);
      for (int i = 0; i < table.length; i += 2) {
        if (table[i].equals(key)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      Objects.requireNonNull(value);
      for (int i = 1; i < table.length; i += 2) {
        if (table[i].equals(value)) {
          return true;
        }
      }
      return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
      Objects.requireNonNull(key);
      for (int i = 0; i < table.length; i += 2) {
        if (table[i].equals(key)) {
          return (V) table[i + 1];
        }
      }
      return null;
    }

    @Override
    public V put(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> elts) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
      Object[] keys = new Object[size];
      for (int i = 0; i < size; ++i) {
        keys[i] = table[i << 1];
      }
      return new SetN<K>(keys);
    }

    @Override
    public Collection<V> values() {
      Object[] vals = new Object[size];
      for (int i = 0; i < size; ++i) {
        vals[i] = table[(i << 1) + 1];
      }
      return new ListN<V>(vals);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
      Object[] entries = new Object[size];
      for (int i = 0; i < size; ++i) {
        entries[i] = new MapEntry<Object, Object>(table[i << 1], table[(i << 1) + 1]);
      }
      return new SetN<Map.Entry<K, V>>(entries);
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof Map)) {
        return false;
      }
      Map<?, ?> map = (Map<?, ?>) other;
      if (map.size() != size) {
        return false;
      }
      for (int i = 0; i < table.length; i += 2) {
        Object k = table[i];
        Object v = table[i + 1];
        Object otherV = map.get(k);
        if (otherV == null || !v.equals(otherV)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      int hash = 0;
      for (int i = 0; i < table.length; i += 2) {
        hash += table[i].hashCode() ^ table[i + 1].hashCode();
      }
      return hash;
    }

    @Override
    public String toString() {
      if (size == 0) {
        return "{}";
      }
      StringBuilder sb = new StringBuilder();
      sb.append('{');
      for (int i = 0; i < size; ++i) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(table[i << 1]).append('=').append(table[(i << 1) + 1]);
      }
      sb.append('}');
      return sb.toString();
    }
  }

  static final class ReverseOrderListView<E> extends AbstractList<E>
      implements List<E>, RandomAccess, Serializable {
    private final List<E> list;

    ReverseOrderListView(List<E> list) {
      this.list = Objects.requireNonNull(list);
    }

    @Override
    public int size() {
      return list.size();
    }

    @Override
    public boolean isEmpty() {
      return list.isEmpty();
    }

    @Override
    public E get(int index) {
      return list.get(list.size() - 1 - index);
    }

    @Override
    public E set(int index, E value) {
      return list.set(list.size() - 1 - index, value);
    }

    @Override
    public void add(int index, E element) {
      list.add(list.size() - index, element);
    }

    @Override
    public E remove(int index) {
      return list.remove(list.size() - 1 - index);
    }

    @Override
    public List<E> reversed() {
      return list;
    }

    @Override
    public ListIterator<E> listIterator() {
      return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
      Objects.checkIndex(index, size() + 1);
      return new ListIterator<E>() {
        private int cursor = index;

        @Override
        public boolean hasNext() {
          return cursor < size();
        }

        @Override
        public E next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }
          return get(cursor++);
        }

        @Override
        public boolean hasPrevious() {
          return cursor > 0;
        }

        @Override
        public E previous() {
          if (!hasPrevious()) {
            throw new NoSuchElementException();
          }
          return get(--cursor);
        }

        @Override
        public int nextIndex() {
          return cursor;
        }

        @Override
        public int previousIndex() {
          return cursor - 1;
        }

        @Override
        public void remove() {
          ReverseOrderListView.this.remove(cursor - 1);
          cursor--;
        }

        @Override
        public void set(E e) {
          ReverseOrderListView.this.set(cursor - 1, e);
        }

        @Override
        public void add(E e) {
          ReverseOrderListView.this.add(cursor, e);
          cursor++;
        }
      };
    }

    @Override
    public int indexOf(Object o) {
      for (int i = 0; i < size(); ++i) {
        if (Objects.equals(get(i), o)) {
          return i;
        }
      }
      return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
      for (int i = size() - 1; i >= 0; --i) {
        if (Objects.equals(get(i), o)) {
          return i;
        }
      }
      return -1;
    }
  }
}
