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

public abstract class AbstractList<T> extends AbstractCollection<T>
  implements List<T>
{
  protected int modCount;

  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof List)) return false;
    Iterator<T> left = iterator();
    Iterator<?> right = ((List<?>) other).iterator();
    while (left.hasNext() && right.hasNext()) {
      T a = left.next();
      Object b = right.next();
      if (a == null ? b != null : !a.equals(b)) return false;
    }
    return !left.hasNext() && !right.hasNext();
  }

  public int hashCode() {
    int hash = 1;
    for (T element : this) hash = 31 * hash + (element == null ? 0 : element.hashCode());
    return hash;
  }

  public boolean add(T o) {
    add(size(), o);
    return true;
  }

  public boolean addAll(Collection<? extends T> c) {
    return addAll(size(), c);
  }

  public boolean addAll(int startIndex, Collection<? extends T> c) {
    if (c == null) {
      throw new NullPointerException("Collection is null");
    }

    int index = startIndex;
    boolean changed = false;

    Iterator<? extends T> it = c.iterator();
    while (it.hasNext()) {
      add(index++, it.next());
      changed = true;
    }

    return changed;
  }

  public Iterator<T> iterator() {
    return listIterator();
  }

  public ListIterator<T> listIterator() {
    return new Collections.ArrayListIterator(this);
  }

  public int indexOf(Object o) {
    int i = 0;
    for (T v: this) {
      if (o == null) {
        if (v == null) {
          return i;
        }
      } else if (o.equals(v)) {
        return i;
      }

      ++ i;
    }
    return -1;
  }
}
