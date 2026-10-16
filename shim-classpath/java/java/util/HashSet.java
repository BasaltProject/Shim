
package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable {
  private static final long serialVersionUID = -5024744406713321676L;

  transient HashMap<E, Object> map;

  static final Object PRESENT = new Object();

  public HashSet() {
    map = new HashMap<>();
  }

  public HashSet(Collection<? extends E> c) {
    map = HashMap.newHashMap(Math.max(c.size(), 12));
    addAll(c);
  }

  public HashSet(int initialCapacity, float loadFactor) {
    map = new HashMap<>(initialCapacity, loadFactor);
  }

  public HashSet(int initialCapacity) {
    map = new HashMap<>(initialCapacity);
  }

  HashSet(int initialCapacity, float loadFactor, boolean dummy) {
    map = new LinkedHashMap<>(initialCapacity, loadFactor);
  }

  public Iterator<E> iterator() {
    return map.keySet().iterator();
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  public boolean add(E e) {
    return map.put(e, PRESENT) == null;
  }

  public boolean remove(Object o) {
    return map.remove(o) == PRESENT;
  }

  public void clear() {
    map.clear();
  }

  @SuppressWarnings("unchecked")
  public Object clone() {
    try {
      HashSet<E> newSet = (HashSet<E>) super.clone();
      newSet.map = (HashMap<E, Object>) map.clone();
      return newSet;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public Object[] toArray() {
    return map.keySet().toArray();
  }

  public <T> T[] toArray(T[] a) {
    return map.keySet().toArray(a);
  }

  public static <T> HashSet<T> newHashSet(int numElements) {
    if (numElements < 0) {
      throw new IllegalArgumentException("Negative number of elements: " + numElements);
    }
    return new HashSet<>(HashMap.calculateHashMapCapacity(numElements));
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeInt(map.capacity());
    s.writeFloat(map.loadFactor());
    s.writeInt(map.size());
    for (E e : map.keySet()) {
      s.writeObject(e);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    int capacity = s.readInt();
    if (capacity < 0) {
      throw new InvalidObjectException("Illegal capacity: " + capacity);
    }
    float loadFactor = s.readFloat();
    if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
      throw new InvalidObjectException("Illegal load factor: " + loadFactor);
    }
    int size = s.readInt();
    if (size < 0) {
      throw new InvalidObjectException("Illegal size: " + size);
    }
    map = (((HashSet<?>) this) instanceof LinkedHashSet ? new LinkedHashMap<E, Object>(capacity, loadFactor)
                                                       : new HashMap<E, Object>(capacity, loadFactor));
    for (int i = 0; i < size; i++) {
      E e = (E) s.readObject();
      map.put(e, PRESENT);
    }
  }
}
