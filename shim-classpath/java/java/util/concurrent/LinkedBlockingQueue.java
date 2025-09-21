
package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LinkedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
  static class Node<E> {
    E item;
    Node<E> next;

    Node(E x) {
      item = x;
    }
  }

  private final int capacity;
  private int count;
  private transient Node<E> head;
  private transient Node<E> last;
  private final Object lock = new Object();

  public LinkedBlockingQueue() {
    this(Integer.MAX_VALUE);
  }

  public LinkedBlockingQueue(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException();
    }
    this.capacity = capacity;
    last = head = new Node<E>(null);
  }

  public LinkedBlockingQueue(Collection<? extends E> c) {
    this(Integer.MAX_VALUE);
    synchronized (lock) {
      int n = 0;
      for (E e : c) {
        if (e == null) {
          throw new NullPointerException();
        }
        if (n == capacity) {
          throw new IllegalStateException("Queue full");
        }
        enqueue(new Node<E>(e));
        ++n;
      }
      count = n;
    }
  }

  private void enqueue(Node<E> node) {
    last = last.next = node;
  }

  private E dequeue() {
    Node<E> h = head;
    Node<E> first = h.next;
    h.next = h;
    head = first;
    E x = first.item;
    first.item = null;
    return x;
  }

  void unlink(Node<E> p, Node<E> pred) {
    p.item = null;
    pred.next = p.next;
    if (last == p) {
      last = pred;
    }
    count--;
    lock.notifyAll();
  }

  public int size() {
    synchronized (lock) {
      return count;
    }
  }

  public int remainingCapacity() {
    synchronized (lock) {
      return capacity - count;
    }
  }

  public void put(E e) throws InterruptedException {
    if (e == null) {
      throw new NullPointerException();
    }
    synchronized (lock) {
      while (count == capacity) {
        lock.wait();
      }
      enqueue(new Node<E>(e));
      count++;
      lock.notifyAll();
    }
  }

  public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
    if (e == null) {
      throw new NullPointerException();
    }
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    synchronized (lock) {
      while (count == capacity) {
        if (nanos <= 0L) {
          return false;
        }
        waitNanos(nanos);
        nanos = deadline - System.nanoTime();
      }
      enqueue(new Node<E>(e));
      count++;
      lock.notifyAll();
      return true;
    }
  }

  private void waitNanos(long nanos) throws InterruptedException {
    long ms = nanos / 1000000L;
    int ns = (int) (nanos % 1000000L);
    lock.wait(ms, ns);
  }

  public boolean offer(E e) {
    if (e == null) {
      throw new NullPointerException();
    }
    synchronized (lock) {
      if (count == capacity) {
        return false;
      }
      enqueue(new Node<E>(e));
      count++;
      lock.notifyAll();
      return true;
    }
  }

  public E take() throws InterruptedException {
    synchronized (lock) {
      while (count == 0) {
        lock.wait();
      }
      E x = dequeue();
      count--;
      lock.notifyAll();
      return x;
    }
  }

  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    long deadline = System.nanoTime() + nanos;
    synchronized (lock) {
      while (count == 0) {
        if (nanos <= 0L) {
          return null;
        }
        waitNanos(nanos);
        nanos = deadline - System.nanoTime();
      }
      E x = dequeue();
      count--;
      lock.notifyAll();
      return x;
    }
  }

  public E poll() {
    synchronized (lock) {
      if (count == 0) {
        return null;
      }
      E x = dequeue();
      count--;
      lock.notifyAll();
      return x;
    }
  }

  public E peek() {
    synchronized (lock) {
      return (count > 0) ? head.next.item : null;
    }
  }

  public boolean remove(Object o) {
    if (o == null) {
      return false;
    }
    synchronized (lock) {
      for (Node<E> pred = head, p = pred.next; p != null; pred = p, p = p.next) {
        if (o.equals(p.item)) {
          unlink(p, pred);
          return true;
        }
      }
      return false;
    }
  }

  public boolean contains(Object o) {
    if (o == null) {
      return false;
    }
    synchronized (lock) {
      for (Node<E> p = head.next; p != null; p = p.next) {
        if (o.equals(p.item)) {
          return true;
        }
      }
      return false;
    }
  }

  public Object[] toArray() {
    synchronized (lock) {
      int size = count;
      Object[] a = new Object[size];
      int k = 0;
      for (Node<E> p = head.next; p != null; p = p.next) {
        a[k++] = p.item;
      }
      return a;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T[] toArray(T[] a) {
    synchronized (lock) {
      int size = count;
      if (a.length < size) {
        a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
      }
      int k = 0;
      for (Node<E> p = head.next; p != null; p = p.next) {
        a[k++] = (T) p.item;
      }
      if (a.length > k) {
        a[k] = null;
      }
      return a;
    }
  }

  public String toString() {
    synchronized (lock) {
      Node<E> p = head.next;
      if (p == null) {
        return "[]";
      }
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (;;) {
        E e = p.item;
        sb.append(e == this ? "(this Collection)" : e);
        p = p.next;
        if (p == null) {
          return sb.append(']').toString();
        }
        sb.append(',').append(' ');
      }
    }
  }

  public void clear() {
    synchronized (lock) {
      for (Node<E> p, h = head; (p = h.next) != null; h = p) {
        h.next = h;
        p.item = null;
      }
      head = last;
      count = 0;
      lock.notifyAll();
    }
  }

  public int drainTo(Collection<? super E> c) {
    return drainTo(c, Integer.MAX_VALUE);
  }

  public int drainTo(Collection<? super E> c, int maxElements) {
    Objects.requireNonNull(c);
    if (c == this) {
      throw new IllegalArgumentException();
    }
    if (maxElements <= 0) {
      return 0;
    }
    synchronized (lock) {
      int n = Math.min(maxElements, count);
      int i = 0;
      while (i < n) {
        c.add(head.next.item);
        dequeue();
        count--;
        ++i;
      }
      if (i > 0) {
        lock.notifyAll();
      }
      return n;
    }
  }

  public Iterator<E> iterator() {
    return new Itr();
  }

  private class Itr implements Iterator<E> {
    private Node<E> next;
    private E nextItem;
    private Node<E> lastRet;

    Itr() {
      synchronized (lock) {
        next = head.next;
        if (next != null) {
          nextItem = next.item;
        }
      }
    }

    public boolean hasNext() {
      return next != null;
    }

    public E next() {
      synchronized (lock) {
        Node<E> p = next;
        if (p == null) {
          throw new NoSuchElementException();
        }
        lastRet = p;
        E x = nextItem;
        E nextItem = null;
        for (;;) {
          p = (p.next == p) ? head.next : p.next;
          if (p == null || (nextItem = p.item) != null) {
            break;
          }
        }
        next = p;
        this.nextItem = nextItem;
        return x;
      }
    }

    public void remove() {
      if (lastRet == null) {
        throw new IllegalStateException();
      }
      synchronized (lock) {
        Node<E> node = lastRet;
        lastRet = null;
        for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) {
          if (p == node) {
            unlink(p, trail);
            break;
          }
        }
      }
    }
  }

  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT);
  }

  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    for (Iterator<E> it = iterator(); it.hasNext();) {
      action.accept(it.next());
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
    boolean removed = false;
    synchronized (lock) {
      for (Node<E> pred = head, p = pred.next; p != null;) {
        if (filter.test(p.item)) {
          unlink(p, pred);
          removed = true;
          p = pred.next;
        } else {
          pred = p;
          p = p.next;
        }
      }
    }
    return removed;
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    synchronized (lock) {
      s.defaultWriteObject();
      for (Node<E> p = head.next; p != null; p = p.next) {
        s.writeObject(p.item);
      }
      s.writeObject(null);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    count = 0;
    last = head = new Node<E>(null);
    for (;;) {
      E item = (E) s.readObject();
      if (item == null) {
        break;
      }
      enqueue(new Node<E>(item));
      count++;
    }
  }
}
