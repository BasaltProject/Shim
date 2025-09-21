
package java.util;

public class LinkedHashSet<E> extends HashSet<E> implements SequencedSet<E>, Cloneable, java.io.Serializable {
  public LinkedHashSet(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor, true);
  }

  public LinkedHashSet(int initialCapacity) {
    super(initialCapacity, .75f, true);
  }

  public LinkedHashSet() {
    super(16, .75f, true);
  }

  public LinkedHashSet(Collection<? extends E> c) {
    super(Math.max(2 * c.size(), 11), .75f, true);
    addAll(c);
  }

  public static <T> LinkedHashSet<T> newLinkedHashSet(int numElements) {
    if (numElements < 0) {
      throw new IllegalArgumentException("Negative number of elements: " + numElements);
    }
    return new LinkedHashSet<>(HashMap.calculateHashMapCapacity(numElements));
  }

  private LinkedHashMap<E, Object> linked() {
    return (LinkedHashMap<E, Object>) map;
  }

  public void addFirst(E e) {
    linked().putFirst(e, PRESENT);
  }

  public void addLast(E e) {
    linked().putLast(e, PRESENT);
  }

  public E getFirst() {
    return linked().sequencedKeySet().getFirst();
  }

  public E getLast() {
    return linked().sequencedKeySet().getLast();
  }

  public E removeFirst() {
    return linked().sequencedKeySet().removeFirst();
  }

  public E removeLast() {
    return linked().sequencedKeySet().removeLast();
  }

  public SequencedSet<E> reversed() {
    return new ReversedView();
  }

  private final class ReversedView extends AbstractSet<E> implements SequencedSet<E> {
    public int size() {
      return LinkedHashSet.this.size();
    }

    public boolean isEmpty() {
      return LinkedHashSet.this.isEmpty();
    }

    public boolean contains(Object o) {
      return LinkedHashSet.this.contains(o);
    }

    public boolean add(E e) {
      LinkedHashSet.this.addFirst(e);
      return true;
    }

    public void addFirst(E e) {
      LinkedHashSet.this.addLast(e);
    }

    public void addLast(E e) {
      LinkedHashSet.this.addFirst(e);
    }

    public E getFirst() {
      return LinkedHashSet.this.getLast();
    }

    public E getLast() {
      return LinkedHashSet.this.getFirst();
    }

    public E removeFirst() {
      return LinkedHashSet.this.removeLast();
    }

    public E removeLast() {
      return LinkedHashSet.this.removeFirst();
    }

    public boolean remove(Object o) {
      return LinkedHashSet.this.remove(o);
    }

    public void clear() {
      LinkedHashSet.this.clear();
    }

    public SequencedSet<E> reversed() {
      return LinkedHashSet.this;
    }

    public Iterator<E> iterator() {
      return linked().reversed().keySet().iterator();
    }
  }
}
