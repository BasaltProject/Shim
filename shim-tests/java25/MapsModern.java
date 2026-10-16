/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;

public class MapsModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      Object value = action.run();
      System.out.println(label + ":" + value);
    } catch (Throwable error) {
      boolean generated = error instanceof NullPointerException || error instanceof ClassCastException;
      System.out.println(label + ":" + error.getClass().getName() + (generated ? "" : ":" + error.getMessage()));
    }
  }

  static final String[] WORDS = {"banana", "apple", "cherry", "date", "elderberry", "fig", "grape", "honeydew", "kiwi",
      "lemon", "mango", "nectarine", "orange", "papaya", "quince", "raspberry", "strawberry", "tangerine", "ugli",
      "vanilla", "watermelon", "xigua", "yam", "zucchini"};

  static Map<String, Integer> words(Map<String, Integer> m, int count) {
    for (int i = 0; i < count; ++i) m.put(WORDS[i], i);
    return m;
  }

  static class Collider {
    final int id;
    Collider(int id) { this.id = id; }
    public int hashCode() { return id % 3; }
    public boolean equals(Object o) { return o instanceof Collider c && c.id == id; }
    public String toString() { return "c" + id; }
  }

  public static void main(String[] args) {
    probe("hash-order-small", () -> words(new HashMap<>(), 5));
    probe("hash-order-resize", () -> words(new HashMap<>(), 14));
    probe("hash-order-large", () -> words(new HashMap<>(), 24));
    probe("hash-order-capacity", () -> words(new HashMap<>(64), 12));
    probe("hash-order-zero", () -> words(new HashMap<>(0), 9));
    probe("hash-int-keys", () -> { Map<Integer, String> m = new HashMap<>(); for (int i = 0; i < 30; ++i) m.put(i * 37 % 101, "v" + i); return m.keySet(); });
    probe("hash-negative-keys", () -> { Map<Integer, Integer> m = new HashMap<>(); for (int i = -10; i < 10; i += 3) m.put(i * 1000003, i); return m; });
    probe("hash-long-double-keys", () -> { Map<Object, Integer> m = new HashMap<>(); m.put(1L << 40, 1); m.put(2.5, 2); m.put('c', 3); m.put(true, 4); m.put(1.5f, 5); return m; });
    probe("hash-collisions", () -> { Map<Collider, Integer> m = new HashMap<>(); for (int i = 0; i < 12; ++i) m.put(new Collider(i), i); return m; });
    probe("hash-null-key", () -> { Map<String, Integer> m = new HashMap<>(); m.put("a", 1); m.put(null, 2); m.put("b", 3); return m + ":" + m.get(null) + ":" + m.containsKey(null) + ":" + m.remove(null) + ":" + m; });
    probe("hash-null-value", () -> { Map<String, Integer> m = new HashMap<>(); m.put("a", null); return m.get("a") + ":" + m.containsKey("a") + ":" + m.containsValue(null) + ":" + m.getOrDefault("a", 5) + ":" + m.getOrDefault("z", 5); });
    probe("hash-copy-ctor", () -> new HashMap<>(words(new LinkedHashMap<>(), 20)));
    probe("hash-put-all", () -> { Map<String, Integer> m = new HashMap<>(); m.put("zzz", 0); m.putAll(words(new LinkedHashMap<>(), 18)); return m; });
    probe("hash-put-returns", () -> { Map<String, Integer> m = new HashMap<>(); return m.put("a", 1) + ":" + m.put("a", 2) + ":" + m.putIfAbsent("a", 3) + ":" + m.putIfAbsent("b", 4) + ":" + m; });
    probe("hash-remove", () -> { Map<String, Integer> m = words(new HashMap<>(), 10); return m.remove("fig") + ":" + m.remove("fig") + ":" + m.remove("kiwi", 99) + ":" + m.remove("kiwi", 8) + ":" + m; });
    probe("hash-replace", () -> { Map<String, Integer> m = words(new HashMap<>(), 3); return m.replace("apple", 10) + ":" + m.replace("zzz", 1) + ":" + m.replace("cherry", 2, 20) + ":" + m.replace("cherry", 2, 30) + ":" + m; });
    probe("hash-compute-if-absent", () -> { Map<String, Integer> m = new HashMap<>(); for (String w : WORDS) m.computeIfAbsent(w, String::length); return m; });
    probe("hash-compute-if-absent-null", () -> { Map<String, Integer> m = new HashMap<>(); return m.computeIfAbsent("a", k -> null) + ":" + m.size(); });
    probe("hash-compute-if-present", () -> { Map<String, Integer> m = words(new HashMap<>(), 4); m.computeIfPresent("date", (k, v) -> v + 100); m.computeIfPresent("apple", (k, v) -> null); m.computeIfPresent("zzz", (k, v) -> 1); return m; });
    probe("hash-compute", () -> { Map<String, Integer> m = new HashMap<>(); for (String w : WORDS) m.compute(w, (k, v) -> v == null ? 1 : v + 1); m.compute("fig", (k, v) -> null); return m; });
    probe("hash-merge", () -> { Map<String, Integer> m = new HashMap<>(); for (int i = 0; i < 40; ++i) m.merge(WORDS[i % 7], 1, Integer::sum); m.merge("grape", 1, (a, b) -> null); return m; });
    probe("hash-merge-null", () -> new HashMap<String, Integer>().merge("a", null, Integer::sum));
    probe("hash-for-each-replace-all", () -> { Map<String, Integer> m = words(new HashMap<>(), 5); StringBuilder sb = new StringBuilder(); m.forEach((k, v) -> sb.append(k.charAt(0)).append(v)); m.replaceAll((k, v) -> v * 2); return sb + ":" + m; });
    probe("hash-entry-set", () -> { Map<String, Integer> m = words(new HashMap<>(), 4); for (Map.Entry<String, Integer> e : m.entrySet()) e.setValue(e.getValue() + 1); return m + ":" + m.entrySet().contains(Map.entry("apple", 2)) + ":" + m.entrySet().remove(Map.entry("apple", 2)) + ":" + m; });
    probe("hash-key-set-ops", () -> { Map<String, Integer> m = words(new HashMap<>(), 6); m.keySet().remove("fig"); m.keySet().removeIf(k -> k.startsWith("b")); m.values().remove(2); return m; });
    probe("hash-key-set-retain", () -> { Map<String, Integer> m = words(new HashMap<>(), 8); m.keySet().retainAll(Set.of("apple", "fig", "date")); return m; });
    probe("hash-values", () -> { Map<String, Integer> m = words(new HashMap<>(), 6); return m.values() + ":" + m.values().contains(3) + ":" + m.values().size(); });
    probe("hash-view-to-array", () -> { Map<String, Integer> m = words(new HashMap<>(), 5); return Arrays.toString(m.keySet().toArray()) + Arrays.toString(m.values().toArray(new Integer[0])); });
    probe("hash-cme-put", () -> { Map<Integer, Integer> m = new HashMap<>(Map.of(1, 1, 2, 2, 3, 3)); for (int x : m.keySet()) m.put(x + 10, 0); return 0; });
    probe("hash-cme-remove", () -> { Map<Integer, Integer> m = new HashMap<>(); for (int i = 0; i < 6; ++i) m.put(i, i); for (int x : m.keySet()) m.remove(x); return 0; });
    probe("hash-iterator-remove", () -> { Map<String, Integer> m = words(new HashMap<>(), 10); Iterator<String> it = m.keySet().iterator(); while (it.hasNext()) if (it.next().length() > 4) it.remove(); return m; });
    probe("hash-iterator-remove-illegal", () -> { Iterator<String> it = words(new HashMap<>(), 3).keySet().iterator(); it.next(); it.remove(); it.remove(); return 0; });
    probe("hash-compute-cme", () -> { Map<String, Integer> m = new HashMap<>(); m.computeIfAbsent("a", k -> { m.put("b", 1); return 1; }); return m; });
    probe("hash-merge-cme", () -> { Map<String, Integer> m = new HashMap<>(); m.put("a", 1); m.merge("a", 1, (x, y) -> { m.put("c", 3); return 2; }); return m; });
    probe("hash-for-each-cme", () -> { Map<String, Integer> m = words(new HashMap<>(), 3); m.forEach((k, v) -> m.put("zz" + k, 0)); return 0; });
    probe("hash-equals-hash", () -> { Map<String, Integer> a = words(new HashMap<>(), 5); Map<String, Integer> b = words(new LinkedHashMap<>(), 5); return a.equals(b) + "," + b.equals(a) + "," + (a.hashCode() == b.hashCode()) + "," + a.hashCode() + "," + a.equals(null) + "," + a.equals(new HashMap<>()) + "," + new HashMap<>().hashCode(); });
    probe("hash-to-string-self", () -> { Map<String, Object> m = new HashMap<>(); m.put("self", m); return m; });
    probe("hash-clone", () -> { HashMap<String, Integer> m = (HashMap<String, Integer>) words(new HashMap<>(), 5); @SuppressWarnings("unchecked") HashMap<String, Integer> c = (HashMap<String, Integer>) m.clone(); c.put("zz", 1); return m.size() + ":" + c.size() + ":" + c; });
    probe("hash-new-hash-map", () -> { HashMap<String, Integer> m = HashMap.newHashMap(20); words(m, 20); return m; });
    probe("hash-new-hash-map-negative", () -> HashMap.newHashMap(-1));
    probe("hash-bad-capacity", () -> new HashMap<String, String>(-1));
    probe("hash-bad-load-factor", () -> new HashMap<String, String>(4, 0f));
    probe("hash-load-factor-order", () -> words(new HashMap<>(4, 2.0f), 12));
    probe("hash-clear-reuse", () -> { Map<String, Integer> m = words(new HashMap<>(), 14); m.clear(); words(m, 6); return m; });

    probe("hash-set-order", () -> { Set<String> s = new HashSet<>(); for (String w : WORDS) s.add(w); return s; });
    probe("hash-set-copy", () -> new HashSet<>(Arrays.asList(WORDS).subList(0, 20)));
    probe("hash-set-ints", () -> { Set<Integer> s = new HashSet<>(); for (int i = 0; i < 25; ++i) s.add(i * 53 % 211); return s; });
    probe("hash-set-ops", () -> { Set<Integer> s = new HashSet<>(List.of(1, 2, 3, 4)); return s.add(5) + ":" + s.add(1) + ":" + s.remove(2) + ":" + s.remove(9) + ":" + s.contains(3) + ":" + s; });
    probe("hash-set-remove-all-big", () -> { Set<Integer> s = new HashSet<>(List.of(1, 2, 3, 4, 5, 6)); boolean r = s.removeAll(List.of(1, 1, 2)); return r + ":" + s; });
    probe("hash-set-remove-all-set", () -> { Set<Integer> s = new HashSet<>(List.of(1, 2, 3)); boolean r = s.removeAll(new HashSet<>(List.of(1, 2, 3, 4, 5))); return r + ":" + s; });
    probe("hash-set-retain-all", () -> { Set<Integer> s = new HashSet<>(List.of(1, 2, 3, 4)); boolean r = s.retainAll(List.of(2, 4, 6)); return r + ":" + s; });
    probe("hash-set-equals-hash", () -> { Set<Integer> a = new HashSet<>(List.of(1, 2, 3)); Set<Integer> b = new TreeSet<>(List.of(3, 2, 1)); return a.equals(b) + "," + b.equals(a) + "," + a.hashCode() + "," + b.hashCode() + "," + a.equals(List.of(1, 2, 3)) + "," + a.equals(Set.of(1, 2, 3)) + "," + new HashSet<>().hashCode(); });
    probe("hash-set-null", () -> { Set<String> s = new HashSet<>(); s.add(null); s.add("a"); return s + ":" + s.contains(null) + ":" + s.remove(null); });
    probe("hash-set-cme", () -> { Set<Integer> s = new HashSet<>(List.of(1, 2, 3)); for (int x : s) s.add(x + 10); return 0; });
    probe("hash-set-clone", () -> { HashSet<Integer> s = new HashSet<>(List.of(1, 2)); @SuppressWarnings("unchecked") HashSet<Integer> c = (HashSet<Integer>) s.clone(); c.add(3); return s + ":" + c; });
    probe("hash-set-new", () -> { HashSet<Integer> s = HashSet.newHashSet(10); s.addAll(List.of(5, 4, 3)); return s; });
    probe("hash-set-to-array", () -> { Set<String> s = new HashSet<>(List.of("a", "b", "c")); return Arrays.toString(s.toArray()) + Arrays.toString(s.toArray(new String[5])); });
    probe("hash-set-stream", () -> new HashSet<>(List.of(3, 1, 2)).stream().map(x -> x * 2).toList());

    probe("linked-order", () -> words(new LinkedHashMap<>(), 8));
    probe("linked-reinsert", () -> { Map<String, Integer> m = words(new LinkedHashMap<>(), 5); m.put("apple", 99); m.remove("cherry"); m.put("cherry", 7); return m; });
    probe("linked-access-order", () -> { LinkedHashMap<String, Integer> m = new LinkedHashMap<>(16, 0.75f, true); words(m, 5); m.get("banana"); m.getOrDefault("date", 0); m.put("apple", 11); m.putIfAbsent("cherry", 0); m.computeIfAbsent("fig", k -> 0); m.get("zzz"); return m; });
    probe("linked-access-order-views", () -> { LinkedHashMap<String, Integer> m = new LinkedHashMap<>(16, 0.75f, true); words(m, 4); m.get("banana"); return m.keySet() + ":" + m.values() + ":" + m.entrySet(); });
    probe("linked-access-cme", () -> { LinkedHashMap<String, Integer> m = new LinkedHashMap<>(16, 0.75f, true); words(m, 4); for (String k : m.keySet()) m.get(k); return 0; });
    probe("linked-lru", () -> {
      LinkedHashMap<Integer, String> m = new LinkedHashMap<>(16, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) { return size() > 3; }
      };
      for (int i = 0; i < 6; ++i) { m.put(i, "v" + i); m.get(i - 1); }
      return m;
    });
    probe("linked-eldest-remove", () -> {
      LinkedHashMap<Integer, String> m = new LinkedHashMap<>() {
        protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) { return size() > 2; }
      };
      m.put(1, "a"); m.put(2, "b"); m.put(3, "c"); m.putAll(Map.of(4, "d"));
      return m;
    });
    probe("linked-contains-value", () -> words(new LinkedHashMap<>(), 5).containsValue(3) + "," + words(new LinkedHashMap<>(), 5).containsValue(30));
    probe("linked-for-each", () -> { StringBuilder sb = new StringBuilder(); words(new LinkedHashMap<>(), 5).forEach((k, v) -> sb.append(k.charAt(0)).append(v)); return sb; });
    probe("linked-replace-all", () -> { Map<String, Integer> m = words(new LinkedHashMap<>(), 4); m.replaceAll((k, v) -> v + 10); return m; });
    probe("linked-cme", () -> { Map<String, Integer> m = words(new LinkedHashMap<>(), 4); for (String k : m.keySet()) m.put("n" + k, 1); return 0; });
    probe("linked-iterator-remove", () -> { Map<String, Integer> m = words(new LinkedHashMap<>(), 8); Iterator<Map.Entry<String, Integer>> it = m.entrySet().iterator(); while (it.hasNext()) if (it.next().getValue() % 2 == 0) it.remove(); return m; });
    probe("linked-clear", () -> { LinkedHashMap<String, Integer> m = new LinkedHashMap<>(); words(m, 4); m.clear(); words(m, 2); return m + ":" + m.firstEntry() + ":" + m.lastEntry(); });
    probe("linked-first-last", () -> { LinkedHashMap<String, Integer> m = (LinkedHashMap<String, Integer>) words(new LinkedHashMap<>(), 4); return m.firstEntry() + ":" + m.lastEntry() + ":" + m.sequencedKeySet().getFirst() + ":" + m.sequencedValues().getLast() + ":" + m.sequencedEntrySet().getFirst(); });
    probe("linked-first-entry-immutable", () -> { LinkedHashMap<String, Integer> m = (LinkedHashMap<String, Integer>) words(new LinkedHashMap<>(), 2); m.firstEntry().setValue(5); return 0; });
    probe("linked-poll", () -> { LinkedHashMap<String, Integer> m = (LinkedHashMap<String, Integer>) words(new LinkedHashMap<>(), 4); return m.pollFirstEntry() + ":" + m.pollLastEntry() + ":" + m + ":" + new LinkedHashMap<String, String>().pollFirstEntry(); });
    probe("linked-put-first-last", () -> { LinkedHashMap<String, Integer> m = (LinkedHashMap<String, Integer>) words(new LinkedHashMap<>(), 4); m.putFirst("cherry", 20); m.putFirst("new", 1); m.putLast("banana", 30); m.putLast("end", 2); return m; });
    probe("linked-reversed", () -> { LinkedHashMap<String, Integer> m = (LinkedHashMap<String, Integer>) words(new LinkedHashMap<>(), 5); SequencedMap<String, Integer> r = m.reversed(); return r + ":" + r.firstEntry() + ":" + r.keySet() + ":" + r.values() + ":" + r.size() + ":" + r.get("fig") + ":" + r.reversed().equals(m); });
    probe("linked-reversed-modify", () -> { LinkedHashMap<String, Integer> m = (LinkedHashMap<String, Integer>) words(new LinkedHashMap<>(), 4); SequencedMap<String, Integer> r = m.reversed(); r.put("zzz", 9); r.pollFirstEntry(); Iterator<String> it = r.keySet().iterator(); it.next(); it.remove(); return m + ":" + r; });
    probe("linked-equals", () -> words(new LinkedHashMap<>(), 5).equals(words(new HashMap<>(), 5)) + "," + words(new LinkedHashMap<>(), 5).hashCode());
    probe("linked-new-linked", () -> { LinkedHashMap<String, Integer> m = LinkedHashMap.newLinkedHashMap(10); words(m, 10); return m; });
    probe("linked-copy-ctor", () -> new LinkedHashMap<>(words(new HashMap<>(), 12)));

    probe("linked-set-order", () -> { Set<String> s = new LinkedHashSet<>(); for (int i = 0; i < 6; ++i) s.add(WORDS[i]); s.add(WORDS[0]); s.remove(WORDS[2]); s.add(WORDS[2]); return s; });
    probe("linked-set-copy", () -> new LinkedHashSet<>(Arrays.asList(WORDS).subList(0, 8)));
    probe("linked-set-sequenced", () -> { LinkedHashSet<String> s = new LinkedHashSet<>(List.of("b", "c")); s.addFirst("a"); s.addLast("d"); s.addFirst("c"); return s + ":" + s.getFirst() + ":" + s.getLast() + ":" + s.removeFirst() + ":" + s.removeLast() + ":" + s; });
    probe("linked-set-reversed", () -> { LinkedHashSet<String> s = new LinkedHashSet<>(List.of("a", "b", "c")); SequencedSet<String> r = s.reversed(); r.addFirst("z"); return s + ":" + r + ":" + r.getFirst() + ":" + r.reversed().equals(s); });
    probe("linked-set-empty-first", () -> new LinkedHashSet<String>().getFirst());
    probe("linked-set-equals", () -> new LinkedHashSet<>(List.of(1, 2)).equals(new HashSet<>(List.of(2, 1))) + "," + new LinkedHashSet<>(List.of(1, 2)).hashCode());
    probe("linked-set-new", () -> { LinkedHashSet<Integer> s = LinkedHashSet.newLinkedHashSet(5); s.add(3); s.add(1); return s; });

    probe("tree-map", () -> { Map<String, Integer> m = words(new TreeMap<>(), 10); return m + ":" + m.equals(words(new HashMap<>(), 10)) + ":" + (m.hashCode() == words(new HashMap<>(), 10).hashCode()); });
    probe("tree-set-equals", () -> { Set<Integer> s = new TreeSet<>(List.of(3, 1, 2)); return s.equals(Set.of(1, 2, 3)) + ":" + s.hashCode() + ":" + s.equals(new HashSet<>(List.of(3, 2, 1))); });
    probe("tree-set-ops", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(5, 1, 9, 3)); return s.first() + "," + s.last() + "," + s.floor(4) + "," + s.higher(5) + "," + s.headSet(5) + "," + s.tailSet(5, false) + "," + s.descendingSet() + "," + s.pollFirst() + "," + s; });
    probe("tree-map-ops", () -> { TreeMap<Integer, String> m = new TreeMap<>(Map.of(1, "a", 5, "e", 3, "c")); return m.firstKey() + "," + m.lastEntry() + "," + m.floorKey(4) + "," + m.headMap(3, true) + "," + m.descendingMap() + "," + m.pollLastEntry() + "," + m.subMap(1, 4); });
    probe("tree-map-null-key", () -> new TreeMap<String, Integer>().put(null, 1));
    probe("tree-map-comparator", () -> { TreeMap<String, Integer> m = new TreeMap<>(String.CASE_INSENSITIVE_ORDER); m.put("B", 1); m.put("a", 2); m.put("b", 3); return m; });
    probe("tree-map-cme", () -> { TreeMap<Integer, Integer> m = new TreeMap<>(Map.of(1, 1, 2, 2, 3, 3)); for (int k : m.keySet()) m.put(k + 10, 0); return 0; });
    probe("tree-set-cme", () -> { TreeSet<Integer> s = new TreeSet<>(List.of(1, 2, 3)); for (int k : s) s.add(k + 10); return 0; });
    probe("tree-map-to-string", () -> new TreeMap<>(Map.of(2, "b", 1, "a")).toString());

    probe("hashtable", () -> { Hashtable<String, Integer> h = new Hashtable<>(); words(h, 12); return h; });
    probe("hashtable-null", () -> new Hashtable<String, Integer>().put("a", null));
    probe("hashtable-enumeration", () -> { Hashtable<String, Integer> h = new Hashtable<>(); words(h, 5); StringBuilder sb = new StringBuilder(); for (Enumeration<String> e = h.keys(); e.hasMoreElements();) sb.append(e.nextElement()).append(','); return sb; });
    probe("identity-map", () -> { IdentityHashMap<String, Integer> m = new IdentityHashMap<>(); String a = new String("k"), b = new String("k"); m.put(a, 1); m.put(b, 2); return m.size() + ":" + m.get(a) + ":" + m.get("k"); });
    probe("weak-map-basic", () -> { WeakHashMap<String, Integer> m = new WeakHashMap<>(); String strong = "key"; m.put(strong, 1); return m.size() + ":" + m.get("key") + ":" + m.containsKey("key"); });
    probe("enum-map", () -> { EnumMap<Thread.State, Integer> m = new EnumMap<>(Thread.State.class); m.put(Thread.State.RUNNABLE, 1); m.put(Thread.State.NEW, 0); return m; });
    probe("enum-set", () -> EnumSet.of(Thread.State.WAITING, Thread.State.NEW, Thread.State.BLOCKED) + ":" + EnumSet.complementOf(EnumSet.of(Thread.State.NEW)) + ":" + EnumSet.noneOf(Thread.State.class) + ":" + EnumSet.range(Thread.State.NEW, Thread.State.BLOCKED));

    probe("map-of-basics", () -> { Map<String, Integer> m = Map.of("a", 1, "b", 2); return m.size() + ":" + m.get("a") + ":" + m.containsKey("z") + ":" + new TreeMap<>(m); });
    probe("map-of-immutable", () -> Map.of("a", 1).put("b", 2));
    probe("map-of-null", () -> Map.of("a", null));
    probe("map-of-duplicate", () -> Map.of("a", 1, "a", 2));
    probe("map-entry", () -> Map.entry("k", "v") + ":" + Map.entry("k", "v").equals(new AbstractMap.SimpleEntry<>("k", "v")) + ":" + Map.entry("k", "v").hashCode());
    probe("map-entry-null", () -> Map.entry("k", null));
    probe("map-copy-of", () -> { Map<String, Integer> m = Map.copyOf(words(new HashMap<>(), 3)); return new TreeMap<>(m); });
    probe("set-of-duplicate", () -> Set.of(1, 1));
    probe("set-of-contains-null", () -> Set.of(1).contains(null));
    probe("list-of-null-contains", () -> List.of(1).contains(null));
    probe("list-of-immutable", () -> List.of(1).set(0, 2));
    probe("list-of-copy", () -> List.copyOf(Arrays.asList(1, 2, 3)) + ":" + List.of(1, 2).equals(Arrays.asList(1, 2)) + ":" + List.of(1, 2).hashCode());
    probe("abstract-map-simple", () -> { Map<String, Integer> m = new AbstractMap<String, Integer>() {
      public Set<Map.Entry<String, Integer>> entrySet() { return Set.of(new AbstractMap.SimpleEntry<>("k", 1)); } }; return m + ":" + m.get("k") + ":" + m.size() + ":" + m.containsValue(1) + ":" + m.keySet() + ":" + m.values() + ":" + m.hashCode(); });
  }
}
