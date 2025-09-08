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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class DequesModern {
  public static void main(String[] args) throws Exception {
    testLinkedBlockingDequeBasic();
    testLinkedBlockingDequeCapacityAndBlocking();
    testConcurrentLinkedDequeBasic();
    testConcurrentLinkedDequeMultithreaded();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testLinkedBlockingDequeBasic() {
    LinkedBlockingDeque<String> deque = new LinkedBlockingDeque<String>();
    if (!deque.isEmpty() || deque.size() != 0) {
      throw new AssertionError("new deque not empty");
    }

    deque.addFirst("B");
    deque.addFirst("A");
    deque.addLast("C");
    // State: [A, B, C]

    if (deque.size() != 3) {
      throw new AssertionError("size expected 3, was " + deque.size());
    }
    if (!deque.getFirst().equals("A") || !deque.getLast().equals("C")) {
      throw new AssertionError("ends mismatch");
    }

    // Test iteration forward
    List<String> list = new ArrayList<String>();
    for (String s : deque) {
      list.add(s);
    }
    if (!list.equals(List.of("A", "B", "C"))) {
      throw new AssertionError("forward iter mismatch: " + list);
    }

    // Test descending iteration
    List<String> desc = new ArrayList<String>();
    for (Iterator<String> it = deque.descendingIterator(); it.hasNext(); ) {
      desc.add(it.next());
    }
    if (!desc.equals(List.of("C", "B", "A"))) {
      throw new AssertionError("descending iter mismatch: " + desc);
    }

    // Test stack operations
    deque.push("Z"); // [Z, A, B, C]
    if (!deque.pop().equals("Z")) {
      throw new AssertionError("pop mismatch");
    }

    // Test poll and remove
    if (!deque.pollFirst().equals("A") || !deque.pollLast().equals("C")) {
      throw new AssertionError("poll mismatch");
    }
    if (!deque.removeFirst().equals("B")) {
      throw new AssertionError("removeFirst mismatch");
    }
    if (!deque.isEmpty()) {
      throw new AssertionError("expected empty");
    }

    try {
      deque.removeFirst();
      throw new AssertionError("expected NoSuchElementException");
    } catch (NoSuchElementException expected) {
    }
  }

  private static void testLinkedBlockingDequeCapacityAndBlocking() throws Exception {
    LinkedBlockingDeque<Integer> bounded = new LinkedBlockingDeque<Integer>(2);
    if (bounded.remainingCapacity() != 2) {
      throw new AssertionError("expected remaining capacity 2");
    }

    bounded.putLast(10);
    bounded.putLast(20);
    if (bounded.remainingCapacity() != 0) {
      throw new AssertionError("expected remaining capacity 0");
    }
    if (bounded.offerLast(30)) {
      throw new AssertionError("offer on full deque should fail");
    }

    boolean timedOut = !bounded.offerLast(30, 20, TimeUnit.MILLISECONDS);
    if (!timedOut) {
      throw new AssertionError("timed offer should time out");
    }

    CountDownLatch latch = new CountDownLatch(1);
    Thread t = new Thread(() -> {
      try {
        int val = bounded.takeFirst();
        if (val == 10) {
          latch.countDown();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    t.start();
    if (!latch.await(2, TimeUnit.SECONDS)) {
      throw new AssertionError("takeFirst thread timed out");
    }
    t.join();

    if (bounded.size() != 1 || !bounded.peekFirst().equals(20)) {
      throw new AssertionError("expected [20]");
    }

    List<Integer> drained = new ArrayList<Integer>();
    int count = bounded.drainTo(drained);
    if (count != 1 || !drained.equals(List.of(20)) || !bounded.isEmpty()) {
      throw new AssertionError("drainTo mismatch");
    }
  }

  private static void testConcurrentLinkedDequeBasic() {
    ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<String>();
    deque.addFirst("two");
    deque.addFirst("one");
    deque.addLast("three");
    // [one, two, three]

    if (deque.size() != 3) {
      throw new AssertionError("expected size 3");
    }
    if (!deque.peekFirst().equals("one") || !deque.peekLast().equals("three")) {
      throw new AssertionError("peeks mismatch");
    }

    deque.removeFirstOccurrence("two");
    if (deque.size() != 2 || deque.contains("two")) {
      throw new AssertionError("occurrence removal failed");
    }

    if (!deque.poll().equals("one") || !deque.poll().equals("three")) {
      throw new AssertionError("poll mismatch");
    }
    if (deque.poll() != null) {
      throw new AssertionError("poll on empty should return null");
    }
  }

  private static void testConcurrentLinkedDequeMultithreaded() throws Exception {
    ConcurrentLinkedDeque<Integer> deque = new ConcurrentLinkedDeque<Integer>();
    int numThreads = 4;
    int itemsPerThread = 250;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(numThreads);

    for (int i = 0; i < numThreads; i++) {
      final int threadId = i;
      new Thread(() -> {
        try {
          startLatch.await();
          for (int j = 0; j < itemsPerThread; j++) {
            if ((threadId & 1) == 0) {
              deque.addFirst(threadId * itemsPerThread + j);
            } else {
              deque.addLast(threadId * itemsPerThread + j);
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          doneLatch.countDown();
        }
      }).start();
    }

    startLatch.countDown();
    if (!doneLatch.await(5, TimeUnit.SECONDS)) {
      throw new AssertionError("multithreaded producer timed out");
    }

    int expectedTotal = numThreads * itemsPerThread;
    if (deque.size() != expectedTotal) {
      throw new AssertionError("expected total " + expectedTotal + ", got " + deque.size());
    }

    int consumed = 0;
    while (deque.pollFirst() != null) {
      consumed++;
    }
    if (consumed != expectedTotal || !deque.isEmpty()) {
      throw new AssertionError("consumed mismatch: " + consumed);
    }
  }
}
