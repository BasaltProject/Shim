/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.util.*;
import java.util.function.Function;

public class CollectionsModern {
    public static void main(String[] args) {
        testListOf();
        testListSequenced();
        testListMutations();
        testSetOf();
        testMapOf();
        testMapDefaultMethods();
        testSequencedMap();
        System.out.println("ALL COLLECTIONS MODERN TESTS PASSED");
    }

    private static void testListOf() {
        List<String> empty = List.of();
        System.out.println("empty size: " + empty.size());
        System.out.println("empty isEmpty: " + empty.isEmpty());
        System.out.println("empty toString: " + empty);

        List<String> l1 = List.of("a");
        List<String> l2 = List.of("a", "b");
        List<String> l3 = List.of("a", "b", "c");
        List<String> l4 = List.of("a", "b", "c", "d");
        List<String> l5 = List.of("a", "b", "c", "d", "e");
        List<String> l6 = List.of("a", "b", "c", "d", "e", "f");
        List<String> l7 = List.of("a", "b", "c", "d", "e", "f", "g");
        List<String> l8 = List.of("a", "b", "c", "d", "e", "f", "g", "h");
        List<String> l9 = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i");
        List<String> l10 = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        List<String> l11 = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k");

        System.out.println("l1 size: " + l1.size() + " elem: " + l1.get(0));
        System.out.println("l3 size: " + l3.size() + " get(1): " + l3.get(1));
        System.out.println("l11 size: " + l11.size() + " get(10): " + l11.get(10));
        System.out.println("l3 contains 'b': " + l3.contains("b"));
        System.out.println("l3 contains 'z': " + l3.contains("z"));
        System.out.println("l3 indexOf 'c': " + l3.indexOf("c"));

        try {
            l3.contains(null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("contains null: npe");
        }

        try {
            List.of("a", null, "b");
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("List.of null element: npe");
        }

        try {
            l3.add("forbidden");
            throw new AssertionError();
        } catch (UnsupportedOperationException expected) {
            System.out.println("List.of add: unsupported");
        }

        try {
            l3.set(0, "forbidden");
            throw new AssertionError();
        } catch (UnsupportedOperationException expected) {
            System.out.println("List.of set: unsupported");
        }

        List<String> copy1 = List.copyOf(l3);
        System.out.println("copyOf l3 same: " + (copy1 == l3));

        List<String> mutable = new ArrayList<String>();
        mutable.add("x");
        mutable.add("y");
        List<String> copy2 = List.copyOf(mutable);
        System.out.println("copyOf mutable: " + copy2 + " size: " + copy2.size());
    }

    private static void testListSequenced() {
        List<String> list = List.of("first", "second", "third");
        System.out.println("getFirst: " + list.getFirst());
        System.out.println("getLast: " + list.getLast());

        List<String> reversed = list.reversed();
        System.out.println("reversed: " + reversed);
        System.out.println("reversed getFirst: " + reversed.getFirst());
        System.out.println("reversed getLast: " + reversed.getLast());
        System.out.println("reversed get(1): " + reversed.get(1));
        System.out.println("rev rev equals: " + (reversed.reversed() == list));

        List<String> mutable = new ArrayList<String>();
        mutable.add("b");
        mutable.addFirst("a");
        mutable.addLast("c");
        System.out.println("mutable after addFirst/addLast: " + mutable);
        System.out.println("removeFirst: " + mutable.removeFirst());
        System.out.println("removeLast: " + mutable.removeLast());
        System.out.println("mutable after removes: " + mutable);

        LinkedList<String> ll = new LinkedList<String>();
        ll.add("1");
        ll.add("2");
        ll.add("3");
        List<String> llRev = ll.reversed();
        System.out.println("ll reversed: " + llRev);
    }

    private static void testListMutations() {
        List<String> mutable = new ArrayList<String>();
        mutable.add("banana");
        mutable.add("apple");
        mutable.add("cherry");
        mutable.sort(Comparator.naturalOrder());
        System.out.println("sorted: " + mutable);

        mutable.replaceAll(s -> s.toUpperCase());
        System.out.println("replaceAll: " + mutable);
    }

    private static void testSetOf() {
        Set<String> empty = Set.of();
        System.out.println("set empty size: " + empty.size());

        Set<String> s1 = Set.of("a");
        Set<String> s2 = Set.of("a", "b");
        Set<String> s3 = Set.of("a", "b", "c");
        System.out.println("s3 size: " + s3.size());
        System.out.println("s3 contains 'a': " + s3.contains("a"));
        System.out.println("s3 contains 'z': " + s3.contains("z"));

        try {
            s3.contains(null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("set contains null: npe");
        }

        try {
            Set.of("a", "b", "a");
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("Set.of duplicate: iae");
        }

        try {
            Set.of("a", null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("Set.of null: npe");
        }

        List<String> dupList = new ArrayList<String>();
        dupList.add("dup");
        dupList.add("dup");
        Set<String> setFromDups = Set.copyOf(dupList);
        System.out.println("Set.copyOf dedup size: " + setFromDups.size() + " elem: " + setFromDups.iterator().next());
    }

    private static void testMapOf() {
        Map<String, Integer> empty = Map.of();
        System.out.println("map empty size: " + empty.size());

        Map<String, Integer> m1 = Map.of("k1", 1);
        Map<String, Integer> m2 = Map.of("k1", 1, "k2", 2);
        Map<String, Integer> m3 = Map.of("k1", 1, "k2", 2, "k3", 3);
        System.out.println("m3 size: " + m3.size());
        System.out.println("m3 get k2: " + m3.get("k2"));
        System.out.println("m3 containsKey k1: " + m3.containsKey("k1"));
        System.out.println("m3 containsValue 3: " + m3.containsValue(3));

        try {
            Map.of("k1", 1, "k1", 2);
            throw new AssertionError();
        } catch (IllegalArgumentException expected) {
            System.out.println("Map.of duplicate key: iae");
        }

        try {
            Map.of("k1", null);
            throw new AssertionError();
        } catch (NullPointerException expected) {
            System.out.println("Map.of null value: npe");
        }

        Map.Entry<String, Integer> e1 = Map.entry("hello", 42);
        System.out.println("entry: " + e1.getKey() + "=" + e1.getValue());

        Map<String, Integer> mEntries = Map.ofEntries(
            Map.entry("alpha", 1),
            Map.entry("beta", 2)
        );
        System.out.println("ofEntries size: " + mEntries.size() + " alpha: " + mEntries.get("alpha"));

        Map<String, Integer> copy = Map.copyOf(mEntries);
        System.out.println("copyOf size: " + copy.size() + " same: " + (copy == mEntries));
    }

    private static void testMapDefaultMethods() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key1", "val1");

        System.out.println("getOrDefault existing: " + map.getOrDefault("key1", "default"));
        System.out.println("getOrDefault missing: " + map.getOrDefault("key2", "default"));

        map.putIfAbsent("key1", "newVal");
        map.putIfAbsent("key2", "val2");
        System.out.println("putIfAbsent key1: " + map.get("key1"));
        System.out.println("putIfAbsent key2: " + map.get("key2"));

        System.out.println("remove wrong value: " + map.remove("key1", "wrong"));
        System.out.println("remove right value: " + map.remove("key1", "val1"));
        System.out.println("containsKey key1 after remove: " + map.containsKey("key1"));

        map.replace("key2", "val2", "val2_replaced");
        System.out.println("replace key2: " + map.get("key2"));

        map.computeIfAbsent("key3", k -> "computed_" + k);
        System.out.println("computeIfAbsent key3: " + map.get("key3"));

        map.computeIfPresent("key3", (k, v) -> v + "_present");
        System.out.println("computeIfPresent key3: " + map.get("key3"));

        map.merge("key3", "_merged", (v1, v2) -> v1 + v2);
        System.out.println("merge key3: " + map.get("key3"));

        List<String> collected = new ArrayList<String>();
        map.forEach((k, v) -> collected.add(k + ":" + v));
        Collections.sort(collected);
        System.out.println("forEach keys: " + collected);
    }

    private static void testSequencedMap() {
        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
        lhm.put("one", 1);
        lhm.put("two", 2);
        lhm.put("three", 3);

        System.out.println("firstEntry: " + lhm.firstEntry().getKey() + "=" + lhm.firstEntry().getValue());
        System.out.println("lastEntry: " + lhm.lastEntry().getKey() + "=" + lhm.lastEntry().getValue());

        Map.Entry<String, Integer> polled = lhm.pollFirstEntry();
        System.out.println("polled first: " + polled.getKey() + "=" + polled.getValue());
        System.out.println("after poll size: " + lhm.size());
    }
}
