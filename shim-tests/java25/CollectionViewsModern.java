/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.util.*;
import java.util.concurrent.*;

public class CollectionViewsModern {
    enum Color { RED, GREEN, BLUE, BLACK }

    interface Body { void run() throws Exception; }

    static void probe(String name, Body b) {
        try {
            b.run();
        } catch (Throwable t) {
            System.out.println(name + " threw " + t.getClass().getName());
        }
    }

    static String sh(Object o) {
        if (o instanceof Map) return new TreeMap<>((Map<?, ?>) o).toString();
        List<String> l = new ArrayList<>();
        for (Object x : (Collection<?>) o) l.add(String.valueOf(x));
        Collections.sort(l);
        return l.toString();
    }

    static void mapViews(String label, Map<String, Integer> m, boolean sorted, boolean weak) {
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        Collection<Integer> values = m.values();
        Set<String> keys = m.keySet();
        Set<Map.Entry<String, Integer>> entries = m.entrySet();
        m.put("d", 4);
        System.out.println(label + " live sizes " + values.size() + " " + keys.size() + " " + entries.size());
        System.out.println(label + " live contains " + keys.contains("d") + " " + values.contains(4));
        m.remove("a");
        System.out.println(label + " after map remove " + sh(keys) + " " + sh(values) + " " + sh(entries));
        keys.remove("b");
        System.out.println(label + " after key view remove " + sh(m));
        values.remove(3);
        System.out.println(label + " after value view remove " + sh(m));
        m.put("x", 10);
        m.put("y", 20);
        m.put("z", 30);
        values.removeIf(v -> v == 20);
        System.out.println(label + " removeIf values " + sh(m));
        keys.removeIf(k -> k.equals("x"));
        System.out.println(label + " removeIf keys " + sh(m));
        entries.removeIf(e -> e.getValue() == 30);
        System.out.println(label + " removeIf entries " + sh(m));
        m.put("p", 1);
        m.put("q", 2);
        for (Iterator<Integer> it = values.iterator(); it.hasNext(); ) {
            if (it.next() == 1) it.remove();
        }
        System.out.println(label + " iterator remove values " + sh(m));
        probe(label + " setValue", () -> {
            for (Map.Entry<String, Integer> e : entries) {
                e.setValue(e.getValue() * 100);
            }
        });
        System.out.println(label + " setValue " + sh(m));
        keys.retainAll(Set.of("nothing"));
        System.out.println(label + " retainAll " + sh(m) + " " + m.isEmpty());
        m.put("k", 1);
        values.clear();
        System.out.println(label + " values clear " + m.size());
        m.put("k", 1);
        keys.clear();
        System.out.println(label + " keys clear " + m.size());
        m.put("k", 1);
        entries.clear();
        System.out.println(label + " entries clear " + m.size());
        probe(label + " add key", () -> keys.add("n"));
        probe(label + " add value", () -> values.add(1));
        probe(label + " add entry", () -> entries.add(Map.entry("n", 1)));
        m.put("k1", 1);
        m.put("k2", 2);
        Iterator<String> it = keys.iterator();
        it.next();
        m.put("k3", 3);
        if (!weak) {
            probe(label + " cme after put", () -> it.next());
        }
        Iterator<String> it2 = keys.iterator();
        probe(label + " remove before next", it2::remove);
        it2.next();
        it2.remove();
        probe(label + " double remove", it2::remove);
        System.out.println(label + " size after iterator remove " + m.size());
        m.put("e1", 1);
        Map.Entry<String, Integer> en = m.entrySet().iterator().next();
        System.out.println(label + " entry equals " + en.equals(Map.entry(en.getKey(), en.getValue())));
        System.out.println(label + " view equals " + keys.equals(new HashSet<>(m.keySet())));
        System.out.println(label + " toArray " + keys.toArray().length + " " + values.toArray(new Integer[0]).length);
    }

