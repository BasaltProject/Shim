// Minimum release: 17
/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicArraysAndAnnotationsModern {
  @Retention(RetentionPolicy.RUNTIME)
  @interface Marker {
    String tag();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface UnusedMarker {}

  @Marker(tag = "test-class")
  static class AnnotatedTarget {}

  public static void main(String[] args) {
    testAtomicReferenceArray();
    testAtomicIntegerArray();
    testAtomicLongArray();
    testAnnotatedElement();
  }

  private static void testAtomicReferenceArray() {
    AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(3);
    expectEquals(3, arr.length());
    expectEquals(null, arr.get(0));

    arr.set(0, "A");
    arr.set(1, "B");
    arr.set(2, "C");
    expectEquals("A", arr.get(0));
    expectEquals("B", arr.get(1));
    expectEquals("C", arr.get(2));

    expectEquals("[A, B, C]", arr.toString());

    // copy constructor
    AtomicReferenceArray<String> copy = new AtomicReferenceArray<>(new String[] { "X", "Y" });
    expectEquals(2, copy.length());
    expectEquals("X", copy.get(0));
    expectEquals("Y", copy.get(1));

    // getAndSet, compareAndSet
    expectEquals("A", arr.getAndSet(0, "A2"));
    expectEquals("A2", arr.get(0));
    expectTrue(arr.compareAndSet(0, "A2", "A3"));
    expectTrue(!arr.compareAndSet(0, "Wrong", "A4"));
    expectEquals("A3", arr.get(0));

    // compareAndExchange
    expectEquals("A3", arr.compareAndExchange(0, "A3", "A_Exchanged"));
    expectEquals("A_Exchanged", arr.get(0));
    expectEquals("A_Exchanged", arr.compareAndExchange(0, "WrongWitness", "Ignored"));
    expectEquals("A_Exchanged", arr.get(0));

    // access modes
    arr.setPlain(1, "B_Plain");
    expectEquals("B_Plain", arr.getPlain(1));
    arr.setOpaque(1, "B_Opaque");
    expectEquals("B_Opaque", arr.getOpaque(1));
    arr.setRelease(1, "B_Release");
    expectEquals("B_Release", arr.getAcquire(1));

    // functional updates
    expectEquals("B_Release", arr.getAndUpdate(1, s -> s + "_Updated"));
    expectEquals("B_Release_Updated", arr.get(1));
    expectEquals("PREFIX:B_Release_Updated", arr.updateAndGet(1, s -> "PREFIX:" + s));

    expectEquals("PREFIX:B_Release_Updated", arr.getAndAccumulate(1, "SUFFIX", (s, acc) -> s + "-" + acc));
    expectEquals("PREFIX:B_Release_Updated-SUFFIX-MORE", arr.accumulateAndGet(1, "MORE", (s, acc) -> s + "-" + acc));

    // bounds check
    try {
      arr.get(3);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException expected) {
      // Expected
    }

    System.out.println("PASS: AtomicReferenceArray");
  }

  private static void testAtomicIntegerArray() {
    AtomicIntegerArray arr = new AtomicIntegerArray(4);
    expectEquals(4, arr.length());
    expectEquals(0, arr.get(0));

    AtomicIntegerArray copy = new AtomicIntegerArray(new int[] { 10, 20, 30 });
    expectEquals(3, copy.length());
    expectEquals(10, copy.get(0));
    expectEquals(20, copy.get(1));
    expectEquals(30, copy.get(2));
    expectEquals("[10, 20, 30]", copy.toString());

    // arithmetic
    expectEquals(10, copy.getAndAdd(0, 5));
    expectEquals(15, copy.get(0));
    expectEquals(25, copy.addAndGet(0, 10));

    expectEquals(25, copy.getAndIncrement(0));
    expectEquals(27, copy.incrementAndGet(0));
    expectEquals(27, copy.getAndDecrement(0));
    expectEquals(25, copy.decrementAndGet(0));

    // compareAndSet, compareAndExchange
    expectTrue(copy.compareAndSet(1, 20, 99));
    expectTrue(!copy.compareAndSet(1, 20, 100));
    expectEquals(99, copy.get(1));

    expectEquals(99, copy.compareAndExchange(1, 99, 150));
    expectEquals(150, copy.get(1));
    expectEquals(150, copy.compareAndExchange(1, 888, 999));
    expectEquals(150, copy.get(1));

    // access modes
    copy.setPlain(2, 50);
    expectEquals(50, copy.getPlain(2));
    copy.setOpaque(2, 60);
    expectEquals(60, copy.getOpaque(2));
    copy.setRelease(2, 70);
    expectEquals(70, copy.getAcquire(2));

    // functional updates
    expectEquals(70, copy.getAndUpdate(2, v -> v * 2));
    expectEquals(140, copy.get(2));
    expectEquals(141, copy.updateAndGet(2, v -> v + 1));

    expectEquals(141, copy.getAndAccumulate(2, 9, (v, x) -> v + x));
    expectEquals(160, copy.accumulateAndGet(2, 10, (v, x) -> v + x));

    // bounds check
    try {
      copy.get(5);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException expected) {
      // Expected
    }

    System.out.println("PASS: AtomicIntegerArray");
  }

  private static void testAtomicLongArray() {
    AtomicLongArray arr = new AtomicLongArray(new long[] { 1000L, 2000L });
    expectEquals(2, arr.length());
    expectEquals(1000L, arr.get(0));
    expectEquals(2000L, arr.get(1));
    expectEquals("[1000, 2000]", arr.toString());

    expectEquals(1000L, arr.getAndAdd(0, 500L));
    expectEquals(1500L, arr.get(0));
    expectEquals(2500L, arr.addAndGet(0, 1000L));

    expectEquals(2500L, arr.getAndIncrement(0));
    expectEquals(2502L, arr.incrementAndGet(0));
    expectEquals(2502L, arr.getAndDecrement(0));
    expectEquals(2500L, arr.decrementAndGet(0));

    expectTrue(arr.compareAndSet(1, 2000L, 7777L));
    expectEquals(7777L, arr.get(1));
    expectEquals(7777L, arr.compareAndExchange(1, 7777L, 8888L));
    expectEquals(8888L, arr.get(1));

    arr.setRelease(1, 9999L);
    expectEquals(9999L, arr.getAcquire(1));

    expectEquals(9999L, arr.getAndUpdate(1, v -> v + 1L));
    expectEquals(10000L, arr.get(1));
    expectEquals(20000L, arr.updateAndGet(1, v -> v * 2L));

    expectEquals(20000L, arr.getAndAccumulate(1, 500L, (v, x) -> v + x));
    expectEquals(21000L, arr.accumulateAndGet(1, 500L, (v, x) -> v + x));

    try {
      arr.get(-1);
      throw new AssertionError("Expected IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException expected) {
      // Expected
    }

    System.out.println("PASS: AtomicLongArray");
  }

  private static void testAnnotatedElement() {
    Class<?> clazz = AnnotatedTarget.class;
    expectTrue(clazz.isAnnotationPresent(Marker.class));
    expectTrue(!clazz.isAnnotationPresent(UnusedMarker.class));

    Marker mDirect = clazz.getDeclaredAnnotation(Marker.class);
    expectTrue(mDirect != null);
    expectEquals("test-class", mDirect.tag());

    UnusedMarker uDirect = clazz.getDeclaredAnnotation(UnusedMarker.class);
    expectEquals(null, uDirect);

    Marker[] mTypes = clazz.getAnnotationsByType(Marker.class);
    expectEquals(1, mTypes.length);
    expectEquals("test-class", mTypes[0].tag());

    Marker[] mDeclTypes = clazz.getDeclaredAnnotationsByType(Marker.class);
    expectEquals(1, mDeclTypes.length);
    expectEquals("test-class", mDeclTypes[0].tag());

    UnusedMarker[] uTypes = clazz.getAnnotationsByType(UnusedMarker.class);
    expectEquals(0, uTypes.length);

    System.out.println("PASS: AnnotatedElement");
  }

  private static void expectEquals(Object expected, Object actual) {
    if (expected == null && actual == null) return;
    if (expected != null && expected.equals(actual)) return;
    throw new AssertionError("Expected " + expected + " but got " + actual);
  }

  private static void expectEquals(long expected, long actual) {
    if (expected != actual) {
      throw new AssertionError("Expected " + expected + " but got " + actual);
    }
  }

  private static void expectTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Condition failed");
    }
  }
}
