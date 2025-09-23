/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.function.Supplier;

public class UnicodeBlocksScriptsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  public static void main(String[] args) {
    Character.UnicodeBlock prev = null;
    int start = 0;
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (int cp = 0; cp <= 0x110000; cp += 16) {
      Character.UnicodeBlock b = cp <= 0x10FFFF ? Character.UnicodeBlock.of(cp) : null;
      if (cp == 0 || b != prev) {
        if (cp > 0) System.out.println("block " + Integer.toHexString(start) + "-" + Integer.toHexString(cp - 16) + " " + prev);
        start = cp;
        prev = b;
      }
    }
    Character.UnicodeScript ps = null;
    for (int cp = 0; cp <= 0x110000; cp += 16) {
      Character.UnicodeScript s = cp <= 0x10FFFF ? Character.UnicodeScript.of(cp) : null;
      if (cp == 0 || s != ps) {
        if (cp > 0) System.out.println("script " + Integer.toHexString(start) + "-" + Integer.toHexString(cp - 16) + " " + ps);
        start = cp;
        ps = s;
      }
    }
    probe("exact-boundaries", () -> { int[] cps = {0x7f, 0x80, 0xff, 0x100, 0x24f, 0x250, 0x36f, 0x370, 0x3ff, 0x400, 0x9fff, 0xa000, 0xd7ff, 0xd800, 0xdfff, 0xe000, 0xf8ff, 0xf900, 0x1f600, 0x1f64f, 0x1f650, 0xe0000, 0xe007f, 0xe0080, 0xf0000, 0x10ffff}; StringBuilder out = new StringBuilder(); for (int c : cps) out.append(Integer.toHexString(c)).append(Character.UnicodeBlock.of(c)).append(Character.UnicodeScript.of(c)).append(" "); return out; });
    probe("of-char", () -> Character.UnicodeBlock.of('a') + " " + Character.UnicodeBlock.of('é') + " " + Character.UnicodeBlock.of('中') + " " + Character.UnicodeBlock.of('\ud800') + " " + Character.UnicodeBlock.of(''));
    probe("of-bad", () -> Character.UnicodeBlock.of(0x110000));
    probe("of-bad-neg", () -> Character.UnicodeBlock.of(-1));
    probe("of-unassigned", () -> Character.UnicodeBlock.of(0x3ffff));
    probe("block-forname", () -> Character.UnicodeBlock.forName("Basic Latin") + " " + Character.UnicodeBlock.forName("basiclatin") + " " + Character.UnicodeBlock.forName("BASIC_LATIN") + " " + Character.UnicodeBlock.forName("Latin-1 Supplement") + " " + Character.UnicodeBlock.forName("greek") + " " + Character.UnicodeBlock.forName("CJK Unified Ideographs") + " " + Character.UnicodeBlock.forName("Cjk_unified_ideographs"));
    probe("block-forname-bad", () -> Character.UnicodeBlock.forName("Not A Block"));
    probe("block-constants", () -> Character.UnicodeBlock.BASIC_LATIN + " " + Character.UnicodeBlock.EMOTICONS + " " + Character.UnicodeBlock.HIRAGANA + " " + (Character.UnicodeBlock.of(0x1F600) == Character.UnicodeBlock.EMOTICONS) + " " + Character.UnicodeBlock.GREEK + " " + Character.UnicodeBlock.SURROGATES_AREA + " " + Character.UnicodeBlock.HIGH_SURROGATES);
    probe("block-subset", () -> { Character.Subset s = Character.UnicodeBlock.BASIC_LATIN; return s.toString() + " " + s.equals(Character.UnicodeBlock.BASIC_LATIN) + " " + s.equals(Character.UnicodeBlock.EMOTICONS) + " " + (s.hashCode() == Character.UnicodeBlock.BASIC_LATIN.hashCode()); });
    probe("block-subset-null", () -> new Character.Subset(null) { });
    probe("script-of", () -> Character.UnicodeScript.of('a') + " " + Character.UnicodeScript.of('1') + " " + Character.UnicodeScript.of(0x3b1) + " " + Character.UnicodeScript.of(0x4e2d) + " " + Character.UnicodeScript.of(0x1F600) + " " + Character.UnicodeScript.of(0x378) + " " + Character.UnicodeScript.of(0xd800) + " " + Character.UnicodeScript.of(0xe000));
    probe("script-of-bad", () -> Character.UnicodeScript.of(-5));
    probe("script-forname", () -> Character.UnicodeScript.forName("Latn") + " " + Character.UnicodeScript.forName("latin") + " " + Character.UnicodeScript.forName("GREK") + " " + Character.UnicodeScript.forName("Cyrillic") + " " + Character.UnicodeScript.forName("zyyy") + " " + Character.UnicodeScript.forName("Hani"));
    probe("script-forname-bad", () -> Character.UnicodeScript.forName("Nonexistent"));
    probe("script-values", () -> Character.UnicodeScript.values().length + " " + Character.UnicodeScript.values()[0] + " " + Character.UnicodeScript.values()[10] + " " + Character.UnicodeScript.valueOf("ARABIC").ordinal() + " " + Character.UnicodeScript.UNKNOWN.ordinal());
  }
}
