/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 17
 */

import java.util.*;

public class CollectionsAlgorithmsModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      Object value = action.run();
      System.out.println(label + ":" + (value instanceof Object[] ? Arrays.toString((Object[]) value) : value));
    } catch (Throwable error) {
      boolean generated = error instanceof NullPointerException || error instanceof ArrayStoreException;
      System.out.println(label + ":" + error.getClass().getName() + (generated ? "" : ":" + error.getMessage()));
    }
  }

  static List<Integer> numbers(int... values) {
    List<Integer> list = new ArrayList<>();
    for (int v : values) list.add(v);
    return list;
  }

  static List<Integer> linked(int... values) {
    return new LinkedList<>(numbers(values));
  }

  record Item(String name, int rank) {
    public String toString() { return name + rank; }
  }

  public static void main(String[] args) {
    probe("sort", () -> { List<Integer> l = numbers(5, 3, 9, 1); Collections.sort(l); return l; });
    probe("sort-cmp", () -> { List<Integer> l = numbers(5, 3, 9, 1); Collections.sort(l, Collections.reverseOrder()); return l; });
    probe("sort-stable", () -> {
      List<Item> l = new ArrayList<>();
      for (int i = 0; i < 40; ++i) l.add(new Item("n" + i, i % 3));
      Collections.sort(l, Comparator.comparingInt(Item::rank));
      return l.subList(0, 8) + "," + l.subList(14, 18);
    });
    probe("sort-linked", () -> { List<Integer> l = linked(4, 2, 8, 6); Collections.sort(l); return l; });
    probe("sort-unmodifiable", () -> { Collections.sort(Collections.unmodifiableList(numbers(2, 1))); return 0; });
    probe("sort-immutable", () -> { Collections.sort(List.of(2, 1)); return 0; });
    probe("shuffle-seeded", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5, 6, 7, 8, 9, 10); Collections.shuffle(l, new Random(42)); return l; });
    probe("shuffle-seeded-linked", () -> { List<Integer> l = linked(1, 2, 3, 4, 5, 6, 7, 8, 9, 10); Collections.shuffle(l, new Random(7)); return l; });
    probe("shuffle-small", () -> { List<Integer> l = numbers(1, 2, 3); Collections.shuffle(l, new Random(1)); return l; });
    probe("shuffle-sublist", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5, 6); Collections.shuffle(l.subList(1, 5), new Random(3)); return l; });
    probe("shuffle-unmodifiable-same", () -> { List<Integer> l = Collections.unmodifiableList(numbers(1)); Collections.shuffle(l, new Random(1)); return l; });
    probe("reverse", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5); Collections.reverse(l); return l; });
    probe("reverse-linked-large", () -> { List<Integer> l = new LinkedList<>(); for (int i = 0; i < 40; ++i) l.add(i); Collections.reverse(l); return l.subList(0, 5); });
    probe("swap", () -> { List<Integer> l = numbers(1, 2, 3); Collections.swap(l, 0, 2); return l; });
    probe("swap-bad", () -> { Collections.swap(numbers(1), 0, 3); return 0; });
    probe("rotate", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5); Collections.rotate(l, 2); return l; });
    probe("rotate-negative", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5); Collections.rotate(l, -7); return l; });
    probe("rotate-empty", () -> { List<Integer> l = numbers(); Collections.rotate(l, 3); return l; });
    probe("rotate-linked", () -> { List<Integer> l = linked(1, 2, 3, 4); Collections.rotate(l, 1); return l; });
    probe("fill", () -> { List<Integer> l = numbers(1, 2, 3); Collections.fill(l, 0); return l; });
    probe("copy", () -> { List<Integer> dest = numbers(9, 9, 9, 9); Collections.copy(dest, numbers(1, 2)); return dest; });
    probe("copy-too-small", () -> { Collections.copy(numbers(9), numbers(1, 2)); return 0; });
    probe("n-copies", () -> { List<String> l = Collections.nCopies(3, "ab"); return l + ":" + l.size() + ":" + l.contains("ab") + ":" + l.indexOf("ab") + ":" + l.lastIndexOf("ab")
        + ":" + l.subList(1, 3) + ":" + l.equals(List.of("ab", "ab", "ab")) + ":" + l.hashCode(); });
    probe("n-copies-negative", () -> Collections.nCopies(-1, "x"));
    probe("n-copies-immutable", () -> { Collections.nCopies(2, "x").add("y"); return 0; });
    probe("n-copies-get", () -> Collections.nCopies(2, "x").get(2));
    probe("min-max", () -> { List<Integer> l = numbers(4, 9, 2, 7); return Collections.min(l) + "," + Collections.max(l) + "," + Collections.min(l, Collections.reverseOrder()) + "," + Collections.max(l, null); });
    probe("min-empty", () -> Collections.min(new ArrayList<Integer>()));
    probe("frequency", () -> { List<String> l = new ArrayList<>(Arrays.asList("a", null, "a", null, "b")); return Collections.frequency(l, "a") + "," + Collections.frequency(l, null) + "," + Collections.frequency(l, "z"); });
    probe("disjoint", () -> Collections.disjoint(List.of(1, 2), List.of(3, 4)) + "," + Collections.disjoint(List.of(1, 2), Set.of(2)) + "," + Collections.disjoint(Set.of(1), List.of()));
    probe("binary-search", () -> { List<Integer> l = numbers(1, 3, 5, 7, 9); return Collections.binarySearch(l, 7) + "," + Collections.binarySearch(l, 4) + "," + Collections.binarySearch(l, 100) + "," + Collections.binarySearch(l, 0) + "," + Collections.binarySearch(l, 1); });
    probe("binary-search-cmp", () -> { List<Integer> l = numbers(9, 7, 5, 3, 1); return Collections.binarySearch(l, 5, Collections.reverseOrder()) + "," + Collections.binarySearch(l, 4, Collections.reverseOrder()) + "," + Collections.binarySearch(l, 4, null); });
    probe("binary-search-linked", () -> Collections.binarySearch(linked(1, 3, 5), 5));
    probe("index-of-sublist", () -> { List<Integer> l = numbers(1, 2, 3, 2, 3, 4); return Collections.indexOfSubList(l, List.of(2, 3)) + "," + Collections.lastIndexOfSubList(l, List.of(2, 3)) + "," + Collections.indexOfSubList(l, List.of(9)) + "," + Collections.indexOfSubList(l, List.of()) + "," + Collections.lastIndexOfSubList(l, List.of()); });
    probe("replace-all", () -> { List<Integer> l = numbers(1, 2, 1, 3); boolean r = Collections.replaceAll(l, 1, 9); return r + ":" + l + ":" + Collections.replaceAll(l, 7, 0); });
    probe("replace-all-null", () -> { List<Integer> l = new ArrayList<>(Arrays.asList(1, null, null)); boolean r = Collections.replaceAll(l, null, 0); return r + ":" + l; });
    probe("add-all", () -> { List<String> l = new ArrayList<>(); boolean r = Collections.addAll(l, "a", "b", "c"); return r + ":" + l + ":" + Collections.addAll(l); });
    probe("add-all-set-dup", () -> { Set<Integer> s = new HashSet<>(); Collections.addAll(s, 1, 1, 2); return s.size() + ":" + Collections.addAll(s, 1); });
    probe("empty-identity", () -> (Collections.emptyList() == Collections.emptyList()) + "," + (Collections.emptySet() == Collections.emptySet()) + "," + (Collections.emptyMap() == Collections.emptyMap())
        + "," + (Collections.EMPTY_SET == Collections.emptySet()) + "," + (Collections.EMPTY_MAP == Collections.emptyMap()) + "," + (Collections.EMPTY_LIST == Collections.emptyList()));
    probe("empty-behaviour", () -> Collections.emptyList() + "," + Collections.emptySet() + "," + Collections.emptyMap() + "," + Collections.emptyList().hashCode() + "," + Collections.emptySet().hashCode()
        + "," + Collections.emptyMap().hashCode() + "," + Collections.emptyList().equals(List.of()) + "," + Collections.emptySet().equals(Set.of()) + "," + Collections.emptyMap().equals(Map.of()));
    probe("empty-add", () -> Collections.emptyList().add(null));
    probe("empty-set-add", () -> Collections.emptySet().add(null));
    probe("empty-map-put", () -> Collections.emptyMap().put(null, null));
    probe("empty-get", () -> Collections.emptyList().get(0));
    probe("empty-iterator", () -> Collections.emptyIterator().hasNext() + "," + Collections.emptyListIterator().hasPrevious() + "," + Collections.emptyEnumeration().hasMoreElements());
    probe("singleton", () -> Collections.singletonList(5) + "," + Collections.singleton(6) + "," + Collections.singletonMap("k", 7));
    probe("singleton-add", () -> Collections.singletonList(5).add(1));
    probe("singleton-set", () -> Collections.singletonList(5).set(0, 1));
    probe("singleton-remove", () -> Collections.singleton(5).remove(5));
    probe("unmodifiable-list", () -> { List<Integer> base = numbers(1, 2); List<Integer> u = Collections.unmodifiableList(base); base.add(3); return u + ":" + u.size(); });
    probe("unmodifiable-add", () -> Collections.unmodifiableList(numbers(1)).add(2));
    probe("unmodifiable-iterator-remove", () -> { Iterator<Integer> it = Collections.unmodifiableList(numbers(1)).iterator(); it.next(); it.remove(); return 0; });
    probe("unmodifiable-sublist-set", () -> Collections.unmodifiableList(numbers(1, 2, 3)).subList(0, 2).set(0, 9));
    probe("unmodifiable-set", () -> { Collections.unmodifiableSet(new HashSet<>(Set.of(1))).clear(); return 0; });
    probe("unmodifiable-map", () -> Collections.unmodifiableMap(new HashMap<>(Map.of(1, 2))).put(3, 4));
    probe("unmodifiable-map-entry", () -> { Map<Integer, Integer> m = Collections.unmodifiableMap(new HashMap<>(Map.of(1, 2))); m.entrySet().iterator().next().setValue(9); return 0; });
    probe("unmodifiable-equals", () -> Collections.unmodifiableList(numbers(1, 2)).equals(numbers(1, 2)) + "," + Collections.unmodifiableList(numbers(1, 2)).hashCode());
    probe("unmodifiable-collection-to-string", () -> Collections.unmodifiableCollection(numbers(1, 2)).toString());
    probe("unmodifiable-sorted-set", () -> { SortedSet<Integer> s = Collections.unmodifiableSortedSet(new TreeSet<>(Set.of(3, 1, 2))); return s + ":" + s.first() + ":" + s.headSet(3); });
    probe("unmodifiable-navigable-map", () -> { NavigableMap<Integer, String> m = Collections.unmodifiableNavigableMap(new TreeMap<>(Map.of(1, "a", 2, "b"))); return m + ":" + m.firstKey() + ":" + m.descendingMap(); });
    probe("unmodifiable-navigable-map-poll", () -> Collections.unmodifiableNavigableMap(new TreeMap<>(Map.of(1, "a"))).pollFirstEntry());
    probe("reverse-order", () -> { List<String> l = new ArrayList<>(List.of("b", "c", "a")); l.sort(Collections.reverseOrder()); return l + ":" + Collections.reverseOrder(Collections.reverseOrder()).compare(1, 2); });
    probe("reverse-order-cmp", () -> { List<String> l = new ArrayList<>(List.of("bb", "c", "aaa")); l.sort(Collections.reverseOrder(Comparator.comparingInt(String::length))); return l; });
    probe("enumeration-list", () -> Collections.list(Collections.enumeration(numbers(1, 2, 3))));
    probe("as-lifo-queue", () -> { Queue<Integer> q = Collections.asLifoQueue(new ArrayDeque<>()); q.add(1); q.add(2); return q.poll() + "," + q.peek(); });
    probe("new-set-from-map", () -> { Set<String> s = Collections.newSetFromMap(new HashMap<>()); s.add("a"); s.add("a"); return s + ":" + s.size() + ":" + s.contains("a"); });

    probe("sync-collection", () -> { Collection<Integer> c = Collections.synchronizedCollection(numbers(1, 2)); c.add(3); return c + ":" + c.size() + ":" + c.contains(2) + ":" + c.getClass().getSimpleName().isEmpty(); });
    probe("sync-list", () -> { List<Integer> l = Collections.synchronizedList(numbers(1, 2, 3)); l.add(0, 9); l.sort(null); return l + ":" + l.indexOf(2) + ":" + l.subList(1, 3) + ":" + (l instanceof RandomAccess) + ":" + l.equals(numbers(1, 2, 3, 9)); });
    probe("sync-list-linked", () -> (Collections.synchronizedList(linked(1)) instanceof RandomAccess));
    probe("sync-set", () -> { Set<Integer> s = Collections.synchronizedSet(new TreeSet<>(Set.of(2, 1))); s.add(3); return s + ":" + s.contains(1) + ":" + s.equals(Set.of(1, 2, 3)) + ":" + s.hashCode(); });
    probe("sync-map", () -> { Map<String, Integer> m = Collections.synchronizedMap(new TreeMap<>()); m.put("a", 1); m.merge("a", 5, Integer::sum); m.computeIfAbsent("b", k -> 2); return m + ":" + m.keySet() + ":" + m.values() + ":" + m.getOrDefault("z", -1); });
    probe("sync-sorted-set", () -> { SortedSet<Integer> s = Collections.synchronizedSortedSet(new TreeSet<>(Set.of(5, 1, 3))); return s.first() + "," + s.last() + "," + s.headSet(3) + "," + s.tailSet(3) + "," + s.subSet(1, 5); });
    probe("sync-navigable-set", () -> { NavigableSet<Integer> s = Collections.synchronizedNavigableSet(new TreeSet<>(Set.of(5, 1, 3))); return s.lower(3) + "," + s.ceiling(4) + "," + s.descendingSet() + "," + s.pollFirst() + "," + s; });
    probe("sync-sorted-map", () -> { SortedMap<Integer, String> m = Collections.synchronizedSortedMap(new TreeMap<>(Map.of(1, "a", 2, "b", 3, "c"))); return m.firstKey() + "," + m.lastKey() + "," + m.headMap(3) + "," + m.tailMap(2) + "," + m.subMap(1, 3); });
    probe("sync-navigable-map", () -> { NavigableMap<Integer, String> m = Collections.synchronizedNavigableMap(new TreeMap<>(Map.of(1, "a", 2, "b", 3, "c"))); return m.floorKey(2) + "," + m.higherEntry(2) + "," + m.descendingMap() + "," + m.pollFirstEntry() + "," + m; });
    probe("sync-null", () -> Collections.synchronizedList(null));
    probe("sync-lock-is-wrapper", () -> {
      List<Integer> l = Collections.synchronizedList(numbers(1));
      final boolean[] blocked = new boolean[1];
      Thread t = new Thread(() -> { synchronized (l) { try { Thread.sleep(300); } catch (InterruptedException e) { } } });
      t.start();
      try { Thread.sleep(100); } catch (InterruptedException e) { }
      long start = System.nanoTime();
      l.add(2);
      blocked[0] = System.nanoTime() - start > 100_000_000L;
      try { t.join(); } catch (InterruptedException e) { }
      return blocked[0];
    });

    probe("checked-list", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) List<Integer> l = Collections.checkedList(new ArrayList<>(), Integer.class); ((List) l).add("s"); return l; });
    probe("checked-list-ok", () -> { List<Integer> l = Collections.checkedList(new ArrayList<>(), Integer.class); l.add(1); l.add(0, 2); l.set(0, 3); return l + ":" + l.subList(0, 1) + ":" + l.contains(1) + ":" + (l instanceof RandomAccess); });
    probe("checked-list-add-all", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) List<Integer> l = Collections.checkedList(numbers(1), Integer.class); ((List) l).addAll(List.of(2, "x")); return l; });
    probe("checked-list-iterator", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) List<Integer> l = Collections.checkedList(numbers(1), Integer.class); ListIterator it = ((List) l).listIterator(); it.next(); it.set("bad"); return l; });
    probe("checked-set", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Set<String> s = Collections.checkedSet(new HashSet<>(), String.class); ((Set) s).add(1); return s; });
    probe("checked-collection", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Collection<String> s = Collections.checkedCollection(new ArrayList<>(), String.class); s.add("a"); ((Collection) s).add(2.5); return s; });
    probe("checked-queue", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Queue<String> q = Collections.checkedQueue(new ArrayDeque<>(), String.class); q.offer("a"); ((Queue) q).offer(1); return q; });
    probe("checked-map", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Map<String, Integer> m = Collections.checkedMap(new HashMap<>(), String.class, Integer.class); m.put("a", 1); ((Map) m).put("b", "x"); return m; });
    probe("checked-map-key", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Map<String, Integer> m = Collections.checkedMap(new HashMap<>(), String.class, Integer.class); ((Map) m).put(1, 1); return m; });
    probe("checked-map-entry", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Map<String, Integer> m = Collections.checkedMap(new HashMap<>(Map.of("a", 1)), String.class, Integer.class); ((Map.Entry) m.entrySet().iterator().next()).setValue("bad"); return m; });
    probe("checked-map-put-all", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) Map<String, Integer> m = Collections.checkedMap(new HashMap<>(), String.class, Integer.class); Map bad = new HashMap(); bad.put("ok", 1); bad.put("no", "x"); ((Map) m).putAll(bad); return m; });
    probe("checked-sorted-set", () -> { @SuppressWarnings({"rawtypes", "unchecked"}) SortedSet<Integer> s = Collections.checkedSortedSet(new TreeSet<>(), Integer.class); s.add(2); s.add(1); return s + ":" + s.first() + ":" + s.headSet(2); });
    probe("checked-navigable-set", () -> { NavigableSet<Integer> s = Collections.checkedNavigableSet(new TreeSet<>(Set.of(3, 1)), Integer.class); return s.lower(3) + ":" + s.descendingSet(); });
    probe("checked-sorted-map", () -> { SortedMap<Integer, String> m = Collections.checkedSortedMap(new TreeMap<>(Map.of(2, "b", 1, "a")), Integer.class, String.class); return m.firstKey() + ":" + m.headMap(2); });
    probe("checked-navigable-map", () -> { NavigableMap<Integer, String> m = Collections.checkedNavigableMap(new TreeMap<>(Map.of(2, "b", 1, "a")), Integer.class, String.class); return m.floorKey(5) + ":" + m.descendingMap(); });
    probe("checked-null-type", () -> Collections.checkedList(new ArrayList<String>(), null));
    probe("checked-null-element", () -> { List<String> l = Collections.checkedList(new ArrayList<>(), String.class); l.add(null); return l; });
  }
}
