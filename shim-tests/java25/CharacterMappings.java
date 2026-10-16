/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.FileOutputStream;

public class CharacterMappings {
  private static int[] values(int cp) {
    return new int[] {Character.toLowerCase(cp), Character.toUpperCase(cp), Character.toTitleCase(cp),
        Character.getNumericValue(cp), Character.digit(cp, 2), Character.digit(cp, 10),
        Character.digit(cp, 16), Character.digit(cp, 36)};
  }

  private static void checkChar(int cp, int[] values) {
    if (cp > 0xffff) return;
    char c = (char) cp;
    int[] actual = {Character.toLowerCase(c), Character.toUpperCase(c), Character.toTitleCase(c),
        Character.getNumericValue(c), Character.digit(c, 2), Character.digit(c, 10),
        Character.digit(c, 16), Character.digit(c, 36)};
    for (int i = 0; i < values.length; ++i) {
      int expected = i < 3 ? (char) values[i] : values[i];
      if (actual[i] != expected) throw new AssertionError("char overload " + cp + ":" + i);
    }
  }

  private static int hash(int hash, int[] values) {
    for (int value : values) hash = 31 * hash + value;
    return hash;
  }

  public static void main(String[] args) throws Exception {
    int[] examples = {0, 'A', 'z', 0xb5, 0xdf, 0xe9, 0x130, 0x131, 0x149, 0x1c4, 0x1c5, 0x1c6,
        0x3a3, 0x3c2, 0x410, 0x660, 0x6f9, 0x966, 0xbc, 0xf33, 0x216c, 0x3007, 0x4e00,
        0xa7cb, 0xff10, 0xff19, 0xff21, 0xff3a, 0xff41, 0xff5a, 0x10400, 0x10428,
        0x10d40, 0x10d50, 0x1e900, 0x1e922, 0x1d7ce, 0x1ecad, 0x10ffff};
    int[] radices = {Integer.MIN_VALUE, -1, 0, 1, 2, 8, 10, 16, 35, 36, 37, Integer.MAX_VALUE};
    for (int cp : examples) {
      int[] result = values(cp);
      checkChar(cp, result);
      int hash = hash(1, result);
      for (int radix : radices) {
        int digit = Character.digit(cp, radix);
        if (cp <= 0xffff && Character.digit((char) cp, radix) != digit) {
          throw new AssertionError("char radix");
        }
        hash = 31 * hash + digit;
      }
      System.out.println(Integer.toHexString(cp) + ":" + Integer.toHexString(hash));
    }
    for (int cp : new int[] {-1, Integer.MIN_VALUE, 0x110000, Integer.MAX_VALUE}) {
      if (Character.toLowerCase(cp) != cp || Character.toUpperCase(cp) != cp || Character.toTitleCase(cp) != cp
          || Character.getNumericValue(cp) != -1 || Character.digit(cp, 36) != -1) {
        throw new AssertionError("invalid code point " + cp);
      }
    }
    FileOutputStream out = args.length == 0 ? null : new FileOutputStream(args[0]);
    byte[] buffer = new byte[65536];
    int position = 0;
    try {
      int step = out == null ? 257 : 1;
      for (int plane = 0; plane <= 16; ++plane) {
        int hash = 1;
        for (int cp = plane << 16; cp < (plane + 1) << 16; cp += step) {
          int[] result = values(cp);
          checkChar(cp, result);
          hash = hash(hash, result);
          if (out != null) {
            for (int value : result) {
              for (int shift = 0; shift < 32; shift += 8) buffer[position++] = (byte) (value >>> shift);
            }
            if (position == buffer.length) { out.write(buffer); position = 0; }
          }
        }
        System.out.println("plane-" + plane + ":" + Integer.toHexString(hash));
      }
      if (out != null && position != 0) out.write(buffer, 0, position);
    } finally {
      if (out != null) out.close();
    }
  }
}
