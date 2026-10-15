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

public interface Set<T> extends Collection<T> {
  static <E> Set<E> of() {
    return ImmutableCollections.emptySet();
  }

  static <E> Set<E> of(E e1) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1 });
  }

  static <E> Set<E> of(E e1, E e2) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2 });
  }

  static <E> Set<E> of(E e1, E e2, E e3) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4, e5 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4, e5, e6 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7, e8 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7, e8, e9 });
  }

  static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
    return new ImmutableCollections.SetN<E>(new Object[] { e1, e2, e3, e4, e5, e6, e7, e8, e9, e10 });
  }

  static <E> Set<E> of(E... elements) {
    if (elements.length == 0) {
      return ImmutableCollections.emptySet();
    }
    return new ImmutableCollections.SetN<E>(elements);
  }

  @SuppressWarnings("unchecked")
  static <E> Set<E> copyOf(Collection<? extends E> coll) {
    if (coll instanceof ImmutableCollections.SetN) {
      return (Set<E>) coll;
    }
    Set<E> set = new HashSet<E>(coll.size());
    for (E e : coll) {
      set.add(Objects.requireNonNull(e));
    }
    return of((E[]) set.toArray());
  }
}
