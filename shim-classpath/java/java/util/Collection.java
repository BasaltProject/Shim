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

public interface Collection<T> extends Iterable<T> {
  public int size();

  public boolean isEmpty();

  public boolean contains(Object element);

  public boolean containsAll(Collection<?> c);

  public boolean add(T element);

  public boolean addAll(Collection<? extends T> collection);

  public boolean remove(Object element);

  public boolean removeAll(Collection<?> c);

  default boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    boolean changed = false;
    Iterator<T> it = iterator();
    while (it.hasNext()) {
      if (!c.contains(it.next())) {
        it.remove();
        changed = true;
      }
    }
    return changed;
  }

  public Object[] toArray();

  public <S> S[] toArray(S[] array);

  public void clear();

  default boolean removeIf(java.util.function.Predicate<? super T> filter) {
    Objects.requireNonNull(filter);
    boolean removed = false;
    Iterator<T> it = iterator();
    while (it.hasNext()) {
      if (filter.test(it.next())) {
        it.remove();
        removed = true;
      }
    }
    return removed;
  }

  @Override
  default Spliterator<T> spliterator() {
    return Spliterators.spliterator(this, 0);
  }

  default java.util.stream.Stream<T> stream() {
    return java.util.stream.StreamSupport.stream(spliterator(), false);
  }

  default java.util.stream.Stream<T> parallelStream() {
    return java.util.stream.StreamSupport.stream(spliterator(), true);
  }

  default <T1> T1[] toArray(java.util.function.IntFunction<T1[]> generator) {
    return toArray(generator.apply(0));
  }
}
