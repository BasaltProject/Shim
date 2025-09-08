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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingQueuesModern {

  static class DelayedItem implements Delayed {
    final String value;
    final long expireTimeNanos;

    DelayedItem(String value, long delayMillis) {
      this.value = value;
      this.expireTimeNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis);
    }

    @Override
    public long getDelay(TimeUnit unit) {
      long remaining = expireTimeNanos - System.nanoTime();
      return unit.convert(remaining, TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
      long diff = this.getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
      return Long.signum(diff);
    }
  }

  public static void main(String[] args) throws Exception {
    testPriorityBlockingQueue();
    testDelayQueue();
    testSynchronousQueue();
    testCachedThreadPool();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testPriorityBlockingQueue() throws Exception {
    PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<Integer>();
    queue.offer(40);
    queue.offer(10);
    queue.offer(30);
    queue.offer(20);

    if (queue.size() != 4 || queue.peek() != 10) {
      throw new AssertionError("unexpected initial state in PriorityBlockingQueue");
    }

    if (queue.poll() != 10 || queue.poll() != 20 || queue.poll() != 30 || queue.poll() != 40) {
      throw new AssertionError("unexpected priority ordering");
    }

    PriorityBlockingQueue<String> custom =
        new PriorityBlockingQueue<String>(5, Comparator.reverseOrder());
    custom.add("apple");
    custom.add("pear");
    custom.add("banana");

    List<String> drained = new ArrayList<String>();
    custom.drainTo(drained);
    if (!drained.equals(List.of("pear", "banana", "apple"))) {
      throw new AssertionError("reverse comparator mismatch: " + drained);
    }
  }

  private static void testDelayQueue() throws Exception {
    DelayQueue<DelayedItem> queue = new DelayQueue<DelayedItem>();
    queue.offer(new DelayedItem("late", 100));
    queue.offer(new DelayedItem("early", 10));

    if (queue.size() != 2) {
      throw new AssertionError("delay queue size mismatch");
    }

    // Immediately polling shouldn't return late or early before 10ms
    DelayedItem item = queue.poll();
    if (item != null && item.getDelay(TimeUnit.NANOSECONDS) > 0) {
      throw new AssertionError("poll returned unexpired item");
    }

    // take blocks until early is ready
    DelayedItem first = queue.take();
    if (!first.value.equals("early")) {
      throw new AssertionError("expected early item, got: " + first.value);
    }

    DelayedItem second = queue.take();
    if (!second.value.equals("late")) {
      throw new AssertionError("expected late item, got: " + second.value);
    }
  }

  private static void testSynchronousQueue() throws Exception {
    SynchronousQueue<String> queue = new SynchronousQueue<String>();
    if (!queue.isEmpty() || queue.size() != 0 || queue.peek() != null) {
      throw new AssertionError("synchronous queue contract mismatch");
    }

    if (queue.offer("test")) {
      throw new AssertionError("offer without waiting consumer must return false");
    }

    CountDownLatch handedOver = new CountDownLatch(1);
    CountDownLatch threadDone = new CountDownLatch(1);

    Thread consumer = new Thread(() -> {
      try {
        handedOver.countDown();
        String item = queue.take();
        if (!item.equals("handover")) {
          throw new AssertionError("unexpected item: " + item);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        threadDone.countDown();
      }
    });

    consumer.start();
    handedOver.await();
    queue.put("handover");

    if (!threadDone.await(2, TimeUnit.SECONDS)) {
      throw new AssertionError("handover timed out");
    }
    consumer.join();
  }

  private static void testCachedThreadPool() throws Exception {
    ExecutorService executor = Executors.newCachedThreadPool();
    int taskCount = 10;
    CountDownLatch latch = new CountDownLatch(taskCount);
    AtomicInteger sum = new AtomicInteger(0);

    for (int i = 0; i < taskCount; i++) {
      final int val = i;
      executor.submit(() -> {
        sum.addAndGet(val);
        latch.countDown();
      });
    }

    if (!latch.await(5, TimeUnit.SECONDS)) {
      throw new AssertionError("cached thread pool execution timed out");
    }
    executor.shutdown();
    if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
      executor.shutdownNow();
    }
    if (sum.get() != 45) {
      throw new AssertionError("sum mismatch: " + sum.get());
    }
  }
}
