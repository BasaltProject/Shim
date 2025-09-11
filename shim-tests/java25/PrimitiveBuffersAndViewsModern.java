/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class PrimitiveBuffersAndViewsModern {

  public static void main(String[] args) {
    testIntBuffer();
    testLongBuffer();
    testShortBuffer();
    testFloatAndDoubleBuffer();
    testByteBufferViews();
    System.out.println("PrimitiveBuffersAndViewsModern passed successfully");
  }

  private static void testIntBuffer() {
    IntBuffer ib = IntBuffer.allocate(5);
    System.out.println("ib capacity: " + ib.capacity());
    System.out.println("ib position: " + ib.position());
    System.out.println("ib limit: " + ib.limit());

    ib.put(10).put(20).put(30);
    System.out.println("ib remaining before flip: " + ib.remaining());
    ib.flip();
    System.out.println("ib remaining after flip: " + ib.remaining());
    System.out.println("ib get 0: " + ib.get());
    System.out.println("ib get 1: " + ib.get());

    int[] wrapArray = new int[] { 100, 200, 300 };
    IntBuffer wrapped = IntBuffer.wrap(wrapArray);
    System.out.println("wrapped hasArray: " + wrapped.hasArray());
    System.out.println("wrapped array[0]: " + wrapped.array()[0]);

    IntBuffer wrapped2 = IntBuffer.wrap(new int[] { 100, 200, 305 });
    System.out.println("mismatch index: " + wrapped.mismatch(wrapped2));
  }

  private static void testLongBuffer() {
    LongBuffer lb = LongBuffer.allocate(4);
    lb.put(1000L).put(2000L);
    lb.flip();
    System.out.println("lb get: " + lb.get());
    System.out.println("lb get index 1: " + lb.get(1));

    LongBuffer wrapped = LongBuffer.wrap(new long[] { 500L, 600L });
    System.out.println("wrapped long hasArray: " + wrapped.hasArray());
    System.out.println("wrapped long get 0: " + wrapped.get(0));
  }

  private static void testShortBuffer() {
    ShortBuffer sb = ShortBuffer.allocate(4);
    sb.put((short) 10).put((short) 20);
    sb.flip();
    System.out.println("sb get: " + sb.get());
    System.out.println("sb remaining: " + sb.remaining());
  }

  private static void testFloatAndDoubleBuffer() {
    FloatBuffer fb = FloatBuffer.allocate(3);
    fb.put(1.5f).put(2.5f);
    fb.flip();
    System.out.println("fb get: " + fb.get());

    DoubleBuffer db = DoubleBuffer.allocate(3);
    db.put(3.14159).put(2.71828);
    db.flip();
    System.out.println("db get: " + (db.get() > 3.14 && db.get() < 3.15));
  }

  private static void testByteBufferViews() {
    ByteBuffer bb = ByteBuffer.allocate(32);

    // Int view
    IntBuffer ib = bb.asIntBuffer();
    System.out.println("asIntBuffer capacity: " + ib.capacity());
    ib.put(0x12345678);
    System.out.println("bb raw getInt: " + (bb.getInt(0) == 0x12345678));
    bb.putInt(4, 0x0ABCDEF0);
    System.out.println("ib view get 1: " + (ib.get(1) == 0x0ABCDEF0));

    // Long view
    LongBuffer lb = bb.asLongBuffer();
    System.out.println("asLongBuffer capacity: " + lb.capacity());
    lb.put(1, 0x1122334455667788L);
    System.out.println("bb raw getLong: " + (bb.getLong(8) == 0x1122334455667788L));

    // Short view
    ShortBuffer sb = bb.asShortBuffer();
    sb.put(10, (short) 0x1234);
    System.out.println("bb raw getShort: " + (bb.getShort(20) == (short) 0x1234));

    // Float view
    FloatBuffer fb = bb.asFloatBuffer();
    fb.put(6, 42.5f);
    System.out.println("bb raw getFloat: " + (bb.getFloat(24) == 42.5f));

    // Char view
    ByteBuffer charBb = ByteBuffer.allocate(8);
    CharBuffer cb = charBb.asCharBuffer();
    cb.put('J').put('a').put('v').put('a');
    cb.flip();
    System.out.println("char view toString: " + cb.toString());
  }
}
