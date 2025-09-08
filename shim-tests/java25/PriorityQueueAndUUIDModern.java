// Minimum release: 21
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

public class PriorityQueueAndUUIDModern {
  public static void main(String[] args) {
    testPriorityQueueBasic();
    testPriorityQueueComparator();
    testPriorityQueueCollectionConstructor();
    testPriorityQueueRemovalAndIteration();
    testUUIDOperations();
    testBitSetModern();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testPriorityQueueBasic() {
    PriorityQueue<Integer> pq = new PriorityQueue<>();
    if (!pq.isEmpty()) throw new AssertionError("expected empty pq");
    if (pq.size() != 0) throw new AssertionError("expected size 0");
    if (pq.peek() != null) throw new AssertionError("expected null peek");
    if (pq.poll() != null) throw new AssertionError("expected null poll");

    pq.offer(42);
    pq.offer(10);
    pq.offer(55);
    pq.offer(5);
    pq.offer(23);

    if (pq.size() != 5) throw new AssertionError("expected size 5");
    if (pq.peek() != 5) throw new AssertionError("expected min 5");

    List<Integer> extracted = new ArrayList<>();
    while (!pq.isEmpty()) {
      extracted.add(pq.poll());
    }

    if (!extracted.equals(Arrays.asList(5, 10, 23, 42, 55))) {
      throw new AssertionError("unexpected sort order: " + extracted);
    }

    try {
      pq.offer(null);
      throw new AssertionError("expected NPE on null offer");
    } catch (NullPointerException expected) {
    }
  }

  private static void testPriorityQueueComparator() {
    PriorityQueue<String> maxPq = new PriorityQueue<>(Comparator.reverseOrder());
    maxPq.offer("apple");
    maxPq.offer("pear");
    maxPq.offer("banana");
    maxPq.offer("orange");

    List<String> result = new ArrayList<>();
    while (!maxPq.isEmpty()) {
      result.add(maxPq.poll());
    }

    if (!result.equals(Arrays.asList("pear", "orange", "banana", "apple"))) {
      throw new AssertionError("unexpected reverse order: " + result);
    }
  }

  private static void testPriorityQueueCollectionConstructor() {
    List<Integer> source = Arrays.asList(30, 10, 50, 20, 40);
    PriorityQueue<Integer> pq = new PriorityQueue<>(source);
    if (pq.size() != 5) throw new AssertionError("expected size 5");

    List<Integer> sorted = new ArrayList<>();
    while (!pq.isEmpty()) {
      sorted.add(pq.poll());
    }
    if (!sorted.equals(Arrays.asList(10, 20, 30, 40, 50))) {
      throw new AssertionError("unexpected collection order: " + sorted);
    }
  }

  private static void testPriorityQueueRemovalAndIteration() {
    PriorityQueue<Integer> pq = new PriorityQueue<>();
    for (int i = 1; i <= 10; i++) {
      pq.offer(i * 10);
    }

    if (!pq.contains(50)) throw new AssertionError("expected contains 50");
    if (pq.contains(999)) throw new AssertionError("expected not contains 999");

    boolean removed = pq.remove(50);
    if (!removed) throw new AssertionError("expected removed 50");
    if (pq.contains(50)) throw new AssertionError("expected 50 gone");
    if (pq.size() != 9) throw new AssertionError("expected size 9");

    Iterator<Integer> it = pq.iterator();
    int count = 0;
    while (it.hasNext()) {
      it.next();
      count++;
    }
    if (count != 9) throw new AssertionError("expected 9 items from iterator");

    pq.clear();
    if (pq.size() != 0) throw new AssertionError("expected clear size 0");
    if (!pq.isEmpty()) throw new AssertionError("expected empty after clear");
  }

  private static void testUUIDOperations() {
    UUID u1 = new UUID(0x1234567890abcdefL, 0x0123456789abcdefL);
    if (u1.getMostSignificantBits() != 0x1234567890abcdefL) throw new AssertionError("bad msb");
    if (u1.getLeastSignificantBits() != 0x0123456789abcdefL) throw new AssertionError("bad lsb");

    String s1 = u1.toString();
    if (!s1.equals("12345678-90ab-cdef-0123-456789abcdef")) {
      throw new AssertionError("unexpected uuid string: " + s1);
    }

    UUID parsed = UUID.fromString(s1);
    if (!u1.equals(parsed)) throw new AssertionError("expected equal parsed uuid");
    if (u1.hashCode() != parsed.hashCode()) throw new AssertionError("expected equal hashcode");
    if (u1.compareTo(parsed) != 0) throw new AssertionError("expected compareTo 0");

    UUID nameUuid = UUID.nameUUIDFromBytes("hello world".getBytes());
    if (!nameUuid.toString().equals("5eb63bbb-e01e-3ed0-93cb-22bb8f5acdc3")) {
      throw new AssertionError("unexpected name UUID: " + nameUuid);
    }
    if (nameUuid.version() != 3) throw new AssertionError("expected version 3, got: " + nameUuid.version());
    if (nameUuid.variant() != 2) throw new AssertionError("expected variant 2, got: " + nameUuid.variant());

    UUID random = UUID.randomUUID();
    if (random.version() != 4) throw new AssertionError("expected version 4, got: " + random.version());
    if (random.variant() != 2) throw new AssertionError("expected variant 2, got: " + random.variant());
    if (random.toString().length() != 36) throw new AssertionError("expected 36 chars");
  }

  private static void testBitSetModern() {
    BitSet bs = new BitSet();
    bs.set(0);
    bs.set(2);
    bs.set(3);
    bs.set(65);
    bs.set(130);

    if (bs.cardinality() != 5) throw new AssertionError("expected cardinality 5");
    if (bs.length() != 131) throw new AssertionError("expected length 131, got: " + bs.length());
    if (bs.size() < 131) throw new AssertionError("expected size >= 131");
    if (bs.isEmpty()) throw new AssertionError("expected non empty");

    if (bs.previousSetBit(130) != 130) throw new AssertionError("prevSet(130) failed");
    if (bs.previousSetBit(129) != 65) throw new AssertionError("prevSet(129) failed");
    if (bs.previousSetBit(65) != 65) throw new AssertionError("prevSet(65) failed");
    if (bs.previousSetBit(64) != 3) throw new AssertionError("prevSet(64) failed");
    if (bs.previousSetBit(3) != 3) throw new AssertionError("prevSet(3) failed");
    if (bs.previousSetBit(1) != 0) throw new AssertionError("prevSet(1) failed");
    if (bs.previousSetBit(0) != 0) throw new AssertionError("prevSet(0) failed");
    if (bs.previousSetBit(-1) != -1) throw new AssertionError("prevSet(-1) failed");

    if (bs.previousClearBit(130) != 129) throw new AssertionError("prevClear(130) failed");
    if (bs.previousClearBit(3) != 1) throw new AssertionError("prevClear(3) failed");
    if (bs.previousClearBit(2) != 1) throw new AssertionError("prevClear(2) failed");
    if (bs.previousClearBit(1) != 1) throw new AssertionError("prevClear(1) failed");
    if (bs.previousClearBit(0) != -1) throw new AssertionError("prevClear(0) failed");
    if (bs.previousClearBit(-1) != -1) throw new AssertionError("prevClear(-1) failed");

    byte[] bytes = bs.toByteArray();
    BitSet fromBytes = BitSet.valueOf(bytes);
    if (!bs.equals(fromBytes)) throw new AssertionError("expected equals from toByteArray");
    if (bs.hashCode() != fromBytes.hashCode()) throw new AssertionError("hashcode mismatch on fromBytes");

    long[] longs = bs.toLongArray();
    BitSet fromLongs = BitSet.valueOf(longs);
    if (!bs.equals(fromLongs)) throw new AssertionError("expected equals from toLongArray");
    if (bs.hashCode() != fromLongs.hashCode()) throw new AssertionError("hashcode mismatch on fromLongs");

    int[] streamed = bs.stream().toArray();
    if (!Arrays.equals(streamed, new int[]{0, 2, 3, 65, 130})) {
      throw new AssertionError("stream mismatch: " + Arrays.toString(streamed));
    }

    String repr = bs.toString();
    if (!repr.equals("{0, 2, 3, 65, 130}")) {
      throw new AssertionError("unexpected BitSet toString: " + repr);
    }
  }
}
