/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 *
 * jvm-args: --add-opens java.base/java.lang.runtime=ALL-UNNAMED
 */

import java.lang.reflect.*;
import java.util.*;
public class ExactConversionsModern {
  public static void main(String[] a) throws Exception {
    Class<?> c = Class.forName("java.lang.runtime.ExactConversionsSupport");
    List<Method> ms = new ArrayList<>(Arrays.asList(c.getDeclaredMethods()));
    ms.sort(Comparator.comparing(Method::getName));
    long[] ls = {0, 1, -1, 127, 128, -128, -129, 255, 256, 32767, 32768, -32768, -32769, 65535, 65536, 16777216, 16777217, 16777215, -16777216, -16777217, Integer.MAX_VALUE, Integer.MIN_VALUE, (long) Integer.MAX_VALUE + 1, (long) Integer.MIN_VALUE - 1, 1L << 53, (1L << 53) + 1, (1L << 53) - 1, -(1L << 53) - 1, 1L << 24, (1L << 24) + 1, 1L << 63, Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1, 9007199254740993L, 4611686018427387904L, 4611686018427387905L};
    double[] ds = {0.0, -0.0, 1.0, -1.0, 0.5, 127.0, 128.0, -128.0, -129.0, 255.0, 65535.0, 65536.0, 65535.5, 32767.0, 32768.0, -32768.0, 2147483647.0, 2147483648.0, -2147483648.0, -2147483649.0, 9.223372036854775807E18, 9.223372036854775808E18, -9.223372036854775808E18, -9.223372036854777E18, 1e30, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, 3.4028235677973366E38, 1.0000001, 1.100000023841858, 0.1, 16777217.0, 16777216.0, 1.4E-45, 1e-50, 3.4028236E38, 1.7976931348623157E308, 0.30000000000000004};
    for (Method m : ms) {
      m.setAccessible(true);
      Class<?> p = m.getParameterTypes()[0];
      StringBuilder sb = new StringBuilder(m.getName() + ":");
      if (p == int.class) for (long v : ls) { if (v == (int) v) sb.append(m.invoke(null, (int) v) + ","); }
      else if (p == long.class) for (long v : ls) sb.append(m.invoke(null, v) + ",");
      else if (p == float.class) for (double v : ds) sb.append(m.invoke(null, (float) v) + ",");
      else for (double v : ds) sb.append(m.invoke(null, v) + ",");
      System.out.println(sb);
    }
  }
}
