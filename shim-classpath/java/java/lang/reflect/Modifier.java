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

public final class Modifier {
  public static final int PUBLIC       = 1 <<  0;
  public static final int PRIVATE      = 1 <<  1;
  public static final int PROTECTED    = 1 <<  2;
  public static final int STATIC       = 1 <<  3;
  public static final int FINAL        = 1 <<  4;
  public static final int SUPER        = 1 <<  5;
  public static final int SYNCHRONIZED = SUPER;
  public static final int VOLATILE     = 1 <<  6;
  public static final int TRANSIENT    = 1 <<  7;
  public static final int NATIVE       = 1 <<  8;
  public static final int INTERFACE    = 1 <<  9;
  public static final int ABSTRACT     = 1 << 10;
  public static final int STRICT       = 1 << 11;

  private Modifier() { }

  public static boolean isPublic   (int v) { return (v &    PUBLIC) != 0; }
  public static boolean isPrivate  (int v) { return (v &   PRIVATE) != 0; }
  public static boolean isProtected(int v) { return (v & PROTECTED) != 0; }
  public static boolean isStatic   (int v) { return (v &    STATIC) != 0; }
  public static boolean isFinal    (int v) { return (v &     FINAL) != 0; }
  public static boolean isTransient(int v) { return (v & TRANSIENT) != 0; }
  public static boolean isVolatile (int v) { return (v &  VOLATILE) != 0; }
  public static boolean isSynchronized(int v) { return (v & SYNCHRONIZED) != 0; }
  public static boolean isSuper    (int v) { return (v &     SUPER) != 0; }
  public static boolean isNative   (int v) { return (v &    NATIVE) != 0; }
  public static boolean isAbstract (int v) { return (v &  ABSTRACT) != 0; }
  public static boolean isInterface(int v) { return (v & INTERFACE) != 0; }
  public static boolean isStrict   (int v) { return (v &    STRICT) != 0; }

  public static String toString(int mod) {
    StringBuilder sb = new StringBuilder();
    if ((mod & PUBLIC) != 0) sb.append("public ");
    if ((mod & PROTECTED) != 0) sb.append("protected ");
    if ((mod & PRIVATE) != 0) sb.append("private ");
    if ((mod & ABSTRACT) != 0) sb.append("abstract ");
    if ((mod & STATIC) != 0) sb.append("static ");
    if ((mod & FINAL) != 0) sb.append("final ");
    if ((mod & TRANSIENT) != 0) sb.append("transient ");
    if ((mod & VOLATILE) != 0) sb.append("volatile ");
    if ((mod & SYNCHRONIZED) != 0) sb.append("synchronized ");
    if ((mod & NATIVE) != 0) sb.append("native ");
    if ((mod & STRICT) != 0) sb.append("strictfp ");
    if ((mod & INTERFACE) != 0) sb.append("interface ");
    int len = sb.length();
    return (len > 0) ? sb.substring(0, len - 1) : "";
  }

  public static int classModifiers() {
    return PUBLIC | PROTECTED | PRIVATE | ABSTRACT | STATIC | FINAL | STRICT;
  }

  public static int interfaceModifiers() {
    return PUBLIC | PROTECTED | PRIVATE | ABSTRACT | STATIC | STRICT;
  }

  public static int constructorModifiers() {
    return PUBLIC | PROTECTED | PRIVATE;
  }

  public static int methodModifiers() {
    return PUBLIC | PROTECTED | PRIVATE | ABSTRACT | STATIC | FINAL | SYNCHRONIZED | NATIVE | STRICT;
  }

  public static int fieldModifiers() {
    return PUBLIC | PROTECTED | PRIVATE | STATIC | FINAL | TRANSIENT | VOLATILE;
  }

  public static int parameterModifiers() {
    return FINAL;
  }
}
