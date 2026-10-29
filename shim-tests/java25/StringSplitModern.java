/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.Arrays;
import java.util.regex.Pattern;

public class StringSplitModern {
  static int checks;
  static long digest;
  static void check(String text, String regex, int limit) {
    String[] actual = text.split(regex, limit);
    String[] expected = Pattern.compile(regex).split(text, limit);
    if (!Arrays.equals(actual, expected)) throw new AssertionError(regex + ":" + limit + ":" + Arrays.toString(actual));
    for (String value : actual) digest = digest * 31 + value.hashCode();
    digest = digest * 31 + actual.length;
    ++checks;
  }
  public static void main(String[] args) {
    String[] texts = {"", "a", "aaa", "a,,b,,", ",", ",,", ",a,", "\n\na\n", "x\u20acy\u20ac", "x\ud83d\ude00y\ud83d\ude00", "x\ud800y\udc00z", "a|b|", "a.b.", "a\\b\\", "a b ", "a-b-"};
    String[] regexes = {",", "a", " ", "\n", "\u20ac", "\ud83d\ude00", "\\|", "\\.", "\\\\", "\\-", "-", "/", "\\/", "\\n", "\\s+", "a*", "", ".", "[ab]", "\\Qa\\E"};
    int[] limits = {Integer.MIN_VALUE, -3, -1, 0, 1, 2, 3, 20, Integer.MAX_VALUE};
    for (String text : texts) for (String regex : regexes) for (int limit : limits) {
      check(text, regex, limit);
      check(("prefix" + text + "suffix").substring(6, 6 + text.length()), regex, limit);
    }
    // A lone surrogate must use the regex engine, not split a code point via
    // indexOf(char). Keep parity with that engine here: its existing handling
    // of lone-surrogate patterns within pairs differs from the host JDK.
    for (String text : texts) for (String regex : new String[] {"\ud83d", "\ude00", "\ud800", "\udc00"}) {
      for (int limit : limits) {
        if (!Arrays.equals(text.split(regex, limit), Pattern.compile(regex).split(text, limit))) throw new AssertionError("surrogate fallback");
        ++checks;
      }
    }
    for (String invalid : new String[] {"[", "\\", "*"}) {
      try { "abc".split(invalid, 1); throw new AssertionError(); }
      catch (java.util.regex.PatternSyntaxException expected) { ++checks; }
    }
    try { "abc".split(null); throw new AssertionError(); }
    catch (NullPointerException expected) { ++checks; }
    System.out.println("split checks=" + checks + " digest=" + digest);
  }
}
