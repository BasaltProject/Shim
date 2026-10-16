
package java.util;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ArrayDeque<E> extends AbstractCollection<E> implements Deque<E>, Cloneable, Serializable {
  private static final long serialVersionUID = 2340985798034038923L;

  transient Object[] elements;
  transient int head;
  transient int tail;

  private void grow(int needed) {
    final int oldCapacity = elements.length;
    int newCapacity;
    int jump = (oldCapacity < 64) ? (oldCapacity + 2) : (oldCapacity >> 1);
    if (jump < needed || (newCapacity = (oldCapacity + jump)) - (Integer.MAX_VALUE - 8) > 0) {
      newCapacity = newCapacity(needed, jump);
    }
    final Object[] es = elements = Arrays.copyOf(elements, newCapacity);
    if (tail < head || (tail == head && es[head] != null)) {
      int newSpace = newCapacity - oldCapacity;
      System.arraycopy(es, head, es, head + newSpace, oldCapacity - head);
      for (int i = head, to = (head += newSpace); i < to; i++) {
        es[i] = null;
      }
    }
  }

  private int newCapacity(int needed, int jump) {
    final int oldCapacity = elements.length, minCapacity;
    if ((minCapacity = oldCapacity + needed) - (Integer.MAX_VALUE - 8) > 0) {
      if (minCapacity < 0) {
        throw new IllegalStateException("Sorry, deque too big");
      }
      return Integer.MAX_VALUE;
    }
    if (needed > jump) {
      return minCapacity;
    }
    return (oldCapacity + jump - (Integer.MAX_VALUE - 8) < 0) ? oldCapacity + jump : Integer.MAX_VALUE;
  }

  public ArrayDeque() {
    elements = new Object[16 + 1];
  }

  public ArrayDeque(int numElements) {
    elements = new Object[(numElements < 1) ? 1 : (numElements == Integer.MAX_VALUE) ? Integer.MAX_VALUE
                                                                                     : numElements + 1];
  }

  public ArrayDeque(Collection<? extends E> c) {
    this(c.size());
    copyElements(c);
  }

  static final int inc(int i, int modulus) {
    if (++i >= modulus) {
      i = 0;
    }
    return i;
  }

  static final int dec(int i, int modulus) {
    if (--i < 0) {
      i = modulus - 1;
    }
    return i;
  }

  static final int sub(int i, int j, int modulus) {
    if ((i -= j) < 0) {
      i += modulus;
    }
    return i;
  }

  @SuppressWarnings("unchecked")
  static final <E> E elementAt(Object[] es, int i) {
    return (E) es[i];
  }

  @SuppressWarnings("unchecked")
  static final <E> E nonNullElementAt(Object[] es, int i) {
    E e = (E) es[i];
    if (e == null) {
      throw new ConcurrentModificationException();
    }
    return e;
  }

  public void addFirst(E e) {
    if (e == null) {
      throw new NullPointerException();
    }
    final Object[] es = elements;
    es[head = dec(head, es.length)] = e;
    if (head == tail) {
      grow(1);
    }
  }

  public void addLast(E e) {
    if (e == null) {
      throw new NullPointerException();
    }
    final Object[] es = elements;
    es[tail] = e;
    if (head == (tail = inc(tail, es.length))) {
      grow(1);
    }
  }

  public boolean addAll(Collection<? extends E> c) {
    final int s, needed;
    if ((needed = (s = size()) + c.size() + 1 - elements.length) > 0) {
      grow(needed);
    }
    copyElements(c);
    return size() > s;
  }

  private void copyElements(Collection<? extends E> c) {
    c.forEach(this::addLast);
  }

  public boolean offerFirst(E e) {
    addFirst(e);
    return true;
  }

  public boolean offerLast(E e) {
    addLast(e);
    return true;
  }

  public E removeFirst() {
    E e = pollFirst();
    if (e == null) {
      throw new NoSuchElementException();
    }
    return e;
  }

  public E removeLast() {
    E e = pollLast();
    if (e == null) {
      throw new NoSuchElementException();
    }
    return e;
  }

  public E pollFirst() {
    final Object[] es;
    final int h;
    E e = elementAt(es = elements, h = head);
    if (e != null) {
      es[h] = null;
      head = inc(h, es.length);
    }
    return e;
  }

  public E pollLast() {
    final Object[] es;
    final int t;
    E e = elementAt(es = elements, t = dec(tail, es.length));
    if (e != null) {
      es[tail = t] = null;
    }
    return e;
  }

  public E getFirst() {
    E e = elementAt(elements, head);
    if (e == null) {
      throw new NoSuchElementException();
    }
    return e;
  }

  public E getLast() {
    final Object[] es = elements;
    E e = elementAt(es, dec(tail, es.length));
    if (e == null) {
      throw new NoSuchElementException();
    }
    return e;
  }

  public E peekFirst() {
    return elementAt(elements, head);
  }

  public E peekLast() {
    final Object[] es;
    return elementAt(es = elements, dec(tail, es.length));
  }

  public boolean removeFirstOccurrence(Object o) {
    if (o != null) {
      final Object[] es = elements;
      for (int i = head, end = tail, to = (i <= end) ? end : es.length;; i = 0, to = end) {
        for (; i < to; i++) {
          if (o.equals(es[i])) {
            delete(i);
            return true;
          }
        }
        if (to == end) {
          break;
        }
      }
    }
    return false;
  }

  public boolean removeLastOccurrence(Object o) {
    if (o != null) {
      final Object[] es = elements;
      for (int i = tail, end = head, to = (i >= end) ? end : 0;; i = es.length, to = end) {
        for (i--; i > to - 1; i--) {
          if (o.equals(es[i])) {
            delete(i);
            return true;
          }
        }
        if (to == end) {
          break;
        }
      }
    }
    return false;
  }

  public boolean add(E e) {
    addLast(e);
    return true;
  }

  public boolean offer(E e) {
    return offerLast(e);
  }

  public E remove() {
    return removeFirst();
  }

  public E poll() {
    return pollFirst();
  }

  public E element() {
    return getFirst();
  }

  public E peek() {
    return peekFirst();
  }

  public void push(E e) {
    addFirst(e);
  }

  public E pop() {
    return removeFirst();
  }

  boolean delete(int i) {
    final Object[] es = elements;
    final int capacity = es.length;
    final int h, t;
    final int front = sub(i, h = head, capacity);
    final int back = sub(t = tail, i, capacity) - 1;
    if (front < back) {
      if (h <= i) {
        System.arraycopy(es, h, es, h + 1, front);
      } else {
        System.arraycopy(es, 0, es, 1, i);
        es[0] = es[capacity - 1];
        System.arraycopy(es, h, es, h + 1, front - (i + 1));
      }
      es[h] = null;
      head = inc(h, capacity);
      return false;
    } else {
      tail = dec(t, capacity);
      if (i <= tail) {
        System.arraycopy(es, i + 1, es, i, back);
      } else {
        System.arraycopy(es, i + 1, es, i, capacity - (i + 1));
        es[capacity - 1] = es[0];
        System.arraycopy(es, 1, es, 0, t - 1);
      }
      es[tail] = null;
      return true;
    }
  }

  public int size() {
    return sub(tail, head, elements.length);
  }

  public boolean isEmpty() {
    return head == tail;
  }

  public Iterator<E> iterator() {
    return new DeqIterator();
  }

  public Iterator<E> descendingIterator() {
    return new DescendingIterator();
  }

  private class DeqIterator implements Iterator<E> {
    int cursor;
    int remaining = size();
    int lastRet = -1;

    DeqIterator() {
      cursor = head;
    }

    public final boolean hasNext() {
      return remaining > 0;
    }

    public E next() {
      if (remaining <= 0) {
        throw new NoSuchElementException();
      }
      final Object[] es = elements;
      E e = nonNullElementAt(es, cursor);
      cursor = inc(lastRet = cursor, es.length);
      remaining--;
      return e;
    }

    void postDelete(boolean leftShifted) {
      if (leftShifted) {
        cursor = dec(cursor, elements.length);
      }
    }

    public final void remove() {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      postDelete(delete(lastRet));
      lastRet = -1;
    }

    public void forEachRemaining(Consumer<? super E> action) {
      Objects.requireNonNull(action);
      int r;
      if ((r = remaining) <= 0) {
        return;
      }
      remaining = 0;
      final Object[] es = elements;
      if (es[cursor] == null || sub(tail, cursor, es.length) != r) {
        throw new ConcurrentModificationException();
      }
      for (int i = cursor, end = tail, to = (i <= end) ? end : es.length;; i = 0, to = end) {
        for (; i < to; i++) {
          action.accept(elementAt(es, i));
        }
        if (to == end) {
          if (end != tail) {
            throw new ConcurrentModificationException();
          }
          lastRet = dec(end, es.length);
          break;
        }
      }
    }
  }

  private class DescendingIterator extends DeqIterator {
    DescendingIterator() {
      cursor = dec(tail, elements.length);
    }

    public final E next() {
      if (remaining <= 0) {
        throw new NoSuchElementException();
      }
      final Object[] es = elements;
      E e = nonNullElementAt(es, cursor);
      cursor = dec(lastRet = cursor, es.length);
      remaining--;
      return e;
    }

    void postDelete(boolean leftShifted) {
      if (!leftShifted) {
        cursor = inc(cursor, elements.length);
      }
    }

    public final void forEachRemaining(Consumer<? super E> action) {
      Objects.requireNonNull(action);
      int r;
      if ((r = remaining) <= 0) {
        return;
      }
      remaining = 0;
      final Object[] es = elements;
      if (es[cursor] == null || sub(cursor, head, es.length) + 1 != r) {
        throw new ConcurrentModificationException();
      }
      for (int i = cursor, end = head, to = (i >= end) ? end : 0;; i = es.length - 1, to = end) {
        for (; i > to - 1; i--) {
          action.accept(elementAt(es, i));
        }
        if (to == end) {
          if (end != head) {
            throw new ConcurrentModificationException();
          }
          lastRet = end;
          break;
        }
      }
    }
  }

  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    final Object[] es = elements;
    for (int i = head, end = tail, to = (i <= end) ? end : es.length;; i = 0, to = end) {
      for (; i < to; i++) {
        action.accept(elementAt(es, i));
      }
      if (to == end) {
        if (end != tail) {
          throw new ConcurrentModificationException();
        }
        break;
      }
    }
  }

  public boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    return bulkRemove(filter);
  }

  public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return bulkRemove(e -> c.contains(e));
  }

  public boolean retainAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return bulkRemove(e -> !c.contains(e));
  }

  private boolean bulkRemove(Predicate<? super E> filter) {
    List<E> survivors = new ArrayList<E>();
    boolean removed = false;
    for (Iterator<E> it = iterator(); it.hasNext();) {
      E e = it.next();
      if (filter.test(e)) {
        removed = true;
      } else {
        survivors.add(e);
      }
    }
    if (removed) {
      clear();
      for (E e : survivors) {
        addLast(e);
      }
    }
    return removed;
  }

  public boolean contains(Object o) {
    if (o != null) {
      final Object[] es = elements;
      for (int i = head, end = tail, to = (i <= end) ? end : es.length;; i = 0, to = end) {
        for (; i < to; i++) {
          if (o.equals(es[i])) {
            return true;
          }
        }
        if (to == end) {
          break;
        }
      }
    }
    return false;
  }

  public boolean remove(Object o) {
    return removeFirstOccurrence(o);
  }

  public void clear() {
    final Object[] es = elements;
    for (int i = 0; i < es.length; i++) {
      es[i] = null;
    }
    head = tail = 0;
  }

  public Object[] toArray() {
    final int size = size();
    Object[] a = new Object[size];
    copyInto(a);
    return a;
  }

  private void copyInto(Object[] a) {
    final Object[] es = elements;
    int n = 0;
    for (int i = head, end = tail, to = (i <= end) ? end : es.length;; i = 0, to = end) {
      for (; i < to; i++) {
        a[n++] = es[i];
      }
      if (to == end) {
        break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    final int size = size();
    if (size > a.length) {
      a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
    }
    Object[] target = a;
    copyInto(target);
    if (size < a.length) {
      a[size] = null;
    }
    return a;
  }

  public ArrayDeque<E> clone() {
    try {
      @SuppressWarnings("unchecked")
      ArrayDeque<E> result = (ArrayDeque<E>) super.clone();
      result.elements = Arrays.copyOf(elements, elements.length);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeInt(size());
    for (Iterator<E> it = iterator(); it.hasNext();) {
      s.writeObject(it.next());
    }
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    int size = s.readInt();
    elements = new Object[size + 1];
    for (int i = 0; i < size; i++) {
      addLast((E) s.readObject());
    }
  }
}