    static void listViews() {
        List<Integer> base = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        List<Integer> sub = base.subList(1, 5);
        base.set(2, 30);
        System.out.println("sublist live set " + sub);
        sub.set(0, 20);
        System.out.println("sublist write through " + base);
        sub.remove(Integer.valueOf(4));
        System.out.println("sublist remove " + base + " " + sub);
        sub.add(99);
        System.out.println("sublist add " + base + " " + sub);
        sub.clear();
        System.out.println("sublist clear " + base + " " + sub.size());
        base.add(7);
        probe("sublist cme size", sub::size);
        probe("sublist cme get", () -> sub.get(0));
        List<Integer> l2 = new ArrayList<>(List.of(5, 3, 1, 4));
        List<Integer> s2 = l2.subList(0, 3);
        Collections.sort(s2);
        System.out.println("sublist sort " + l2);
        s2.replaceAll(x -> x * 2);
        System.out.println("sublist replaceAll " + l2);
        List<Integer> s3 = s2.subList(1, 3);
        s3.remove(0);
        System.out.println("nested sublist " + l2 + " " + s2 + " " + s3);
        LinkedList<Integer> ll = new LinkedList<>(List.of(1, 2, 3, 4));
        List<Integer> ls = ll.subList(1, 3);
        ll.addFirst(0);
        probe("linked sublist cme", ls::size);
        List<Integer> al = new ArrayList<>(List.of(1, 2, 3));
        Iterator<Integer> it = al.iterator();
        it.next();
        al.add(4);
        probe("arraylist iterator cme", it::next);
        ListIterator<Integer> li = al.listIterator();
        li.next();
        li.set(10);
        li.add(11);
        li.next();
        li.remove();
        System.out.println("listiterator " + al);
        List<Integer> rev = al.reversed();
        al.add(50);
        System.out.println("reversed live " + rev);
        rev.add(0, 60);
        System.out.println("reversed write " + al);
        List<Integer> view = Collections.unmodifiableList(al);
        al.add(70);
        System.out.println("unmodifiable live " + view.size() + " " + view.get(view.size() - 1));
        probe("unmodifiable write", () -> view.add(1));
        List<Integer> syn = Collections.synchronizedList(al);
        syn.add(80);
        System.out.println("synchronized live " + al.get(al.size() - 1));
        List<Integer> checked = Collections.checkedList(al, Integer.class);
        checked.add(90);
        System.out.println("checked live " + al.get(al.size() - 1));
    }

    static void navigable() {
        TreeMap<Integer, String> tm = new TreeMap<>();
        for (int i = 1; i <= 8; i++) tm.put(i, "v" + i);
        NavigableMap<Integer, String> head = tm.headMap(5, true);
        NavigableMap<Integer, String> tail = tm.tailMap(4, false);
        NavigableMap<Integer, String> sub = tm.subMap(2, true, 6, false);
        NavigableMap<Integer, String> desc = tm.descendingMap();
        tm.put(0, "v0");
        tm.put(5, "five");
        tm.remove(3);
        System.out.println("head " + head + " tail " + tail + " sub " + sub + " desc " + desc);
        head.remove(1);
        sub.put(2, "two");
        tail.remove(7);
        System.out.println("through views " + tm);
        probe("sub out of range put", () -> sub.put(9, "no"));
        probe("head out of range put", () -> head.put(7, "no"));
        System.out.println("pollFirst " + head.pollFirstEntry() + " " + tm.firstKey());
        System.out.println("desc pollFirst " + desc.pollFirstEntry() + " " + tm.lastKey());
        NavigableSet<Integer> ks = tm.navigableKeySet();
        tm.put(100, "h");
        System.out.println("navigableKeySet live " + ks + " " + ks.descendingSet());
        ks.remove(100);
        System.out.println("keyset remove " + tm.containsKey(100));
        NavigableSet<Integer> hs = ks.headSet(4, true);
        hs.clear();
        System.out.println("headSet clear " + tm);
        TreeSet<String> ts = new TreeSet<>(List.of("a", "b", "c", "d", "e"));
        SortedSet<String> tsub = ts.subSet("b", "e");
        ts.add("bb");
        ts.remove("c");
        System.out.println("treeset sub " + tsub);
        tsub.add("cc");
        System.out.println("treeset write " + ts);
        probe("treeset range", () -> tsub.add("z"));
        Iterator<String> it = ts.iterator();
        it.next();
        ts.add("zz");
        probe("treeset cme", it::next);
        ConcurrentSkipListMap<Integer, String> sk = new ConcurrentSkipListMap<>();
        for (int i = 1; i <= 6; i++) sk.put(i, "s" + i);
        ConcurrentNavigableMap<Integer, String> skh = sk.headMap(4);
        sk.put(0, "z");
        sk.remove(2);
        System.out.println("skip head " + skh);
        skh.remove(1);
        System.out.println("skip write " + sk.keySet() + " " + sk.descendingKeySet());
        Iterator<Integer> sit = sk.keySet().iterator();
        sit.next();
        sk.put(99, "w");
        System.out.println("skip weakly consistent " + sit.next());
    }

