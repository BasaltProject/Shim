/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.*;
import shim.jdi.Wire.Out;

public final class Prim {
  final Vm vm;
  final char tag;
  final Object value;

  private Prim(Vm vm, char tag, Object value) {
    this.vm = vm;
    this.tag = tag;
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  static <T> T of(Vm vm, char tag, Object value) {
    Class<?> c;
    switch (tag) {
      case 'Z':
        c = BooleanValue.class;
        break;
      case 'B':
        c = ByteValue.class;
        break;
      case 'C':
        c = CharValue.class;
        break;
      case 'S':
        c = ShortValue.class;
        break;
      case 'I':
        c = IntegerValue.class;
        break;
      case 'J':
        c = LongValue.class;
        break;
      case 'F':
        c = FloatValue.class;
        break;
      case 'D':
        c = DoubleValue.class;
        break;
      default:
        c = VoidValue.class;
        break;
    }
    return (T) Proxies.make(new Prim(vm, tag, value), c);
  }

  void write(Out o) {
    o.u8(tag);
    switch (tag) {
      case 'Z':
        o.bool((Boolean) value);
        break;
      case 'B':
        o.u8((Byte) value);
        break;
      case 'C':
        o.u8(((Character) value) >> 8).u8((Character) value);
        break;
      case 'S':
        o.u8(((Short) value) >> 8).u8((Short) value);
        break;
      case 'I':
        o.i32((Integer) value);
        break;
      case 'J':
        o.i64((Long) value);
        break;
      case 'F':
        o.i32(Float.floatToIntBits((Float) value));
        break;
      case 'D':
        o.i64(Double.doubleToLongBits((Double) value));
        break;
      default:
        break;
    }
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public Type type() {
    return type(vm, tag);
  }

  public boolean value_() {
    return (Boolean) value;
  }

  public Object value() {
    return value;
  }

  private Number num() {
    if (value instanceof Character) {
      return Integer.valueOf((Character) value);
    }
    return (Number) value;
  }

  public boolean booleanValue() {
    return (Boolean) value;
  }

  public byte byteValue() {
    return num().byteValue();
  }

  public char charValue() {
    return value instanceof Character ? (Character) value : (char) num().intValue();
  }

  public short shortValue() {
    return num().shortValue();
  }

  public int intValue() {
    return num().intValue();
  }

  public long longValue() {
    return num().longValue();
  }

  public float floatValue() {
    return num().floatValue();
  }

  public double doubleValue() {
    return num().doubleValue();
  }

  public String toString() {
    return String.valueOf(value);
  }

  public int hashCode() {
    return value == null ? 0 : value.hashCode();
  }

  public boolean equals(Object o) {
    Object i = Proxies.implOf(o);
    return i instanceof Prim && ((Prim) i).tag == tag && java.util.Objects.equals(((Prim) i).value, value);
  }

  public int compareTo(Object o) {
    return ((Comparable) value).compareTo(((Prim) Proxies.implOf(o)).value);
  }

  /* ---------- primitive types ---------- */

  static Type type(Vm vm, char tag) {
    Class<?> c;
    String name;
    switch (tag) {
      case 'Z':
        c = BooleanType.class;
        name = "boolean";
        break;
      case 'B':
        c = ByteType.class;
        name = "byte";
        break;
      case 'C':
        c = CharType.class;
        name = "char";
        break;
      case 'S':
        c = ShortType.class;
        name = "short";
        break;
      case 'I':
        c = IntegerType.class;
        name = "int";
        break;
      case 'J':
        c = LongType.class;
        name = "long";
        break;
      case 'F':
        c = FloatType.class;
        name = "float";
        break;
      case 'D':
        c = DoubleType.class;
        name = "double";
        break;
      default:
        c = VoidType.class;
        name = "void";
        break;
    }
    return Proxies.make(new PrimType(vm, tag, name), c);
  }

  public static final class PrimType {
    final Vm vm;
    final char tag;
    final String name;

    PrimType(Vm vm, char tag, String name) {
      this.vm = vm;
      this.tag = tag;
      this.name = name;
    }

    public VirtualMachine virtualMachine() {
      return vm.proxy;
    }

    public String name() {
      return name;
    }

    public String signature() {
      return String.valueOf(tag);
    }

    public String toString() {
      return name;
    }

    public boolean equals(Object o) {
      Object i = Proxies.implOf(o);
      return i instanceof PrimType && ((PrimType) i).tag == tag;
    }

    public int hashCode() {
      return tag;
    }
  }
}
