/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;

public class ConcurrentSkipListSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, Serializable {

  private static final long serialVersionUID = -2479143111061671589L;
  private static final Object PRESENT = Boolean.TRUE;

  private ConcurrentNavigableMap<E, Object> m;

  public ConcurrentSkipListSet() {
    this(new ConcurrentSkipListMap<E, Object>());
  }

  public ConcurrentSkipListSet(Comparator<? super E> comparator) {
    this(new ConcurrentSkipListMap<E, Object>(comparator));
  }

  public ConcurrentSkipListSet(Collection<? extends E> c) {
    this();
    addAll(c);
  }

  public ConcurrentSkipListSet(SortedSet<E> s) {
    this(s.comparator());
    addAll(s);
  }

  ConcurrentSkipListSet(ConcurrentNavigableMap<E, Object> m) {
    this.m = m;
  }

  @Override
  public Iterator<E> iterator() {
    return m.navigableKeySet().iterator();
  }

  @Override
  public java.util.Spliterator<E> spliterator() {
    return m.keySet().spliterator();
  }

  @Override
  public Iterator<E> descendingIterator() {
    return m.descendingKeySet().iterator();
  }

  @Override
  public NavigableSet<E> descendingSet() {
    return new ConcurrentSkipListSet<E>(m.descendingMap());
  }

  @Override
  public int size() {
    return m.size();
  }

  @Override
  public boolean isEmpty() {
    return m.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return m.containsKey(o);
  }

  @Override
  public boolean add(E e) {
    return m.putIfAbsent(e, PRESENT) == null;
  }

  @Override
  public boolean remove(Object o) {
    return m.remove(o, PRESENT);
  }

  @Override
  public void clear() {
    m.clear();
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    for (E e : c) {
      if (add(e)) {
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public NavigableSet<E> subSet(
      E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
    return new ConcurrentSkipListSet<E>(
        m.subMap(fromElement, fromInclusive, toElement, toInclusive));
  }

  @Override
  public NavigableSet<E> headSet(E toElement, boolean inclusive) {
    return new ConcurrentSkipListSet<E>(m.headMap(toElement, inclusive));
  }

  @Override
  public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
    return new ConcurrentSkipListSet<E>(m.tailMap(fromElement, inclusive));
  }

  @Override
  public NavigableSet<E> subSet(E fromElement, E toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public NavigableSet<E> headSet(E toElement) {
    return headSet(toElement, false);
  }

  @Override
  public NavigableSet<E> tailSet(E fromElement) {
    return tailSet(fromElement, true);
  }

  @Override
  public Comparator<? super E> comparator() {
    return m.comparator();
  }

  @Override
  public E first() {
    return m.firstKey();
  }

  @Override
  public E last() {
    return m.lastKey();
  }

  @Override
  public E lower(E e) {
    return m.lowerKey(e);
  }

  @Override
  public E floor(E e) {
    return m.floorKey(e);
  }

  @Override
  public E ceiling(E e) {
    return m.ceilingKey(e);
  }

  @Override
  public E higher(E e) {
    return m.higherKey(e);
  }

  @Override
  public E pollFirst() {
    Map.Entry<E, Object> e = m.pollFirstEntry();
    return (e == null) ? null : e.getKey();
  }

  @Override
  public E pollLast() {
    Map.Entry<E, Object> e = m.pollLastEntry();
    return (e == null) ? null : e.getKey();
  }

  @Override
  public ConcurrentSkipListSet<E> clone() {
    ConcurrentSkipListSet<E> clone;
    try {
      clone = (ConcurrentSkipListSet<E>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
    clone.m = new ConcurrentSkipListMap<E, Object>((java.util.SortedMap<E, ?>) m);
    return clone;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(m.comparator());
    out.writeInt(m.size());
    for (E e : m.keySet()) {
      out.writeObject(e);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Comparator<? super E> c = (Comparator<? super E>) in.readObject();
    ConcurrentSkipListMap<E, Object> map = new ConcurrentSkipListMap<E, Object>(c);
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      map.put((E) in.readObject(), PRESENT);
    }
    try {
      java.lang.reflect.Field f = ConcurrentSkipListSet.class.getDeclaredField("m");
      f.setAccessible(true);
      f.set(this, map);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
