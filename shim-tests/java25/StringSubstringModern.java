/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringSubstringModern {
  private static int checks;

  private static void expect(boolean value) {
    if (!value) throw new AssertionError("substring check " + checks);
    ++checks;
  }

  private static void checkSlices(String text) {
    char[] chars = text.toCharArray();
    for (int start = 0; start <= chars.length; ++start) {
      for (int end = start; end <= chars.length; ++end) {
        String expected = new String(Arrays.copyOfRange(chars, start, end));
        String slice = text.substring(start, end);
        expect(slice.equals(expected));
        expect(slice.length() == end - start);
        expect(slice.hashCode() == expected.hashCode());
        expect(Arrays.equals(slice.toCharArray(), expected.toCharArray()));
        expect(Arrays.equals(slice.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8)));
        for (int index = 0; index < slice.length(); ++index) {
          expect(slice.charAt(index) == chars[start + index]);
        }
        for (int nested = 0; nested <= slice.length(); ++nested) {
          expect(slice.substring(nested).equals(new String(Arrays.copyOfRange(chars, start + nested, end))));
        }
      }
      expect(text.substring(start).equals(new String(Arrays.copyOfRange(chars, start, chars.length))));
    }
  }

  private static void invalid(Runnable operation) {
    try {
      operation.run();
      throw new AssertionError("Invalid substring bounds accepted");
    } catch (IndexOutOfBoundsException expected) {
      ++checks;
    }
  }

  public static void main(String[] args) {
    for (String text : new String[] {"", "a", "plain ASCII", "\u00f1\u00e9\u20ac\u6f22\ud83d\ude00",
        new String(new char[] {'x', 0, 'y', (char) 0xd800, 'z', (char) 0xdc00})}) {
      checkSlices(text);
      if (text.length() > 2) checkSlices(text.substring(1, text.length() - 1));
      invalid(() -> text.substring(-1));
      invalid(() -> text.substring(text.length() + 1));
      invalid(() -> text.substring(0, text.length() + 1));
      invalid(() -> text.substring(1, 0));
      invalid(() -> text.substring(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }
    byte[] utf8 = "prefix-\u00f1\u20ac\ud83d\ude00-suffix".getBytes(StandardCharsets.UTF_8);
    String decoded = new String(utf8, StandardCharsets.UTF_8);
    expect(decoded.equals("prefix-\u00f1\u20ac\ud83d\ude00-suffix"));
    String middle = decoded.substring(7, decoded.length() - 7);
    expect(middle.equals("\u00f1\u20ac\ud83d\ude00"));
    utf8[7] = 'x';
    expect(middle.equals("\u00f1\u20ac\ud83d\ude00"));
    char[] mutable = "prefix-value-suffix".toCharArray();
    String original = new String(mutable);
    String slice = original.substring(7, 12);
    mutable[7] = 'X';
    expect(slice.equals("value"));
    checkSlices(slice);

    char[] large = new char[128 * 1024];
    Arrays.fill(large, 'a');
    large[large.length / 2] = 'z';
    String ascii = new String(large);
    for (int at = 0; at < 1000; ++at) {
      int start = large.length / 2 - 1;
      expect(ascii.substring(start, start + 3).equals("aza"));
    }
    System.out.println("STRING SUBSTRING TESTS PASSED: " + checks);
  }
}
