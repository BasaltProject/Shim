/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.function.Supplier;
import java.util.regex.*;

public class RegexUnicodeScriptsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String find(String regex, String text) {
    Matcher m = Pattern.compile(regex).matcher(text);
    StringBuilder sb = new StringBuilder();
    while (m.find()) sb.append('[').append(m.group()).append(']');
    return sb.toString();
  }

  public static void main(String[] args) {
    String text = "Hello Γειά Привет 世界 こんにちは مرحبا 123 שלום";
    for (String r : new String[] {"\\p{IsLatin}+", "\\p{IsGreek}+", "\\p{IsCyrillic}+", "\\p{IsHan}+", "\\p{IsHiragana}+", "\\p{IsArabic}+", "\\p{IsHebrew}+", "\\p{IsCommon}+", "\\p{script=Greek}+", "\\p{sc=Cyrl}+", "\\p{IsLATN}+", "\\P{IsLatin}+", "\\p{InGreek}+", "\\p{InBasicLatin}+", "\\p{InCyrillic}+", "\\p{block=Hiragana}+", "\\p{blk=CJK_Unified_Ideographs}+", "\\p{InGreekandCoptic}+", "\\p{InGreek_and_Coptic}+", "[\\p{IsGreek}\\p{IsCyrillic}]+", "\\p{IsLatin}\\p{Ll}+"}) {
      final String rr = r;
      probe("find[" + r + "]", () -> find(rr, text));
    }
    for (String r : new String[] {"\\p{IsNoSuchScript}", "\\p{InNoSuchBlock}", "\\p{script=Foo}", "\\p{block=Foo}", "\\p{foo=Bar}"}) {
      final String rr = r;
      probe("bad[" + r + "]", () -> { try { Pattern.compile(rr); return "ok"; } catch (PatternSyntaxException e) { return e.getDescription() + " @" + e.getIndex(); } });
    }
    probe("flags", () -> Pattern.compile("\\p{IsLatin}+", Pattern.CASE_INSENSITIVE).matcher("abcXYZ").matches() + " " + Pattern.compile("\\p{InGreek}", Pattern.UNICODE_CASE).matcher("α").matches());
  }
}
