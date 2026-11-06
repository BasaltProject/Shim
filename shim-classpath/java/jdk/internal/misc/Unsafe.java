/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.misc;

import java.lang.reflect.Field;

public final class Unsafe {
  private static final Unsafe theUnsafe = new Unsafe();
  private static final sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();

  public static final int INVALID_FIELD_OFFSET = -1;
  public static final int ADDRESS_SIZE = u.addressSize();
  public static final int ARRAY_BOOLEAN_BASE_OFFSET = u.arrayBaseOffset(boolean[].class);
  public static final int ARRAY_BYTE_BASE_OFFSET = u.arrayBaseOffset(byte[].class);
  public static final int ARRAY_SHORT_BASE_OFFSET = u.arrayBaseOffset(short[].class);
  public static final int ARRAY_CHAR_BASE_OFFSET = u.arrayBaseOffset(char[].class);
  public static final int ARRAY_INT_BASE_OFFSET = u.arrayBaseOffset(int[].class);
  public static final int ARRAY_LONG_BASE_OFFSET = u.arrayBaseOffset(long[].class);
  public static final int ARRAY_FLOAT_BASE_OFFSET = u.arrayBaseOffset(float[].class);
  public static final int ARRAY_DOUBLE_BASE_OFFSET = u.arrayBaseOffset(double[].class);
  public static final int ARRAY_OBJECT_BASE_OFFSET = u.arrayBaseOffset(Object[].class);
  public static final int ARRAY_BOOLEAN_INDEX_SCALE = u.arrayIndexScale(boolean[].class);
  public static final int ARRAY_BYTE_INDEX_SCALE = u.arrayIndexScale(byte[].class);
  public static final int ARRAY_SHORT_INDEX_SCALE = u.arrayIndexScale(short[].class);
  public static final int ARRAY_CHAR_INDEX_SCALE = u.arrayIndexScale(char[].class);
  public static final int ARRAY_INT_INDEX_SCALE = u.arrayIndexScale(int[].class);
  public static final int ARRAY_LONG_INDEX_SCALE = u.arrayIndexScale(long[].class);
  public static final int ARRAY_FLOAT_INDEX_SCALE = u.arrayIndexScale(float[].class);
  public static final int ARRAY_DOUBLE_INDEX_SCALE = u.arrayIndexScale(double[].class);
  public static final int ARRAY_OBJECT_INDEX_SCALE = u.arrayIndexScale(Object[].class);

  private Unsafe() {}

  public static Unsafe getUnsafe() {
    return theUnsafe;
  }

  public int addressSize() { return ADDRESS_SIZE; }
  public int pageSize() { return u.pageSize(); }
  public long allocateMemory(long bytes) { return u.allocateMemory(bytes); }
  public long reallocateMemory(long address, long bytes) { return u.reallocateMemory(address, bytes); }
  public void freeMemory(long address) { u.freeMemory(address); }
  public void setMemory(long address, long bytes, byte value) { u.setMemory(address, bytes, value); }
  public void setMemory(Object o, long offset, long bytes, byte value) { u.setMemory(o, offset, bytes, value); }
  public void copyMemory(long src, long dst, long bytes) { u.copyMemory(src, dst, bytes); }
  public void copyMemory(Object sb, long so, Object db, long d, long bytes) { u.copyMemory(sb, so, db, d, bytes); }
  public Object allocateInstance(Class<?> c) throws InstantiationException { return u.allocateInstance(c); }
  public void throwException(Throwable t) { u.throwException(t); }
  public long objectFieldOffset(Field f) { return u.objectFieldOffset(f); }
  public long staticFieldOffset(Field f) { return u.staticFieldOffset(f); }
  public Object staticFieldBase(Field f) { return u.staticFieldBase(f); }
  public int arrayBaseOffset(Class<?> c) { return u.arrayBaseOffset(c); }
  public int arrayIndexScale(Class<?> c) { return u.arrayIndexScale(c); }
  public void park(boolean absolute, long time) { u.park(absolute, time); }
  public void unpark(Object thread) { u.unpark(thread); }
  public void loadFence() { u.loadFence(); }
  public void storeFence() { u.storeFence(); }
  public void fullFence() { u.fullFence(); }
  public long getAddress(long a) { return u.getAddress(a); }
  public void putAddress(long a, long x) { u.putAddress(a, x); }
  public Object getReference(Object o, long off) { return u.getObject(o, off); }
  public void putReference(Object o, long off, Object x) { u.putObject(o, off, x); }
  public Object getReferenceVolatile(Object o, long off) { return u.getObjectVolatile(o, off); }
  public void putReferenceVolatile(Object o, long off, Object x) { u.putObjectVolatile(o, off, x); }
  public Object getReferenceAcquire(Object o, long off) { return u.getObjectVolatile(o, off); }
  public void putReferenceRelease(Object o, long off, Object x) { u.putOrderedObject(o, off, x); }
  public boolean compareAndSetReference(Object o, long off, Object e, Object x) { return u.compareAndSwapObject(o, off, e, x); }
  public Object compareAndExchangeReference(Object o, long off, Object e, Object x) {
    for (;;) {
      Object v = u.getObjectVolatile(o, off);
      if (v != e || u.compareAndSwapObject(o, off, e, x)) { return v; }
    }
  }
  public Object getAndSetReference(Object o, long off, Object x) { return u.getAndSetObject(o, off, x); }
  public boolean compareAndSetInt(Object o, long off, int e, int x) { return u.compareAndSwapInt(o, off, e, x); }
  public boolean compareAndSetLong(Object o, long off, long e, long x) { return u.compareAndSwapLong(o, off, e, x); }
  public int compareAndExchangeInt(Object o, long off, int e, int x) {
    for (;;) {
      int v = u.getIntVolatile(o, off);
      if (v != e || u.compareAndSwapInt(o, off, e, x)) { return v; }
    }
  }
  public long compareAndExchangeLong(Object o, long off, long e, long x) {
    for (;;) {
      long v = u.getLongVolatile(o, off);
      if (v != e || u.compareAndSwapLong(o, off, e, x)) { return v; }
    }
  }
  public int getAndAddInt(Object o, long off, int d) { return u.getAndAddInt(o, off, d); }
  public long getAndAddLong(Object o, long off, long d) { return u.getAndAddLong(o, off, d); }
  public int getAndSetInt(Object o, long off, int x) { return u.getAndSetInt(o, off, x); }
  public long getAndSetLong(Object o, long off, long x) { return u.getAndSetLong(o, off, x); }
  public void putIntRelease(Object o, long off, int x) { u.putOrderedInt(o, off, x); }
  public void putLongRelease(Object o, long off, long x) { u.putOrderedLong(o, off, x); }
  public int getIntAcquire(Object o, long off) { return u.getIntVolatile(o, off); }
  public long getLongAcquire(Object o, long off) { return u.getLongVolatile(o, off); }

