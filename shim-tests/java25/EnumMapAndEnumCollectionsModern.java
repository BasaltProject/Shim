/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class EnumMapAndEnumCollectionsModern {

  enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  public static void main(String[] args) {
    testEnumMapBasic();
    testEnumMapNullValues();
    testEnumMapViewsAndEntries();
    testEnumMapCopyAndEquals();
    testEnumSetOperations();
    System.out.println("EnumMapAndEnumCollectionsModern passed successfully");
  }

  private static void testEnumMapBasic() {
    EnumMap<Priority, String> map = new EnumMap<>(Priority.class);
    System.out.println("initial size: " + map.size());
    System.out.println("initial isEmpty: " + map.isEmpty());

    map.put(Priority.LOW, "P3");
    map.put(Priority.HIGH, "P1");
    map.put(Priority.CRITICAL, "P0");

    System.out.println("size after 3 puts: " + map.size());
    System.out.println("get LOW: " + map.get(Priority.LOW));
    System.out.println("get MEDIUM: " + map.get(Priority.MEDIUM));
    System.out.println("containsKey LOW: " + map.containsKey(Priority.LOW));
    System.out.println("containsKey MEDIUM: " + map.containsKey(Priority.MEDIUM));
    System.out.println("containsValue P1: " + map.containsValue("P1"));
    System.out.println("containsValue P4: " + map.containsValue("P4"));

    String removed = map.remove(Priority.LOW);
    System.out.println("removed LOW: " + removed);
    System.out.println("size after remove: " + map.size());
    System.out.println("containsKey LOW after remove: " + map.containsKey(Priority.LOW));

    boolean npeThrew = false;
    try {
      map.put(null, "error");
    } catch (NullPointerException e) {
      npeThrew = true;
    }
    System.out.println("put null key threw NPE: " + npeThrew);
  }

  private static void testEnumMapNullValues() {
    EnumMap<Priority, String> map = new EnumMap<>(Priority.class);
    map.put(Priority.MEDIUM, null);

    System.out.println("null value containsKey: " + map.containsKey(Priority.MEDIUM));
    System.out.println("null value containsValue: " + map.containsValue(null));
    System.out.println("null value get: " + map.get(Priority.MEDIUM));
    System.out.println("null value size: " + map.size());

    map.remove(Priority.MEDIUM);
    System.out.println("after remove containsKey: " + map.containsKey(Priority.MEDIUM));
    System.out.println("after remove containsValue: " + map.containsValue(null));
  }

  private static void testEnumMapViewsAndEntries() {
    EnumMap<Priority, Integer> map = new EnumMap<>(Priority.class);
    map.put(Priority.LOW, 10);
    map.put(Priority.MEDIUM, 20);
    map.put(Priority.CRITICAL, 40);

    Set<Priority> keys = map.keySet();
    System.out.println("keySet size: " + keys.size());
    System.out.println("keySet contains LOW: " + keys.contains(Priority.LOW));
    System.out.println("keySet contains HIGH: " + keys.contains(Priority.HIGH));

    Collection<Integer> vals = map.values();
    System.out.println("values size: " + vals.size());
    System.out.println("values contains 20: " + vals.contains(20));
    System.out.println("values contains 99: " + vals.contains(99));

    Set<Map.Entry<Priority, Integer>> entries = map.entrySet();
    System.out.println("entrySet size: " + entries.size());

    for (Map.Entry<Priority, Integer> entry : entries) {
      if (entry.getKey() == Priority.MEDIUM) {
        entry.setValue(25);
      }
    }
    System.out.println("get MEDIUM after setValue: " + map.get(Priority.MEDIUM));
  }

  private static void testEnumMapCopyAndEquals() {
    EnumMap<Priority, String> original = new EnumMap<>(Priority.class);
    original.put(Priority.LOW, "val1");
    original.put(Priority.HIGH, "val2");

    EnumMap<Priority, String> copy = new EnumMap<>(original);
    System.out.println("copy equals original: " + copy.equals(original));
    System.out.println("copy hash equals original: " + (copy.hashCode() == original.hashCode()));

    EnumMap<Priority, String> cloned = original.clone();
    System.out.println("cloned equals original: " + cloned.equals(original));

    cloned.put(Priority.CRITICAL, "val3");
    System.out.println("cloned equals after mutation: " + cloned.equals(original));
  }

  private static void testEnumSetOperations() {
    EnumSet<Priority> all = EnumSet.allOf(Priority.class);
    System.out.println("allOf size: " + all.size());

    EnumSet<Priority> none = EnumSet.noneOf(Priority.class);
    System.out.println("noneOf size: " + none.size());

    EnumSet<Priority> range = EnumSet.range(Priority.LOW, Priority.HIGH);
    System.out.println("range size: " + range.size());
    System.out.println("range contains LOW: " + range.contains(Priority.LOW));
    System.out.println("range contains HIGH: " + range.contains(Priority.HIGH));
    System.out.println("range contains CRITICAL: " + range.contains(Priority.CRITICAL));

    EnumSet<Priority> comp = EnumSet.complementOf(range);
    System.out.println("complement size: " + comp.size());
    System.out.println("complement contains CRITICAL: " + comp.contains(Priority.CRITICAL));
  }
}
