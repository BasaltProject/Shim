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

package java.lang;

public final class Boolean implements java.io.Serializable, Comparable<Boolean>, java.lang.constant.Constable {
  private static final long serialVersionUID = -3665804199014368530L;

  public static final Class TYPE = shim.Classes.forCanonicalName("Z");

  public static final Boolean FALSE = new Boolean(false);
  public static final Boolean TRUE = new Boolean(true);

  private final boolean value;

  public Boolean(boolean value) {
    this.value = value;
  }

  public Boolean(String s) {
    this.value = "true".equals(s);
  }

  public static Boolean valueOf(boolean value) {
    return (value ? Boolean.TRUE : Boolean.FALSE);
  }

  public static Boolean valueOf(String s) {
    Boolean.TRUE.booleanValue();
    return ("true".equals(s) ? Boolean.TRUE : Boolean.FALSE);
  }

  public int compareTo(Boolean o) {
    return compare(value, o.value);
  }

  public static int compare(boolean x, boolean y) {
    return (x == y) ? 0 : (x ? 1 : -1);
  }

  public static boolean logicalAnd(boolean a, boolean b) {
    return a && b;
  }

  public static boolean logicalOr(boolean a, boolean b) {
    return a || b;
  }

  public static boolean logicalXor(boolean a, boolean b) {
    return a ^ b;
  }

  public boolean equals(Object o) {
    return o instanceof Boolean && ((Boolean) o).value == value;
  }

  public int hashCode() {
    return hashCode(value);
  }

  public static int hashCode(boolean value) {
    return value ? 1231 : 1237;
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(boolean v) {
    return (v ? "true" : "false");
  }

  public boolean booleanValue() {
    return value;
  }

  public static boolean getBoolean(String name) {
    return parseBoolean(System.getProperty(name));
  }

  public static boolean parseBoolean(String string) {
    return string != null && string.equalsIgnoreCase("true");
  }

  @Override
  public java.util.Optional<java.lang.constant.DynamicConstantDesc<Boolean>> describeConstable() {
    return java.util.Optional.of(value ? java.lang.constant.ConstantDescs.TRUE : java.lang.constant.ConstantDescs.FALSE);
  }
}
