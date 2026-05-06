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

package java.lang.reflect;

import shim.VMField;
import shim.AnnotationInvocationHandler;
import shim.SystemClassLoader;
import shim.Classes;

import java.lang.annotation.Annotation;

public class Field<T> extends AccessibleObject implements Member {
  private static final int VoidField = 0;
  private static final int ByteField = 1;
  private static final int CharField = 2;
  private static final int DoubleField = 3;
  private static final int FloatField = 4;
  private static final int IntField = 5;
  private static final int LongField = 6;
  private static final int ShortField = 7;
  private static final int BooleanField = 8;
  private static final int ObjectField = 9;

  public final VMField vmField;
  private boolean accessible = true;

  public Field(VMField vmField) {
    this.vmField = vmField;
  }

  public boolean isAccessible() {
    return accessible;
  }

  private boolean overridden;

  public void setAccessible(boolean v) {
    accessible = v;
    overridden = v;
  }

  public Class<T> getDeclaringClass() {
    return SystemClassLoader.getClass(vmField.class_);
  }

  public int getModifiers() {
    return vmField.flags;
  }

  @Override
  public java.util.Set<AccessFlag> accessFlags() {
    return AccessFlag.maskToAccessFlags(getModifiers(), AccessFlag.Location.FIELD);
  }
  
  public boolean isSynthetic() {
    return (vmField.flags & ACC_SYNTHETIC) != 0;
  }

  public String getName() {
    return getName(vmField);
  }

  public static String getName(VMField vmField) {
    return Classes.makeString(vmField.name, 0, vmField.name.length - 1);
  }

  public Class getType() {
    return Classes.forCanonicalName
      (vmField.class_.loader,
       Classes.makeString(vmField.spec, 0, vmField.spec.length - 1));
  }

  public Type getGenericType() {
    if (vmField.addendum == null || vmField.addendum.signature == null) {
      return getType();
    }
    String signature = Classes.toString((byte[]) vmField.addendum.signature);
    return SignatureParser.parse(vmField.class_.loader, signature, getDeclaringClass());
  }

  public AnnotatedType getAnnotatedType() {
    return AnnotatedTypeImpl.create(getGenericType());
  }

  private String describe() {
    return getType().getName() + " field " + getDeclaringClass().getName() + "." + getName();
  }

  private Object target(Object instance, boolean setting) {
    if ((vmField.flags & Modifier.STATIC) != 0) {
      Classes.initialize(vmField.class_);
      return vmField.class_.staticTable;
    }
    if (instance == null) {
      throw new NullPointerException("Cannot invoke \"Object.getClass()\" because \"obj\" is null");
    }
    if (!Classes.isInstance(vmField.class_, instance)) {
      throw new IllegalArgumentException("Can not " + (setting ? "set " : "get ") + describe()
                                         + (setting ? " to " : " on ") + instance.getClass().getName());
    }
    return instance;
  }

  public Object get(Object instance) throws IllegalAccessException {
    Object target = target(instance, false);

    switch (vmField.code) {
    case ByteField:
      return Byte.valueOf
        ((byte) getPrimitive(target, vmField.code, vmField.offset));

    case BooleanField:
      return Boolean.valueOf
        (getPrimitive(target, vmField.code, vmField.offset) != 0);

    case CharField:
      return Character.valueOf
        ((char) getPrimitive(target, vmField.code, vmField.offset));

    case ShortField:
      return Short.valueOf
        ((short) getPrimitive(target, vmField.code, vmField.offset));

    case IntField:
      return Integer.valueOf
        ((int) getPrimitive(target, vmField.code, vmField.offset));

    case LongField:
      return Long.valueOf
        (getPrimitive(target, vmField.code, vmField.offset));

    case FloatField:
      return Float.valueOf
        (Float.intBitsToFloat
         ((int) getPrimitive(target, vmField.code, vmField.offset)));

    case DoubleField:
      return Double.valueOf
        (Double.longBitsToDouble
         (getPrimitive(target, vmField.code, vmField.offset)));

    case ObjectField:
      return getObject(target, vmField.offset);

    default:
      throw new Error();
    }
  }

  private static int valueCode(Object value) {
    if (value instanceof Boolean) return BooleanField;
    if (value instanceof Byte) return ByteField;
    if (value instanceof Character) return CharField;
    if (value instanceof Short) return ShortField;
    if (value instanceof Integer) return IntField;
    if (value instanceof Long) return LongField;
    if (value instanceof Float) return FloatField;
    if (value instanceof Double) return DoubleField;
    throw new IllegalArgumentException();
  }

