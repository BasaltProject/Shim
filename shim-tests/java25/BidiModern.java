/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.text.*;
import java.util.*;

public class BidiModern {
static class A {
  static String esc(String s) { StringBuilder b = new StringBuilder(); for (char c : s.toCharArray()) { if (c < 33 || c > 126) b.append(String.format("\\u%04x", (int) c)); else b.append(c); } return b.toString(); }
  static String describe(Bidi b) {
    StringBuilder sb = new StringBuilder();
    sb.append("len=").append(b.getLength()).append(" base=").append(b.getBaseLevel()).append(" ltr=").append(b.isLeftToRight()).append(" rtl=").append(b.isRightToLeft()).append(" mixed=").append(b.isMixed()).append(" baseLtr=").append(b.baseIsLeftToRight()).append(" runs=").append(b.getRunCount()).append(" lv=");
    for (int i = 0; i < b.getLength(); i++) sb.append(b.getLevelAt(i));
    sb.append(" runs:");
    for (int r = 0; r < b.getRunCount(); r++) sb.append(' ').append(b.getRunStart(r)).append('-').append(b.getRunLimit(r)).append('@').append(b.getRunLevel(r));
    return sb.toString();
  }
  public static void run() {
    String alpha = "abאבاب 12١٢.,:-+$%()[]\"‎‏‪‫‬‭‮⁦⁧⁨⁩\n\t̀";
    int[] flags = {Bidi.DIRECTION_LEFT_TO_RIGHT, Bidi.DIRECTION_RIGHT_TO_LEFT, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT};
    Random r = new Random(7);
    String[] fixed = {"", "abc", "אבג", "abc אבג def", "אב abc 123", "abc 123 א", "اب 123", "a(b)c", "א(ב)", "a א (b) ב", "1 2 3", "a1", "א1", "א 1.5", "א $12", "א-1", "a‫b‬", "a‮b‬", "‭abc‬", "a⁧b⁩c", "a⁦א⁩", "⁨א⁩", "[a]א[b]", "(א)", "a (א b) c"};
    int n = 0;
    for (String s : fixed) for (int f : flags) System.out.println(esc(s) + " f" + f + " " + describe(new Bidi(s, f)));
    for (int k = 0; k < 600; k++) {
      int len = 1 + r.nextInt(10); StringBuilder sb = new StringBuilder();
      for (int i = 0; i < len; i++) sb.append(alpha.charAt(r.nextInt(alpha.length())));
      String s = sb.toString();
      int f = flags[r.nextInt(4)];
      System.out.println(esc(s) + " f" + f + " " + describe(new Bidi(s, f)));
    }
  }
}

static class B { 
  static String lv(Bidi b) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < b.getLength(); i++) sb.append(b.getLevelAt(i)); return sb + " base=" + b.getBaseLevel(); }
  public static void run() {
    String[] ss = {"‭abc‬", "x‭abc‬", "‭abc", "‪abc‬", "‭א‬", "ab‭cd‬", "‭1‬", "‭ a‬", "‭‭abc‬‬", "‪‪abc", "a‫‪ba", "‮abc‬", "‮‭abc", "⁦abc⁩", "⁦א⁩", "x‫‫x"};
    for (int f : new int[] {0, 1}) for (String s : ss) { StringBuilder e = new StringBuilder(); for (char c : s.toCharArray()) e.append(c < 128 ? String.valueOf(c) : String.format("<%04x>", (int) c)); System.out.println("f" + f + " " + e + " -> " + lv(new Bidi(s, f))); }
  }
}

static class C { 
  static String lv(Bidi b) { StringBuilder sb = new StringBuilder(); for (int i = 0; i < b.getLength(); i++) sb.append(b.getLevelAt(i)); return sb + " base=" + b.getBaseLevel() + " ltr=" + b.isLeftToRight() + " rtl=" + b.isRightToLeft(); }
  public static void run() {
    String[] ss = {"٢", "٢٢", "٢ ", "٢a", "a٢", "٢‪", "٢‬", "١‬", "1‬", "ا٢", " ٢", "٢א", "$٢", "٢\n", "٢‎", "‎٢", "א٢"};
    for (int f : new int[] {0, 1, -2, -1}) for (String s : ss) { StringBuilder e = new StringBuilder(); for (char c : s.toCharArray()) e.append(c < 128 ? (c == 10 ? "\\n" : String.valueOf(c)) : String.format("<%04x>", (int) c)); System.out.println("f" + f + " " + e + " -> " + lv(new Bidi(s, f))); }
  }
}