  public boolean getBoolean(Object o, long off) { return u.getBoolean(o, off); }
  public void putBoolean(Object o, long off, boolean x) { u.putBoolean(o, off, x); }
  public boolean getBooleanVolatile(Object o, long off) { return u.getBooleanVolatile(o, off); }
  public void putBooleanVolatile(Object o, long off, boolean x) { u.putBooleanVolatile(o, off, x); }
  public byte getByte(Object o, long off) { return u.getByte(o, off); }
  public void putByte(Object o, long off, byte x) { u.putByte(o, off, x); }
  public byte getByteVolatile(Object o, long off) { return u.getByteVolatile(o, off); }
  public void putByteVolatile(Object o, long off, byte x) { u.putByteVolatile(o, off, x); }
  public byte getByte(long a) { return u.getByte(a); }
  public void putByte(long a, byte x) { u.putByte(a, x); }
  public short getShort(Object o, long off) { return u.getShort(o, off); }
  public void putShort(Object o, long off, short x) { u.putShort(o, off, x); }
  public short getShortVolatile(Object o, long off) { return u.getShortVolatile(o, off); }
  public void putShortVolatile(Object o, long off, short x) { u.putShortVolatile(o, off, x); }
  public short getShort(long a) { return u.getShort(a); }
  public void putShort(long a, short x) { u.putShort(a, x); }
  public char getChar(Object o, long off) { return u.getChar(o, off); }
  public void putChar(Object o, long off, char x) { u.putChar(o, off, x); }
  public char getCharVolatile(Object o, long off) { return u.getCharVolatile(o, off); }
  public void putCharVolatile(Object o, long off, char x) { u.putCharVolatile(o, off, x); }
  public char getChar(long a) { return u.getChar(a); }
  public void putChar(long a, char x) { u.putChar(a, x); }
  public int getInt(Object o, long off) { return u.getInt(o, off); }
  public void putInt(Object o, long off, int x) { u.putInt(o, off, x); }
  public int getIntVolatile(Object o, long off) { return u.getIntVolatile(o, off); }
  public void putIntVolatile(Object o, long off, int x) { u.putIntVolatile(o, off, x); }
  public int getInt(long a) { return u.getInt(a); }
  public void putInt(long a, int x) { u.putInt(a, x); }
  public long getLong(Object o, long off) { return u.getLong(o, off); }
  public void putLong(Object o, long off, long x) { u.putLong(o, off, x); }
  public long getLongVolatile(Object o, long off) { return u.getLongVolatile(o, off); }
  public void putLongVolatile(Object o, long off, long x) { u.putLongVolatile(o, off, x); }
  public long getLong(long a) { return u.getLong(a); }
  public void putLong(long a, long x) { u.putLong(a, x); }
  public float getFloat(Object o, long off) { return u.getFloat(o, off); }
  public void putFloat(Object o, long off, float x) { u.putFloat(o, off, x); }
  public float getFloatVolatile(Object o, long off) { return u.getFloatVolatile(o, off); }
  public void putFloatVolatile(Object o, long off, float x) { u.putFloatVolatile(o, off, x); }
  public float getFloat(long a) { return u.getFloat(a); }
  public void putFloat(long a, float x) { u.putFloat(a, x); }
  public double getDouble(Object o, long off) { return u.getDouble(o, off); }
  public void putDouble(Object o, long off, double x) { u.putDouble(o, off, x); }
  public double getDoubleVolatile(Object o, long off) { return u.getDoubleVolatile(o, off); }
  public void putDoubleVolatile(Object o, long off, double x) { u.putDoubleVolatile(o, off, x); }
  public double getDouble(long a) { return u.getDouble(a); }
  public void putDouble(long a, double x) { u.putDouble(a, x); }
}
