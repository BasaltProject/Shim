/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.util.*;
public class CollectionsWrappers {
  interface C { Object call() throws Throwable; }
  static void t(String l, C c) { try { Object v = c.call(); System.out.println(l + ": " + v); } catch (Throwable e) { System.out.println(l + ": !" + e.getClass().getName() + ": " + e.getMessage()); } }
  public static void main(String[] a) {
    List<String> base = new ArrayList<>(List.of("a", "b", "c"));
    Set<String> bs = new TreeSet<>(List.of("x", "y", "z")); Map<String, Integer> bm = new TreeMap<>(Map.of("k1", 1, "k2", 2));
    List<List<String>> lists = List.of(Collections.unmodifiableList(base), Collections.unmodifiableList(new LinkedList<>(base)), Collections.synchronizedList(base), Collections.checkedList(base, String.class), Collections.singletonList("s"), Collections.nCopies(3, "n"), Collections.emptyList());
    for (int i = 0; i < lists.size(); i++) { final List<String> l = lists.get(i); final String n = "list" + i;
      t(n + " str", () -> l + " " + l.size() + " " + l.isEmpty() + " " + l.hashCode() + " " + l.equals(new ArrayList<>(l)) + " " + new ArrayList<>(l).equals(l) + " " + l.contains("a") + " " + l.indexOf("b") + " " + l.lastIndexOf("a") + " " + l.containsAll(List.of("a")) + " " + Arrays.toString(l.toArray()) + " " + Arrays.toString(l.toArray(new String[0])));
      t(n + " get", () -> l.get(0)); t(n + " get9", () -> l.get(9)); t(n + " iter", () -> { Iterator<String> it = l.iterator(); StringBuilder b = new StringBuilder(); while (it.hasNext()) b.append(it.next()); it.remove(); return b; });
      t(n + " add", () -> l.add("q")); t(n + " set", () -> l.set(0, "q")); t(n + " remove", () -> l.remove("a")); t(n + " clear", () -> { l.clear(); return l.size(); });
      t(n + " sub", () -> l.subList(0, 1)); t(n + " listIter", () -> { ListIterator<String> it = l.listIterator(); it.next(); it.set("z"); return it.nextIndex(); });
      t(n + " stream", () -> l.stream().count()); t(n + " forEach", () -> { StringBuilder b = new StringBuilder(); l.forEach(b::append); return b; }); t(n + " removeIf", () -> l.removeIf(s -> true)); t(n + " replaceAll", () -> { l.replaceAll(s -> s + s); return l; }); t(n + " sort", () -> { l.sort(null); return l; });
      t(n + " reversed", () -> l.reversed()); t(n + " getFirst", () -> l.getFirst()); t(n + " addFirst", () -> { l.addFirst("f"); return l; }); t(n + " ra", () -> l instanceof RandomAccess);
    }
    List<Set<String>> sets = List.of(Collections.unmodifiableSet(bs), Collections.synchronizedSet(bs), Collections.checkedSet(bs, String.class), Collections.singleton("s"), Collections.emptySet(), Collections.unmodifiableSortedSet((SortedSet<String>) bs), Collections.unmodifiableNavigableSet((NavigableSet<String>) bs), Collections.newSetFromMap(new HashMap<String, Boolean>()));
    for (int i = 0; i < sets.size(); i++) { final Set<String> s = sets.get(i); final String n = "set" + i;
      t(n + " str", () -> s + " " + s.size() + " " + s.hashCode() + " " + s.equals(new HashSet<>(s)) + " " + s.contains("x") + " " + s.containsAll(List.of("x")) + " " + Arrays.toString(s.toArray()));
      t(n + " add", () -> s.add("q")); t(n + " remove", () -> s.remove("x")); t(n + " iter", () -> { Iterator<String> it = s.iterator(); it.hasNext(); it.next(); it.remove(); return 1; }); t(n + " removeIf", () -> s.removeIf(x -> true)); t(n + " retain", () -> s.retainAll(List.of())); t(n + " addAll", () -> s.addAll(List.of("w"))); t(n + " clear", () -> { s.clear(); return s; });
      t(n + " first", () -> ((SortedSet<String>) s).first());
    }
    List<Map<String, Integer>> maps = List.of(Collections.unmodifiableMap(bm), Collections.synchronizedMap(bm), Collections.checkedMap(bm, String.class, Integer.class), Collections.singletonMap("s", 1), Collections.emptyMap(), Collections.unmodifiableSortedMap((SortedMap<String, Integer>) bm), Collections.unmodifiableNavigableMap((NavigableMap<String, Integer>) bm));
    for (int i = 0; i < maps.size(); i++) { final Map<String, Integer> m = maps.get(i); final String n = "map" + i;
      t(n + " str", () -> m + " " + m.size() + " " + m.hashCode() + " " + m.equals(new HashMap<>(m)) + " " + m.containsKey("k1") + " " + m.containsValue(1) + " " + m.get("k1") + " " + m.getOrDefault("zz", -1) + " " + m.keySet() + " " + m.values() + " " + m.entrySet());
      t(n + " put", () -> m.put("n", 1)); t(n + " putIfAbsent", () -> m.putIfAbsent("n", 1)); t(n + " remove", () -> m.remove("k1")); t(n + " compute", () -> m.compute("k1", (k, v) -> 5)); t(n + " merge", () -> m.merge("k1", 1, Integer::sum)); t(n + " clear", () -> { m.clear(); return m; }); t(n + " forEach", () -> { StringBuilder b = new StringBuilder(); m.forEach((k, v) -> b.append(k).append(v)); return b; }); t(n + " replaceAll", () -> { m.replaceAll((k, v) -> v + 1); return m; });
      t(n + " entry set", () -> { Map.Entry<String, Integer> e = m.entrySet().iterator().next(); e.setValue(9); return e; }); t(n + " keyset rm", () -> m.keySet().remove("k1")); t(n + " first", () -> ((SortedMap<String, Integer>) m).firstKey());
    }
    Collection<String> col = Collections.unmodifiableCollection(base); t("coll", () -> col + " " + col.size() + " " + col.contains("a") + " " + col.containsAll(base) + " " + col.getClass().getSimpleName());
    t("coll add", () -> col.add("z")); t("coll rm", () -> col.remove("a")); t("coll toArray", () -> col.toArray(new String[0]).length); t("coll stream", () -> col.stream().count());
    t("rev", () -> { List<Integer> l = new ArrayList<>(List.of(3, 1, 2)); l.sort(Collections.reverseOrder()); return l; });
    t("rev cmp", () -> { List<String> l = new ArrayList<>(List.of("b", "a", "c")); l.sort(Collections.reverseOrder(Comparator.<String>naturalOrder())); Comparator<String> c = Collections.reverseOrder(Collections.reverseOrder()); return l + " " + (c == null) + " " + (Collections.reverseOrder(null) == Collections.reverseOrder()) + " " + ((Object) Collections.reverseOrder().reversed() == (Object) Comparator.naturalOrder()); });
    t("rev cmp2", () -> { Comparator<String> r = Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER); return r.compare("a", "B") + " " + r.equals(Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER)) + " " + (r.hashCode() == Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER).hashCode()) + " " + (r.reversed() == String.CASE_INSENSITIVE_ORDER); });
    t("static ops", () -> { List<Integer> l = new ArrayList<>(List.of(5, 3, 9)); Collections.sort(l); Collections.reverse(l); Collections.swap(l, 0, 2); Collections.rotate(l, 1); return l + " " + Collections.max(l) + " " + Collections.min(l) + " " + Collections.frequency(l, 3) + " " + Collections.binarySearch(List.of(1, 3, 5), 3) + " " + Collections.disjoint(l, List.of(1)) + " " + Collections.indexOfSubList(l, List.of(9)) + " " + Collections.emptyIterator().hasNext(); });
    t("nCopies", () -> Collections.nCopies(-1, "x")); t("sing hash", () -> Collections.singletonList(null).hashCode() + " " + Collections.singleton(null).hashCode() + " " + Collections.singletonMap(null, null).hashCode());
    t("checked", () -> { List raw = Collections.checkedList(new ArrayList<String>(), String.class); raw.add(1); return raw; });
    t("lifo", () -> { Queue<Integer> q = Collections.asLifoQueue(new ArrayDeque<>()); q.add(1); q.add(2); return q.poll(); });
    t("seq", () -> { SequencedSet<String> s = Collections.unmodifiableSequencedSet(new LinkedHashSet<>(List.of("a", "b"))); return s.reversed() + " " + s.getFirst(); });
    t("seq add", () -> { Collections.unmodifiableSequencedSet(new LinkedHashSet<>(List.of("a", "b"))).addFirst("c"); return 1; });
    t("seqmap", () -> { SequencedMap<String, Integer> m = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(Map.of("a", 1))); return m.firstEntry() + " " + m.reversed(); });
    t("seqfrom", () -> { SequencedSet<String> s = Collections.newSequencedSetFromMap(new LinkedHashMap<String, Boolean>()); s.add("a"); s.addFirst("b"); return s + " " + s.reversed(); });
    t("empty sorted", () -> Collections.emptySortedSet().size() + " " + Collections.emptyNavigableMap().isEmpty() + " " + Collections.emptyEnumeration().hasMoreElements() + " " + Collections.emptyListIterator().hasNext());
  }
}
