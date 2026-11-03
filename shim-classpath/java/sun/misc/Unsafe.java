/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package sun.misc;

import java.lang.reflect.Field;

public final class Unsafe {
  private void Unsafe() { }

  private static final Unsafe theUnsafe = new Unsafe();

  public static Unsafe getUnsafe() {
    return theUnsafe;
  }

  public native long allocateMemory(long bytes);

  public native void setMemory
    (Object base, long offset, long count, byte value);

  public native void freeMemory(long address);

  public Object allocateInstance(Class c) throws InstantiationException {
    if (c.isPrimitive()) {
      throw new InstantiationException();
    }
    if (c.isInterface() || c.isArray() || java.lang.reflect.Modifier.isAbstract(c.getModifiers())) {
      throw new InstantiationException(c.getName());
    }
    return allocateInstance0(c);
  }

  private native Object allocateInstance0(Class c);

  public void invokeCleaner(java.nio.ByteBuffer directBuffer) {
    if (!directBuffer.isDirect()) {
      throw new IllegalArgumentException("Not a direct buffer");
    }
  }

  public native byte getByte(long address);

  public native void putByte(long address, byte x);

  public native short getShort(long address);

  public native void putShort(long address, short x);

  public native char getChar(long address);

  public native void putChar(long address, char x);

  public native int getInt(long address);

  public native void putInt(long address, int x);

  public native long getLong(long address);

  public native void putLong(long address, long x);

  public native float getFloat(long address);

  public native void putFloat(long address, float x);

  public native double getDouble(long address);

  public native void putDouble(long address, double x);

  public native boolean getBooleanVolatile(Object o, long offset);

  public native void putBooleanVolatile(Object o, long offset, boolean x);

  public native byte getByteVolatile(Object o, long offset);

  public native void putByteVolatile(Object o, long offset, byte x);

  public native short getShortVolatile(Object o, long offset);

  public native void putShortVolatile(Object o, long offset, short x);

  public native char getCharVolatile(Object o, long offset);

  public native void putCharVolatile(Object o, long offset, char x);

  public native int getIntVolatile(Object o, long offset);

  public native void putIntVolatile(Object o, long offset, int x);

  public native float getFloatVolatile(Object o, long offset);

  public native void putFloatVolatile(Object o, long offset, float x);

  public native double getDoubleVolatile(Object o, long offset);

  public native void putDoubleVolatile(Object o, long offset, double x);

  public native long getLongVolatile(Object o, long offset);

  public native void putLongVolatile(Object o, long offset, long x);

  public long getLong(Object o, long offset) {
    return getLongVolatile(o, offset);
  }

  public void putLong(Object o, long offset, long x) {
    putLongVolatile(o, offset, x);
  }

  public double getDouble(Object o, long offset) {
    return getDoubleVolatile(o, offset);
  }

  public void putDouble(Object o, long offset, double x) {
    putDoubleVolatile(o, offset, x);
  }

  public native void putOrderedLong(Object o, long offset, long x);

  public native void putOrderedInt(Object o, long offset, int x);

  public native Object getObject(Object o, long offset);

  public native void putObject(Object o, long offset, Object x);

  public native void putObjectVolatile(Object o, long offset, Object x);

  public native void putOrderedObject(Object o, long offset, Object x);

  public native Object getObjectVolatile(Object o, long offset);

  public native long getAddress(long address);

  public native void putAddress(long address, long x);

  public native int arrayBaseOffset(Class arrayClass);

  public native int arrayIndexScale(Class arrayClass);

  public native long objectFieldOffset(Field field);

  public long staticFieldOffset(Field field) {
    return objectFieldOffset(field);
  }

  public Object staticFieldBase(Field field) {
    return field.getDeclaringClass();
  }

  public native void park(boolean absolute, long time);

  public native void unpark(Object target);

  public native void copyMemory(Object srcBase, long srcOffset,
                                Object destBase, long destOffset,
                                long count);

  public native boolean compareAndSwapInt(Object o, long offset, int old,
                                          int new_);

  public native boolean compareAndSwapLong(Object o, long offset,
                                                 long old, long new_);

  public native boolean compareAndSwapObject(Object o, long offset, Object old,
                                             Object new_);

  public void copyMemory(long src, long dst, long count) {
    copyMemory(null, src, null, dst, count);
  }

  public native void throwException(Throwable t);

