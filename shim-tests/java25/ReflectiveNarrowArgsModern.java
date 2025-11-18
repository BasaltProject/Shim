/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.reflect.*;
public class ReflectiveNarrowArgsModern { static String f(byte b, short s, char c, boolean z) { return b + "," + s + "," + (int) c + "," + z; }
 static byte rb(byte b) { return b; }
 public static void main(String[] a) throws Throwable {
  Method m = ReflectiveNarrowArgsModern.class.getDeclaredMethod("f", byte.class, short.class, char.class, boolean.class);
  System.out.println(m.invoke(null, (byte) -56, (short) -4464, 'x', true));
  Method r = ReflectiveNarrowArgsModern.class.getDeclaredMethod("rb", byte.class);
  System.out.println(r.invoke(null, (byte) -56));
  System.out.println(f((byte) -56, (short) -4464, 'x', true));
 }}