  private static int wrapperCode(Object value) {
    if (value instanceof Boolean) return BooleanField;
    if (value instanceof Byte) return ByteField;
    if (value instanceof Character) return CharField;
    if (value instanceof Short) return ShortField;
    if (value instanceof Integer) return IntField;
    if (value instanceof Long) return LongField;
    if (value instanceof Float) return FloatField;
    if (value instanceof Double) return DoubleField;
    return -1;
  }

  private static boolean widens(int source, int destination) {
    if (source == destination) return true;
    switch (source) {
    case ByteField:
      if (destination == ShortField) return true;
    case ShortField:
    case CharField:
      if (destination == IntField) return true;
    case IntField:
      if (destination == LongField) return true;
    case LongField:
      if (destination == FloatField) return true;
    case FloatField:
      return destination == DoubleField;
    default:
      return false;
    }
  }

  private static Object convert(Object value, int destination) {
    int source = valueCode(value);
    if (!widens(source, destination)) throw new IllegalArgumentException("argument type mismatch");
    if (source == destination) return value;
    Number number = source == CharField ? Integer.valueOf((Character) value) : (Number) value;
    switch (destination) {
    case ShortField: return Short.valueOf(number.shortValue());
    case IntField: return Integer.valueOf(number.intValue());
    case LongField: return Long.valueOf(number.longValue());
    case FloatField: return Float.valueOf(number.floatValue());
    case DoubleField: return Double.valueOf(number.doubleValue());
    default: throw new IllegalArgumentException();
    }
  }

  private static String codeName(int code) {
    switch (code) {
    case BooleanField: return "boolean";
    case ByteField: return "byte";
    case CharField: return "char";
    case ShortField: return "short";
    case IntField: return "int";
    case LongField: return "long";
    case FloatField: return "float";
    case DoubleField: return "double";
    default: return "object";
    }
  }

  private Object getConverted(Object instance, int destination) throws IllegalAccessException {
    Object value = get(instance);
    if (vmField.code == ObjectField || !widens(vmField.code, destination)) {
      throw new IllegalArgumentException("Attempt to get " + getType().getName() + " field \""
                                         + getDeclaringClass().getName() + "." + getName()
                                         + "\" with illegal data type conversion to " + codeName(destination));
    }
    return convert(value, destination);
  }

  public boolean getBoolean(Object instance) throws IllegalAccessException {
    return ((Boolean) getConverted(instance, BooleanField)).booleanValue();
  }

  public byte getByte(Object instance) throws IllegalAccessException {
    return ((Byte) getConverted(instance, ByteField)).byteValue();
  }

  public short getShort(Object instance) throws IllegalAccessException {
    return ((Short) getConverted(instance, ShortField)).shortValue();
  }

  public char getChar(Object instance) throws IllegalAccessException {
    return ((Character) getConverted(instance, CharField)).charValue();
  }

  public int getInt(Object instance) throws IllegalAccessException {
    return ((Integer) getConverted(instance, IntField)).intValue();
  }

  public float getFloat(Object instance) throws IllegalAccessException {
    return ((Float) getConverted(instance, FloatField)).floatValue();
  }

  public long getLong(Object instance) throws IllegalAccessException {
    return ((Long) getConverted(instance, LongField)).longValue();
  }

  public double getDouble(Object instance) throws IllegalAccessException {
    return ((Double) getConverted(instance, DoubleField)).doubleValue();
  }

  public void set(Object instance, Object value) throws IllegalAccessException {
    setValue(instance, value, false);
  }

  private String valueText(Object value, boolean primitiveOnly) {
    if (value == null) return "null value";
    if (!primitiveOnly) return value.getClass().getName();
    String name = codeName(wrapperCode(value));
    return "(" + name + ")" + value;
  }

