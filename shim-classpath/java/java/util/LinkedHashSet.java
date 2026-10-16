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

package java.util;

public class LinkedHashSet<T> extends AbstractSet<T> implements SequencedSet<T>, Cloneable, java.io.Serializable {
  private static final Object Value = new Object();

  private final LinkedHashMap<T, Object> map;

  public LinkedHashSet(Collection<? extends T> c) {
    map = new LinkedHashMap(c.size());
    addAll(c);
  }

  public LinkedHashSet(int capacity) {
    map = new LinkedHashMap(capacity);
  }

  public LinkedHashSet() {
    this(0);
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean contains(Object element) {
    return map.containsKey(element);
  }

  public boolean add(T element) {
    return map.put(element, Value) != Value;
  }

  public boolean addAll(Collection<? extends T> collection) {
    boolean change = false;
    for (T t: collection) if (add(t)) change = true;
    return change;
  }

  public boolean remove(Object element) {
    return map.remove(element) != Value;
  }

  public void clear() {
    map.clear();
  }

  public Iterator<T> iterator() {
    return new MyIterator(map.iterator());
  }

  @Override
  public void addFirst(T element) {
    map.putFirst(element, Value);
  }

  @Override
  public void addLast(T element) {
    map.putLast(element, Value);
  }

  @Override
  public T getFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return iterator().next();
  }

  @Override
  public T getLast() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return reversed().iterator().next();
  }

  @Override
  public T removeFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    Iterator<T> it = iterator();
    T elem = it.next();
    it.remove();
    return elem;
  }

  @Override
  public T removeLast() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    Iterator<T> it = reversed().iterator();
    T elem = it.next();
    it.remove();
    return elem;
  }

  @Override
  public SequencedSet<T> reversed() {
    return new ReversedView();
  }

  public String toString() {
    return shim.Data.toString(this);
  }

  private static class MyIterator<T> implements Iterator<T> {
    private final Iterator<Map.Entry<T, Object>> it;

    public MyIterator(Iterator<Map.Entry<T, Object>> it) {
      this.it = it;
    }

    public T next() {
      return it.next().getKey();
    }

    public boolean hasNext() {
      return it.hasNext();
    }

    public void remove() {
      it.remove();
    }
  }

  private class ReversedView extends AbstractSet<T> implements SequencedSet<T> {
    @Override
    public int size() {
      return LinkedHashSet.this.size();
    }

    @Override
    public boolean isEmpty() {
      return LinkedHashSet.this.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return LinkedHashSet.this.contains(o);
    }

    @Override
    public boolean add(T element) {
      LinkedHashSet.this.addFirst(element);
      return true;
    }

    @Override
    public void addFirst(T element) {
      LinkedHashSet.this.addLast(element);
    }

    @Override
    public void addLast(T element) {
      LinkedHashSet.this.addFirst(element);
    }

    @Override
    public T getFirst() {
      return LinkedHashSet.this.getLast();
    }

    @Override
    public T getLast() {
      return LinkedHashSet.this.getFirst();
    }

    @Override
    public T removeFirst() {
      return LinkedHashSet.this.removeLast();
    }

    @Override
    public T removeLast() {
      return LinkedHashSet.this.removeFirst();
    }

    @Override
    public boolean remove(Object o) {
      return LinkedHashSet.this.remove(o);
    }

    @Override
    public void clear() {
      LinkedHashSet.this.clear();
    }

    @Override
    public SequencedSet<T> reversed() {
      return LinkedHashSet.this;
    }

    @Override
    public Iterator<T> iterator() {
      return new MyIterator<T>(map.reversed().entrySet().iterator());
    }
  }
}

