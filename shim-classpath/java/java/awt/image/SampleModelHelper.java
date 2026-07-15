/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

final class SampleModelHelper {
  private SampleModelHelper() { }

  static int[] toInts(Object obj, int[] reuse) {
    int n = java.lang.reflect.Array.getLength(obj);
    int[] out = reuse != null && reuse.length >= n ? reuse : new int[n];
    if (obj instanceof byte[]) {
      byte[] b = (byte[]) obj;
      for (int i = 0; i < n; i++) {
        out[i] = b[i] & 0xff;
      }
    } else {
      short[] s = (short[]) obj;
      for (int i = 0; i < n; i++) {
        out[i] = s[i] & 0xffff;
      }
    }
    return out;
  }
}
