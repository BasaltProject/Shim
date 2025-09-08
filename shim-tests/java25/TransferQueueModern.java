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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TransferQueueModern {

  public static void main(String[] args) throws Exception {
    testBasicOperations();
    testTryTransferNoConsumer();
    testTryTransferWithConsumer();
    testBlockingTransfer();
    testDrainAndBulkOperations();
    System.out.println("ALL TESTS PASSED");
  }

  private static void testBasicOperations() {
    TransferQueue<String> queue = new LinkedTransferQueue<String>();
    if (!queue.isEmpty() || queue.size() != 0 || queue.hasWaitingConsumer() || queue.getWaitingConsumerCount() != 0) {
      throw new AssertionError("unexpected initial state");
    }

    queue.offer("A");
    queue.offer("B");
    queue.add("C");

    if (queue.size() != 3 || queue.isEmpty()) {
      throw new AssertionError("queue size mismatch");
    }

    if (!"A".equals(queue.peek()) || !"A".equals(queue.poll())) {
      throw new AssertionError("peek or poll mismatch");
    }
    if (!"B".equals(queue.poll()) || !"C".equals(queue.poll())) {
      throw new AssertionError("poll sequence mismatch");
    }
    if (queue.poll() != null || !queue.isEmpty()) {
      throw new AssertionError("expected empty queue");
    }
  }

  private static void testTryTransferNoConsumer() throws Exception {
    TransferQueue<Integer> queue = new LinkedTransferQueue<Integer>();
    if (queue.tryTransfer(42)) {
      throw new AssertionError("tryTransfer should fail when no consumer waits");
    }
    if (queue.size() != 0) {
      throw new AssertionError("tryTransfer must not enqueue on failure");
    }

    boolean timedOut = queue.tryTransfer(99, 10, TimeUnit.MILLISECONDS);
    if (timedOut) {
      throw new AssertionError("timed tryTransfer should fail when no consumer arrives");
    }
    if (queue.size() != 0) {
      throw new AssertionError("timed tryTransfer must not leave element enqueued after timeout");
    }
  }

  private static void testTryTransferWithConsumer() throws Exception {
    final TransferQueue<String> queue = new LinkedTransferQueue<String>();
    final CountDownLatch consumerStarted = new CountDownLatch(1);
    final AtomicReference<String> received = new AtomicReference<String>();

    Thread consumer = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          consumerStarted.countDown();
          received.set(queue.take());
        } catch (InterruptedException ignored) {
        }
      }
    });
    consumer.start();

    consumerStarted.await();
    // Wait until consumer is actually registered as waiting
    while (!queue.hasWaitingConsumer()) {
      Thread.sleep(5);
    }

    if (queue.getWaitingConsumerCount() != 1) {
      throw new AssertionError("expected 1 waiting consumer");
    }

    boolean transferred = queue.tryTransfer("handshake");
    if (!transferred) {
      throw new AssertionError("tryTransfer should succeed with waiting consumer");
    }

    consumer.join(5000);
    if (!"handshake".equals(received.get())) {
      throw new AssertionError("consumer received wrong value: " + received.get());
    }
    if (queue.hasWaitingConsumer()) {
      throw new AssertionError("expected no waiting consumer after transfer");
    }
  }

  private static void testBlockingTransfer() throws Exception {
    final TransferQueue<String> queue = new LinkedTransferQueue<String>();
    final CountDownLatch transferEntered = new CountDownLatch(1);
    final AtomicBoolean transferCompleted = new AtomicBoolean(false);

    Thread producer = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          transferEntered.countDown();
          queue.transfer("blocked-item");
          transferCompleted.set(true);
        } catch (InterruptedException ignored) {
        }
      }
    });
    producer.start();

    transferEntered.await();
    // Give producer thread time to enqueue the transfer node and block
    Thread.sleep(50);

    if (transferCompleted.get()) {
      throw new AssertionError("transfer should remain blocked until consumed");
    }
    if (queue.size() != 1 || !"blocked-item".equals(queue.peek())) {
      throw new AssertionError("queue should contain the transferred item");
    }

    String taken = queue.take();
    if (!"blocked-item".equals(taken)) {
      throw new AssertionError("taken item mismatch: " + taken);
    }

    producer.join(5000);
    if (!transferCompleted.get()) {
      throw new AssertionError("producer transfer should complete after take");
    }
  }

  private static void testDrainAndBulkOperations() {
    LinkedTransferQueue<Integer> queue = new LinkedTransferQueue<Integer>(Arrays.asList(1, 2, 3, 4, 5));
    if (queue.size() != 5) {
      throw new AssertionError("constructor collection size mismatch");
    }

    if (!queue.contains(3) || queue.contains(99)) {
      throw new AssertionError("contains check mismatch");
    }

    queue.remove(3);
    if (queue.size() != 4 || queue.contains(3)) {
      throw new AssertionError("remove check mismatch");
    }

    List<Integer> drained = new ArrayList<Integer>();
    int count = queue.drainTo(drained, 2);
    if (count != 2 || !drained.equals(List.of(1, 2))) {
      throw new AssertionError("drainTo bounded mismatch: " + drained);
    }

    drained.clear();
    queue.drainTo(drained);
    if (!drained.equals(List.of(4, 5))) {
      throw new AssertionError("drainTo remaining mismatch: " + drained);
    }
    if (!queue.isEmpty()) {
      throw new AssertionError("queue should be empty after drain");
    }
  }
}
