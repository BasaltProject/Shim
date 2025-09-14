/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Field;

public class FieldConversions {
  private static int initializations;
  public static class BadGet { public static int field = ++initializations; }
  public static class BadSet { public static int field = ++initializations; }
  public static class BadBoxedSet { public static int field = ++initializations; }
  public static class Data {
    public boolean f0 = true;
    public byte f1 = (byte) -7;
    public char f2 = (char) 65530;
    public short f3 = (short) -32000;
    public int f4 = -16777217;
    public long f5 = 9007199254740993L;
    public float f6 = -1.25f;
    public double f7 = -3.5d;
    public Object f8 = Integer.valueOf(9);
    public String f9 = "text";
    public Integer f10 = Integer.valueOf(10);
  }
  public static class Statics {
    public static boolean f0 = true;
    public static byte f1 = (byte) -7;
    public static char f2 = (char) 65530;
    public static short f3 = (short) -32000;
    public static int f4 = -16777217;
    public static long f5 = 9007199254740993L;
    public static float f6 = -1.25f;
    public static double f7 = -3.5d;
    public static Object f8 = Integer.valueOf(9);
    public static String f9 = "text";
    public static Integer f10 = Integer.valueOf(10);
  }
  private static String value(Object value) {
    if (value == null) return "null";
    if (value.getClass() == Object.class) return "object";
    if (value instanceof Float) return "float:" + Float.floatToRawIntBits((Float) value);
    if (value instanceof Double) return "double:" + Double.doubleToRawLongBits((Double) value);
    if (value instanceof Character) return "char:" + (int) ((Character) value).charValue();
    return value.getClass().getName() + ":" + value;
  }

  private static Object read(Field field, Object owner, int kind) throws Exception {
    switch (kind) {
    case 0: return field.getBoolean(owner);
    case 1: return field.getByte(owner);
    case 2: return field.getChar(owner);
    case 3: return field.getShort(owner);
    case 4: return field.getInt(owner);
    case 5: return field.getLong(owner);
    case 6: return field.getFloat(owner);
    case 7: return field.getDouble(owner);
    default: return field.get(owner);
    }
  }
  private static void write(Field field, Object owner, int kind) throws Exception {
    switch (kind) {
    case 0: field.setBoolean(owner, true); break;
    case 1: field.setByte(owner, (byte) -7); break;
    case 2: field.setChar(owner, (char) 65530); break;
    case 3: field.setShort(owner, (short) -32000); break;
    case 4: field.setInt(owner, -16777217); break;
    case 5: field.setLong(owner, 9007199254740993L); break;
    case 6: field.setFloat(owner, -1.25f); break;
    case 7: field.setDouble(owner, -3.5d); break;
    default: throw new AssertionError();
    }
  }
  private static void reset() {
    Statics.f0 = true;
    Statics.f1 = (byte) -7;
    Statics.f2 = (char) 65530;
    Statics.f3 = (short) -32000;
    Statics.f4 = -16777217;
    Statics.f5 = 9007199254740993L;
    Statics.f6 = -1.25f;
    Statics.f7 = -3.5d;
    Statics.f8 = Integer.valueOf(9);
    Statics.f9 = "text";
    Statics.f10 = Integer.valueOf(10);
  }
  public static void main(String[] args) throws Exception {
    Object[] values = {Boolean.TRUE, Byte.valueOf((byte) -7), Character.valueOf((char) 65530),
      Short.valueOf((short) -32000), Integer.valueOf(-16777217), Long.valueOf(9007199254740993L),
      Float.valueOf(-1.25f), Double.valueOf(-3.5d), null, "value", new Object(), new java.math.BigInteger("12")};
    for (Class<?> type : new Class<?>[] {Data.class, Statics.class}) {
      for (int destination = 0; destination < 11; ++destination) {
        Field field = type.getField("f" + destination);
        for (int kind = 0; kind < 9; ++kind) {
          String result;
          try { result = value(read(field, new Data(), kind)); }
          catch (Exception e) { result = e.getClass().getSimpleName(); }
          System.out.println(type.getName() + ":read:" + destination + ":" + kind + ":" + result);
        }
        for (int kind = 0; kind < 8; ++kind) {
          reset();
          Data owner = new Data();
          String result = "ok";
          try { write(field, owner, kind); }
          catch (Exception e) { result = e.getClass().getSimpleName(); }
          System.out.println(type.getName() + ":write:" + destination + ":" + kind + ":" + result + ":" + value(field.get(owner)));
        }
        for (int source = 0; source < values.length; ++source) {
          reset();
          Data owner = new Data();
          String result = "ok";
          try { field.set(owner, values[source]); }
          catch (Exception e) { result = e.getClass().getSimpleName(); }
          System.out.println(type.getName() + ":boxed:" + destination + ":" + source + ":" + result + ":" + value(field.get(owner)));
        }
      }
      Field field = type.getField("f4");
      for (Object owner : new Object[] {null, new Object()}) {
        for (int kind = 0; kind < 9; ++kind) {
          try { System.out.println("receiver-read:" + value(read(field, owner, kind))); }
          catch (Exception e) { System.out.println("receiver-read:" + e.getClass().getSimpleName()); }
          try {
            if (kind == 8) field.set(owner, Integer.valueOf(1)); else write(field, owner, kind);
            System.out.println("receiver-write:ok");
          } catch (Exception e) { System.out.println("receiver-write:" + e.getClass().getSimpleName()); }
        }
      }
    }
    for (int code : new int[] {0, 127, 128, 32767, 32768, 65530, 65535}) {
      Data chars = new Data();
      chars.f2 = (char) code;
      Statics.f2 = (char) code;
      System.out.println("char:" + (int) chars.f2 + ":" + (int) Statics.f2 + ":"
        + (int) Character.valueOf((char) code).charValue());
    }
    try { BadGet.class.getField("field").getChar(null); }
    catch (IllegalArgumentException expected) { System.out.println("bad read:" + initializations); }
    try { BadSet.class.getField("field").setDouble(null, 3.0); }
    catch (IllegalArgumentException expected) { System.out.println("bad write:" + initializations); }
    try { BadBoxedSet.class.getField("field").set(null, "wrong"); }
    catch (IllegalArgumentException expected) { System.out.println("bad boxed write:" + initializations); }
    Data owner = new Data();
    Field floats = Data.class.getField("f6"), doubles = Data.class.getField("f7");
    Field longs = Data.class.getField("f5");
    for (long number : new long[] {Long.MIN_VALUE, Long.MAX_VALUE, (1L << 62) + (1L << 38) + 1}) {
      owner.f5 = number;
      System.out.println(value(longs.getFloat(owner)));
      floats.setLong(owner, number);
      System.out.println(value(floats.get(owner)));
    }
    for (int bits : new int[] {0x80000000, 0x7fc12345, 0xff800000}) {
      floats.setFloat(owner, Float.intBitsToFloat(bits));
      System.out.println(value(floats.get(owner)));
      doubles.setFloat(owner, Float.intBitsToFloat(bits));
      System.out.println(value(doubles.get(owner)));
    }
    for (long bits : new long[] {0x8000000000000000L, 0x7ff8123456789abcL, 0xfff0000000000000L}) {
      doubles.setDouble(owner, Double.longBitsToDouble(bits));
      System.out.println(value(doubles.get(owner)));
    }
  }
}
