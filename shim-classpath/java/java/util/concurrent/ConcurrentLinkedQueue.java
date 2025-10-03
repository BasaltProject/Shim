
package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E>, java.io.Serializable {
  private static final long serialVersionUID = 196745693267521676L;

  private static class Node<E> {
    volatile E item;
    volatile Node<E> next;

    Node(E item) {
      this.item = item;
    }
  }

  private transient Node<E> head;
  private transient Node<E> tail;
  private transient Object lock = new Object();

  public ConcurrentLinkedQueue() {
    head = tail = new Node<E>(null);
  }

  public ConcurrentLinkedQueue(Collection<? extends E> c) {
    this();
    for (E e : c) {
      offer(e);
    }
  }

  public boolean add(E e) {
    return offer(e);
  }

  public boolean offer(E e) {
    Node<E> node = new Node<E>(Objects.requireNonNull(e));
    synchronized (lock) {
      tail.next = node;
      tail = node;
    }
    return true;
  }

  public E poll() {
    synchronized (lock) {
      Node<E> first = head.next;
      if (first == null) {
        return null;
      }
      E item = first.item;
      Node<E> old = head;
      old.next = old;
      head = first;
      first.item = null;
      return item;
    }
  }

  public E peek() {
    synchronized (lock) {
      Node<E> first = head.next;
      return first == null ? null : first.item;
    }
  }

  public boolean isEmpty() {
    synchronized (lock) {
      return head.next == null;
    }
  }

  public int size() {
    synchronized (lock) {
      int count = 0;
      for (Node<E> p = head.next; p != null; p = p.next) {
        if (p.item != null) {
          ++count;
        }
      }
      return count;
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

  private void unlink(Node<E> p, Node<E> pred) {
    p.item = null;
    pred.next = p.next;
    if (tail == p) {
      tail = pred;
    }
  }

  public boolean addAll(Collection<? extends E> c) {
    if (c == this) {
      throw new IllegalArgumentException();
    }
    boolean added = false;
    for (E e : c) {
      offer(e);
      added = true;
    }
    return added;
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

  public Object[] toArray() {
    synchronized (lock) {
      java.util.ArrayList<E> list = new java.util.ArrayList<E>();
      for (Node<E> p = head.next; p != null; p = p.next) {
        list.add(p.item);
      }
      return list.toArray();
    }
  }

  public <T> T[] toArray(T[] a) {
    synchronized (lock) {
      java.util.ArrayList<E> list = new java.util.ArrayList<E>();
      for (Node<E> p = head.next; p != null; p = p.next) {
        list.add(p.item);
      }
      return list.toArray(a);
    }
  }

  public void clear() {
    synchronized (lock) {
      Node<E> p = head.next;
      while (p != null) {
        Node<E> n = p.next;
        p.item = null;
        p = n;
      }
      head.next = null;
      tail = head;
    }
  }

  public Iterator<E> iterator() {
    return new Itr();
  }

  private class Itr implements Iterator<E> {
    private Node<E> nextNode;
    private E nextItem;
    private Node<E> lastRet;

    Itr() {
      synchronized (lock) {
        advance(head);
      }
    }

    private void advance(Node<E> from) {
      Node<E> p = from;
      for (;;) {
        p = (p.next == p) ? head : p.next;
        if (p == null) {
          nextNode = null;
          nextItem = null;
          return;
        }
        E item = p.item;
        if (item != null) {
          nextNode = p;
          nextItem = item;
          return;
        }
      }
    }

    public boolean hasNext() {
      return nextNode != null;
    }

    public E next() {
      synchronized (lock) {
        Node<E> p = nextNode;
        if (p == null) {
          throw new NoSuchElementException();
        }
        E x = nextItem;
        lastRet = p;
        advance(p);
        return x;
      }
    }

    public void remove() {
      Node<E> l = lastRet;
      if (l == null) {
        throw new IllegalStateException();
      }
      lastRet = null;
      synchronized (lock) {
        for (Node<E> pred = head, p = pred.next; p != null; pred = p, p = p.next) {
          if (p == l) {
            unlink(p, pred);
            break;
          }
        }
      }
    }
  }

  public Spliterator<E> spliterator() {
    return Spliterators.spliterator(this, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.CONCURRENT);
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

  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    for (Iterator<E> it = iterator(); it.hasNext();) {
      action.accept(it.next());
    }
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    synchronized (lock) {
      for (Node<E> p = head.next; p != null; p = p.next) {
        if (p.item != null) {
          s.writeObject(p.item);
        }
      }
    }
    s.writeObject(null);
  }

  @SuppressWarnings("unchecked")
  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    lock = new Object();
    head = tail = new Node<E>(null);
    for (Object item; (item = s.readObject()) != null;) {
      offer((E) item);
    }
  }
}
