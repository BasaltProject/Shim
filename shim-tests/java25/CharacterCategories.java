/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.FileOutputStream;

public class CharacterCategories {
  public static void main(String[] args) throws Exception {
    byte[] types = new byte[0x110000];
    for (int plane = 0; plane <= 16; ++plane) {
      int hash = 1;
      for (int cp = plane << 16; cp < (plane + 1) << 16; ++cp) {
        int type = Character.getType(cp);
        types[cp] = (byte) type;
        hash = 31 * hash + type;
      }
      System.out.println(plane + ":" + Integer.toHexString(hash));
    }
    int[] invalid = {-1, Integer.MIN_VALUE, 0x110000, Integer.MAX_VALUE};
    for (int cp : invalid) {
      if (Character.getType(cp) != Character.UNASSIGNED) throw new AssertionError("invalid code point");
    }
    if (args.length != 0) {
      try (FileOutputStream out = new FileOutputStream(args[0])) {
        out.write(types);
      }
    }
  }
}