  public static final int INVALID_FIELD_OFFSET = -1;
  public static final int ARRAY_BOOLEAN_BASE_OFFSET = theUnsafe.arrayBaseOffset(boolean[].class);
  public static final int ARRAY_BYTE_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);
  public static final int ARRAY_SHORT_BASE_OFFSET = theUnsafe.arrayBaseOffset(short[].class);
  public static final int ARRAY_CHAR_BASE_OFFSET = theUnsafe.arrayBaseOffset(char[].class);
  public static final int ARRAY_INT_BASE_OFFSET = theUnsafe.arrayBaseOffset(int[].class);
  public static final int ARRAY_LONG_BASE_OFFSET = theUnsafe.arrayBaseOffset(long[].class);
  public static final int ARRAY_FLOAT_BASE_OFFSET = theUnsafe.arrayBaseOffset(float[].class);
  public static final int ARRAY_DOUBLE_BASE_OFFSET = theUnsafe.arrayBaseOffset(double[].class);
  public static final int ARRAY_OBJECT_BASE_OFFSET = theUnsafe.arrayBaseOffset(Object[].class);
  public static final int ARRAY_BOOLEAN_INDEX_SCALE = theUnsafe.arrayIndexScale(boolean[].class);
  public static final int ARRAY_BYTE_INDEX_SCALE = theUnsafe.arrayIndexScale(byte[].class);
  public static final int ARRAY_SHORT_INDEX_SCALE = theUnsafe.arrayIndexScale(short[].class);
  public static final int ARRAY_CHAR_INDEX_SCALE = theUnsafe.arrayIndexScale(char[].class);
  public static final int ARRAY_INT_INDEX_SCALE = theUnsafe.arrayIndexScale(int[].class);
  public static final int ARRAY_LONG_INDEX_SCALE = theUnsafe.arrayIndexScale(long[].class);
  public static final int ARRAY_FLOAT_INDEX_SCALE = theUnsafe.arrayIndexScale(float[].class);
  public static final int ARRAY_DOUBLE_INDEX_SCALE = theUnsafe.arrayIndexScale(double[].class);
  public static final int ARRAY_OBJECT_INDEX_SCALE = theUnsafe.arrayIndexScale(Object[].class);
  public static final int ADDRESS_SIZE = theUnsafe.addressSize();

  public int addressSize() {
    return "32".equals(System.getProperty("sun.arch.data.model")) ? 4 : 8;
  }

  public int pageSize() {
    return 4096;
  }

  public native long reallocateMemory(long address, long bytes);

  public void setMemory(long address, long bytes, byte value) {
    setMemory(null, address, bytes, value);
  }

  public int getLoadAverage(double[] loadavg, int nelems) {
    if (nelems < 0 || nelems > 3 || nelems > loadavg.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    try {
      java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader("/proc/loadavg"));
      try {
        String[] parts = r.readLine().split(" ");
        int n = Math.min(nelems, 3);
        for (int i = 0; i < n; i++) {
          loadavg[i] = Double.parseDouble(parts[i]);
        }
        return n;
      } finally {
        r.close();
      }
    } catch (java.io.IOException e) {
      return -1;
    } catch (RuntimeException e) {
      return -1;
    }
  }

  private static volatile int fenceCounter;

  public void loadFence() {
    int x = fenceCounter;
  }

  public void storeFence() {
    fenceCounter = 0;
  }

  public void fullFence() {
    fenceCounter = fenceCounter + 1;
  }

  public boolean getBoolean(Object o, long offset) {
    return getBooleanVolatile(o, offset);
  }

  public void putBoolean(Object o, long offset, boolean x) {
    putBooleanVolatile(o, offset, x);
  }

  public byte getByte(Object o, long offset) {
    return getByteVolatile(o, offset);
  }

  public void putByte(Object o, long offset, byte x) {
    putByteVolatile(o, offset, x);
  }

  public short getShort(Object o, long offset) {
    return getShortVolatile(o, offset);
  }

  public void putShort(Object o, long offset, short x) {
    putShortVolatile(o, offset, x);
  }

  public char getChar(Object o, long offset) {
    return getCharVolatile(o, offset);
  }

  public void putChar(Object o, long offset, char x) {
    putCharVolatile(o, offset, x);
  }

  public int getInt(Object o, long offset) {
    return getIntVolatile(o, offset);
  }

  public void putInt(Object o, long offset, int x) {
    putIntVolatile(o, offset, x);
  }

  public float getFloat(Object o, long offset) {
    return getFloatVolatile(o, offset);
  }

  public void putFloat(Object o, long offset, float x) {
    putFloatVolatile(o, offset, x);
  }

  public final int getAndAddInt(Object o, long offset, int delta) {
    int v;
    do {
      v = getIntVolatile(o, offset);
    } while (!compareAndSwapInt(o, offset, v, v + delta));
    return v;
  }

  public final long getAndAddLong(Object o, long offset, long delta) {
    long v;
    do {
      v = getLongVolatile(o, offset);
    } while (!compareAndSwapLong(o, offset, v, v + delta));
    return v;
  }

  public final int getAndSetInt(Object o, long offset, int newValue) {
    int v;
    do {
      v = getIntVolatile(o, offset);
    } while (!compareAndSwapInt(o, offset, v, newValue));
    return v;
  }

  public final long getAndSetLong(Object o, long offset, long newValue) {
    long v;
    do {
      v = getLongVolatile(o, offset);
    } while (!compareAndSwapLong(o, offset, v, newValue));
    return v;
  }

  public final Object getAndSetObject(Object o, long offset, Object newValue) {
    Object v;
    do {
      v = getObjectVolatile(o, offset);
    } while (!compareAndSwapObject(o, offset, v, newValue));
    return v;
  }
}
