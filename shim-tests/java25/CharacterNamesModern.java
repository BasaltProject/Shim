/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */
import java.util.concurrent.Callable;

public class CharacterNamesModern {
  static void t(Callable<Object> c) {
    try {
      System.out.println(c.call());
    } catch (Throwable e) {
      System.out.println(e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public static void main(String[] a) {
    long h = 17;
    int named = 0;
    for (int cp = 0; cp <= 0x10FFFF; cp++) {
      String n = Character.getName(cp);
      h = h * 31 + (n == null ? 0 : n.hashCode());
      if (n != null) {
        named++;
      }
    }
    System.out.println(named + " " + h);
    int mismatches = 0;
    for (int cp = 0; cp <= 0x10FFFF; cp += 7) {
      String n = Character.getName(cp);
      if (n != null) {
        try {
          if (Character.codePointOf(n) != cp) {
            mismatches++;
          }
        } catch (IllegalArgumentException e) {
          mismatches++;
        }
      }
    }
    System.out.println("roundtrip mismatches " + mismatches);
    t(() -> Character.codePointOf("latin small letter a"));
    t(() -> Character.codePointOf("  LATIN SMALL LETTER A "));
    t(() -> Character.codePointOf("LATIN SMALL LETTER  A"));
    t(() -> Character.codePointOf("cjk unified ideographs 4e00"));
    t(() -> Character.codePointOf("CJK UNIFIED IDEOGRAPHS 4E0"));
    t(() -> Character.codePointOf("CJK UNIFIED IDEOGRAPHS 04E00"));
    t(() -> Character.codePointOf("BASIC LATIN 41"));
    t(() -> Character.codePointOf("LATIN 1 SUPPLEMENT 84"));
    t(() -> Character.codePointOf("NOPE"));
    t(() -> Character.codePointOf(""));
    t(() -> Character.getName(-1));
    t(() -> Character.getName(0x110000));
    t(() -> Character.getName(0x378));
    t(() -> Character.getName(0xD800));
    t(() -> Character.getName(0xAC00));
    t(() -> Character.getName(10));
  }
}
