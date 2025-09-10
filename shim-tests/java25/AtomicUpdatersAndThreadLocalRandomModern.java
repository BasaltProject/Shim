/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicUpdatersAndThreadLocalRandomModern {

  static class IntHolder {
    volatile int count = 10;
  }

  static class LongHolder {
    volatile long value = 100L;
  }

  static class RefHolder {
    volatile String name = "initial";
  }

  public static void main(String[] args) {
    testAtomicIntegerFieldUpdater();
    testAtomicLongFieldUpdater();
    testAtomicReferenceFieldUpdater();
    testAtomicStampedReference();
    testAtomicMarkableReference();
    testThreadLocalRandom();
    System.out.println("AtomicUpdatersAndThreadLocalRandomModern passed successfully");
  }

  private static void testAtomicIntegerFieldUpdater() {
    AtomicIntegerFieldUpdater<IntHolder> updater =
        AtomicIntegerFieldUpdater.newUpdater(IntHolder.class, "count");
    IntHolder holder = new IntHolder();

    System.out.println("initial int: " + updater.get(holder));
    System.out.println("cas success: " + updater.compareAndSet(holder, 10, 20));
    System.out.println("cas failure: " + updater.compareAndSet(holder, 10, 30));
    System.out.println("after cas: " + updater.get(holder));

    System.out.println("getAndIncrement: " + updater.getAndIncrement(holder));
    System.out.println("incrementAndGet: " + updater.incrementAndGet(holder));
    System.out.println("addAndGet: " + updater.addAndGet(holder, 5));
    System.out.println("getAndAdd: " + updater.getAndAdd(holder, 3));
    System.out.println("current: " + updater.get(holder));

    updater.set(holder, 50);
    System.out.println("after set: " + updater.get(holder));

    System.out.println("getAndUpdate: " + updater.getAndUpdate(holder, x -> x * 2));
    System.out.println("after getAndUpdate: " + updater.get(holder));
    System.out.println("updateAndGet: " + updater.updateAndGet(holder, x -> x + 10));

    System.out.println("getAndAccumulate: " + updater.getAndAccumulate(holder, 5, (a, b) -> a - b));
    System.out.println("after getAndAccumulate: " + updater.get(holder));
    System.out.println("accumulateAndGet: " + updater.accumulateAndGet(holder, 2, (a, b) -> a * b));
  }

  private static void testAtomicLongFieldUpdater() {
    AtomicLongFieldUpdater<LongHolder> updater =
        AtomicLongFieldUpdater.newUpdater(LongHolder.class, "value");
    LongHolder holder = new LongHolder();

    System.out.println("initial long: " + updater.get(holder));
    System.out.println("cas long success: " + updater.compareAndSet(holder, 100L, 200L));
    System.out.println("cas long fail: " + updater.compareAndSet(holder, 100L, 300L));
    System.out.println("after cas long: " + updater.get(holder));

    System.out.println("getAndAdd long: " + updater.getAndAdd(holder, 50L));
    System.out.println("addAndGet long: " + updater.addAndGet(holder, 50L));
    System.out.println("getAndUpdate long: " + updater.getAndUpdate(holder, x -> x / 3L));
    System.out.println("after getAndUpdate long: " + updater.get(holder));
  }

  private static void testAtomicReferenceFieldUpdater() {
    AtomicReferenceFieldUpdater<RefHolder, String> updater =
        AtomicReferenceFieldUpdater.newUpdater(RefHolder.class, String.class, "name");
    RefHolder holder = new RefHolder();

    System.out.println("initial ref: " + updater.get(holder));
    System.out.println("cas ref success: " + updater.compareAndSet(holder, "initial", "updated"));
    System.out.println("cas ref fail: " + updater.compareAndSet(holder, "initial", "other"));
    System.out.println("after cas ref: " + updater.get(holder));

    System.out.println("getAndSet ref: " + updater.getAndSet(holder, "replaced"));
    System.out.println("after getAndSet: " + updater.get(holder));

    System.out.println("getAndUpdate ref: " + updater.getAndUpdate(holder, s -> s + "_suffix"));
    System.out.println("after getAndUpdate ref: " + updater.get(holder));
  }

  private static void testAtomicStampedReference() {
    AtomicStampedReference<String> asr = new AtomicStampedReference<>("alpha", 1);
    System.out.println("initial ref: " + asr.getReference());
    System.out.println("initial stamp: " + asr.getStamp());

    int[] stampHolder = new int[1];
    String ref = asr.get(stampHolder);
    System.out.println("holder ref: " + ref);
    System.out.println("holder stamp: " + stampHolder[0]);

    System.out.println("cas asr success: " + asr.compareAndSet("alpha", "beta", 1, 2));
    System.out.println("cas asr wrong stamp: " + asr.compareAndSet("beta", "gamma", 1, 3));
    System.out.println("cas asr wrong ref: " + asr.compareAndSet("alpha", "gamma", 2, 3));
    System.out.println("after cas ref: " + asr.getReference());
    System.out.println("after cas stamp: " + asr.getStamp());

    System.out.println("attemptStamp success: " + asr.attemptStamp("beta", 10));
    System.out.println("attemptStamp fail: " + asr.attemptStamp("wrong", 20));
    System.out.println("stamp after attempt: " + asr.getStamp());

    asr.set("omega", 99);
    System.out.println("after set ref: " + asr.getReference());
    System.out.println("after set stamp: " + asr.getStamp());
  }

  private static void testAtomicMarkableReference() {
    AtomicMarkableReference<String> amr = new AtomicMarkableReference<>("first", false);
    System.out.println("initial mark ref: " + amr.getReference());
    System.out.println("initial mark: " + amr.isMarked());

    boolean[] markHolder = new boolean[1];
    String ref = amr.get(markHolder);
    System.out.println("holder mark ref: " + ref);
    System.out.println("holder mark: " + markHolder[0]);

    System.out.println("cas amr success: " + amr.compareAndSet("first", "second", false, true));
    System.out.println("cas amr wrong mark: " + amr.compareAndSet("second", "third", false, true));
    System.out.println("cas amr wrong ref: " + amr.compareAndSet("first", "third", true, false));
    System.out.println("after cas amr ref: " + amr.getReference());
    System.out.println("after cas amr mark: " + amr.isMarked());

    System.out.println("attemptMark success: " + amr.attemptMark("second", false));
    System.out.println("attemptMark fail: " + amr.attemptMark("wrong", true));
    System.out.println("mark after attempt: " + amr.isMarked());

    amr.set("final", true);
    System.out.println("after set amr ref: " + amr.getReference());
    System.out.println("after set amr mark: " + amr.isMarked());
  }

  private static void testThreadLocalRandom() {
    ThreadLocalRandom tlr = ThreadLocalRandom.current();
    System.out.println("tlr non-null: " + (tlr != null));
    System.out.println("tlr same instance: " + (tlr == ThreadLocalRandom.current()));

    boolean setSeedThrew = false;
    try {
      tlr.setSeed(12345L);
    } catch (UnsupportedOperationException e) {
      setSeedThrew = true;
    }
    System.out.println("setSeed threw UOE: " + setSeedThrew);

    // Bounded ranges
    int rInt = tlr.nextInt(10, 20);
    System.out.println("bounded int in range: " + (rInt >= 10 && rInt < 20));

    long rLong = tlr.nextLong(100L, 200L);
    System.out.println("bounded long in range: " + (rLong >= 100L && rLong < 200L));

    double rDouble = tlr.nextDouble(1.0, 5.0);
    System.out.println("bounded double in range: " + (rDouble >= 1.0 && rDouble < 5.0));

    float rFloat = tlr.nextFloat();
    System.out.println("float non-negative: " + (rFloat >= 0.0f && rFloat <= 1.0f));

    boolean rBool = tlr.nextBoolean();
    System.out.println("bool valid: " + (rBool == true || rBool == false));
  }
}
