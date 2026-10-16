/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;

public class LangGapsModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      Object value = action.run();
      System.out.println(label + ":" + (value instanceof Object[] ? Arrays.toString((Object[]) value) : value));
    } catch (Throwable error) {
      System.out.println(label + ":" + error.getClass().getName());
    }
  }

  static double round(double value) {
    return Math.round(value * 1e12) / 1e12;
  }

  public static void main(String[] args) {
    probe("log10", () -> Math.log10(1000.0) + "," + Math.log10(0.001) + "," + Math.log10(1.0));
    probe("cbrt", () -> Math.cbrt(27.0) + "," + Math.cbrt(-8.0) + "," + Math.cbrt(0.0));
    probe("hypot", () -> Math.hypot(3.0, 4.0) + "," + Math.hypot(5.0, 12.0) + "," + Math.hypot(Double.NaN, Double.POSITIVE_INFINITY));
    probe("expm1", () -> Math.expm1(0.0) + "," + round(Math.expm1(1e-5)) + "," + round(Math.expm1(1.0)));
    probe("log1p", () -> Math.log1p(0.0) + "," + round(Math.log1p(1e-5)) + "," + round(Math.log1p(1.0)));
    probe("remainder", () -> Math.IEEEremainder(5.0, 3.0) + "," + Math.IEEEremainder(10.0, 4.0) + ","
        + Math.IEEEremainder(-7.5, 2.0));
    probe("radians", () -> Math.toRadians(180.0) + "," + Math.toDegrees(Math.PI) + "," + Math.toRadians(90.0));
    probe("rint", () -> Math.rint(2.5) + "," + Math.rint(3.5) + "," + Math.rint(-2.5) + "," + Math.rint(0.5) + ","
        + Math.rint(-0.5) + "," + Math.rint(1e300) + "," + Math.rint(-1.4));
    probe("scalb", () -> Math.scalb(1.0, 10) + "," + Math.scalb(1.0, -1074) + "," + Math.scalb(1.0, -1075) + ","
        + Math.scalb(3.0f, 2) + "," + Math.scalb(1.0, 2000) + "," + Math.scalb(0.75, -3));
    probe("full", () -> Math.multiplyFull(Integer.MAX_VALUE, Integer.MAX_VALUE));
    probe("abs-exact", () -> Math.absExact(-5) + "," + Math.absExact(-5L));
    probe("abs-exact-min", () -> Math.absExact(Integer.MIN_VALUE));
    probe("abs-exact-min-long", () -> Math.absExact(Long.MIN_VALUE));
    probe("pow-exact", () -> Math.powExact(2, 10) + "," + Math.powExact(-2, 31) + "," + Math.powExact(0, 0) + ","
        + Math.powExact(3L, 20) + "," + Math.powExact(-3, 3));
    probe("pow-exact-over", () -> Math.powExact(2, 31));
    probe("pow-exact-long-over", () -> Math.powExact(10L, 19));
    probe("pow-exact-negative", () -> Math.powExact(2, -1));
    probe("unsigned-exact", () -> Math.unsignedMultiplyExact(65535, 65537) + "," + Math.unsignedMultiplyExact(1L << 31, 2));
    probe("unsigned-exact-over", () -> Math.unsignedMultiplyExact(65536, 65536));
    probe("unsigned-pow", () -> Math.unsignedPowExact(2, 31) + "," + Math.unsignedPowExact(3L, 40));
    probe("strict", () -> StrictMath.log10(100.0) + "," + StrictMath.toDegrees(Math.PI) + "," + StrictMath.rint(2.5));

    String text = "hello wörld 😀 hello";
    probe("index-of-range", () -> text.indexOf('o', 5, 12) + "," + text.indexOf('o', 0, 4) + "," + text.indexOf(0x1F600, 0, text.length()));
    probe("index-of-string-range", () -> text.indexOf("hello", 1, text.length()) + "," + text.indexOf("hello", 0, 4)
        + "," + text.indexOf("", 3, 5));
    probe("index-of-range-bad", () -> text.indexOf('o', 5, 2));
    probe("offset-by", () -> text.offsetByCodePoints(0, 13) + "," + text.offsetByCodePoints(15, -2));
    probe("offset-by-bad", () -> text.offsetByCodePoints(0, 99));
    probe("content-equals", () -> "abc".contentEquals(new StringBuilder("abc")) + "," + "abc".contentEquals(new StringBuffer("abd"))
        + "," + "abc".contentEquals("abcd"));
    probe("copy-value", () -> String.copyValueOf(new char[] {'a', 'b', 'c'}) + "," + String.copyValueOf(new char[] {'a', 'b', 'c'}, 1, 2));
    probe("ctor-code-points", () -> new String(new int[] {104, 0x1F600, 105}, 0, 3));
    probe("ctor-builder", () -> new String(new StringBuilder("sb")) + new String(new StringBuffer("sf")));
    probe("split-delimiters", () -> "a1b22c333d".splitWithDelimiters("\\d+", 0));
    probe("split-delimiters-limit", () -> "a,b,c,d".splitWithDelimiters(",", 2));
    probe("split-delimiters-none", () -> "abc".splitWithDelimiters("x", 0));
    probe("split-delimiters-trailing", () -> "a,b,,".splitWithDelimiters(",", 0));
    probe("builder-insert", () -> {
      StringBuilder b = new StringBuilder("0123");
      b.insert(1, true).insert(0, 2.5).insert(0, 7L).insert(2, 1.5f).insert(0, new char[] {'x', 'y'})
          .insert(1, new char[] {'a', 'b', 'c'}, 1, 2).insert(0, "Q", 0, 1).insert(3, (Object) "obj");
      return b;
    });
    probe("builder-repeat", () -> new StringBuilder("[").repeat("ab", 3).repeat((int) 'z', 2).repeat(0x1F600, 1).append(']'));
    probe("builder-repeat-negative", () -> new StringBuilder().repeat("a", -1));
    probe("builder-code-point", () -> new StringBuilder().appendCodePoint(65).appendCodePoint(0x1F600));
    probe("builder-code-point-bad", () -> new StringBuilder().appendCodePoint(0x110000));
    probe("builder-ctor", () -> new StringBuilder((CharSequence) "seq").append(new StringBuffer("buf")));
    probe("buffer", () -> {
      StringBuffer b = new StringBuffer("abc");
      b.reverse().insert(1, 42).insert(0, "x", 0, 1).repeat("-", 2).appendCodePoint(0x41);
      return b + "," + b.substring(2) + "," + b.lastIndexOf("-") + "," + b.lastIndexOf("-", 4) + "," + b.length();
    });
    probe("buffer-ctor", () -> new StringBuffer((CharSequence) new StringBuilder("cs")));
    probe("buffer-offset", () -> new StringBuffer("a😀b").offsetByCodePoints(0, 2));

    probe("chars-to-chars", () -> {
      char[] target = new char[4];
      int n = Character.toChars(0x1F600, target, 1);
      return n + "," + (int) target[1] + "," + (int) target[2];
    });
    probe("chars-code-point-at", () -> Character.codePointAt("a😀".toCharArray(), 1) + "," + Character.codePointAt("a😀".toCharArray(), 1, 2));
    probe("chars-code-point-before", () -> Character.codePointBefore("a😀".toCharArray(), 3) + "," + Character.codePointBefore("a😀".toCharArray(), 2));
    probe("chars-count", () -> Character.codePointCount("a😀b".toCharArray(), 0, 4));
    probe("chars-offset", () -> Character.offsetByCodePoints("a😀b".toCharArray(), 0, 4, 0, 2) + "," + Character.offsetByCodePoints("a😀b", 4, -2));
    probe("chars-legacy", () -> Character.isJavaLetter('a') + "," + Character.isJavaLetterOrDigit('1') + "," + Character.isSpace(' ') + ","
        + Character.isSpace('x'));

    probe("decode", () -> Integer.decode("0x1F") + "," + Integer.decode("-#10") + "," + Integer.decode("010") + ","
        + Integer.decode("0") + "," + Integer.decode("+7") + "," + Integer.decode("-0") + "," + Integer.decode("-2147483648"));
    probe("decode-bad-sign", () -> Integer.decode("--1"));
    probe("decode-empty", () -> Integer.decode(""));
    probe("decode-bad-hex", () -> Integer.decode("0xZ"));
    probe("long-decode", () -> Long.decode("0x7FFFFFFFFFFFFFFF") + "," + Long.decode("-0x8000000000000000") + "," + Long.decode("0777"));
    probe("value-of-radix", () -> Integer.valueOf("ff", 16) + "," + Long.valueOf("-zz", 36) + "," + Integer.valueOf("101", 2));
    System.setProperty("shim.int", "0x10");
    System.setProperty("shim.long", "12345678901");
    System.setProperty("shim.bad", "nope");
    probe("get-integer", () -> Integer.getInteger("shim.int") + "," + Integer.getInteger("shim.bad", 5) + "," + Integer.getInteger("shim.none")
        + "," + Integer.getInteger("shim.none", Integer.valueOf(9)));
    probe("get-long", () -> Long.getLong("shim.long") + "," + Long.getLong("shim.bad", 6L) + "," + Long.getLong("shim.none"));
  }
}
