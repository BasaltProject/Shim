/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.FileOutputStream;

public class EmojiProperties {
  private static int flags(int cp) {
    return (Character.isEmoji(cp) ? 1 : 0)
        | (Character.isEmojiPresentation(cp) ? 2 : 0)
        | (Character.isEmojiModifier(cp) ? 4 : 0)
        | (Character.isEmojiModifierBase(cp) ? 8 : 0)
        | (Character.isEmojiComponent(cp) ? 16 : 0)
        | (Character.isExtendedPictographic(cp) ? 32 : 0);
  }

  public static void main(String[] args) throws Exception {
    int[] examples = {0, '#', '*', '0', '9', 'A', 0xa9, 0xae, 0x200d, 0x20e3, 0x2194, 0x2328,
        0x261d, 0x26f9, 0x2708, 0x270a, 0x270d, 0x2764, 0xfe0f, 0x1f000, 0x1f004,
        0x1f0cf, 0x1f1e6, 0x1f1ff, 0x1f3fb, 0x1f3ff, 0x1f468, 0x1f90c, 0x1f93c,
        0x1fa89, 0x1fae9, 0x1faf8, 0x1faff, 0x1fb00, 0x1fffd, 0xe0020, 0xe007f, 0x10ffff};
    for (int cp : examples) System.out.println(Integer.toHexString(cp) + ":" + flags(cp));
    for (int cp : new int[] {-1, Integer.MIN_VALUE, 0x110000, Integer.MAX_VALUE}) {
      if (flags(cp) != 0) throw new AssertionError("invalid code point");
    }
    byte[] output = args.length == 0 ? null : new byte[0x110000];
    int step = output == null ? 257 : 1;
    for (int plane = 0; plane <= 16; ++plane) {
      int hash = 1;
      for (int cp = plane << 16; cp < (plane + 1) << 16; cp += step) {
        int value = flags(cp);
        hash = 31 * hash + value;
        if (output != null) output[cp] = (byte) value;
      }
      System.out.println("plane-" + plane + ":" + Integer.toHexString(hash));
    }
    if (output != null) {
      try (FileOutputStream out = new FileOutputStream(args[0])) { out.write(output); }
    }
  }
}
