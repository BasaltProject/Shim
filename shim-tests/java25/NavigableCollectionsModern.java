/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NavigableCollectionsModern {

  private static void check(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void testTreeSetAndMapNavigation() {
    TreeSet<Integer> set = new TreeSet<Integer>();
    set.add(10);
    set.add(20);
    set.add(30);
    set.add(40);
    set.add(50);

    check(set.size() == 5, "set size");
    check(set.first() == 10, "set first");
    check(set.last() == 50, "set last");
    check(set.lower(30) == 20, "set lower 30");
    check(set.floor(30) == 30, "set floor 30");
    check(set.floor(25) == 20, "set floor 25");
    check(set.ceiling(30) == 30, "set ceiling 30");
    check(set.ceiling(35) == 40, "set ceiling 35");
    check(set.higher(30) == 40, "set higher 30");
    check(set.lower(10) == null, "set lower 10 null");
    check(set.higher(50) == null, "set higher 50 null");

    check(set.getFirst() == 10, "set getFirst");
    check(set.getLast() == 50, "set getLast");

    NavigableSet<Integer> desc = set.descendingSet();
    check(desc.first() == 50, "desc first");
    check(desc.last() == 10, "desc last");
    check(desc.higher(30) == 20, "desc higher 30");

    NavigableSet<Integer> sub = set.subSet(20, true, 40, false);
    check(sub.size() == 2, "subSet size");
    check(sub.contains(20), "subSet contains 20");
    check(sub.contains(30), "subSet contains 30");
    check(!sub.contains(40), "subSet does not contain 40");

    NavigableSet<Integer> head = set.headSet(30, true);
    check(head.size() == 3, "headSet size");
    check(head.last() == 30, "headSet last");

    NavigableSet<Integer> tail = set.tailSet(30, false);
    check(tail.size() == 2, "tailSet size");
    check(tail.first() == 40, "tailSet first");

    check(set.pollFirst() == 10, "pollFirst 10");
    check(set.pollLast() == 50, "pollLast 50");
    check(set.size() == 3, "size after polls");

    TreeMap<String, Integer> map = new TreeMap<String, Integer>();
    map.put("alpha", 1);
    map.put("beta", 2);
    map.put("gamma", 3);
    map.put("delta", 4);

    check(map.size() == 4, "map size");
    check("alpha".equals(map.firstKey()), "map firstKey");
    check("gamma".equals(map.lastKey()), "map lastKey");

    Map.Entry<String, Integer> floorEntry = map.floorEntry("charlie");
    check(floorEntry != null && "beta".equals(floorEntry.getKey()), "floorEntry charlie");

    Map.Entry<String, Integer> ceilingEntry = map.ceilingEntry("charlie");
    check(ceilingEntry != null && "delta".equals(ceilingEntry.getKey()), "ceilingEntry charlie");

    check("alpha".equals(map.lowerKey("beta")), "lowerKey beta");
    check("gamma".equals(map.higherKey("delta")), "higherKey delta");

    NavigableMap<String, Integer> subMap = map.subMap("beta", true, "gamma", true);
    check(subMap.size() == 3, "subMap size");
    check(subMap.containsKey("beta"), "subMap contains beta");
    check(subMap.containsKey("delta"), "subMap contains delta");
    check(subMap.containsKey("gamma"), "subMap contains gamma");
    check(!subMap.containsKey("alpha"), "subMap excludes alpha");

    NavigableMap<String, Integer> descMap = map.descendingMap();
    check("gamma".equals(descMap.firstKey()), "descMap firstKey");
    check("alpha".equals(descMap.lastKey()), "descMap lastKey");

    check(map.pollFirstEntry().getValue() == 1, "pollFirstEntry value");
    check(map.pollLastEntry().getValue() == 3, "pollLastEntry value");
    check(map.size() == 2, "map size after poll");
  }

  private static void testConcurrentSkipList() throws Exception {
    ConcurrentSkipListMap<Integer, String> cMap = new ConcurrentSkipListMap<Integer, String>();
    cMap.put(100, "hundred");
    cMap.put(200, "two hundred");
    cMap.put(300, "three hundred");

    check(cMap.size() == 3, "cMap size");
    check(cMap.firstKey() == 100, "cMap firstKey");
    check(cMap.lastKey() == 300, "cMap lastKey");
    check("two hundred".equals(cMap.get(200)), "cMap get 200");

    check(cMap.putIfAbsent(200, "duplicate") != null, "putIfAbsent existing");
    check(cMap.putIfAbsent(400, "four hundred") == null, "putIfAbsent new");
    check(cMap.size() == 4, "cMap size 4");

    check(!cMap.replace(100, "wrong", "new"), "replace wrong oldValue");
    check(cMap.replace(100, "hundred", "century"), "replace correct oldValue");
    check("century".equals(cMap.get(100)), "cMap get after replace");

    check(cMap.floorKey(250) == 200, "cMap floorKey 250");
    check(cMap.ceilingKey(250) == 300, "cMap ceilingKey 250");
    check(cMap.lowerKey(200) == 100, "cMap lowerKey 200");
    check(cMap.higherKey(300) == 400, "cMap higherKey 300");

    ConcurrentNavigableMap<Integer, String> sub = cMap.subMap(200, true, 400, false);
    check(sub.size() == 2, "cMap subMap size");
    check(sub.containsKey(200), "sub contains 200");
    check(sub.containsKey(300), "sub contains 300");
    check(!sub.containsKey(400), "sub excludes 400");

    ConcurrentNavigableMap<Integer, String> desc = cMap.descendingMap();
    check(desc.firstKey() == 400, "desc firstKey 400");
    check(desc.lastKey() == 100, "desc lastKey 100");

    ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<String>();
    set.add("apple");
    set.add("banana");
    set.add("cherry");
    set.add("date");

    check(set.size() == 4, "cSet size");
    check("apple".equals(set.first()), "cSet first");
    check("date".equals(set.last()), "cSet last");
    check("banana".equals(set.lower("cherry")), "cSet lower cherry");
    check("date".equals(set.higher("cherry")), "cSet higher cherry");

    check(set.pollFirst().equals("apple"), "cSet pollFirst apple");
    check(set.pollLast().equals("date"), "cSet pollLast date");
    check(set.size() == 2, "cSet size after polls");

    int threadCount = 4;
    int itemsPerThread = 100;
    ExecutorService exec = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    ConcurrentSkipListMap<Integer, Integer> concurrentMap =
        new ConcurrentSkipListMap<Integer, Integer>();

    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      exec.submit(() -> {
        try {
          for (int i = 0; i < itemsPerThread; i++) {
            concurrentMap.put(threadId * itemsPerThread + i, i);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    check(latch.await(10, TimeUnit.SECONDS), "threads completed");
    exec.shutdown();
    check(concurrentMap.size() == threadCount * itemsPerThread, "concurrent map total entries");
    check(concurrentMap.firstKey() == 0, "concurrent map firstKey 0");
    check(concurrentMap.lastKey() == (threadCount * itemsPerThread - 1), "concurrent map lastKey max");
  }

  public static void main(String[] args) throws Exception {
    testTreeSetAndMapNavigation();
    testConcurrentSkipList();
    System.out.println("NavigableCollectionsModern OK");
  }
}