static class D {
  static void t(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + e.getMessage()); } }
  static String d(Bidi b) { StringBuilder sb = new StringBuilder("len=" + b.getLength() + " base=" + b.getBaseLevel() + " ltr=" + b.isLeftToRight() + " rtl=" + b.isRightToLeft() + " mixed=" + b.isMixed() + " lv="); for (int i = 0; i < b.getLength(); i++) sb.append(b.getLevelAt(i)); sb.append(" runs="); for (int r = 0; r < b.getRunCount(); r++) sb.append(b.getRunStart(r)).append('-').append(b.getRunLimit(r)).append('@').append(b.getRunLevel(r)).append(' '); return sb.toString(); }
  public static void run() {
    Bidi b = new Bidi("abc אבג def 123", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
    t("toString", () -> b.toString());
    t("desc", () -> d(b));
    t("levelAtOut", () -> b.getLevelAt(-1) + " " + b.getLevelAt(100));
    t("runOut", () -> b.getRunLevel(99));
    t("runOut2", () -> b.getRunStart(99));
    t("runOut3", () -> b.getRunLimit(-1));
    t("line", () -> d(b.createLineBidi(0, 7)));
    t("line2", () -> d(b.createLineBidi(4, 11)));
    t("line3", () -> d(b.createLineBidi(0, b.getLength())));
    t("lineEmpty", () -> d(b.createLineBidi(3, 3)));
    t("lineBad", () -> b.createLineBidi(5, 2));
    t("lineBad2", () -> b.createLineBidi(-1, 2));
    t("lineBad3", () -> b.createLineBidi(0, 99));
    t("lineToString", () -> b.createLineBidi(0, 7).toString());
    t("lineRtlTrailing", () -> d(new Bidi("אב abc  ", Bidi.DIRECTION_LEFT_TO_RIGHT).createLineBidi(0, 8)));
    t("lineTrail", () -> d(new Bidi("abc אב xyz   ", Bidi.DIRECTION_RIGHT_TO_LEFT).createLineBidi(0, 14)));
    t("req", () -> Bidi.requiresBidi("abc".toCharArray(), 0, 3) + " " + Bidi.requiresBidi("aא".toCharArray(), 0, 2) + " " + Bidi.requiresBidi("aא".toCharArray(), 0, 1) + " " + Bidi.requiresBidi("١".toCharArray(), 0, 1) + " " + Bidi.requiresBidi("‫".toCharArray(), 0, 1) + " " + Bidi.requiresBidi("⁧".toCharArray(), 0, 1) + " " + Bidi.requiresBidi("‏".toCharArray(), 0, 1) + " " + Bidi.requiresBidi("12".toCharArray(), 0, 2));
    t("reqBad", () -> Bidi.requiresBidi("abc".toCharArray(), 2, 1));
    t("reqBad2", () -> Bidi.requiresBidi("abc".toCharArray(), 0, 9));
    t("reorder", () -> { byte[] lv = {0, 0, 1, 1, 1, 0, 0, 2, 2}; Object[] o = {"a", "b", "c", "d", "e", "f", "g", "h", "i"}; Bidi.reorderVisually(lv, 0, o, 0, 9); return Arrays.toString(o); });
    t("reorder2", () -> { byte[] lv = {1, 1, 1, 1}; Object[] o = {"a", "b", "c", "d"}; Bidi.reorderVisually(lv, 1, o, 1, 3); return Arrays.toString(o); });
    t("reorder3", () -> { byte[] lv = {0, 1, 2, 3, 4}; Object[] o = {"a", "b", "c", "d", "e"}; Bidi.reorderVisually(lv, 0, o, 0, 5); return Arrays.toString(o); });
    t("reorderBad", () -> { Bidi.reorderVisually(new byte[2], 0, new Object[2], 0, 5); return ""; });
    t("reorderBad2", () -> { Bidi.reorderVisually(new byte[2], 0, new Object[2], 1, 2); return ""; });
    t("reorderBad3", () -> { Bidi.reorderVisually(new byte[2], 0, new Object[2], 0, -1); return ""; });
    t("reorderBad4", () -> { Bidi.reorderVisually(new byte[2], -1, new Object[2], 0, 1); return ""; });
    t("ctorBad", () -> new Bidi(new char[3], 2, null, 0, 5, 0));
    t("ctorBad2", () -> new Bidi(new char[3], 0, null, 0, -1, 0));
    t("ctorBad3", () -> new Bidi(null, 0, null, 0, 0, 0));
    t("ctorBad4", () -> new Bidi(new char[3], 0, new byte[1], 0, 3, 0));
    t("ctorBad5", () -> new Bidi(new char[3], -1, null, 0, 1, 0));
    t("ctorNullStr", () -> new Bidi((String) null, 0));
    t("emb", () -> d(new Bidi("abc def".toCharArray(), 0, new byte[] {0, 0, 0, 1, 1, 1, 1}, 0, 7, Bidi.DIRECTION_LEFT_TO_RIGHT)));
    t("emb2", () -> d(new Bidi("abc def".toCharArray(), 0, new byte[] {0, 0, 0, 2, 2, -3, -3}, 0, 7, Bidi.DIRECTION_LEFT_TO_RIGHT)));
    t("emb3", () -> d(new Bidi("אב abc".toCharArray(), 0, new byte[] {1, 1, 0, 0, 0, 0}, 0, 6, Bidi.DIRECTION_RIGHT_TO_LEFT)));
    t("embOff", () -> d(new Bidi("xxאב abcxx".toCharArray(), 2, null, 0, 6, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)));
    t("flags", () -> d(new Bidi("abc", 7)));
    t("flags2", () -> d(new Bidi("א", 5)));
    t("null", () -> d(new Bidi("", 0)));
    t("multi", () -> d(new Bidi("abc\nאב\nxyz", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)));
    t("multi2", () -> d(new Bidi("א\n abc", Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT)));
    t("num", () -> d(new Bidi("א 123 ב 4.5 ,6", Bidi.DIRECTION_LEFT_TO_RIGHT)));
    t("arabic", () -> d(new Bidi("اب 123 ١٢ abc", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)));
    t("brackets", () -> d(new Bidi("a (אב) b [c א] d", Bidi.DIRECTION_LEFT_TO_RIGHT)));
    t("brackets2", () -> d(new Bidi("א (a b) ב {c} ג", Bidi.DIRECTION_RIGHT_TO_LEFT)));
    t("brackets3", () -> d(new Bidi("a(b[cא)d]e", Bidi.DIRECTION_LEFT_TO_RIGHT)));
  }
}

  public static void main(String[] args) {
    A.run();
    B.run();
    C.run();
    D.run();
  }
}