  private void setValue(Object instance, Object value, boolean primitiveOnly) throws IllegalAccessException {
    Object target = target(instance, true);
    if ((vmField.flags & Modifier.FINAL) != 0 && ((vmField.flags & Modifier.STATIC) != 0 || !overridden)
        && !getDeclaringClass().getName().equals("java.lang.System")) {
      throw new IllegalAccessException("Can not set " + ((vmField.flags & Modifier.STATIC) != 0 ? "static " : "")
                                       + "final " + describe() + " to " + valueText(value, primitiveOnly));
    }
    if (vmField.code == ObjectField) {
      if (primitiveOnly || (value != null && !getType().isInstance(value))) {
        throw new IllegalArgumentException("Can not set " + describe() + " to " + valueText(value, primitiveOnly));
      }
      setObject(target, vmField.offset, value);
      return;
    }
    if (value == null || wrapperCode(value) < 0 || !widens(wrapperCode(value), vmField.code)) {
      throw new IllegalArgumentException("Can not set " + describe() + " to " + valueText(value, primitiveOnly));
    }
    value = convert(value, vmField.code);
    long bits;
    switch (vmField.code) {
    case BooleanField: bits = ((Boolean) value) ? 1 : 0; break;
    case CharField: bits = (Character) value; break;
    case FloatField: bits = Float.floatToRawIntBits((Float) value); break;
    case DoubleField: bits = Double.doubleToRawLongBits((Double) value); break;
    default: bits = ((Number) value).longValue(); break;
    }
    setPrimitive(target, vmField.code, vmField.offset, bits);
  }

  public void setBoolean(Object instance, boolean value) throws IllegalAccessException {
    setValue(instance, Boolean.valueOf(value), true);
  }

  public void setByte(Object instance, byte value) throws IllegalAccessException {
    setValue(instance, Byte.valueOf(value), true);
  }

  public void setShort(Object instance, short value) throws IllegalAccessException {
    setValue(instance, Short.valueOf(value), true);
  }

  public void setChar(Object instance, char value) throws IllegalAccessException {
    setValue(instance, Character.valueOf(value), true);
  }

  public void setInt(Object instance, int value) throws IllegalAccessException {
    setValue(instance, Integer.valueOf(value), true);
  }

  public void setFloat(Object instance, float value) throws IllegalAccessException {
    setValue(instance, Float.valueOf(value), true);
  }

  public void setLong(Object instance, long value) throws IllegalAccessException {
    setValue(instance, Long.valueOf(value), true);
  }

  public void setDouble(Object instance, double value) throws IllegalAccessException {
    setValue(instance, Double.valueOf(value), true);
  }

  private Annotation getAnnotation(Object[] a) {
    if (a[0] == null) {
      a[0] = Proxy.newProxyInstance
        (vmField.class_.loader, new Class[] { (Class) a[1] },
         new AnnotationInvocationHandler(a));
    }
    return (Annotation) a[0];
  }

  private boolean hasAnnotations() {
    return vmField.addendum != null
      && vmField.addendum.annotationTable != null;
  }

  public <T extends Annotation> T getAnnotation(Class<T> class_) {
    if (hasAnnotations()) {
      Object[] table = (Object[]) vmField.addendum.annotationTable;
      for (int i = 0; i < table.length; ++i) {
        Object[] a = (Object[]) table[i];
        if (a[1] == class_) {
          return (T) getAnnotation(a);
        }
      }
    }
    return null;
  }

  public Annotation[] getAnnotations() {
    if (hasAnnotations()) {
      Object[] table = (Object[]) vmField.addendum.annotationTable;
      Annotation[] array = new Annotation[table.length];
      for (int i = 0; i < table.length; ++i) {
        array[i] = getAnnotation((Object[]) table[i]);
      }
      return array;
    } else {
      return new Annotation[0];
    }
  }

  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }

  public boolean equals(Object obj) {
    if (obj instanceof Field) {
      Field other = (Field) obj;
      return getDeclaringClass() == other.getDeclaringClass() && getName().equals(other.getName())
          && getType() == other.getType();
    }
    return false;
  }

  public int hashCode() {
    return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & Modifier.fieldModifiers();
    if (mod != 0) {
      sb.append(Modifier.toString(mod)).append(' ');
    }
    sb.append(getType().getTypeName()).append(' ');
    sb.append(getDeclaringClass().getTypeName()).append('.');
    sb.append(getName());
    return sb.toString();
  }

  public String toGenericString() {
    StringBuilder sb = new StringBuilder();
    int mod = getModifiers() & Modifier.fieldModifiers();
    if (mod != 0) {
      sb.append(Modifier.toString(mod)).append(' ');
    }
    sb.append(getType().getTypeName()).append(' ');
    sb.append(getDeclaringClass().getTypeName()).append('.');
    sb.append(getName());
    return sb.toString();
  }

  private static native long getPrimitive
    (Object instance, int code, int offset);

  private static native Object getObject
    (Object instance, int offset);

  private static native void setPrimitive
    (Object instance, int code, int offset, long value);

  private static native void setObject
    (Object instance, int offset, Object value);

  public boolean isEnumConstant() {
    return (getModifiers() & 0x4000) != 0;
  }
}
