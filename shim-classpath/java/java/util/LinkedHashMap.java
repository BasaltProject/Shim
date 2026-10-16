
package java.util;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LinkedHashMap<K, V> extends HashMap<K, V> implements SequencedMap<K, V> {
  private static final long serialVersionUID = 3801124242820219131L;

  static class Entry<K, V> extends HashMap.Node<K, V> {
    Entry<K, V> before, after;

    Entry(int hash, K key, V value, Node<K, V> next) {
      super(hash, key, value, next);
    }
  }

  transient LinkedHashMap.Entry<K, V> head;
  transient LinkedHashMap.Entry<K, V> tail;
  final boolean accessOrder;

  private void linkNodeLast(LinkedHashMap.Entry<K, V> p) {
    LinkedHashMap.Entry<K, V> last = tail;
    tail = p;
    if (last == null) {
      head = p;
    } else {
      p.before = last;
      last.after = p;
    }
  }

  private void linkNodeFirst(LinkedHashMap.Entry<K, V> p) {
    LinkedHashMap.Entry<K, V> first = head;
    head = p;
    if (first == null) {
      tail = p;
    } else {
      p.after = first;
      first.before = p;
    }
  }

  private void unlink(LinkedHashMap.Entry<K, V> p) {
    LinkedHashMap.Entry<K, V> b = p.before;
    LinkedHashMap.Entry<K, V> a = p.after;
    p.before = p.after = null;
    if (b == null) {
      head = a;
    } else {
      b.after = a;
    }
    if (a == null) {
      tail = b;
    } else {
      a.before = b;
    }
  }

  Node<K, V> newNode(int hash, K key, V value, Node<K, V> e) {
    LinkedHashMap.Entry<K, V> p = new LinkedHashMap.Entry<>(hash, key, value, e);
    linkNodeLast(p);
    return p;
  }

  void reinitialize() {
    super.reinitialize();
    head = tail = null;
  }

  void afterNodeRemoval(Node<K, V> e) {
    unlink((LinkedHashMap.Entry<K, V>) e);
  }

  void afterNodeInsertion(boolean evict) {
    LinkedHashMap.Entry<K, V> first;
    if (evict && (first = head) != null && removeEldestEntry(first)) {
      K key = first.key;
      removeNode(hash(key), key, null, false, true);
    }
  }

  void afterNodeAccess(Node<K, V> e) {
    LinkedHashMap.Entry<K, V> last;
    if (accessOrder && (last = tail) != e) {
      LinkedHashMap.Entry<K, V> p = (LinkedHashMap.Entry<K, V>) e;
      LinkedHashMap.Entry<K, V> b = p.before;
      LinkedHashMap.Entry<K, V> a = p.after;
      p.after = null;
      if (b == null) {
        head = a;
      } else {
        b.after = a;
      }
      if (a != null) {
        a.before = b;
      } else {
        last = b;
      }
      if (last == null) {
        head = p;
      } else {
        p.before = last;
        last.after = p;
      }
      tail = p;
      ++modCount;
    }
  }

  void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
    for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
      s.writeObject(e.key);
      s.writeObject(e.value);
    }
  }

  public LinkedHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    accessOrder = false;
  }

  public LinkedHashMap(int initialCapacity) {
    super(initialCapacity);
    accessOrder = false;
  }

  public LinkedHashMap() {
    super();
    accessOrder = false;
  }

  public LinkedHashMap(Map<? extends K, ? extends V> m) {
    super();
    accessOrder = false;
    putMapEntries(m, false);
  }

  public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
    super(initialCapacity, loadFactor);
    this.accessOrder = accessOrder;
  }

  public boolean containsValue(Object value) {
    for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
      V v = e.value;
      if (v == value || (value != null && value.equals(v))) {
        return true;
      }
    }
    return false;
  }

  public V get(Object key) {
    Node<K, V> e;
    if ((e = getNode(key)) == null) {
      return null;
    }
    if (accessOrder) {
      afterNodeAccess(e);
    }
    return e.value;
  }

  public V getOrDefault(Object key, V defaultValue) {
    Node<K, V> e;
    if ((e = getNode(key)) == null) {
      return defaultValue;
    }
    if (accessOrder) {
      afterNodeAccess(e);
    }
    return e.value;
  }

  public void clear() {
    super.clear();
    head = tail = null;
  }

  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return false;
  }

  public Set<K> keySet() {
    Set<K> ks = keySet;
    if (ks == null) {
      ks = new LinkedKeySet();
      keySet = ks;
    }
    return ks;
  }

  final class LinkedKeySet extends AbstractSet<K> {
    public final int size() {
      return size;
    }

    public final void clear() {
      LinkedHashMap.this.clear();
    }

    public final Iterator<K> iterator() {
      return new LinkedKeyIterator();
    }

    public final boolean contains(Object o) {
      return containsKey(o);
    }

    public final boolean remove(Object key) {
      return removeNode(hash(key), key, null, false, true) != null;
    }

    public final void forEach(Consumer<? super K> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      int mc = modCount;
      for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
        action.accept(e.key);
      }
      if (modCount != mc) {
        throw new ConcurrentModificationException();
      }
    }
  }

  public Collection<V> values() {
    Collection<V> vs = values;
    if (vs == null) {
      vs = new LinkedValues();
      values = vs;
    }
    return vs;
  }

  final class LinkedValues extends AbstractCollection<V> {
    public final int size() {
      return size;
    }

    public final void clear() {
      LinkedHashMap.this.clear();
    }

    public final Iterator<V> iterator() {
      return new LinkedValueIterator();
    }

    public final boolean contains(Object o) {
      return containsValue(o);
    }

    public final void forEach(Consumer<? super V> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      int mc = modCount;
      for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
        action.accept(e.value);
      }
      if (modCount != mc) {
        throw new ConcurrentModificationException();
      }
    }
  }

  public Set<Map.Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> es;
    return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
  }

  final class LinkedEntrySet extends AbstractSet<Map.Entry<K, V>> {
    public final int size() {
      return size;
    }

    public final void clear() {
      LinkedHashMap.this.clear();
    }

    public final Iterator<Map.Entry<K, V>> iterator() {
      return new LinkedEntryIterator();
    }

    public final boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      Object key = e.getKey();
      Node<K, V> candidate = getNode(key);
      return candidate != null && candidate.equals(e);
    }

    public final boolean remove(Object o) {
      if (o instanceof Map.Entry) {
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        Object key = e.getKey();
        Object value = e.getValue();
        return removeNode(hash(key), key, value, true, true) != null;
      }
      return false;
    }

    public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
      if (action == null) {
        throw new NullPointerException();
      }
      int mc = modCount;
      for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
        action.accept(e);
      }
      if (modCount != mc) {
        throw new ConcurrentModificationException();
      }
    }
  }

  public void forEach(BiConsumer<? super K, ? super V> action) {
    if (action == null) {
      throw new NullPointerException();
    }
    int mc = modCount;
    for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
      action.accept(e.key, e.value);
    }
    if (modCount != mc) {
      throw new ConcurrentModificationException();
    }
  }

  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    if (function == null) {
      throw new NullPointerException();
    }
    int mc = modCount;
    for (LinkedHashMap.Entry<K, V> e = head; e != null; e = e.after) {
      e.value = function.apply(e.key, e.value);
    }
    if (modCount != mc) {
      throw new ConcurrentModificationException();
    }
  }

  abstract class LinkedHashIterator {
    LinkedHashMap.Entry<K, V> next;
    LinkedHashMap.Entry<K, V> current;
    int expectedModCount;

    LinkedHashIterator() {
      next = head;
      expectedModCount = modCount;
      current = null;
    }

    public final boolean hasNext() {
      return next != null;
    }

    final LinkedHashMap.Entry<K, V> nextNode() {
      LinkedHashMap.Entry<K, V> e = next;
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (e == null) {
        throw new NoSuchElementException();
      }
      current = e;
      next = e.after;
      return e;
    }

    public final void remove() {
      Node<K, V> p = current;
      if (p == null) {
        throw new IllegalStateException();
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      current = null;
      removeNode(p.hash, p.key, null, false, false);
      expectedModCount = modCount;
    }
  }

  final class LinkedKeyIterator extends LinkedHashIterator implements Iterator<K> {
    public final K next() {
      return nextNode().getKey();
    }
  }

  final class LinkedValueIterator extends LinkedHashIterator implements Iterator<V> {
    public final V next() {
      return nextNode().value;
    }
  }

  final class LinkedEntryIterator extends LinkedHashIterator implements Iterator<Map.Entry<K, V>> {
    public final Map.Entry<K, V> next() {
      return nextNode();
    }
  }

  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int numMappings) {
    if (numMappings < 0) {
      throw new IllegalArgumentException("Negative number of mappings: " + numMappings);
    }
    return new LinkedHashMap<>(HashMap.calculateHashMapCapacity(numMappings));
  }

  private static <K, V> Map.Entry<K, V> snapshot(LinkedHashMap.Entry<K, V> e) {
    return e == null ? null : new NullableKeyValueHolder<K, V>(e.key, e.value);
  }

  private static final class NullableKeyValueHolder<K, V> implements Map.Entry<K, V> {
    private final K key;
    private final V value;

    NullableKeyValueHolder(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public V setValue(V value) {
      throw new UnsupportedOperationException("not supported");
    }

    public boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
    }

    public int hashCode() {
      return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    public String toString() {
      return key + "=" + value;
    }
  }

  public Map.Entry<K, V> firstEntry() {
    return snapshot(head);
  }

  public Map.Entry<K, V> lastEntry() {
    return snapshot(tail);
  }

  public Map.Entry<K, V> pollFirstEntry() {
    LinkedHashMap.Entry<K, V> e = head;
    if (e == null) {
      return null;
    }
    Map.Entry<K, V> result = snapshot(e);
    removeNode(e.hash, e.key, null, false, false);
    return result;
  }

  public Map.Entry<K, V> pollLastEntry() {
    LinkedHashMap.Entry<K, V> e = tail;
    if (e == null) {
      return null;
    }
    Map.Entry<K, V> result = snapshot(e);
    removeNode(e.hash, e.key, null, false, false);
    return result;
  }

  public V putFirst(K k, V v) {
    Node<K, V> existing = getNode(k);
    if (existing == null) {
      V old = put(k, v);
      moveToFirst((LinkedHashMap.Entry<K, V>) getNode(k), false);
      return old;
    }
    V old = existing.value;
    existing.value = v;
    moveToFirst((LinkedHashMap.Entry<K, V>) existing, true);
    return old;
  }

  public V putLast(K k, V v) {
    Node<K, V> existing = getNode(k);
    if (existing == null) {
      return put(k, v);
    }
    V old = existing.value;
    existing.value = v;
    moveToLast((LinkedHashMap.Entry<K, V>) existing);
    return old;
  }

  private void moveToFirst(LinkedHashMap.Entry<K, V> p, boolean counted) {
    if (head != p) {
      unlink(p);
      linkNodeFirst(p);
      if (counted) {
        ++modCount;
      }
    } else if (counted) {
      ++modCount;
    }
  }

  private void moveToLast(LinkedHashMap.Entry<K, V> p) {
    if (tail != p) {
      unlink(p);
      linkNodeLast(p);
    }
    ++modCount;
  }

  public SequencedMap<K, V> reversed() {
    return new ReversedView();
  }

  private final class ReversedView extends AbstractMap<K, V> implements SequencedMap<K, V> {
    public int size() {
      return LinkedHashMap.this.size();
    }

    public boolean containsKey(Object key) {
      return LinkedHashMap.this.containsKey(key);
    }

    public boolean containsValue(Object value) {
      return LinkedHashMap.this.containsValue(value);
    }

    public V get(Object key) {
      return LinkedHashMap.this.get(key);
    }

    public V put(K key, V value) {
      return LinkedHashMap.this.put(key, value);
    }

    public V remove(Object key) {
      return LinkedHashMap.this.remove(key);
    }

    public void clear() {
      LinkedHashMap.this.clear();
    }

    public SequencedMap<K, V> reversed() {
      return LinkedHashMap.this;
    }

    public Map.Entry<K, V> firstEntry() {
      return LinkedHashMap.this.lastEntry();
    }

    public Map.Entry<K, V> lastEntry() {
      return LinkedHashMap.this.firstEntry();
    }

    public Map.Entry<K, V> pollFirstEntry() {
      return LinkedHashMap.this.pollLastEntry();
    }

    public Map.Entry<K, V> pollLastEntry() {
      return LinkedHashMap.this.pollFirstEntry();
    }

    public V putFirst(K key, V value) {
      return LinkedHashMap.this.putLast(key, value);
    }

    public V putLast(K key, V value) {
      return LinkedHashMap.this.putFirst(key, value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
      return new AbstractSet<Map.Entry<K, V>>() {
        public int size() {
          return LinkedHashMap.this.size();
        }

        public Iterator<Map.Entry<K, V>> iterator() {
          return new ReversedIterator<Map.Entry<K, V>>() {
            Map.Entry<K, V> map(LinkedHashMap.Entry<K, V> e) {
              return e;
            }
          };
        }
      };
    }

    public Set<K> keySet() {
      return new AbstractSet<K>() {
        public int size() {
          return LinkedHashMap.this.size();
        }

        public boolean contains(Object o) {
          return LinkedHashMap.this.containsKey(o);
        }

        public Iterator<K> iterator() {
          return new ReversedIterator<K>() {
            K map(LinkedHashMap.Entry<K, V> e) {
              return e.key;
            }
          };
        }
      };
    }

    public Collection<V> values() {
      return new AbstractCollection<V>() {
        public int size() {
          return LinkedHashMap.this.size();
        }

        public Iterator<V> iterator() {
          return new ReversedIterator<V>() {
            V map(LinkedHashMap.Entry<K, V> e) {
              return e.value;
            }
          };
        }
      };
    }
  }

  private abstract class ReversedIterator<T> implements Iterator<T> {
    LinkedHashMap.Entry<K, V> next = tail;
    LinkedHashMap.Entry<K, V> current;
    int expectedModCount = modCount;

    abstract T map(LinkedHashMap.Entry<K, V> e);

    public boolean hasNext() {
      return next != null;
    }

    public T next() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (next == null) {
        throw new NoSuchElementException();
      }
      current = next;
      next = next.before;
      return map(current);
    }

    public void remove() {
      if (current == null) {
        throw new IllegalStateException();
      }
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      removeNode(current.hash, current.key, null, false, false);
      current = null;
      expectedModCount = modCount;
    }
  }
}
