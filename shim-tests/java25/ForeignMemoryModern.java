/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 25

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ForeignMemoryModern {
  public static void main(String[] args) throws Throwable {
    testNullSegment();
    testArenaLifecycle();
    testNativeSegmentOperations();
    testHeapSegmentOperations();
    testSlicingAllocator();
    testLayoutsAndHandles();
    testFunctionDescriptorAndLinker();
    System.out.println("ALL FOREIGN MEMORY MODERN TESTS PASSED");
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new AssertionError(message + ": expected <" + expected + ">, but was <" + actual + ">");
    }
  }

  private static void testNullSegment() {
    MemorySegment nullSeg = MemorySegment.NULL;
    assertEquals(0L, nullSeg.address(), "NULL address must be 0");
    assertEquals(0L, nullSeg.byteSize(), "NULL byteSize must be 0");
    assertTrue(nullSeg.isNative(), "NULL must be native");
    assertTrue(!nullSeg.isReadOnly(), "NULL must not be read-only");
    assertTrue(nullSeg.scope().isAlive(), "NULL scope must be alive");
    assertEquals(nullSeg, MemorySegment.NULL, "NULL must equal MemorySegment.NULL");
    assertEquals(nullSeg, MemorySegment.ofAddress(0L), "NULL must equal ofAddress(0)");
  }

  private static void testArenaLifecycle() throws Exception {
    Arena global = Arena.global();
    assertTrue(global.scope().isAlive(), "Global arena scope must be alive");
    try {
      global.close();
      throw new AssertionError("global.close() must throw UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
      // expected
    }

    Arena auto = Arena.ofAuto();
    assertTrue(auto.scope().isAlive(), "Auto arena scope must be alive");
    try {
      auto.close();
      throw new AssertionError("auto.close() must throw UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
      // expected
    }

    MemorySegment closedSeg;
    try (Arena confined = Arena.ofConfined()) {
      assertTrue(confined.scope().isAlive(), "Confined arena scope must be alive before close");
      closedSeg = confined.allocate(16L);
      closedSeg.set(ValueLayout.JAVA_INT, 0L, 42);
      assertEquals(42, closedSeg.get(ValueLayout.JAVA_INT, 0L), "Read before close");

      AtomicBoolean otherThreadOk = new AtomicBoolean(false);
      Thread t = new Thread(() -> {
        try {
          closedSeg.get(ValueLayout.JAVA_INT, 0L);
        } catch (WrongThreadException expected) {
          otherThreadOk.set(true);
        } catch (Throwable other) {
          // not expected
        }
      });
      t.start();
      t.join();
      assertTrue(otherThreadOk.get(), "Access from non-owner thread must throw WrongThreadException");
    }

    assertTrue(!closedSeg.scope().isAlive(), "Scope must be closed after try-with-resources");
    try {
      closedSeg.get(ValueLayout.JAVA_INT, 0L);
      throw new AssertionError("Access on closed segment must throw IllegalStateException");
    } catch (IllegalStateException expected) {
      // expected
    }

    try (Arena shared = Arena.ofShared()) {
      MemorySegment seg = shared.allocate(16L);
      seg.set(ValueLayout.JAVA_INT, 0L, 100);
      AtomicBoolean sharedThreadOk = new AtomicBoolean(false);
      Thread t = new Thread(() -> {
        try {
          int val = seg.get(ValueLayout.JAVA_INT, 0L);
          if (val == 100) {
            sharedThreadOk.set(true);
          }
        } catch (Throwable ignored) {
        }
      });
      t.start();
      t.join();
      assertTrue(sharedThreadOk.get(), "Shared arena must allow access from another thread");
    }
  }

  private static void testNativeSegmentOperations() {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment seg = arena.allocate(64L, 8L);
      assertEquals(64L, seg.byteSize(), "Allocated size must be 64");
      assertTrue(seg.isNative(), "Allocated segment must be native");

      seg.set(ValueLayout.JAVA_BYTE, 0L, (byte) 12);
      assertEquals((byte) 12, seg.get(ValueLayout.JAVA_BYTE, 0L), "byte access");

      seg.set(ValueLayout.JAVA_BOOLEAN, 1L, true);
      assertTrue(seg.get(ValueLayout.JAVA_BOOLEAN, 1L), "boolean access");

      seg.set(ValueLayout.JAVA_SHORT, 2L, (short) 1234);
      assertEquals((short) 1234, seg.get(ValueLayout.JAVA_SHORT, 2L), "short access");

      seg.set(ValueLayout.JAVA_CHAR, 4L, 'Z');
      assertEquals('Z', seg.get(ValueLayout.JAVA_CHAR, 4L), "char access");

      seg.set(ValueLayout.JAVA_INT, 8L, 0x12345678);
      assertEquals(0x12345678, seg.get(ValueLayout.JAVA_INT, 8L), "int access");

      seg.set(ValueLayout.JAVA_FLOAT, 12L, 3.14159f);
      assertEquals(3.14159f, seg.get(ValueLayout.JAVA_FLOAT, 12L), "float access");

      seg.set(ValueLayout.JAVA_LONG, 16L, 0x0102030405060708L);
      assertEquals(0x0102030405060708L, seg.get(ValueLayout.JAVA_LONG, 16L), "long access");

      seg.set(ValueLayout.JAVA_DOUBLE, 24L, 2.718281828459045);
      assertEquals(2.718281828459045, seg.get(ValueLayout.JAVA_DOUBLE, 24L), "double access");

      MemorySegment sub = arena.allocate(8L);
      sub.set(ValueLayout.JAVA_INT, 0L, 999);
      seg.set(ValueLayout.ADDRESS, 32L, sub);
      MemorySegment deref = seg.get(ValueLayout.ADDRESS, 32L);
      assertEquals(sub.address(), deref.address(), "Address layout dereference address");

      MemorySegment strSeg = arena.allocateFrom("Hello, Java 25!");
      assertEquals("Hello, Java 25!", strSeg.getString(0L), "getString");

      MemorySegment customStr = arena.allocate(32L);
      customStr.setString(0L, "Basalt Shim");
      assertEquals("Basalt Shim", customStr.getString(0L), "setString and getString");

      MemorySegment fillSeg = arena.allocate(8L);
      fillSeg.fill((byte) 0x7F);
      for (long i = 0; i < 8L; i++) {
        assertEquals((byte) 0x7F, fillSeg.get(ValueLayout.JAVA_BYTE, i), "fill at " + i);
      }

      MemorySegment arrSeg = arena.allocateFrom(ValueLayout.JAVA_INT, 10, 20, 30, 40);
      assertEquals(16L, arrSeg.byteSize(), "int array size");
      assertEquals(20, arrSeg.getAtIndex(ValueLayout.JAVA_INT, 1L), "getAtIndex 1");
      arrSeg.setAtIndex(ValueLayout.JAVA_INT, 2L, 99);
      assertEquals(99, arrSeg.getAtIndex(ValueLayout.JAVA_INT, 2L), "after setAtIndex");

      int[] extracted = arrSeg.toArray(ValueLayout.JAVA_INT);
      assertTrue(Arrays.equals(new int[]{10, 20, 99, 40}, extracted), "toArray matches");

      MemorySegment copyDst = arena.allocate(16L);
      copyDst.copyFrom(arrSeg);
      assertEquals(-1L, arrSeg.mismatch(copyDst), "mismatch after copyFrom must be -1");

      copyDst.setAtIndex(ValueLayout.JAVA_INT, 3L, 9999);
      assertEquals(12L, arrSeg.mismatch(copyDst), "mismatch offset must be 12");

      MemorySegment slice = arrSeg.asSlice(4L, 8L);
      assertEquals(8L, slice.byteSize(), "slice byteSize");
      assertEquals(20, slice.get(ValueLayout.JAVA_INT, 0L), "slice element 0");
      assertEquals(99, slice.get(ValueLayout.JAVA_INT, 4L), "slice element 1");

      List<Integer> streamList = arrSeg.elements(ValueLayout.JAVA_INT)
          .map(s -> s.get(ValueLayout.JAVA_INT, 0L))
          .toList();
      assertEquals(Arrays.asList(10, 20, 99, 40), streamList, "elements stream");
    }
  }

  private static void testHeapSegmentOperations() {
    int[] array = new int[]{100, 200, 300, 400};
    MemorySegment heapSeg = MemorySegment.ofArray(array);
    assertTrue(!heapSeg.isNative(), "Heap segment is not native");
    assertEquals(0L, heapSeg.address(), "Heap segment address must be 0");
    assertTrue(heapSeg.heapBase().isPresent(), "Heap segment must have heapBase");
    assertEquals(array, heapSeg.heapBase().get(), "HeapBase must match underlying array");
    assertEquals(16L, heapSeg.byteSize(), "Heap segment size must be 16");

    assertEquals(200, heapSeg.getAtIndex(ValueLayout.JAVA_INT, 1L), "getAtIndex heap");
    heapSeg.setAtIndex(ValueLayout.JAVA_INT, 2L, 888);
    assertEquals(888, array[2], "Mutation via heap segment must modify underlying array");

    try (Arena arena = Arena.ofConfined()) {
      MemorySegment nativeSeg = arena.allocate(16L);
      MemorySegment.copy(heapSeg, 0L, nativeSeg, 0L, 16L);
      assertEquals(-1L, heapSeg.mismatch(nativeSeg), "Heap to native copy mismatch must be -1");
      assertEquals(888, nativeSeg.getAtIndex(ValueLayout.JAVA_INT, 2L), "Native element after copy");
    }
  }

  private static void testSlicingAllocator() {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment block = arena.allocate(64L);
      SegmentAllocator slicing = SegmentAllocator.slicingAllocator(block);

      MemorySegment s1 = slicing.allocate(ValueLayout.JAVA_INT);
      assertEquals(4L, s1.byteSize(), "s1 size");
      s1.set(ValueLayout.JAVA_INT, 0L, 123);

      MemorySegment s2 = slicing.allocate(ValueLayout.JAVA_LONG);
      assertEquals(8L, s2.byteSize(), "s2 size");
      s2.set(ValueLayout.JAVA_LONG, 0L, 456L);

      assertEquals(123, s1.get(ValueLayout.JAVA_INT, 0L), "s1 value retained");
      assertEquals(456L, s2.get(ValueLayout.JAVA_LONG, 0L), "s2 value retained");

      try {
        slicing.allocate(100L);
        throw new AssertionError("slicing allocate exceeding capacity must throw IndexOutOfBoundsException");
      } catch (IndexOutOfBoundsException expected) {
        // expected
      }
    }
  }

  private static void testLayoutsAndHandles() throws Throwable {
    StructLayout point = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT.withName("x"),
        ValueLayout.JAVA_INT.withName("y"),
        MemoryLayout.paddingLayout(8L).withName("pad")
    );
    assertEquals(16L, point.byteSize(), "point struct size");
    assertEquals(4L, point.byteAlignment(), "point struct alignment");
    assertEquals(0L, point.byteOffset(MemoryLayout.PathElement.groupElement("x")), "offset x");
    assertEquals(4L, point.byteOffset(MemoryLayout.PathElement.groupElement("y")), "offset y");

    SequenceLayout points = MemoryLayout.sequenceLayout(5L, point);
    assertEquals(80L, points.byteSize(), "points sequence size");
    assertEquals(5L, points.elementCount(), "points element count");

    SequenceLayout flattened = points.flatten();
    assertEquals(points.byteSize(), flattened.byteSize(), "flattened byteSize matches");

    UnionLayout union = MemoryLayout.unionLayout(
        ValueLayout.JAVA_INT.withName("asInt"),
        ValueLayout.JAVA_LONG.withName("asLong")
    );
    assertEquals(8L, union.byteSize(), "union size must be max member size");
    assertEquals(8L, union.byteAlignment(), "union alignment must be max member alignment");
    assertEquals(0L, union.byteOffset(MemoryLayout.PathElement.groupElement("asInt")), "union offset asInt");
    assertEquals(0L, union.byteOffset(MemoryLayout.PathElement.groupElement("asLong")), "union offset asLong");

    MethodHandle scaleH = ValueLayout.JAVA_INT.scaleHandle();
    long scaled = (long) scaleH.invokeWithArguments(10L, 3L);
    assertEquals(22L, scaled, "scaleHandle result: 10 + 3*4 = 22");

    MethodHandle offsetH = point.byteOffsetHandle(MemoryLayout.PathElement.groupElement("y"));
    long yOff = (long) offsetH.invokeWithArguments(0L);
    assertEquals(4L, yOff, "byteOffsetHandle for y");

    VarHandle vhX = point.varHandle(MemoryLayout.PathElement.groupElement("x"));
    VarHandle vhY = point.varHandle(MemoryLayout.PathElement.groupElement("y"));
    MethodHandle vhXSet = vhX.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle vhXGet = vhX.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle vhYSet = vhY.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle vhYGet = vhY.toMethodHandle(VarHandle.AccessMode.GET);
    MethodHandle vhXCas = vhX.toMethodHandle(VarHandle.AccessMode.COMPARE_AND_SET);
    MethodHandle vhXGas = vhX.toMethodHandle(VarHandle.AccessMode.GET_AND_SET);
    MethodHandle vhXGaa = vhX.toMethodHandle(VarHandle.AccessMode.GET_AND_ADD);

    try (Arena arena = Arena.ofConfined()) {
      MemorySegment seg = arena.allocate(point);
      vhXSet.invokeWithArguments(seg, 0L, 111);
      vhYSet.invokeWithArguments(seg, 0L, 222);
      assertEquals(111, (int) vhXGet.invokeWithArguments(seg, 0L), "read x via VarHandle");
      assertEquals(222, (int) vhYGet.invokeWithArguments(seg, 0L), "read y via VarHandle");

      boolean casOk = (boolean) vhXCas.invokeWithArguments(seg, 0L, 111, 333);
      assertTrue(casOk, "CAS on VarHandle must succeed");
      assertEquals(333, (int) vhXGet.invokeWithArguments(seg, 0L), "read after CAS");

      int oldVal = (int) vhXGas.invokeWithArguments(seg, 0L, 444);
      assertEquals(333, oldVal, "getAndSet old value");
      assertEquals(444, (int) vhXGet.invokeWithArguments(seg, 0L), "read after getAndSet");

      int prev = (int) vhXGaa.invokeWithArguments(seg, 0L, 6);
      assertEquals(444, prev, "getAndAdd old value");
      assertEquals(450, (int) vhXGet.invokeWithArguments(seg, 0L), "read after getAndAdd");
    }

    VarHandle arrVh = ValueLayout.JAVA_INT.arrayElementVarHandle();
    MethodHandle arrVhSet = arrVh.toMethodHandle(VarHandle.AccessMode.SET);
    MethodHandle arrVhGet = arrVh.toMethodHandle(VarHandle.AccessMode.GET);
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment seg = arena.allocate(16L);
      arrVhSet.invokeWithArguments(seg, 0L, 2L, 777);
      assertEquals(777, (int) arrVhGet.invokeWithArguments(seg, 0L, 2L), "arrayElementVarHandle read");
    }
  }

  private static void testFunctionDescriptorAndLinker() {
    FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG);
    assertTrue(fd.returnLayout().isPresent(), "Return layout must be present");
    assertEquals(2, fd.argumentLayouts().size(), "Two argument layouts");
    assertEquals("(a8j8)i4", fd.toString(), "FunctionDescriptor toString");

    FunctionDescriptor voidFd = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
    assertTrue(voidFd.returnLayout().isEmpty(), "Return layout must be empty for void");
    assertEquals("(i4)v", voidFd.toString(), "Void descriptor toString");

    Linker linker = Linker.nativeLinker();
    assertTrue(linker != null, "Linker must not be null");
    assertTrue(linker.canonicalLayouts().containsKey("int"), "Canonical layouts contains int");
    assertTrue(linker.canonicalLayouts().containsKey("void*"), "Canonical layouts contains void*");

    SymbolLookup loader = SymbolLookup.loaderLookup();
    assertTrue(loader.find("non_existent_symbol_12345").isEmpty(), "Missing symbol must be empty");
  }
}