    static void concurrent() {
        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
        chm.put("a", 1);
        chm.put("b", 2);
        Set<String> ks = chm.keySet();
        Collection<Integer> vs = chm.values();
        Set<Map.Entry<String, Integer>> es = chm.entrySet();
        chm.put("c", 3);
        System.out.println("chm live " + ks.size() + " " + vs.size() + " " + es.size());
        Iterator<String> it = ks.iterator();
        it.next();
        chm.put("d", 4);
        it.next();
        System.out.println("chm weakly consistent ok");
        ks.remove("a");
        vs.remove(2);
        System.out.println("chm view remove " + new TreeMap<>(chm));
        for (Map.Entry<String, Integer> e : es) {
            e.setValue(e.getValue() + 1);
        }
        System.out.println("chm setValue " + new TreeMap<>(chm));
        probe("chm keyset add", () -> ks.add("zz"));
        Set<String> keySetWithDefault = chm.keySet(0);
        keySetWithDefault.add("def");
        System.out.println("chm keySet(default) " + chm.get("def"));
        Set<String> nks = ConcurrentHashMap.newKeySet();
        nks.add("x");
        System.out.println("newKeySet " + nks);
        CopyOnWriteArrayList<Integer> cow = new CopyOnWriteArrayList<>(List.of(1, 2, 3));
        Iterator<Integer> cit = cow.iterator();
        cow.add(4);
        int n = 0;
        while (cit.hasNext()) { cit.next(); n++; }
        System.out.println("cow snapshot " + n);
        probe("cow iterator remove", () -> { Iterator<Integer> i = cow.iterator(); i.next(); i.remove(); });
        List<Integer> csub = cow.subList(1, 3);
        cow.add(5);
        probe("cow sublist cme", csub::size);
    }

    static void special() {
        EnumMap<Color, Integer> em = new EnumMap<>(Color.class);
        em.put(Color.RED, 1);
        em.put(Color.GREEN, 2);
        Set<Color> ek = em.keySet();
        Collection<Integer> ev = em.values();
        Set<Map.Entry<Color, Integer>> ee = em.entrySet();
        em.put(Color.BLUE, 3);
        System.out.println("enummap live " + ek + " " + ev + " " + ee.size());
        ek.remove(Color.RED);
        ev.remove(2);
        System.out.println("enummap view remove " + em);
        for (Map.Entry<Color, Integer> e : ee) e.setValue(e.getValue() * 7);
        System.out.println("enummap setValue " + em);
        Iterator<Color> it = ek.iterator();
        it.next();
        em.put(Color.BLACK, 4);
        System.out.println("enummap no cme " + it.hasNext());
        EnumSet<Color> es = EnumSet.of(Color.RED, Color.BLUE);
        Iterator<Color> sit = es.iterator();
        sit.next();
        es.add(Color.GREEN);
        System.out.println("enumset iterator " + sit.next());
        IdentityHashMap<String, Integer> im = new IdentityHashMap<>();
        String k1 = new String("k");
        String k2 = new String("k");
        im.put(k1, 1);
        im.put(k2, 2);
        Set<String> ik = im.keySet();
        Collection<Integer> iv = im.values();
        Set<Map.Entry<String, Integer>> ie = im.entrySet();
        System.out.println("identity sizes " + ik.size() + " " + iv.size() + " " + ie.size() + " "
            + ik.contains(new String("k")));
        im.put(new String("j"), 3);
        System.out.println("identity live " + ik.size());
        ik.remove(k1);
        iv.remove(3);
        System.out.println("identity view remove " + im.size());
        for (Map.Entry<String, Integer> e : ie) e.setValue(55);
        System.out.println("identity setValue " + sh(im.values()));
        Iterator<String> iit = ik.iterator();
        iit.next();
        im.put(new String("m"), 9);
        probe("identity cme", iit::next);
        Hashtable<String, Integer> ht = new Hashtable<>();
        ht.put("a", 1);
        ht.put("b", 2);
        Set<String> hk = ht.keySet();
        Collection<Integer> hv = ht.values();
        Set<Map.Entry<String, Integer>> he = ht.entrySet();
        ht.put("c", 3);
        System.out.println("hashtable live " + hk.size() + " " + hv.size() + " " + he.size());
        hk.remove("a");
        hv.remove(2);
        System.out.println("hashtable view remove " + ht);
        for (Map.Entry<String, Integer> e : he) e.setValue(8);
        System.out.println("hashtable setValue " + ht);
        Iterator<String> hit = hk.iterator();
        hit.next();
        ht.put("d", 4);
        probe("hashtable cme", hit::next);
        Enumeration<String> en = ht.keys();
        System.out.println("hashtable enumeration " + en.hasMoreElements());
        probe("hashtable keyset add", () -> hk.add("q"));
    }

