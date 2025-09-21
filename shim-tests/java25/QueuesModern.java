/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.concurrent.*;

public class QueuesModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      System.out.println(label + ":" + action.run());
    } catch (Throwable error) {
      boolean generated = error instanceof NullPointerException || error instanceof ClassCastException;
      System.out.println(label + ":" + error.getClass().getName() + (generated ? "" : ":" + error.getMessage()));
    }
  }

  static <T> List<T> drain(Queue<T> q) {
    List<T> out = new ArrayList<>();
    while (!q.isEmpty()) out.add(q.poll());
    return out;
  }

  public static void main(String[] args) {
    int[] data = {9, 4, 7, 1, 8, 2, 6, 3, 5, 0, 11, 10};
    probe("pq-iteration-order", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); for (int x : data) q.add(x); return q; });
    probe("pq-drain", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); for (int x : data) q.add(x); return drain(q); });
    probe("pq-comparator", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.reverseOrder()); for (int x : data) q.offer(x); return q + ":" + q.peek() + ":" + q.size(); });
    probe("pq-from-collection", () -> new PriorityQueue<>(List.of(5, 3, 8, 1, 9, 2, 7)));
    probe("pq-from-sorted-set", () -> new PriorityQueue<>(new TreeSet<>(List.of(5, 3, 8, 1))));
    probe("pq-from-pq", () -> { PriorityQueue<Integer> src = new PriorityQueue<>(Comparator.<Integer>reverseOrder()); src.addAll(List.of(1, 4, 2, 9)); return new PriorityQueue<>(src); });
    probe("pq-remove-object", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(5, 3, 8, 1, 9, 2, 7)); boolean r = q.remove(3); return r + ":" + q + ":" + q.remove(42); });
    probe("pq-remove-then-drain", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); for (int x : data) q.add(x); q.remove(4); q.remove(9); return q + ":" + drain(q); });
    probe("pq-iterator-remove", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); for (int x : data) q.add(x); Iterator<Integer> it = q.iterator(); while (it.hasNext()) if (it.next() % 3 == 0) it.remove(); return q + ":" + drain(q); });
    probe("pq-contains-toarray", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(4, 2, 6)); return q.contains(6) + ":" + Arrays.toString(q.toArray()) + ":" + Arrays.toString(q.toArray(new Integer[0])); });
    probe("pq-null", () -> new PriorityQueue<Integer>().add(null));
    probe("pq-not-comparable", () -> { PriorityQueue<Object> q = new PriorityQueue<>(); q.add(new Object()); q.add(new Object()); return q; });
    probe("pq-bad-capacity", () -> new PriorityQueue<Integer>(0));
    probe("pq-empty", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); return q.poll() + ":" + q.peek() + ":" + q.size(); });
    probe("pq-element-remove-empty", () -> new PriorityQueue<Integer>().remove());
    probe("pq-element-empty", () -> new PriorityQueue<Integer>().element());
    probe("pq-cme", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(1, 2, 3)); for (int x : q) q.add(x + 10); return 0; });
    probe("pq-clear", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(1, 2, 3)); q.clear(); q.add(7); return q; });
    probe("pq-remove-if", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(5, 3, 8, 1, 9, 2, 7)); q.removeIf(x -> x % 2 == 1); return q; });
    probe("pq-remove-all", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(5, 3, 8, 1, 9, 2, 7)); q.removeAll(List.of(3, 9)); return q; });
    probe("pq-retain-all", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(List.of(5, 3, 8, 1, 9, 2, 7)); q.retainAll(List.of(3, 9, 8)); return q; });
    probe("pq-for-each", () -> { StringBuilder sb = new StringBuilder(); new PriorityQueue<>(List.of(5, 3, 8, 1)).forEach(sb::append); return sb; });
    probe("pq-strings", () -> { PriorityQueue<String> q = new PriorityQueue<>(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder())); q.addAll(List.of("ccc", "a", "bb", "dd", "e")); return drain(q); });
    probe("pq-large", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); Random r = new Random(5); for (int i = 0; i < 200; ++i) q.add(r.nextInt(1000)); List<Integer> d = drain(q); boolean sorted = true; for (int i = 1; i < d.size(); ++i) if (d.get(i - 1) > d.get(i)) sorted = false; return sorted + ":" + d.size() + ":" + d.hashCode(); });
    probe("pq-large-order", () -> { PriorityQueue<Integer> q = new PriorityQueue<>(); Random r = new Random(5); for (int i = 0; i < 40; ++i) q.add(r.nextInt(1000)); return q; });
    probe("pq-equals-hash", () -> new PriorityQueue<>(List.of(1)).equals(new PriorityQueue<>(List.of(1))));

    probe("deque-basic", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(); d.addFirst(1); d.addLast(2); d.offerFirst(0); d.push(-1); d.offer(3); return d + ":" + d.peekFirst() + ":" + d.peekLast() + ":" + d.size(); });
    probe("deque-pop-poll", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3, 4)); return d.pop() + ":" + d.pollLast() + ":" + d.removeFirst() + ":" + d.removeLast() + ":" + d.poll() + ":" + d.peek(); });
    probe("deque-empty-ops", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(); return d.pollFirst() + ":" + d.peekLast() + ":" + d.isEmpty(); });
    probe("deque-empty-remove", () -> new ArrayDeque<Integer>().removeFirst());
    probe("deque-empty-pop", () -> new ArrayDeque<Integer>().pop());
    probe("deque-empty-element", () -> new ArrayDeque<Integer>().getLast());
    probe("deque-null", () -> new ArrayDeque<Integer>().add(null));
    probe("deque-grow", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(2); for (int i = 0; i < 50; ++i) { if (i % 2 == 0) d.addFirst(i); else d.addLast(i); } return d; });
    probe("deque-descending", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3)); StringBuilder sb = new StringBuilder(); d.descendingIterator().forEachRemaining(sb::append); return sb; });
    probe("deque-remove-occurrence", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 1, 3, 1)); return d.removeFirstOccurrence(1) + ":" + d.removeLastOccurrence(1) + ":" + d + ":" + d.removeFirstOccurrence(9); });
    probe("deque-remove-object", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 1)); d.remove(1); return d; });
    probe("deque-contains", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2)); return d.contains(2) + ":" + d.contains(3) + ":" + d.contains(null); });
    probe("deque-iterator-remove", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3, 4, 5)); Iterator<Integer> it = d.iterator(); while (it.hasNext()) if (it.next() % 2 == 0) it.remove(); return d; });
    probe("deque-cme", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3)); for (int x : d) d.add(x); return 0; });
    probe("deque-reversed", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3)); Deque<Integer> r = d.reversed(); r.addFirst(9); return d + ":" + r + ":" + r.peekFirst() + ":" + r.pollLast(); });
    probe("deque-to-array", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(3, 1, 2)); return Arrays.toString(d.toArray()) + Arrays.toString(d.toArray(new Integer[5])); });
    probe("deque-clone", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2)); ArrayDeque<Integer> c = d.clone(); c.add(3); return d + ":" + c; });
    probe("deque-remove-if", () -> { ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3, 4)); d.removeIf(x -> x > 2); return d; });
    probe("deque-equals", () -> new ArrayDeque<>(List.of(1)).equals(new ArrayDeque<>(List.of(1))));
    probe("deque-stack-use", () -> { Deque<String> s = new ArrayDeque<>(); s.push("a"); s.push("b"); s.push("c"); return s + ":" + s.pop() + ":" + s.peek(); });
    probe("deque-for-each", () -> { StringBuilder sb = new StringBuilder(); new ArrayDeque<>(List.of(1, 2, 3)).forEach(sb::append); return sb; });
    probe("linked-deque-ops", () -> { Deque<Integer> d = new LinkedList<>(List.of(1, 2, 3)); d.addFirst(0); return d + ":" + d.pollLast() + ":" + d.peekFirst() + ":" + d.pop() + ":" + d; });

    probe("tree-map-order", () -> { TreeMap<String, Integer> m = new TreeMap<>(); for (String w : new String[] {"pear", "apple", "fig", "banana", "cherry"}) m.put(w, w.length()); return m; });
    probe("tree-map-nav", () -> { TreeMap<Integer, String> m = new TreeMap<>(); for (int i = 1; i <= 9; i += 2) m.put(i, "v" + i); return m.floorEntry(4) + "," + m.ceilingEntry(4) + "," + m.lowerKey(1) + "," + m.higherKey(9) + "," + m.firstEntry() + "," + m.lastEntry(); });
    probe("tree-map-sub", () -> { TreeMap<Integer, String> m = new TreeMap<>(); for (int i = 1; i <= 9; ++i) m.put(i, "v" + i); return m.subMap(3, 6) + "," + m.subMap(3, false, 6, true) + "," + m.headMap(3) + "," + m.tailMap(7) + "," + m.descendingMap().headMap(7); });
    probe("tree-map-sub-write", () -> { TreeMap<Integer, String> m = new TreeMap<>(); for (int i = 1; i <= 5; ++i) m.put(i, "v" + i); m.subMap(2, 4).put(3, "x"); m.subMap(2, 4).remove(2); return m; });
    probe("tree-map-sub-out-of-range", () -> { TreeMap<Integer, String> m = new TreeMap<>(); for (int i = 1; i <= 5; ++i) m.put(i, "v" + i); m.subMap(2, 4).put(9, "x"); return m; });
    probe("tree-map-bad-range", () -> new TreeMap<Integer, String>().subMap(5, 2));
    probe("tree-map-poll", () -> { TreeMap<Integer, String> m = new TreeMap<>(Map.of(1, "a", 2, "b", 3, "c")); return m.pollFirstEntry() + ":" + m.pollLastEntry() + ":" + m; });
    probe("tree-map-views", () -> { TreeMap<Integer, String> m = new TreeMap<>(Map.of(1, "a", 2, "b", 3, "c")); return m.keySet() + ":" + m.values() + ":" + m.entrySet() + ":" + m.navigableKeySet().descendingSet() + ":" + m.descendingKeySet(); });
    probe("tree-map-entry-immutable", () -> new TreeMap<>(Map.of(1, "a")).firstEntry().setValue("z"));
    probe("tree-map-entry-live", () -> { TreeMap<Integer, String> m = new TreeMap<>(Map.of(1, "a")); m.entrySet().iterator().next().setValue("z"); return m; });
    probe("tree-map-compute", () -> { TreeMap<String, Integer> m = new TreeMap<>(); for (String w : "to be or not to be".split(" ")) m.merge(w, 1, Integer::sum); m.computeIfAbsent("zz", k -> 9); m.computeIfPresent("be", (k, v) -> null); return m; });
    probe("tree-map-first-empty", () -> new TreeMap<Integer, Integer>().firstKey());
    probe("tree-map-first-entry-empty", () -> new TreeMap<Integer, Integer>().firstEntry());
    probe("tree-map-cmp-null-key", () -> { TreeMap<String, Integer> m = new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder())); m.put(null, 1); m.put("a", 2); return m; });
    probe("tree-map-copy", () -> { TreeMap<Integer, String> src = new TreeMap<>(Comparator.reverseOrder()); src.put(1, "a"); src.put(2, "b"); return new TreeMap<>(src) + ":" + new TreeMap<>((Map<Integer, String>) src); });
    probe("tree-map-clone", () -> { TreeMap<Integer, String> m = new TreeMap<>(Map.of(1, "a")); @SuppressWarnings("unchecked") TreeMap<Integer, String> c = (TreeMap<Integer, String>) m.clone(); c.put(2, "b"); return m + ":" + c; });
    probe("tree-map-large", () -> { TreeMap<Integer, Integer> m = new TreeMap<>(); Random r = new Random(9); for (int i = 0; i < 500; ++i) m.put(r.nextInt(300), i); int removed = 0; for (int i = 0; i < 300; i += 3) if (m.remove(i) != null) removed++; return m.size() + ":" + removed + ":" + m.firstKey() + ":" + m.lastKey() + ":" + m.hashCode(); });
    probe("tree-map-reversed", () -> { TreeMap<Integer, String> m = new TreeMap<>(Map.of(1, "a", 2, "b", 3, "c")); SequencedMap<Integer, String> r = m.reversed(); return r + ":" + r.firstEntry() + ":" + r.lastEntry(); });
    probe("tree-set-nav", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(10, 20, 30, 40)); return s.floor(25) + "," + s.ceiling(25) + "," + s.lower(10) + "," + s.higher(40) + "," + s.headSet(30, true) + "," + s.tailSet(20, false) + "," + s.subSet(10, 30) + "," + s.descendingSet() + "," + s.pollLast() + "," + s; });
    probe("tree-set-sub-write", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 3, 4, 5)); s.subSet(2, 4).add(3); s.headSet(3).clear(); return s; });
    probe("tree-set-sub-out", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 3)); s.headSet(2).add(5); return s; });
    probe("tree-set-comparator", () -> { TreeSet<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); s.add("B"); s.add("a"); s.add("b"); return s + ":" + s.contains("A") + ":" + s.comparator().equals(String.CASE_INSENSITIVE_ORDER); });
    probe("tree-set-first-empty", () -> new TreeSet<Integer>().first());
    probe("tree-set-add-non-comparable", () -> new TreeSet<Object>().add(new Object()));
    probe("tree-set-iterator-remove", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 3, 4, 5, 6)); Iterator<Integer> it = s.iterator(); while (it.hasNext()) if (it.next() % 2 == 0) it.remove(); return s; });
    probe("tree-set-descending-iterator", () -> { StringBuilder sb = new StringBuilder(); new TreeSet<>(List.of(1, 2, 3)).descendingIterator().forEachRemaining(sb::append); return sb; });
    probe("tree-set-reversed", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 3)); SequencedSet<Integer> r = s.reversed(); return r + ":" + r.getFirst() + ":" + r.removeFirst() + ":" + s; });
    probe("tree-set-clone-equals", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(1, 2)); @SuppressWarnings("unchecked") TreeSet<Integer> c = (TreeSet<Integer>) s.clone(); c.add(3); return s + ":" + c + ":" + s.equals(new TreeSet<>(List.of(2, 1))); });

    probe("chm-basic", () -> { ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>(); for (int i = 0; i < 6; ++i) m.put("k" + i, i); return m.get("k3") + ":" + m.size() + ":" + m.containsKey("k9") + ":" + m.remove("k0") + ":" + new TreeMap<>(m); });
    probe("chm-null-key", () -> new ConcurrentHashMap<String, Integer>().put(null, 1));
    probe("chm-null-value", () -> new ConcurrentHashMap<String, Integer>().put("a", null));
    probe("chm-compute", () -> { ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>(); m.merge("a", 1, Integer::sum); m.merge("a", 1, Integer::sum); m.computeIfAbsent("b", k -> 5); m.compute("c", (k, v) -> v == null ? 1 : v + 1); m.computeIfPresent("b", (k, v) -> v * 2); return new TreeMap<>(m); });
    probe("chm-put-if-absent", () -> { ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>(); return m.putIfAbsent("a", 1) + ":" + m.putIfAbsent("a", 2) + ":" + m.replace("a", 1, 7) + ":" + m.replace("a", 9) + ":" + m; });
    probe("chm-iteration-small", () -> { ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>(); for (String w : new String[] {"banana", "apple", "cherry", "date", "elderberry"}) m.put(w, w.length()); return m; });
    probe("chm-iteration-ints", () -> { ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>(); for (int i = 0; i < 30; ++i) m.put(i * 37 % 101, i); return m.keySet(); });
    probe("chm-no-cme", () -> { ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>(); for (int i = 0; i < 5; ++i) m.put(i, i); int n = 0; for (int k : m.keySet()) { m.put(k + 100, 0); n++; } return m.size() >= 5; });
    probe("chm-mapping-count", () -> { ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>(); for (int i = 0; i < 12; ++i) m.put(i, i); return m.mappingCount() + ":" + m.reduceValues(1, Integer::sum) + ":" + m.search(1, (k, v) -> v == 7 ? k : null); });
    probe("chm-for-each", () -> { ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>(); for (int i = 0; i < 4; ++i) m.put(i, i * i); StringBuilder sb = new StringBuilder(); m.forEach(1, (k, v) -> { synchronized (sb) { sb.append(k); } }); return sb.length(); });
    probe("chm-key-set-default", () -> { ConcurrentHashMap.KeySetView<String, Boolean> s = ConcurrentHashMap.newKeySet(); s.add("a"); s.add("b"); return s.size() + ":" + s.contains("a"); });
    probe("chm-equals-hash", () -> { Map<String, Integer> a = new ConcurrentHashMap<>(Map.of("a", 1)); return a.equals(Map.of("a", 1)) + ":" + a.hashCode() + ":" + a; });
    probe("chm-view-remove", () -> { ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>(); m.put("a", 1); m.put("b", 2); m.keySet().remove("a"); m.values().remove(2); return m; });
    probe("chm-clear", () -> { ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>(); m.put("a", 1); m.clear(); return m.isEmpty() + ":" + m; });
    probe("chm-bad-capacity", () -> new ConcurrentHashMap<String, Integer>(-1));
    probe("clq-ops", () -> { ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>(List.of(1, 2, 3)); q.add(4); return q.poll() + ":" + q.peek() + ":" + q + ":" + q.size() + ":" + q.contains(3) + ":" + q.remove(2) + ":" + q; });
    probe("cld-ops", () -> { ConcurrentLinkedDeque<Integer> d = new ConcurrentLinkedDeque<>(List.of(1, 2)); d.addFirst(0); d.addLast(3); return d + ":" + d.pollLast() + ":" + d.peekFirst() + ":" + d.size(); });
    probe("lbq-ops", () -> { LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(3); q.add(1); q.offer(2); q.offer(3); return q.offer(4) + ":" + q.remainingCapacity() + ":" + q.poll() + ":" + q + ":" + q.size(); });
    probe("abq-ops", () -> { ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(2); q.add(1); q.add(2); return q.offer(3) + ":" + q + ":" + q.remainingCapacity(); });
    probe("abq-full-add", () -> { ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(1); q.add(1); q.add(2); return q; });
    probe("pbq-ops", () -> { PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(); for (int x : data) q.add(x); return q + ":" + q.poll() + ":" + q.peek(); });
    probe("cow-list", () -> { CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(List.of(1, 2, 3)); for (int x : l) l.add(x + 10); return l; });
    probe("cow-set", () -> { CopyOnWriteArraySet<Integer> s = new CopyOnWriteArraySet<>(List.of(1, 2, 2, 3)); s.add(1); s.add(4); return s; });
    probe("skip-list-map", () -> { ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>(); for (int i = 5; i > 0; --i) m.put(i, "v" + i); return m + ":" + m.firstKey() + ":" + m.floorKey(3) + ":" + m.headMap(3) + ":" + m.descendingMap(); });
    probe("skip-list-set", () -> { ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>(List.of(5, 1, 3)); return s + ":" + s.first() + ":" + s.ceiling(2) + ":" + s.pollFirst() + ":" + s; });

    probe("properties-basic", () -> { Properties p = new Properties(); p.setProperty("b", "2"); p.setProperty("a", "1"); return p.getProperty("a") + ":" + p.getProperty("z") + ":" + p.getProperty("z", "dflt") + ":" + p.size() + ":" + new TreeMap<>(p); });
    probe("properties-defaults", () -> { Properties d = new Properties(); d.setProperty("x", "dx"); Properties p = new Properties(d); p.setProperty("y", "py"); return p.getProperty("x") + ":" + p.getProperty("y") + ":" + p.size() + ":" + new TreeSet<>(p.stringPropertyNames()) + ":" + p.containsKey("x"); });
    probe("properties-string-names", () -> { Properties p = new Properties(); p.setProperty("a", "1"); p.put("b", 2); return p.stringPropertyNames(); });
    probe("properties-null", () -> new Properties().put("a", null));
    probe("properties-load", () -> { Properties p = new Properties(); p.load(new java.io.StringReader("# c\na=1\nb : 2\nc 3\nd\\\n  e=4\nf=\\u0041\n! x\n\nspace key=val ue")); return new TreeMap<>(p); });
    probe("properties-store", () -> { Properties p = new Properties(); p.setProperty("k", "v"); java.io.StringWriter w = new java.io.StringWriter(); p.store(w, null); String[] lines = w.toString().split("\n"); return lines.length + ":" + lines[lines.length - 1]; });
    probe("properties-store-escape", () -> { Properties p = new Properties(); p.setProperty("key with space", "va=l:ue\n#x"); java.io.StringWriter w = new java.io.StringWriter(); p.store(w, null); String[] lines = w.toString().split("\n"); return lines[lines.length - 1]; });
    probe("properties-order", () -> { Properties p = new Properties(); for (String w : new String[] {"banana", "apple", "cherry", "date", "elderberry", "fig", "grape"}) p.setProperty(w, w.toUpperCase()); return p; });
    probe("properties-remove-clear", () -> { Properties p = new Properties(); p.setProperty("a", "1"); p.remove("a"); p.setProperty("b", "2"); p.clear(); return p.isEmpty(); });
    probe("properties-get-property-non-string", () -> { Properties p = new Properties(); p.put("n", 5); return p.getProperty("n"); });
  }
}
