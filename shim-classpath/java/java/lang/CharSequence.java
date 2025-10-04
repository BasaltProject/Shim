/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public interface CharSequence {
  public char charAt(int index);
        
  int length();
        
  CharSequence subSequence(int start, int end);
        
  String toString();

  default void getChars(int start, int end, char[] dst, int dstBegin) {
    java.util.Objects.checkFromToIndex(start, end, length());
    java.util.Objects.checkFromIndexSize(dstBegin, end - start, dst.length);
    for (int i = start, j = dstBegin; i < end; i++, j++) {
      dst[j] = charAt(i);
    }
  }

  default boolean isEmpty() {
    return length() == 0;
  }

  public static int compare(CharSequence cs1, CharSequence cs2) {
    if (cs1 == cs2) {
      return 0;
    }
    java.util.Objects.requireNonNull(cs1);
    java.util.Objects.requireNonNull(cs2);
    for (int i = 0, len = Math.min(cs1.length(), cs2.length()); i < len; i++) {
      char a = cs1.charAt(i);
      char b = cs2.charAt(i);
      if (a != b) {
        return a - b;
      }
    }
    return cs1.length() - cs2.length();
  }

  default java.util.stream.IntStream chars() {
    class CharIterator implements java.util.PrimitiveIterator.OfInt {
      int cur = 0;

      @Override
      public boolean hasNext() {
        return cur < length();
      }

      @Override
      public int nextInt() {
        if (!hasNext()) {
          throw new java.util.NoSuchElementException();
        }
        return charAt(cur++);
      }
    }
    return java.util.stream.StreamSupport.intStream(
        java.util.Spliterators.spliterator(new CharIterator(), (long) length(), java.util.Spliterator.ORDERED),
        false);
  }

  default java.util.stream.IntStream codePoints() {
    class CodePointIterator implements java.util.PrimitiveIterator.OfInt {
      int cur = 0;

      @Override
      public boolean hasNext() {
        return cur < length();
      }

      @Override
      public int nextInt() {
        if (!hasNext()) {
          throw new java.util.NoSuchElementException();
        }
        char c1 = charAt(cur++);
        if (Character.isHighSurrogate(c1) && cur < length()) {
          char c2 = charAt(cur);
          if (Character.isLowSurrogate(c2)) {
            cur++;
            return Character.toCodePoint(c1, c2);
          }
        }
        return c1;
      }
    }
    return java.util.stream.StreamSupport.intStream(
        java.util.Spliterators.spliteratorUnknownSize(new CodePointIterator(), java.util.Spliterator.ORDERED),
        false);
  }
}
