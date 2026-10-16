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
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CopyOnWriteArraySet<E> extends AbstractSet<E> implements Serializable {
  private static final long serialVersionUID = 5457747651344034263L;

  private final CopyOnWriteArrayList<E> al;

  public CopyOnWriteArraySet() {
    al = new CopyOnWriteArrayList<E>();
  }

  public CopyOnWriteArraySet(Collection<? extends E> c) {
    if (c.getClass() == CopyOnWriteArraySet.class) {
      @SuppressWarnings("unchecked")
      CopyOnWriteArraySet<E> cc = (CopyOnWriteArraySet<E>) c;
      al = new CopyOnWriteArrayList<E>(cc.al);
    } else {
      al = new CopyOnWriteArrayList<E>();
      al.addAllAbsent(c);
    }
  }

  public int size() {
    return al.size();
  }

  public boolean isEmpty() {
    return al.isEmpty();
  }

  public boolean contains(Object o) {
    return al.contains(o);
  }

  public Object[] toArray() {
    return al.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return al.toArray(a);
  }

  public void clear() {
    al.clear();
  }

  public boolean remove(Object o) {
    return al.remove(o);
  }

  public boolean add(E e) {
    return al.addIfAbsent(e);
  }

  public boolean containsAll(Collection<?> c) {
    return al.containsAll(c);
  }

  public boolean addAll(Collection<? extends E> c) {
    return al.addAllAbsent(c) > 0;
  }

  public boolean removeAll(Collection<?> c) {
    return al.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return al.retainAll(c);
  }

  public Iterator<E> iterator() {
    return al.iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Set)) return false;
    Set<?> set = (Set<?>) o;
    Iterator<?> it = set.iterator();
    Object[] elements = al.toArray();
    int len = elements.length;
    boolean[] matched = new boolean[len];
    int count = 0;
    while (it.hasNext()) {
      count++;
      if (count > len) return false;
      Object x = it.next();
      boolean found = false;
      for (int i = 0; i < len; ++i) {
        if (!matched[i] && Objects.equals(x, elements[i])) {
          matched[i] = true;
          found = true;
          break;
        }
      }
      if (!found) return false;
    }
    return count == len;
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    return al.removeIf(filter);
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    al.forEach(action);
  }
}
