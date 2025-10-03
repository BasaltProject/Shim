
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
  private static final long serialVersionUID = 8683452581122892189L;

  private static final int DEFAULT_CAPACITY = 10;
  private static final Object[] EMPTY_ELEMENTDATA = {};
  private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

  transient Object[] elementData;

  private int size;

  public ArrayList(int initialCapacity) {
    if (initialCapacity > 0) {
      this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
      this.elementData = EMPTY_ELEMENTDATA;
    } else {
      throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
    }
  }

  public ArrayList() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
  }

  public ArrayList(Collection<? extends E> c) {
    Object[] a = c.toArray();
    if ((size = a.length) != 0) {
      if (c.getClass() == ArrayList.class) {
        elementData = a;
      } else {
        elementData = Arrays.copyOf(a, size, Object[].class);
      }
    } else {
      elementData = EMPTY_ELEMENTDATA;
    }
  }

  public void trimToSize() {
    modCount++;
    if (size < elementData.length) {
      elementData = (size == 0) ? EMPTY_ELEMENTDATA : Arrays.copyOf(elementData, size);
    }
  }

  public void ensureCapacity(int minCapacity) {
    if (minCapacity > elementData.length
        && !(elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA && minCapacity <= DEFAULT_CAPACITY)) {
      modCount++;
      grow(minCapacity);
    }
  }

  private Object[] grow(int minCapacity) {
    int oldCapacity = elementData.length;
    if (oldCapacity > 0 || elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
      int newCapacity = oldCapacity + Math.max(minCapacity - oldCapacity, oldCapacity >> 1);
      if (newCapacity < 0) {
        throw new OutOfMemoryError("Required array length " + oldCapacity + " + " + (minCapacity - oldCapacity)
                                   + " is too large");
      }
      return elementData = Arrays.copyOf(elementData, newCapacity);
    } else {
      return elementData = new Object[Math.max(DEFAULT_CAPACITY, minCapacity)];
    }
  }

  private Object[] grow() {
    return grow(size + 1);
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  public int indexOf(Object o) {
    Object[] es = elementData;
    if (o == null) {
      for (int i = 0; i < size; i++) {
        if (es[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < size; i++) {
        if (o.equals(es[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  public int lastIndexOf(Object o) {
    Object[] es = elementData;
    if (o == null) {
      for (int i = size - 1; i >= 0; i--) {
        if (es[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = size - 1; i >= 0; i--) {
        if (o.equals(es[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  public Object clone() {
    try {
      ArrayList<?> v = (ArrayList<?>) super.clone();
      v.elementData = Arrays.copyOf(elementData, size);
      v.modCount = 0;
      return v;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public Object[] toArray() {
    return Arrays.copyOf(elementData, size);
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    if (a.length < size) {
      return (T[]) Arrays.copyOf(elementData, size, a.getClass());
    }
    System.arraycopy(elementData, 0, a, 0, size);
    if (a.length > size) {
      a[size] = null;
    }
    return a;
  }

  @SuppressWarnings("unchecked")
  E elementData(int index) {
    return (E) elementData[index];
  }

  public E get(int index) {
    Objects.checkIndex(index, size);
    return elementData(index);
  }

  public E set(int index, E element) {
    Objects.checkIndex(index, size);
    E oldValue = elementData(index);
    elementData[index] = element;
    return oldValue;
  }

  private void add(E e, Object[] elementData, int s) {
    if (s == elementData.length) {
      elementData = grow();
    }
    elementData[s] = e;
    size = s + 1;
  }

  public boolean add(E e) {
    modCount++;
    add(e, elementData, size);
    return true;
  }

  public void add(int index, E element) {
    rangeCheckForAdd(index);
    modCount++;
    final int s;
    Object[] elementData;
    if ((s = size) == (elementData = this.elementData).length) {
      elementData = grow();
    }
    System.arraycopy(elementData, index, elementData, index + 1, s - index);
    elementData[index] = element;
    size = s + 1;
  }

  public E remove(int index) {
    Objects.checkIndex(index, size);
    final Object[] es = elementData;
    @SuppressWarnings("unchecked")
    E oldValue = (E) es[index];
    fastRemove(es, index);
    return oldValue;
  }

  public boolean remove(Object o) {
    final Object[] es = elementData;
    final int size = this.size;
    int i = 0;
    found: {
      if (o == null) {
        for (; i < size; i++) {
          if (es[i] == null) {
            break found;
          }
        }
      } else {
        for (; i < size; i++) {
          if (o.equals(es[i])) {
            break found;
          }
        }
      }
      return false;
    }
    fastRemove(es, i);
    return true;
  }

  private void fastRemove(Object[] es, int i) {
    modCount++;
    final int newSize;
    if ((newSize = size - 1) > i) {
      System.arraycopy(es, i + 1, es, i, newSize - i);
    }
    es[size = newSize] = null;
  }

  public void clear() {
    modCount++;
    final Object[] es = elementData;
    for (int to = size, i = size = 0; i < to; i++) {
      es[i] = null;
    }
  }

  public boolean addAll(Collection<? extends E> c) {
    Object[] a = c.toArray();
    modCount++;
    int numNew = a.length;
    if (numNew == 0) {
      return false;
    }
    Object[] elementData;
    final int s;
    if (numNew > (elementData = this.elementData).length - (s = size)) {
      elementData = grow(s + numNew);
    }
    System.arraycopy(a, 0, elementData, s, numNew);
    size = s + numNew;
    return true;
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    rangeCheckForAdd(index);
    Object[] a = c.toArray();
    modCount++;
    int numNew = a.length;
    if (numNew == 0) {
      return false;
    }
    Object[] elementData;
    final int s;
    if (numNew > (elementData = this.elementData).length - (s = size)) {
      elementData = grow(s + numNew);
    }
    int numMoved = s - index;
    if (numMoved > 0) {
      System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
    }
    System.arraycopy(a, 0, elementData, index, numNew);
    size = s + numNew;
    return true;
  }

  protected void removeRange(int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new IndexOutOfBoundsException(outOfBoundsMsg(fromIndex, toIndex));
    }
    modCount++;
    shiftTailOverGap(elementData, fromIndex, toIndex);
  }

  private void shiftTailOverGap(Object[] es, int lo, int hi) {
    System.arraycopy(es, hi, es, lo, size - hi);
    for (int to = size, i = (size -= hi - lo); i < to; i++) {
      es[i] = null;
    }
  }

  private void rangeCheckForAdd(int index) {
    if (index > size || index < 0) {
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
  }

  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Size: " + size;
  }

  private static String outOfBoundsMsg(int fromIndex, int toIndex) {
    return "From Index: " + fromIndex + " > To Index: " + toIndex;
  }

  public boolean removeAll(Collection<?> c) {
    return batchRemove(c, false, 0, size);
  }

  public boolean retainAll(Collection<?> c) {
    return batchRemove(c, true, 0, size);
  }

  boolean batchRemove(Collection<?> c, boolean complement, final int from, final int end) {
    Objects.requireNonNull(c);
    final Object[] es = elementData;
    int r;
    for (r = from; ; r++) {
      if (r == end) {
        return false;
      }
      if (c.contains(es[r]) != complement) {
        break;
      }
    }
    int w = r++;
    try {
      for (Object e; r < end; r++) {
        if (c.contains(e = es[r]) == complement) {
          es[w++] = e;
        }
      }
    } catch (Throwable ex) {
      System.arraycopy(es, r, es, w, end - r);
      w += end - r;
      throw ex;
    } finally {
      modCount += end - w;
      shiftTailOverGap(es, w, end);
    }
    return true;
  }

  public boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    int expectedModCount = modCount;
    final Object[] es = elementData;
    int i = 0;
    int end = size;
    for (; i < end && !filter.test(elementAt(es, i)); i++) {
      ;
    }
    if (i < end) {
      final int beg = i;
      final boolean[] deathRow = new boolean[end - beg];
      deathRow[0] = true;
      for (i = beg + 1; i < end; i++) {
        if (filter.test(elementAt(es, i))) {
          deathRow[i - beg] = true;
        }
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      modCount++;
      int w = beg;
      for (i = beg; i < end; i++) {
        if (!deathRow[i - beg]) {
          es[w++] = es[i];
        }
      }
      shiftTailOverGap(es, w, end);
      return true;
    } else {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  static <E> E elementAt(Object[] es, int index) {
    return (E) es[index];
  }

  public void replaceAll(UnaryOperator<E> operator) {
    Objects.requireNonNull(operator);
    final int expectedModCount = modCount;
    final Object[] es = elementData;
    final int size = this.size;
    for (int i = 0; modCount == expectedModCount && i < size; i++) {
      es[i] = operator.apply(elementAt(es, i));
    }
    if (modCount != expectedModCount) {
      throw new ConcurrentModificationException();
    }
    modCount++;
  }

  @SuppressWarnings("unchecked")
  public void sort(Comparator<? super E> c) {
    final int expectedModCount = modCount;
    Arrays.sort((E[]) elementData, 0, size, c);
    if (modCount != expectedModCount) {
      throw new ConcurrentModificationException();
    }
    modCount++;
  }

  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    final int expectedModCount = modCount;
    final Object[] es = elementData;
    final int size = this.size;
    for (int i = 0; modCount == expectedModCount && i < size; i++) {
      action.accept(elementAt(es, i));
    }
    if (modCount != expectedModCount) {
      throw new ConcurrentModificationException();
    }
  }

  public ListIterator<E> listIterator(int index) {
    rangeCheckForAdd(index);
    return new ListItr(index);
  }

  public ListIterator<E> listIterator() {
    return new ListItr(0);
  }

  public Iterator<E> iterator() {
    return new Itr();
  }

  private class Itr implements Iterator<E> {
    int cursor;
    int lastRet = -1;
    int expectedModCount = modCount;

    Itr() { }

    public boolean hasNext() {
      return cursor != size;
    }

    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      int i = cursor;
      if (i >= size) {
        throw new NoSuchElementException();
      }
      Object[] elementData = ArrayList.this.elementData;
      if (i >= elementData.length) {
        throw new ConcurrentModificationException();
      }
      cursor = i + 1;
      return (E) elementData[lastRet = i];
    }

    public void remove() {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      checkForComodification();
      try {
        ArrayList.this.remove(lastRet);
        cursor = lastRet;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
      Objects.requireNonNull(action);
      final int size = ArrayList.this.size;
      int i = cursor;
      if (i < size) {
        final Object[] es = elementData;
        if (i >= es.length) {
          throw new ConcurrentModificationException();
        }
        for (; i < size && modCount == expectedModCount; i++) {
          action.accept(elementAt(es, i));
        }
        cursor = i;
        lastRet = i - 1;
        checkForComodification();
      }
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  private class ListItr extends Itr implements ListIterator<E> {
    ListItr(int index) {
      super();
      cursor = index;
    }

    public boolean hasPrevious() {
      return cursor != 0;
    }

    public int nextIndex() {
      return cursor;
    }

    public int previousIndex() {
      return cursor - 1;
    }

    @SuppressWarnings("unchecked")
    public E previous() {
      checkForComodification();
      int i = cursor - 1;
      if (i < 0) {
        throw new NoSuchElementException();
      }
      Object[] elementData = ArrayList.this.elementData;
      if (i >= elementData.length) {
        throw new ConcurrentModificationException();
      }
      cursor = i;
      return (E) elementData[lastRet = i];
    }

    public void set(E e) {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      checkForComodification();
      try {
        ArrayList.this.set(lastRet, e);
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    public void add(E e) {
      checkForComodification();
      try {
        int i = cursor;
        ArrayList.this.add(i, e);
        cursor = i + 1;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    int expectedModCount = modCount;
    s.defaultWriteObject();
    s.writeInt(size);
    for (int i = 0; i < size; i++) {
      s.writeObject(elementData[i]);
    }
    if (modCount != expectedModCount) {
      throw new ConcurrentModificationException();
    }
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    s.readInt();
    if (size > 0) {
      Object[] elements = new Object[size];
      for (int i = 0; i < size; i++) {
        elements[i] = s.readObject();
      }
      elementData = elements;
    } else if (size == 0) {
      elementData = EMPTY_ELEMENTDATA;
    } else {
      throw new java.io.InvalidObjectException("Invalid size: " + size);
    }
  }
}