    static void wrappers() {
        Map<String, Integer> backing = new HashMap<>();
        backing.put("a", 1);
        Map<String, Integer> um = Collections.unmodifiableMap(backing);
        Set<String> uk = um.keySet();
        Collection<Integer> uv = um.values();
        backing.put("b", 2);
        System.out.println("unmodifiableMap live " + um.size() + " " + uk.size() + " " + uv.size());
        probe("unmodifiable keyset remove", () -> uk.remove("a"));
        probe("unmodifiable values clear", uv::clear);
        probe("unmodifiable entry setValue", () -> um.entrySet().iterator().next().setValue(3));
        probe("unmodifiable iterator remove", () -> { Iterator<String> i = uk.iterator(); i.next(); i.remove(); });
        Map<String, Integer> sm = Collections.synchronizedMap(backing);
        Set<String> sk = sm.keySet();
        sm.put("c", 3);
        System.out.println("synchronizedMap live " + sk.size());
        sk.remove("a");
        System.out.println("synchronizedMap view remove " + backing.containsKey("a"));
        Map<String, Integer> cm = Collections.checkedMap(backing, String.class, Integer.class);
        cm.put("d", 4);
        System.out.println("checkedMap live " + backing.get("d") + " " + cm.keySet().size());
        probe("checkedMap bad put", () -> { Map raw = cm; raw.put(1, 2); });
        Set<String> kset = Collections.newSetFromMap(new HashMap<>());
        kset.add("x");
        System.out.println("newSetFromMap " + kset);
        List<Integer> src = new ArrayList<>(List.of(1, 2, 3));
        List<Integer> nc = Collections.nCopies(3, 7);
        System.out.println("nCopies " + nc + " " + nc.subList(1, 3));
        List<Integer> as = Arrays.asList(1, 2, 3);
        as.set(0, 9);
        probe("asList add", () -> as.add(1));
        System.out.println("asList " + as);
        Integer[] arr = {3, 2, 1};
        List<Integer> view = Arrays.asList(arr);
        Collections.sort(view);
        System.out.println("asList writes through " + Arrays.toString(arr));
        Collection<Integer> uc = Collections.unmodifiableCollection(src);
        src.add(4);
        System.out.println("unmodifiableCollection live " + uc);
        Set<Integer> us = Collections.unmodifiableSet(new TreeSet<>(src));
        System.out.println("unmodifiableSet " + us);
        SortedMap<Integer, String> usm = Collections.unmodifiableSortedMap(new TreeMap<>(Map.of(1, "a", 2, "b", 3, "c")));
        System.out.println("unmodifiableSortedMap " + usm.headMap(3) + " " + usm.firstKey());
        probe("unmodifiableSortedMap headMap put", () -> usm.headMap(3).put(0, "z"));
        Map<String, Integer> wm = new WeakHashMap<>();
        String key = "strong";
        wm.put(key, 1);
        wm.put("lit", 2);
        Set<String> wk = wm.keySet();
        Collection<Integer> wv = wm.values();
        wm.put("third", 3);
        System.out.println("weakhashmap live " + wk.size() + " " + wv.size());
        wk.remove("lit");
        wv.remove(3);
        System.out.println("weakhashmap view remove " + sh(wm));
        for (Map.Entry<String, Integer> e : wm.entrySet()) e.setValue(5);
        System.out.println("weakhashmap setValue " + sh(wm));
        Map<String, Integer> lhm = new LinkedHashMap<>(16, 0.75f, true);
        lhm.put("a", 1);
        lhm.put("b", 2);
        lhm.put("c", 3);
        Iterator<String> lit = lhm.keySet().iterator();
        lhm.get("a");
        probe("accessOrder cme", lit::next);
        SequencedMap<String, Integer> seq = new LinkedHashMap<>();
        seq.put("x", 1);
        seq.put("y", 2);
        SequencedMap<String, Integer> rseq = seq.reversed();
        seq.put("z", 3);
        System.out.println("sequenced reversed live " + rseq);
        rseq.putFirst("w", 0);
        System.out.println("sequenced reversed write " + seq);
        SequencedCollection<Integer> sv = seq.sequencedValues();
        System.out.println("sequencedValues " + sv + " " + sv.reversed());
    }

