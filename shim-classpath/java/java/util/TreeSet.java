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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, Serializable {

  public static final long serialVersionUID = -2479143000061671589L;

  private static final Object PRESENT = new Object();

  private transient NavigableMap<E, Object> m;

  TreeSet(NavigableMap<E, Object> m) {
    this.m = m;
  }

  public TreeSet() {
    this(new TreeMap<E, Object>());
  }

  public TreeSet(Comparator<? super E> comparator) {
    this(new TreeMap<E, Object>(comparator));
  }

  public TreeSet(Collection<? extends E> c) {
    this();
    addAll(c);
  }

  public TreeSet(SortedSet<E> s) {
    this(s.comparator());
    addAll(s);
  }

  @Override
  public Iterator<E> iterator() {
    return m.navigableKeySet().iterator();
  }

  @Override
  public Iterator<E> descendingIterator() {
    return m.descendingKeySet().iterator();
  }

  @Override
  public NavigableSet<E> descendingSet() {
    return new TreeSet<E>(m.descendingMap());
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
    return m.put(e, PRESENT) == null;
  }

  @Override
  public boolean remove(Object o) {
    return m.remove(o) == PRESENT;
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
    return new TreeSet<E>(
        m.subMap(fromElement, fromInclusive, toElement, toInclusive));
  }

  @Override
  public NavigableSet<E> headSet(E toElement, boolean inclusive) {
    return new TreeSet<E>(m.headMap(toElement, inclusive));
  }

  @Override
  public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
    return new TreeSet<E>(m.tailMap(fromElement, inclusive));
  }

  @Override
  public SortedSet<E> subSet(E fromElement, E toElement) {
    return subSet(fromElement, true, toElement, false);
  }

  @Override
  public SortedSet<E> headSet(E toElement) {
    return headSet(toElement, false);
  }

  @Override
  public SortedSet<E> tailSet(E fromElement) {
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
  public Object clone() {
    TreeSet<E> clone;
    try {
      clone = (TreeSet<E>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
    clone.m = new TreeMap<E, Object>(m);
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
    TreeMap<E, Object> tm = new TreeMap<E, Object>(c);
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      tm.put((E) in.readObject(), PRESENT);
    }
    this.m = tm;
  }
}
