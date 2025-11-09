/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SequencedCollectionsModern {

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null && actual == null) {
      return;
    }
    if (expected != null && expected.equals(actual)) {
      return;
    }
    throw new AssertionError(message + " expected: " + expected + ", actual: " + actual);
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void testLinkedHashSet() {
    LinkedHashSet<String> set = new LinkedHashSet<>();
    set.add("b");
    set.addFirst("a");
    set.addLast("c");

    assertEquals(3, set.size(), "set size");
    assertEquals("a", set.getFirst(), "getFirst");
    assertEquals("c", set.getLast(), "getLast");

    set.addFirst("c");
    assertEquals("c", set.getFirst(), "getFirst after addFirst reposition");
    assertEquals("b", set.getLast(), "getLast after addFirst reposition");

    SequencedSet<String> rev = set.reversed();
    assertEquals("b", rev.getFirst(), "reversed getFirst");
    assertEquals("c", rev.getLast(), "reversed getLast");

    assertEquals("c", set.removeFirst(), "removeFirst");
    assertEquals("b", set.removeLast(), "removeLast");
    assertEquals(1, set.size(), "size after removes");
    assertEquals("a", set.getFirst(), "remaining element");
  }

  private static void testLinkedHashMap() {
    LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
    map.put("b", 2);
    map.putFirst("a", 1);
    map.putLast("c", 3);

    assertEquals(3, map.size(), "map size");
    assertEquals("a", map.firstEntry().getKey(), "firstEntry key");
    assertEquals(1, map.firstEntry().getValue(), "firstEntry value");
    assertEquals("c", map.lastEntry().getKey(), "lastEntry key");
    assertEquals(3, map.lastEntry().getValue(), "lastEntry value");

    map.putLast("a", 10);
    assertEquals("b", map.firstEntry().getKey(), "firstEntry after putLast reposition");
    assertEquals("a", map.lastEntry().getKey(), "lastEntry after putLast reposition");
    assertEquals(10, map.lastEntry().getValue(), "lastEntry value updated");

    SequencedSet<String> keys = map.sequencedKeySet();
    assertEquals("b", keys.getFirst(), "sequencedKeySet getFirst");
    assertEquals("a", keys.getLast(), "sequencedKeySet getLast");

    Map.Entry<String, Integer> polled = map.pollFirstEntry();
    assertEquals("b", polled.getKey(), "polled key");
    assertEquals(2, polled.getValue(), "polled value");
    assertEquals(2, map.size(), "map size after poll");
  }

  private static void testCollectionsSequencedWrappers() {
    List<String> list = new ArrayList<>(List.of("x", "y", "z"));
    SequencedCollection<String> unmodSeqColl = Collections.unmodifiableSequencedCollection(list);
    assertEquals("x", unmodSeqColl.getFirst(), "unmodSeqColl getFirst");
    assertEquals("z", unmodSeqColl.getLast(), "unmodSeqColl getLast");
    assertEquals("z", unmodSeqColl.reversed().getFirst(), "unmodSeqColl reversed getFirst");

    try {
      unmodSeqColl.addFirst("w");
      throw new AssertionError("addFirst should fail on unmodifiableSequencedCollection");
    } catch (UnsupportedOperationException expected) {
    }

    LinkedHashSet<String> set = new LinkedHashSet<>(List.of("1", "2", "3"));
    SequencedSet<String> unmodSeqSet = Collections.unmodifiableSequencedSet(set);
    assertEquals("1", unmodSeqSet.getFirst(), "unmodSeqSet getFirst");
    assertEquals("3", unmodSeqSet.getLast(), "unmodSeqSet getLast");

    LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
    map.put("k1", 100);
    map.put("k2", 200);
    SequencedMap<String, Integer> unmodSeqMap = Collections.unmodifiableSequencedMap(map);
    assertEquals("k1", unmodSeqMap.firstEntry().getKey(), "unmodSeqMap firstEntry");
    assertEquals("k2", unmodSeqMap.lastEntry().getKey(), "unmodSeqMap lastEntry");

    try {
      unmodSeqMap.putFirst("k0", 0);
      throw new AssertionError("putFirst should fail on unmodifiableSequencedMap");
    } catch (UnsupportedOperationException expected) {
    }
  }

  private static void testSetFromMapAndLifoQueue() {
    LinkedHashMap<String, Boolean> map = new LinkedHashMap<>();
    SequencedSet<String> seqSet = Collections.newSequencedSetFromMap(map);
    seqSet.add("alpha");
    seqSet.addFirst("omega");
    seqSet.addLast("beta");

    assertEquals("omega", seqSet.getFirst(), "newSequencedSetFromMap getFirst");
    assertEquals("beta", seqSet.getLast(), "newSequencedSetFromMap getLast");
    assertEquals("omega", seqSet.removeFirst(), "newSequencedSetFromMap removeFirst");
    assertEquals(2, seqSet.size(), "newSequencedSetFromMap size");

    HashMap<String, Boolean> plainMap = new HashMap<>();
    Set<String> plainSet = Collections.newSetFromMap(plainMap);
    plainSet.add("item");
    assertTrue(plainSet.contains("item"), "newSetFromMap contains");
    assertEquals(1, plainSet.size(), "newSetFromMap size");

    Deque<String> deque = new ArrayDeque<>();
    Queue<String> lifo = Collections.asLifoQueue(deque);
    lifo.offer("first");
    lifo.offer("second");
    assertEquals("second", lifo.poll(), "asLifoQueue poll second");
    assertEquals("first", lifo.poll(), "asLifoQueue poll first");
    assertTrue(lifo.isEmpty(), "asLifoQueue empty");
  }

  private static void testEmptyAndNavigableCollections() {
    NavigableSet<String> emptyNavSet = Collections.emptyNavigableSet();
    assertTrue(emptyNavSet.isEmpty(), "emptyNavigableSet isEmpty");
    assertEquals(0, emptyNavSet.size(), "emptyNavigableSet size");
    assertEquals(emptyNavSet, Collections.emptySortedSet(), "emptySortedSet equals");

    NavigableMap<String, Integer> emptyNavMap = Collections.emptyNavigableMap();
    assertTrue(emptyNavMap.isEmpty(), "emptyNavigableMap isEmpty");
    assertEquals(0, emptyNavMap.size(), "emptyNavigableMap size");
    assertEquals(emptyNavMap, Collections.emptySortedMap(), "emptySortedMap equals");

    TreeSet<Integer> treeSet = new TreeSet<>(List.of(10, 20, 30));
    NavigableSet<Integer> unmodNavSet = Collections.unmodifiableNavigableSet(treeSet);
    assertEquals(Integer.valueOf(10), unmodNavSet.first(), "unmodNavSet first");
    assertEquals(Integer.valueOf(30), unmodNavSet.last(), "unmodNavSet last");
    assertEquals(Integer.valueOf(20), unmodNavSet.floor(25), "unmodNavSet floor");
    assertEquals(Integer.valueOf(30), unmodNavSet.ceiling(25), "unmodNavSet ceiling");

    TreeMap<String, Integer> treeMap = new TreeMap<>();
    treeMap.put("a", 1);
    treeMap.put("b", 2);
    treeMap.put("c", 3);
    NavigableMap<String, Integer> unmodNavMap = Collections.unmodifiableNavigableMap(treeMap);
    assertEquals("a", unmodNavMap.firstEntry().getKey(), "unmodNavMap firstEntry");
    assertEquals("c", unmodNavMap.lastEntry().getKey(), "unmodNavMap lastEntry");
    assertEquals("b", unmodNavMap.lowerKey("c"), "unmodNavMap lowerKey");
  }

  public static void main(String[] args) {
    testLinkedHashSet();
    testLinkedHashMap();
    testCollectionsSequencedWrappers();
    testSetFromMapAndLifoQueue();
    testEmptyAndNavigableCollections();
    System.out.println("OK");
  }
}