    static void modCounts() {
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("a", 1);
        hm.put("b", 2);
        Iterator<String> it = hm.keySet().iterator();
        it.next();
        hm.put("a", 5);
        System.out.println("hashmap value replace no cme " + it.hasNext());
        hm.put("c", 3);
        probe("hashmap structural cme", it::next);
        Iterator<String> it2 = hm.keySet().iterator();
        hm.remove("zzz");
        System.out.println("hashmap noop remove no cme " + it2.hasNext());
        hm.clear();
        probe("hashmap clear cme", it2::next);
        probe("hashmap computeIfAbsent cme", () -> {
            HashMap<Integer, Integer> m = new HashMap<>();
            m.computeIfAbsent(1, k -> { m.put(2, 2); return 1; });
        });
        probe("hashmap forEach cme", () -> {
            HashMap<Integer, Integer> m = new HashMap<>(Map.of(1, 1, 2, 2));
            m.forEach((k, v) -> m.put(k + 10, v));
        });
        probe("hashmap replaceAll cme", () -> {
            HashMap<Integer, Integer> m = new HashMap<>(Map.of(1, 1, 2, 2));
            m.replaceAll((k, v) -> { m.put(99, 99); return v; });
        });
        probe("arraylist forEach cme", () -> {
            List<Integer> l = new ArrayList<>(List.of(1, 2, 3));
            l.forEach(x -> l.add(x));
        });
        probe("arraylist removeIf cme", () -> {
            List<Integer> l = new ArrayList<>(List.of(1, 2, 3));
            l.removeIf(x -> { l.add(1); return false; });
        });
        probe("treemap forEach cme", () -> {
            TreeMap<Integer, Integer> m = new TreeMap<>(Map.of(1, 1, 2, 2));
            m.forEach((k, v) -> m.put(k + 10, v));
        });
        probe("linkedhashmap values cme", () -> {
            LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>(Map.of(1, 1, 2, 2));
            for (Integer v : m.values()) m.put(v + 10, v);
        });
        probe("hashset cme", () -> {
            Set<Integer> s = new HashSet<>(Set.of(1, 2, 3));
            for (Integer v : s) s.add(v + 10);
        });
        probe("arraydeque cme", () -> {
            ArrayDeque<Integer> d = new ArrayDeque<>(List.of(1, 2, 3));
            for (Integer v : d) d.add(v);
        });
        probe("priorityqueue cme", () -> {
            PriorityQueue<Integer> q = new PriorityQueue<>(List.of(1, 2, 3));
            for (Integer v : q) q.add(v + 10);
        });
        probe("vector cme", () -> {
            Vector<Integer> v = new Vector<>(List.of(1, 2, 3));
            for (Integer x : v) v.add(x);
        });
        List<Integer> last = new ArrayList<>(List.of(1, 2, 3));
        int seen = 0;
        for (Integer x : last) {
            seen++;
            if (x == 2) last.remove(x);
        }
        System.out.println("arraylist remove second-to-last no cme " + seen);
        HashMap<String, Integer> big = new HashMap<>();
        for (int i = 0; i < 100; i++) big.put("k" + i, i);
        Collection<Integer> bv = big.values();
        for (int i = 0; i < 100; i += 2) big.remove("k" + i);
        System.out.println("hashmap resize live " + bv.size() + " " + big.keySet().size());
        for (int i = 100; i < 300; i++) big.put("k" + i, i);
        System.out.println("hashmap grow live " + bv.size() + " " + big.entrySet().size());
        bv.removeIf(v -> v % 3 == 0);
        System.out.println("hashmap big removeIf " + big.size());
    }

    public static void main(String[] args) {
        mapViews("HashMap", new HashMap<>(), false, false);
        mapViews("LinkedHashMap", new LinkedHashMap<>(), false, false);
        mapViews("TreeMap", new TreeMap<>(), true, false);
        mapViews("ConcurrentHashMap", new ConcurrentHashMap<>(), false, true);
        mapViews("ConcurrentSkipListMap", new ConcurrentSkipListMap<>(), true, true);
        mapViews("Hashtable", new Hashtable<>(), false, false);
        mapViews("IdentityHashMap", new IdentityHashMap<>(), false, false);
        mapViews("WeakHashMap", new WeakHashMap<>(), false, false);
        mapViews("SyncMap", Collections.synchronizedMap(new HashMap<>()), false, false);
        mapViews("CheckedMap", Collections.checkedMap(new HashMap<>(), String.class, Integer.class), false, false);
        listViews();
        navigable();
        concurrent();
        special();
        wrappers();
        modCounts();
        System.out.println("ALL COLLECTION VIEW TESTS DONE");
    }
}
