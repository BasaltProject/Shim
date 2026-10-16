/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.FileOutputStream;

public class CharacterProperties {
  private static int flags(int cp) {
    return (Character.isDefined(cp) ? 1 : 0)
        | (Character.isDigit(cp) ? 2 : 0)
        | (Character.isLetter(cp) ? 4 : 0)
        | (Character.isLetterOrDigit(cp) ? 8 : 0)
        | (Character.isLowerCase(cp) ? 16 : 0)
        | (Character.isUpperCase(cp) ? 32 : 0)
        | (Character.isTitleCase(cp) ? 64 : 0)
        | (Character.isSpaceChar(cp) ? 128 : 0)
        | (Character.isWhitespace(cp) ? 256 : 0)
        | (Character.isIdentifierIgnorable(cp) ? 512 : 0)
        | (Character.isJavaIdentifierStart(cp) ? 1024 : 0)
        | (Character.isJavaIdentifierPart(cp) ? 2048 : 0)
        | (Character.isUnicodeIdentifierStart(cp) ? 4096 : 0)
        | (Character.isUnicodeIdentifierPart(cp) ? 8192 : 0)
        | (Character.isAlphabetic(cp) ? 16384 : 0)
        | (Character.isIdeographic(cp) ? 32768 : 0);
  }

  private static int flags(char cp) {
    return (Character.isDefined(cp) ? 1 : 0)
        | (Character.isDigit(cp) ? 2 : 0)
        | (Character.isLetter(cp) ? 4 : 0)
        | (Character.isLetterOrDigit(cp) ? 8 : 0)
        | (Character.isLowerCase(cp) ? 16 : 0)
        | (Character.isUpperCase(cp) ? 32 : 0)
        | (Character.isTitleCase(cp) ? 64 : 0)
        | (Character.isSpaceChar(cp) ? 128 : 0)
        | (Character.isWhitespace(cp) ? 256 : 0)
        | (Character.isIdentifierIgnorable(cp) ? 512 : 0)
        | (Character.isJavaIdentifierStart(cp) ? 1024 : 0)
        | (Character.isJavaIdentifierPart(cp) ? 2048 : 0)
        | (Character.isUnicodeIdentifierStart(cp) ? 4096 : 0)
        | (Character.isUnicodeIdentifierPart(cp) ? 8192 : 0);
  }

  public static void main(String[] args) throws Exception {
    byte[] result = new byte[0x220000];
    int step = args.length == 0 ? 257 : 1;
    for (int plane = 0; plane <= 16; ++plane) {
      int hash = 1;
      for (int cp = plane << 16; cp < (plane + 1) << 16; cp += step) {
        int value = flags(cp);
        if (cp <= 0xffff && flags((char) cp) != (value & 0x3fff)) {
          throw new AssertionError("char overload");
        }
        result[cp * 2] = (byte) value;
        result[cp * 2 + 1] = (byte) (value >>> 8);
        hash = 31 * hash + value;
      }
      System.out.println(plane + ":" + Integer.toHexString(hash));
    }
    int[] invalid = {-1, Integer.MIN_VALUE, 0x110000, Integer.MAX_VALUE};
    for (int cp : invalid) {
      if (flags(cp) != 0) throw new AssertionError("invalid code point");
    }
    if (args.length != 0) {
      try (FileOutputStream out = new FileOutputStream(args[0])) {
        out.write(result);
      }
    }
  }
}
