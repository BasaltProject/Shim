/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.function.*;

public class CoreLangEdgeModern {
  interface Op { Object run() throws Exception; }

  static void t(String name, Op op) {
    try {
      System.out.println(name + " = " + op.run());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void main(String[] args) {
    Locale.setDefault(Locale.ENGLISH);
    t("charAt", () -> "".charAt(0));
    t("charAtNeg", () -> "abc".charAt(-1));
    t("substring", () -> "abc".substring(2, 1));
    t("subSequence", () -> "abc".subSequence(0, 9));
    t("getChars", () -> { "abc".getChars(0, 3, new char[2], 0); return null; });
    t("indexOfRange", () -> "abc".indexOf('b', 2, 1));
    t("codePointAt", () -> "abc".codePointAt(3));
    t("codePointCount", () -> "abc".codePointCount(2, 9));
    t("valueOfChars", () -> String.valueOf(new char[2], 1, 5));
    t("lastIndexOf", () -> "abcabc".lastIndexOf("c", 4) + " " + "abc".lastIndexOf("") + " " + "abc".indexOf("", 9));
    t("supplementary", () -> "a😀b".indexOf(0x1F600) + " " + "a😀b".lastIndexOf(0x1F600));
    t("trimControl", () -> "\u0000 x \u0001".trim().length());
    t("upper", () -> "straße ﬁ".toUpperCase());
    t("lower", () -> "İ ΣAΣ".toLowerCase());
    t("turkic", () -> "TITLE i".toLowerCase(Locale.forLanguageTag("tr")) + " " + "title".toUpperCase(Locale.forLanguageTag("tr")));
    t("upperNullLocale", () -> "x".toUpperCase(null));
    t("compareIgnoreCase", () -> "Hello".compareToIgnoreCase("😀") + " " + "İa".compareToIgnoreCase("😀"));
    t("replaceNonAscii", () -> "a b".replace(' ', 'é'));
    t("indentMin", () -> "  Hello".indent(Integer.MIN_VALUE));
    t("repeatLimit", () -> "ab".repeat(Integer.MAX_VALUE));
    t("repeatNeg", () -> "ab".repeat(-1));
    t("parseDoubleHex", () -> Double.parseDouble("0x1F"));
    t("parseDoubleWs", () -> Double.parseDouble("\u0000 1.5e1 \n"));
    t("parseDoubleMsg", () -> Double.parseDouble("a"));
    t("parseDoubleEmpty", () -> Double.parseDouble("  "));
    t("parseDoubleLit", () -> Double.parseDouble("0x1.8p1") + " " + Double.parseDouble("-Infinity") + " " + Float.parseFloat("1f"));
    t("parseDoubleBadCase", () -> Double.parseDouble("infinity"));
    t("radixToString", () -> Integer.toString(255, 1) + " " + Long.toString(-255L, 99) + " " + Integer.toUnsignedString(-1, 0));
    t("parseUnsignedMsg", () -> Integer.parseUnsignedInt("a", 2));
    t("parseUnsignedRadix", () -> Integer.parseUnsignedInt("", 1));
    t("parseUnsignedLong", () -> Long.parseUnsignedLong("-1", 10));
    t("enumFinal", () -> Thread.State.NEW.equals(Thread.State.NEW) + " " + Thread.State.NEW.hashCode() * 0);
    t("lambdaCast", () -> { Function f = (Function<String, Integer>) s -> s.length(); return f.apply(3); });
    t("lambdaArrayCast", () -> { Comparator c = Comparator.naturalOrder(); return c.compare(new int[1], null); });
    t("arraysAsList", () -> Arrays.asList(1, 2).getClass().getName());
  }
}
