/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class SkipListModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static ConcurrentSkipListMap<Integer, String> sample() {
    ConcurrentSkipListMap<Integer, String> m = new ConcurrentSkipListMap<>();
    for (int i = 1; i <= 9; i += 2) m.put(i, "v" + i);
    return m;
  }

  public static void main(String[] args) {
    probe("map", () -> sample());
    probe("first-last", () -> sample().firstKey() + " " + sample().lastKey() + " " + sample().firstEntry() + " " + sample().lastEntry());
    probe("nav", () -> { var m = sample(); return m.lowerKey(5) + " " + m.floorKey(4) + " " + m.ceilingKey(4) + " " + m.higherKey(9) + " " + m.lowerEntry(1) + " " + m.higherEntry(5); });
    probe("poll", () -> { var m = sample(); return m.pollFirstEntry() + " " + m.pollLastEntry() + " " + m; });
    probe("head", () -> sample().headMap(5) + " " + sample().headMap(5, true) + " " + sample().tailMap(5) + " " + sample().tailMap(5, false));
    probe("sub", () -> sample().subMap(3, 7) + " " + sample().subMap(3, false, 7, true));
    probe("desc", () -> sample().descendingMap() + " " + sample().descendingKeySet() + " " + sample().descendingMap().firstKey());
    probe("desc-sub", () -> sample().descendingMap().subMap(7, 3) + " " + sample().descendingMap().headMap(5));
    probe("view-write", () -> { var m = sample(); var h = m.headMap(5); h.put(2, "x"); m.put(0, "z"); try { h.put(6, "bad"); } catch (IllegalArgumentException e) { return h + " " + m + " " + e.getMessage(); } return "none"; });
    probe("view-size", () -> { var m = sample(); var s = m.subMap(3, 8); return s.size() + " " + s.isEmpty() + " " + s.containsKey(5) + " " + s.containsKey(9); });
    probe("keyset", () -> { var m = sample(); var k = m.keySet(); return k + " " + k.first() + " " + k.last() + " " + k.headSet(5) + " " + k.tailSet(5, false) + " " + k.subSet(1, 7) + " " + k.descendingSet() + " " + k.pollFirst() + " " + m; });
    probe("values", () -> { var m = sample(); m.values().remove("v3"); m.values().removeIf(v -> v.equals("v9")); return m.values() + " " + m.values().size(); });
    probe("entryset", () -> { var m = sample(); m.entrySet().removeIf(e -> e.getKey() > 5); var e = m.entrySet().iterator().next(); try { e.setValue("q"); } catch (UnsupportedOperationException ex) { return m.entrySet() + " uoe"; } return m + " set"; });
    probe("compute", () -> { var m = sample(); m.compute(1, (k, v) -> v + "!"); m.computeIfAbsent(2, k -> "n2"); m.computeIfPresent(3, (k, v) -> null); m.merge(5, "m", String::concat); m.merge(100, "m", String::concat); return m; });
    probe("replace", () -> { var m = sample(); return m.replace(1, "a") + " " + m.replace(1, "a", "b") + " " + m.replace(2, "c") + " " + m.remove(3, "no") + " " + m.remove(3, "v3") + " " + m; });
    probe("putIfAbsent", () -> { var m = sample(); return m.putIfAbsent(1, "x") + " " + m.putIfAbsent(2, "y") + " " + m; });
    probe("null-key", () -> sample().put(null, "x"));
    probe("null-value", () -> sample().put(1, null));
    probe("get-null", () -> sample().get(null));
    probe("cmp", () -> { var m = new ConcurrentSkipListMap<String, Integer>(Comparator.reverseOrder()); m.put("a", 1); m.put("c", 3); m.put("b", 2); return m + " " + m.comparator().compare("a", "b") + " " + m.firstKey(); });
    probe("ctor-sorted", () -> { var t = new TreeMap<Integer, String>(Comparator.reverseOrder()); t.put(1, "a"); t.put(2, "b"); var m = new ConcurrentSkipListMap<>(t); return m + " " + (m.comparator() != null); });
    probe("ctor-map", () -> new ConcurrentSkipListMap<>(Map.of(3, "c", 1, "a")));
    probe("equals", () -> sample().equals(new TreeMap<>(sample())) + " " + sample().hashCode() + " " + sample().toString().length());
    probe("clone", () -> { var m = sample(); var c = m.clone(); m.clear(); return c + " " + m; });
    probe("foreach", () -> { var sb = new StringBuilder(); sample().forEach((k, v) -> sb.append(k).append(v)); return sb; });
    probe("replaceAll", () -> { var m = sample(); m.replaceAll((k, v) -> v + k); return m; });
    probe("empty", () -> { var m = new ConcurrentSkipListMap<Integer, Integer>(); try { m.firstKey(); } catch (NoSuchElementException e) { return "nse " + m.firstEntry() + " " + m.pollFirstEntry() + " " + m.isEmpty(); } return "none"; });
    probe("iter-remove", () -> { var m = sample(); var it = m.keySet().iterator(); it.next(); it.remove(); try { it.remove(); } catch (IllegalStateException e) { return m + " ise"; } return "none"; });
    probe("submap-bad", () -> sample().subMap(7, 3));
    probe("headmap-poll", () -> { var m = sample(); var h = m.headMap(6); return h.pollFirstEntry() + " " + h.pollLastEntry() + " " + h + " " + m; });
    probe("tailmap-first", () -> { var m = sample(); var t = m.tailMap(4); return t.firstKey() + " " + t.lastKey() + " " + t.firstEntry() + " " + t.lowerKey(5) + " " + t.floorKey(3) + " " + t.ceilingKey(3); });
    probe("desc-keyset-iter", () -> { var it = sample().descendingKeySet().iterator(); var sb = new StringBuilder(); while (it.hasNext()) sb.append(it.next()); return sb; });
    probe("spliterator", () -> { var m = sample(); return m.keySet().spliterator().characteristics() + " " + m.entrySet().spliterator().characteristics() + " " + m.values().spliterator().characteristics() + " " + m.keySet().spliterator().estimateSize(); });
    probe("stream", () -> sample().entrySet().stream().map(e -> e.getKey() + e.getValue()).toList() + " " + sample().keySet().stream().mapToInt(i -> i).sum());
    probe("entry-compare", () -> { var e = sample().firstEntry(); return e.getClass().getSimpleName().length() > 0 ? e.getKey() + "=" + e.getValue() + " " + e.equals(Map.entry(1, "v1")) + " " + e.hashCode() : ""; });
    probe("entry-immutable", () -> { var e = sample().firstEntry(); e.setValue("z"); return e; });
    probe("contains-value", () -> sample().containsValue("v3") + " " + sample().containsValue("zz") + " " + sample().containsValue(null));
    probe("getOrDefault", () -> sample().getOrDefault(2, "d") + " " + sample().getOrDefault(1, "d"));
    probe("keyset-ops", () -> { var k = sample().keySet(); return k.contains(3) + " " + k.remove(3) + " " + k.remove(3) + " " + k.size() + " " + k.containsAll(List.of(1, 5)) + " " + k.retainAll(List.of(1, 5)) + " " + k; });
    probe("keyset-lower", () -> { var k = sample().keySet(); return k.lower(5) + " " + k.floor(4) + " " + k.ceiling(4) + " " + k.higher(9) + " " + k.pollLast(); });
    probe("keyset-toarray", () -> Arrays.toString(sample().keySet().toArray()) + " " + Arrays.toString(sample().values().toArray(new String[0])));
    probe("keyset-equals", () -> sample().keySet().equals(new TreeSet<>(sample().keySet())) + " " + sample().keySet().hashCode());

    ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>(List.of(5, 1, 9, 3, 7));
    probe("set", () -> s + " " + s.first() + " " + s.last() + " " + s.size());
    probe("set-nav", () -> s.lower(5) + " " + s.floor(4) + " " + s.ceiling(4) + " " + s.higher(9) + " " + s.lower(1));
    probe("set-sub", () -> s.subSet(3, 8) + " " + s.subSet(3, false, 9, true) + " " + s.headSet(5) + " " + s.headSet(5, true) + " " + s.tailSet(5) + " " + s.tailSet(5, false));
    probe("set-desc", () -> s.descendingSet() + " " + s.descendingSet().first() + " " + s.descendingSet().headSet(5) + " " + s.descendingSet().subSet(7, 3));
    probe("set-poll", () -> { var c = new ConcurrentSkipListSet<>(s); return c.pollFirst() + " " + c.pollLast() + " " + c; });
    probe("set-add", () -> { var c = new ConcurrentSkipListSet<>(s); return c.add(5) + " " + c.add(6) + " " + c.remove(1) + " " + c.remove(1) + " " + c; });
    probe("set-null", () -> new ConcurrentSkipListSet<Integer>().add(null));
    probe("set-clone", () -> { var c = s.clone(); c.add(100); return c.size() + " " + s.size(); });
    probe("set-equals", () -> s.equals(new TreeSet<>(s)) + " " + s.hashCode() + " " + s.containsAll(List.of(1, 3)) + " " + s.removeAll(List.of(100)));
    probe("set-cmp", () -> { var c = new ConcurrentSkipListSet<String>(String.CASE_INSENSITIVE_ORDER); c.add("b"); c.add("A"); c.add("B"); return c + " " + c.comparator().compare("a", "A"); });
    probe("set-ctor", () -> (new ConcurrentSkipListSet<>(new TreeSet<>(Comparator.<Integer>reverseOrder())).comparator() != null) + " " + new ConcurrentSkipListSet<>(Comparator.<Integer>naturalOrder()).isEmpty());
    probe("set-stream", () -> s.stream().map(String::valueOf).reduce("", String::concat) + " " + s.descendingSet().stream().toList());
    probe("set-removeIf", () -> { var c = new ConcurrentSkipListSet<>(s); c.removeIf(x -> x > 4); return c; });
    probe("set-iter", () -> { var it = s.descendingIterator(); var sb = new StringBuilder(); while (it.hasNext()) sb.append(it.next()); return sb; });
    probe("set-toarray", () -> Arrays.toString(s.toArray(new Integer[0])) + " " + s.toArray().length);
    probe("set-spliterator", () -> s.spliterator().characteristics() + " " + s.spliterator().estimateSize());
    probe("set-subset-bad", () -> s.subSet(8, 2));
    probe("set-viewrange", () -> { var c = new ConcurrentSkipListSet<>(s); var h = c.headSet(5); try { h.add(6); } catch (IllegalArgumentException e) { return e.getMessage(); } return "none"; });
  }
}
