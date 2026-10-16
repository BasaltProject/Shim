/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class ArrayExceptions {
  public static Object readBooleanVariable(boolean[] a, int i) { return a[i]; }
  public static void writeBooleanVariable(boolean[] a, int i) { a[i] = true; }
  public static Object readBooleanNegative(boolean[] a) { return a[-1]; }
  public static void writeBooleanNegative(boolean[] a) { a[-1] = true; }
  public static Object readBooleanZero(boolean[] a) { return a[0]; }
  public static void writeBooleanZero(boolean[] a) { a[0] = true; }
  public static Object readBooleanPositive(boolean[] a) { return a[1]; }
  public static void writeBooleanPositive(boolean[] a) { a[1] = true; }
  public static Object readByteVariable(byte[] a, int i) { return a[i]; }
  public static void writeByteVariable(byte[] a, int i) { a[i] = (byte) 1; }
  public static Object readByteNegative(byte[] a) { return a[-1]; }
  public static void writeByteNegative(byte[] a) { a[-1] = (byte) 1; }
  public static Object readByteZero(byte[] a) { return a[0]; }
  public static void writeByteZero(byte[] a) { a[0] = (byte) 1; }
  public static Object readBytePositive(byte[] a) { return a[1]; }
  public static void writeBytePositive(byte[] a) { a[1] = (byte) 1; }
  public static Object readCharVariable(char[] a, int i) { return a[i]; }
  public static void writeCharVariable(char[] a, int i) { a[i] = 'x'; }
  public static Object readCharNegative(char[] a) { return a[-1]; }
  public static void writeCharNegative(char[] a) { a[-1] = 'x'; }
  public static Object readCharZero(char[] a) { return a[0]; }
  public static void writeCharZero(char[] a) { a[0] = 'x'; }
  public static Object readCharPositive(char[] a) { return a[1]; }
  public static void writeCharPositive(char[] a) { a[1] = 'x'; }
  public static Object readShortVariable(short[] a, int i) { return a[i]; }
  public static void writeShortVariable(short[] a, int i) { a[i] = (short) 1; }
  public static Object readShortNegative(short[] a) { return a[-1]; }
  public static void writeShortNegative(short[] a) { a[-1] = (short) 1; }
  public static Object readShortZero(short[] a) { return a[0]; }
  public static void writeShortZero(short[] a) { a[0] = (short) 1; }
  public static Object readShortPositive(short[] a) { return a[1]; }
  public static void writeShortPositive(short[] a) { a[1] = (short) 1; }
  public static Object readIntVariable(int[] a, int i) { return a[i]; }
  public static void writeIntVariable(int[] a, int i) { a[i] = 1; }
  public static Object readIntNegative(int[] a) { return a[-1]; }
  public static void writeIntNegative(int[] a) { a[-1] = 1; }
  public static Object readIntZero(int[] a) { return a[0]; }
  public static void writeIntZero(int[] a) { a[0] = 1; }
  public static Object readIntPositive(int[] a) { return a[1]; }
  public static void writeIntPositive(int[] a) { a[1] = 1; }
  public static Object readLongVariable(long[] a, int i) { return a[i]; }
  public static void writeLongVariable(long[] a, int i) { a[i] = 1L; }
  public static Object readLongNegative(long[] a) { return a[-1]; }
  public static void writeLongNegative(long[] a) { a[-1] = 1L; }
  public static Object readLongZero(long[] a) { return a[0]; }
  public static void writeLongZero(long[] a) { a[0] = 1L; }
  public static Object readLongPositive(long[] a) { return a[1]; }
  public static void writeLongPositive(long[] a) { a[1] = 1L; }
  public static Object readFloatVariable(float[] a, int i) { return a[i]; }
  public static void writeFloatVariable(float[] a, int i) { a[i] = 1f; }
  public static Object readFloatNegative(float[] a) { return a[-1]; }
  public static void writeFloatNegative(float[] a) { a[-1] = 1f; }
  public static Object readFloatZero(float[] a) { return a[0]; }
  public static void writeFloatZero(float[] a) { a[0] = 1f; }
  public static Object readFloatPositive(float[] a) { return a[1]; }
  public static void writeFloatPositive(float[] a) { a[1] = 1f; }
  public static Object readDoubleVariable(double[] a, int i) { return a[i]; }
  public static void writeDoubleVariable(double[] a, int i) { a[i] = 1d; }
  public static Object readDoubleNegative(double[] a) { return a[-1]; }
  public static void writeDoubleNegative(double[] a) { a[-1] = 1d; }
  public static Object readDoubleZero(double[] a) { return a[0]; }
  public static void writeDoubleZero(double[] a) { a[0] = 1d; }
  public static Object readDoublePositive(double[] a) { return a[1]; }
  public static void writeDoublePositive(double[] a) { a[1] = 1d; }
  public static Object readReferenceVariable(Object[] a, int i) { return a[i]; }
  public static void writeReferenceVariable(Object[] a, int i) { a[i] = new Object(); }
  public static Object readReferenceNegative(Object[] a) { return a[-1]; }
  public static void writeReferenceNegative(Object[] a) { a[-1] = new Object(); }
  public static Object readReferenceZero(Object[] a) { return a[0]; }
  public static void writeReferenceZero(Object[] a) { a[0] = new Object(); }
  public static Object readReferencePositive(Object[] a) { return a[1]; }
  public static void writeReferencePositive(Object[] a) { a[1] = new Object(); }
  public static void writeWrong(Object[] array, int index) { array[index] = Integer.valueOf(1); }
  public static Object[] array(Class<?> component) {
    return new Object[] {null, java.lang.reflect.Array.newInstance(component, 0),
      java.lang.reflect.Array.newInstance(component, 1)};
  }
  private static String result(Object value) {
    if (value == null) return "null";
    if (value instanceof Character) return "char:" + (int) ((Character) value).charValue();
    if (value.getClass() == Object.class) return "object";
    return value.toString();
  }
  public static String caught(int[] array) {
    int marker = 17;
    try { return "value:" + array[-1]; }
    catch (NullPointerException e) { return "null:" + marker; }
    catch (ArrayIndexOutOfBoundsException e) { return "bounds:" + marker; }
  }
  public static void main(String[] args) throws Exception {
    String[] names = {"Boolean", "Byte", "Char", "Short", "Int", "Long", "Float", "Double", "Reference"};
    Class<?>[] components = {boolean.class, byte.class, char.class, short.class, int.class,
      long.class, float.class, double.class, Object.class};
    for (int type = 0; type < names.length; ++type) {
      Object[] arrays = array(components[type]);
      Class<?> arrayType = arrays[1].getClass();
      for (String operation : new String[] {"read", "write"}) {
        for (String mode : new String[] {"Variable", "Negative", "Zero", "Positive"}) {
          String name = operation + names[type] + mode;
          boolean variable = mode.equals("Variable");
          Method method = variable ? ArrayExceptions.class.getMethod(name, arrayType, int.class)
            : ArrayExceptions.class.getMethod(name, arrayType);
          for (int target = 0; target < arrays.length; ++target) {
            int[] indices = variable ? new int[] {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE} : new int[] {0};
            for (int index : indices) {
              String outcome;
              try {
                Object value = variable ? method.invoke(null, arrays[target], index) : method.invoke(null, arrays[target]);
                outcome = result(value);
              } catch (InvocationTargetException e) { outcome = e.getCause().getClass().getSimpleName(); }
              System.out.println(name + ":" + target + ":" + index + ":" + outcome);
            }
          }
        }
      }
    }
    for (Object[] array : new Object[][] {null, new String[0], new String[1]}) {
      for (int index : new int[] {-1, 0, 1}) {
        try { writeWrong(array, index); throw new AssertionError(); }
        catch (NullPointerException e) { System.out.println("wrong:null"); }
        catch (ArrayIndexOutOfBoundsException e) { System.out.println("wrong:bounds"); }
        catch (ArrayStoreException e) { System.out.println("wrong:type"); }
      }
    }
    System.out.println(caught(null));
    System.out.println(caught(new int[1]));
    Object[][] destinations = {new String[1], new Number[1], new CharSequence[1], new Cloneable[1],
      new java.io.Serializable[1], new String[1][], new int[1][], new Object[1][]};
    Object[] candidates = {null, "text", Integer.valueOf(2), new Object(), new String[0],
      new Object[0], new int[0], new long[0], new int[0][]};
    for (int target = 0; target < destinations.length; ++target) {
      for (int candidate = 0; candidate < candidates.length; ++candidate) {
        Object[] destination = destinations[target];
        destination[0] = null;
        String outcome;
        try {
          destination[0] = candidates[candidate];
          outcome = destination[0] == candidates[candidate] ? "stored" : "wrong";
        } catch (ArrayStoreException expected) {
          if (destination[0] != null) throw new AssertionError();
          outcome = "rejected";
        }
        System.out.println("component:" + target + ":" + candidate + ":" + outcome);
      }
    }
  }
}
