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

public class EnumSet<T extends Enum<T>> extends AbstractSet<T> implements Cloneable, java.io.Serializable {
  
  private BitSet bitset;
  private Class<T> elementType;
  
  private static class SerializationProxy<E extends Enum<E>> implements java.io.Serializable {
    private static final long serialVersionUID = 1009687484059888093L;
    private final Class<E> elementType;
    private final Enum<?>[] elements;

    SerializationProxy(EnumSet<E> set) {
      elementType = set.elementType;
      elements = set.toArray(new Enum<?>[0]);
    }

    @SuppressWarnings("unchecked")
    private Object readResolve() {
      EnumSet<E> result = EnumSet.noneOf(elementType);
      for (Enum<?> e : elements) {
        result.add((E) e);
      }
      return result;
    }
  }

  Object writeReplace() {
    return new SerializationProxy<T>(this);
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.InvalidObjectException {
    throw new java.io.InvalidObjectException("Proxy required");
  }

  private EnumSet(int size, Class<T> type) {
    bitset = new BitSet(size);
    elementType = type;
  }
  
  @Override
  public boolean add(T element) {
    int index = element.ordinal();
    boolean contains = bitset.get(index);
    if (!contains) {
      bitset.set(index);
    }
    
    return !contains;
  }
  
  @Override
  public boolean remove(Object toRemove) {
    T element =  tryToCast(toRemove);
    
    int index = element.ordinal();
    boolean contains = bitset.get(index);
    if (contains) {
      bitset.clear(index);
    }
    
    return contains;
  }
  
  @Override
  public boolean contains(Object toCheck) {
    T element =  tryToCast(toCheck);
    int index = element.ordinal();
    return bitset.get(index);
  }
  
  @Override
  public int size() {
    return bitset.cardinality();
  }

  @Override
  public Iterator<T> iterator() {
    return new EnumSetIterator();
  }
  
  public static <T extends Enum<T>>EnumSet<T> allOf(Class<T> elementType) {
    EnumSet<T> enumSet = createEmptyEnumSet(elementType);
    enumSet.bitset.set(0, elementType.getEnumConstants().length);
    
    return enumSet;
  }
  
  public static <T extends Enum<T>>EnumSet<T> noneOf(Class<T> elementType) {    
    return createEmptyEnumSet(elementType);
  }
  
  public static <E extends Enum<E>> EnumSet<E> of(E e) {
    EnumSet<E> result = noneOf(e.getDeclaringClass());
    result.add(e);
    return result;
  }

  public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    return result;
  }

  public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    result.add(e3);
    return result;
  }

  public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    result.add(e3);
    result.add(e4);
    return result;
  }

  public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    result.add(e3);
    result.add(e4);
    result.add(e5);
    return result;
  }

  public static <T extends Enum<T>>EnumSet<T> of(T first, T ... rest) {
    EnumSet<T> enumSet = createEmptyEnumSet(first.getDeclaringClass());
    enumSet.add(first);
    addAllElementsToSet(Arrays.asList(rest), enumSet);
    
    return enumSet;
  }
  
  public static <T extends Enum<T>>EnumSet<T> complementOf(EnumSet<T> s) {
    EnumSet<T> enumSet = copyOf(s);
    enumSet.bitset.flip(0, s.elementType.getEnumConstants().length);
    
    return enumSet;
  }
  
  public static <T extends Enum<T>>EnumSet<T> copyOf(EnumSet<T> s) {
    EnumSet<T> enumSet = createEmptyEnumSet(s.elementType);
    enumSet.bitset.or(s.bitset);
    
    return enumSet;
  }

  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
    if (c instanceof EnumSet) {
      return copyOf((EnumSet<E>) c);
    }
    if (c.isEmpty()) {
      throw new IllegalArgumentException("Collection is empty");
    }
    Iterator<E> it = c.iterator();
    E first = it.next();
    EnumSet<E> result = of(first);
    while (it.hasNext()) {
      result.add(it.next());
    }
    return result;
  }

  public static <E extends Enum<E>> EnumSet<E> range(E from, E to) {
    if (from.compareTo(to) > 0) {
      throw new IllegalArgumentException(from + " > " + to);
    }
    EnumSet<E> result = noneOf(from.getDeclaringClass());
    E[] constants = from.getDeclaringClass().getEnumConstants();
    for (int i = from.ordinal(); i <= to.ordinal(); i++) {
      result.add(constants[i]);
    }
    return result;
  }
  
  private static <T extends Enum<T>>EnumSet<T> createEmptyEnumSet(Class<T> elementType) {
    T[] constants = elementType.getEnumConstants();
    EnumSet<T> enumSet = new EnumSet<T>(constants.length, elementType);
    
    return enumSet;
  }
  
  private static <T extends Enum<T>> void addAllElementsToSet(Iterable<T> elements, EnumSet<T> enumSet) {
    for (T element : elements) {
      enumSet.add(element);
    }
  }
  
  @SuppressWarnings("unchecked")
  private T tryToCast(Object object) throws ClassCastException {
    //We want the class cast exception if we can't convert.
    return (T) object;
  }

  @Override
  @SuppressWarnings("unchecked")
  public EnumSet<T> clone() {
    try {
      EnumSet<T> result = (EnumSet<T>) super.clone();
      result.bitset = (BitSet) bitset.clone();
      return result;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  private class EnumSetIterator implements Iterator<T> {
    private int currentIndex = 0;
    private boolean removeAllowed = false;
    
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException("EnumSet has no more elements");
      }

      int indexOfNextValue = nextIndex();
      T element = elementType.getEnumConstants()[indexOfNextValue];
      currentIndex = indexOfNextValue + 1;
      removeAllowed = true;
      
      return element;
    }

    public boolean hasNext() {
      int indexOfNextValue = nextIndex();
      if (indexOfNextValue >= 0) {
        return true;
      } else {
        return false;
      }
    }

    public void remove() {
      if (!removeAllowed) {
        throw new IllegalStateException("Cannot remove from this iterator in this state");
      }
      
      bitset.clear(currentIndex - 1);
      removeAllowed = false;
    }
    
    private int nextIndex() {
      return bitset.nextSetBit(currentIndex);
    }
  }
}
