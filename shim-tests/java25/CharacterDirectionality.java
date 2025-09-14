/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.FileOutputStream;

public class CharacterDirectionality {
  private static int value(int cp) {
    byte direction = Character.getDirectionality(cp);
    boolean mirrored = Character.isMirrored(cp);
    if (cp <= 0xffff && (direction != Character.getDirectionality((char) cp)
        || mirrored != Character.isMirrored((char) cp))) {
      throw new AssertionError("char overload " + cp);
    }
    return (direction & 255) | (mirrored ? 256 : 0);
  }

  public static void main(String[] args) throws Exception {
    int[] examples = {0, 9, 10, 13, 32, 'A', '0', '+', '$', ',', '(', ')', '<', '>', 0x300,
        0x378, 0x5d0, 0x620, 0x660, 0x61c, 0x200c, 0x200e, 0x200f, 0x202a, 0x202b,
        0x202c, 0x202d, 0x202e, 0x2066, 0x2067, 0x2068, 0x2069, 0x2208, 0x2231,
        0x2e55, 0x4e00, 0xd800, 0xe000, 0xffff, 0x10800, 0x10d40, 0x1d6db, 0xe0001,
        0xf0000, 0x10fffd, 0x10ffff};
    for (int cp : examples) System.out.println(Integer.toHexString(cp) + ":" + value(cp));
    for (int cp : new int[] {-1, Integer.MIN_VALUE, 0x110000, Integer.MAX_VALUE}) {
      if (Character.getDirectionality(cp) != -1 || Character.isMirrored(cp)) {
        throw new AssertionError("invalid code point");
      }
    }
    byte[] output = args.length == 0 ? null : new byte[0x220000];
    int step = output == null ? 257 : 1;
    for (int plane = 0; plane <= 16; ++plane) {
      int hash = 1;
      for (int cp = plane << 16; cp < (plane + 1) << 16; cp += step) {
        int value = value(cp);
        hash = 31 * hash + value;
        if (output != null) {
          output[cp * 2] = (byte) value;
          output[cp * 2 + 1] = (byte) (value >>> 8);
        }
      }
      System.out.println("plane-" + plane + ":" + Integer.toHexString(hash));
    }
    if (output != null) {
      try (FileOutputStream out = new FileOutputStream(args[0])) { out.write(output); }
    }
  }
}
