/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
  private static final long serialVersionUID = 8673264195747942595L;

  private transient Object lock = new Object();
  private transient volatile Object[] elements;

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    Object[] snapshot = getArray();
    s.writeInt(snapshot.length);
    for (Object element : snapshot) {
      s.writeObject(element);
    }
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    lock = new Object();
    int len = s.readInt();
    if (len < 0) {
      throw new java.io.InvalidObjectException("Negative length: " + len);
    }
    Object[] newElements = new Object[len];
    for (int i = 0; i < len; i++) {
      newElements[i] = s.readObject();
    }
    setArray(newElements);
  }

  public CopyOnWriteArrayList() {
    setArray(new Object[0]);
  }

  public CopyOnWriteArrayList(Collection<? extends E> c) {
    Object[] arr = c.toArray();
    setArray(Arrays.copyOf(arr, arr.length, Object[].class));
  }

  public CopyOnWriteArrayList(E[] toCopyIn) {
    setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
  }

  final Object[] getArray() {
    return elements;
  }

  final void setArray(Object[] a) {
    elements = a;
  }

  public int size() {
    return getArray().length;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  public int indexOf(Object o) {
    return indexOf(o, getArray(), 0, size());
  }

  public int indexOf(E e, int index) {
    Object[] es = getArray();
    return indexOf(e, es, index, es.length);
  }

  private static int indexOf(Object o, Object[] es, int from, int to) {
    if (o == null) {
      for (int i = from; i < to; i++) {
        if (es[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = from; i < to; i++) {
        if (o.equals(es[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  public int lastIndexOf(Object o) {
    return lastIndexOf(o, getArray(), size() - 1);
  }

  public int lastIndexOf(E e, int index) {
    return lastIndexOf(e, getArray(), index);
  }

  private static int lastIndexOf(Object o, Object[] es, int from) {
    if (o == null) {
      for (int i = from; i >= 0; i--) {
        if (es[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = from; i >= 0; i--) {
        if (o.equals(es[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public Object clone() {
    try {
      @SuppressWarnings("unchecked")
      CopyOnWriteArrayList<E> clone = (CopyOnWriteArrayList<E>) super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  public Object[] toArray() {
    return getArray().clone();
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    Object[] es = getArray();
    int len = es.length;
    if (a.length < len) {
      return (T[]) Arrays.copyOf(es, len, a.getClass());
    }
    System.arraycopy(es, 0, a, 0, len);
    if (a.length > len) {
      a[len] = null;
    }
    return a;
  }

  @SuppressWarnings("unchecked")
  public E get(int index) {
    return (E) getArray()[index];
  }

  @SuppressWarnings("unchecked")
  public E set(int index, E element) {
    synchronized (lock) {
      Object[] es = getArray();
      E oldValue = (E) es[index];
      if (oldValue != element) {
        Object[] newElements = Arrays.copyOf(es, es.length);
        newElements[index] = element;
        setArray(newElements);
      }
      return oldValue;
    }
  }

  public boolean add(E e) {
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      Object[] newElements = Arrays.copyOf(es, len + 1);
      newElements[len] = e;
      setArray(newElements);
      return true;
    }
  }

  public void add(int index, E element) {
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      if (index > len || index < 0) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
      }
      Object[] newElements = new Object[len + 1];
      System.arraycopy(es, 0, newElements, 0, index);
      newElements[index] = element;
      System.arraycopy(es, index, newElements, index + 1, len - index);
      setArray(newElements);
    }
  }

  @SuppressWarnings("unchecked")
  public E remove(int index) {
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      E oldValue = (E) es[index];
      int numMoved = len - index - 1;
      Object[] newElements = new Object[len - 1];
      System.arraycopy(es, 0, newElements, 0, index);
      System.arraycopy(es, index + 1, newElements, index, numMoved);
      setArray(newElements);
      return oldValue;
    }
  }

  public boolean remove(Object o) {
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      int index = indexOf(o, es, 0, len);
      if (index < 0) {
        return false;
      }
      int numMoved = len - index - 1;
      Object[] newElements = new Object[len - 1];
      System.arraycopy(es, 0, newElements, 0, index);
      System.arraycopy(es, index + 1, newElements, index, numMoved);
      setArray(newElements);
      return true;
    }
  }

  public boolean addIfAbsent(E e) {
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      if (indexOf(e, es, 0, len) >= 0) {
        return false;
      }
      Object[] newElements = Arrays.copyOf(es, len + 1);
      newElements[len] = e;
      setArray(newElements);
      return true;
    }
  }

  public int addAllAbsent(Collection<? extends E> c) {
    Object[] cs = c.toArray();
    if (cs.length == 0) {
      return 0;
    }
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      int added = 0;
      Object[] unique = new Object[cs.length];
      for (Object e : cs) {
        if (indexOf(e, es, 0, len) < 0 && indexOf(e, unique, 0, added) < 0) {
          unique[added++] = e;
        }
      }
      if (added > 0) {
        Object[] newElements = Arrays.copyOf(es, len + added);
        System.arraycopy(unique, 0, newElements, len, added);
        setArray(newElements);
      }
      return added;
    }
  }

  public void clear() {
    synchronized (lock) {
      setArray(new Object[0]);
    }
  }

  public boolean addAll(Collection<? extends E> c) {
    Object[] cs = (c.getClass() == CopyOnWriteArrayList.class)
        ? ((CopyOnWriteArrayList<?>) c).getArray()
        : c.toArray();
    if (cs.length == 0) {
      return false;
    }
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      Object[] newElements = Arrays.copyOf(es, len + cs.length);
      System.arraycopy(cs, 0, newElements, len, cs.length);
      setArray(newElements);
      return true;
    }
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    Object[] cs = c.toArray();
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      if (index > len || index < 0) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
      }
      if (cs.length == 0) {
        return false;
      }
      Object[] newElements = new Object[len + cs.length];
      System.arraycopy(es, 0, newElements, 0, index);
      System.arraycopy(cs, 0, newElements, index, cs.length);
      System.arraycopy(es, index, newElements, index + cs.length, len - index);
      setArray(newElements);
      return true;
    }
  }

  public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      if (len == 0) {
        return false;
      }
      Object[] temp = new Object[len];
      int newLen = 0;
      for (Object e : es) {
        if (!c.contains(e)) {
          temp[newLen++] = e;
        }
      }
      if (newLen == len) {
        return false;
      }
      setArray(Arrays.copyOf(temp, newLen));
      return true;
    }
  }

  public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      if (len == 0) {
        return false;
      }
      Object[] temp = new Object[len];
      int newLen = 0;
      for (Object e : es) {
        if (c.contains(e)) {
          temp[newLen++] = e;
        }
      }
      if (newLen == len) {
        return false;
      }
      setArray(Arrays.copyOf(temp, newLen));
      return true;
    }
  }

  public boolean containsAll(Collection<?> c) {
    for (Object e : c) {
      if (!contains(e)) {
        return false;
      }
    }
    return true;
  }

  public Iterator<E> iterator() {
    return new COWIterator<E>(getArray(), 0);
  }

  public ListIterator<E> listIterator() {
    return new COWIterator<E>(getArray(), 0);
  }

  public ListIterator<E> listIterator(int index) {
    Object[] es = getArray();
    int len = es.length;
    if (index < 0 || index > len) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
    }
    return new COWIterator<E>(es, index);
  }

  public List<E> subList(int fromIndex, int toIndex) {
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      if (fromIndex < 0 || toIndex > len || fromIndex > toIndex) {
        throw new IndexOutOfBoundsException();
      }
      return new COWSubList<E>(this, fromIndex, toIndex);
    }
  }

  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    for (Object x : getArray()) {
      @SuppressWarnings("unchecked")
      E e = (E) x;
      action.accept(e);
    }
  }

  public boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      Object[] temp = new Object[len];
      int newLen = 0;
      for (Object x : es) {
        @SuppressWarnings("unchecked")
        E e = (E) x;
        if (!filter.test(e)) {
          temp[newLen++] = e;
        }
      }
      if (newLen == len) {
        return false;
      }
      setArray(Arrays.copyOf(temp, newLen));
      return true;
    }
  }

  public void replaceAll(UnaryOperator<E> operator) {
    Objects.requireNonNull(operator);
    synchronized (lock) {
      Object[] es = getArray();
      int len = es.length;
      Object[] newElements = Arrays.copyOf(es, len);
      for (int i = 0; i < len; i++) {
        @SuppressWarnings("unchecked")
        E e = (E) es[i];
        newElements[i] = operator.apply(e);
      }
      setArray(newElements);
    }
  }

  public void sort(Comparator<? super E> c) {
    synchronized (lock) {
      Object[] es = getArray();
      Object[] newElements = Arrays.copyOf(es, es.length);
      @SuppressWarnings("unchecked")
      E[] cast = (E[]) newElements;
      Arrays.sort(cast, c);
      setArray(newElements);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof List)) return false;
    Iterator<?> it = ((List<?>) o).iterator();
    for (Object e : getArray()) {
      if (!it.hasNext() || !Objects.equals(e, it.next())) {
        return false;
      }
    }
    return !it.hasNext();
  }

  @Override
  public int hashCode() {
    int h = 1;
    for (Object e : getArray()) {
      h = 31 * h + (e == null ? 0 : e.hashCode());
    }
    return h;
  }

  @Override
  public String toString() {
    return Arrays.toString(getArray());
  }

  static class COWIterator<E> implements ListIterator<E> {
    private final Object[] snapshot;
    private int cursor;

    COWIterator(Object[] snapshot, int initialCursor) {
      this.snapshot = snapshot;
      this.cursor = initialCursor;
    }

    public boolean hasNext() {
      return cursor < snapshot.length;
    }

    public boolean hasPrevious() {
      return cursor > 0;
    }

    @SuppressWarnings("unchecked")
    public E next() {
      if (!hasNext()) throw new NoSuchElementException();
      return (E) snapshot[cursor++];
    }

    @SuppressWarnings("unchecked")
    public E previous() {
      if (!hasPrevious()) throw new NoSuchElementException();
      return (E) snapshot[--cursor];
    }

    public int nextIndex() {
      return cursor;
    }

    public int previousIndex() {
      return cursor - 1;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public void set(E e) {
      throw new UnsupportedOperationException();
    }

    public void add(E e) {
      throw new UnsupportedOperationException();
    }

    public void forEachRemaining(Consumer<? super E> action) {
      Objects.requireNonNull(action);
      int size = snapshot.length;
      for (int i = cursor; i < size; i++) {
        @SuppressWarnings("unchecked")
        E e = (E) snapshot[i];
        action.accept(e);
      }
      cursor = size;
    }
  }

  static class COWSubList<E> extends java.util.AbstractList<E> implements RandomAccess {
    private final CopyOnWriteArrayList<E> parent;
    private final int offset;
    private int size;
    private Object[] expectedArray;

    COWSubList(CopyOnWriteArrayList<E> parent, int fromIndex, int toIndex) {
      this.parent = parent;
      this.offset = fromIndex;
      this.size = toIndex - fromIndex;
      this.expectedArray = parent.getArray();
    }

    private void checkForComodification() {
      if (parent.getArray() != expectedArray) {
        throw new java.util.ConcurrentModificationException();
      }
    }

    public int size() {
      synchronized (parent.lock) {
        checkForComodification();
        return size;
      }
    }

    public E get(int index) {
      synchronized (parent.lock) {
        java.util.Objects.checkIndex(index, size);
        checkForComodification();
        return parent.get(offset + index);
      }
    }

    public E set(int index, E element) {
      synchronized (parent.lock) {
        java.util.Objects.checkIndex(index, size);
        checkForComodification();
        E x = parent.set(offset + index, element);
        expectedArray = parent.getArray();
        return x;
      }
    }

    public boolean add(E e) {
      synchronized (parent.lock) {
        checkForComodification();
        parent.add(offset + size, e);
        expectedArray = parent.getArray();
        size++;
        return true;
      }
    }

    public void add(int index, E element) {
      synchronized (parent.lock) {
        checkForComodification();
        if (index < 0 || index > size) {
          throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        parent.add(offset + index, element);
        expectedArray = parent.getArray();
        size++;
      }
    }

    public E remove(int index) {
      synchronized (parent.lock) {
        java.util.Objects.checkIndex(index, size);
        checkForComodification();
        E res = parent.remove(offset + index);
        expectedArray = parent.getArray();
        size--;
        return res;
      }
    }

    public void clear() {
      synchronized (parent.lock) {
        checkForComodification();
        for (int i = size - 1; i >= 0; i--) {
          parent.remove(offset + i);
        }
        expectedArray = parent.getArray();
        size = 0;
      }
    }

    public java.util.Iterator<E> iterator() {
      synchronized (parent.lock) {
        checkForComodification();
        return super.iterator();
      }
    }
  }
}
