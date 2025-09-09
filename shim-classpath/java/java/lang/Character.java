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

public final class Character implements Comparable<Character>, java.lang.constant.Constable {
  public static final int MIN_RADIX = 2;
  public static final int MAX_RADIX = 36;

  public static final Class TYPE = shim.Classes.forCanonicalName("C");

  public static final char MIN_VALUE = '\u0000';
  public static final char MAX_VALUE = '\uffff';

  public static final int SIZE = 16;
  public static final int BYTES = 2;

  public static final char MIN_HIGH_SURROGATE = '\uD800';
  public static final char MAX_HIGH_SURROGATE = '\uDBFF';
  public static final char MIN_LOW_SURROGATE = '\uDC00';
  public static final char MAX_LOW_SURROGATE = '\uDFFF';
  public static final char MIN_SURROGATE = MIN_HIGH_SURROGATE;
  public static final char MAX_SURROGATE = MAX_LOW_SURROGATE;

  public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;
  public static final int MIN_CODE_POINT = 0x000000;
  public static final int MAX_CODE_POINT = 0x10FFFF;

  private final char value;

  public Character(char value) {
    this.value = value;
  }

  public static Character valueOf(char value) {
    return new Character(value);
  }

  public int compareTo(Character o) {
    return value - o.value;
  }

  public boolean equals(Object o) {
    return o instanceof Character && ((Character) o).value == value;
  }

  public int hashCode() {
    return hashCode(value);
  }

  public static int hashCode(char value) {
    return (int) value;
  }

  public static boolean isSurrogate(char ch) {
    return ch >= MIN_SURROGATE && ch <= MAX_SURROGATE;
  }

  public static char highSurrogate(int codePoint) {
    return (char) ((codePoint >>> 10) + ('\uD800' - (0x010000 >>> 10)));
  }

  public static char lowSurrogate(int codePoint) {
    return (char) ((codePoint & 0x3ff) + '\uDC00');
  }

  @Override
  public java.util.Optional<java.lang.constant.DynamicConstantDesc<Character>> describeConstable() {
    return java.util.Optional.of(java.lang.constant.DynamicConstantDesc.ofNamed(
      java.lang.constant.ConstantDescs.BSM_EXPLICIT_CAST,
      java.lang.constant.ConstantDescs.DEFAULT_NAME,
      java.lang.constant.ConstantDescs.CD_char,
      (int) value));
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(char v) {
    return new String(new char[] { v });
  }

  public static String toString(int codePoint) {
    return new String(toChars(codePoint));
  }

  public static int compare(char x, char y) {
    return x - y;
  }

  public char charValue() {
    return value;
  }

  public static char toLowerCase(char c) {
    if (c >= 'A' && c <= 'Z') {
      return (char) ((c - 'A') + 'a');
    } else {
      return c;
    }
  }

  public static int toLowerCase(int codePoint) {
    if (isSupplementaryCodePoint(codePoint)) {
      return codePoint;
    } else {
      return toLowerCase((char) codePoint);
    }
  }

  public static char toUpperCase(char c) {
    if (c >= 'a' && c <= 'z') {
      return (char) ((c - 'a') + 'A');
    } else {
      return c;
    }
  }

  public static int toUpperCase(int codePoint) {
    if (isSupplementaryCodePoint(codePoint)) {
      return codePoint;
    } else {
      return toUpperCase((char) codePoint);
    }
  }

  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  public static boolean isDigit(int c) {
    return c >= '0' && c <= '9';
  }

  public static int digit(char c, int radix) {
    int digit = 0;
    if ((c >= '0') && (c <= '9')) {
      digit = c - '0';
    } else if ((c >= 'a') && (c <= 'z')) {
      digit = c - 'a' + 10;
    } else if ((c >= 'A') && (c <= 'Z')) {
      digit = c - 'A' + 10;
    } else {
      return -1;
    }

    if (digit < radix) {
      return digit;
    } else {
      return -1;
    }
  }

  public static char forDigit(int digit, int radix) {
    if (MIN_RADIX <= radix && radix <= MAX_RADIX) {
      if (0 <= digit && digit < radix) {
        return (char) (digit < 10 ? digit + '0' : digit + 'a' - 10);
      }
    }
    return 0;
  }

  public static boolean isLetter(int c) {
    return canCastToChar(c) && isLetter((char) c);
  }

  public static boolean isLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  public static boolean isLetterOrDigit(char c) {
    return isDigit(c) || isLetter(c);
  }

  public static boolean isLetterOrDigit(int c) {
    return canCastToChar(c) && (isDigit((char) c) || isLetter((char) c));
  }

  public static boolean isLowerCase(int c) {
    return canCastToChar(c) && isLowerCase((char) c);
  }

  public static boolean isLowerCase(char c) {
    return (c >= 'a' && c <= 'z');
  }

  public static boolean isUpperCase(char c) {
    return (c >= 'A' && c <= 'Z');
  }

  public static boolean isUpperCase(int c) {
    return canCastToChar(c) && isUpperCase((char) c);
  }

  public static boolean isWhitespace(char c) {
    return isWhitespace((int) c);
  }

  public static boolean isWhitespace(int c) {
    if (c >= 0x0009 && c <= 0x000D) return true;
    if (c >= 0x001C && c <= 0x0020) return true;
    if (c == 0x1680) return true;
    if (c >= 0x2000 && c <= 0x2006) return true;
    if (c >= 0x2008 && c <= 0x200A) return true;
    if (c == 0x2028 || c == 0x2029 || c == 0x205F || c == 0x3000) return true;
    return false;
  }

  public static boolean isSpaceChar(char c) {
    return isWhitespace(c);
  }

  public static boolean isHighSurrogate(char ch) {
    return ch >= '\uD800' && ch <= '\uDBFF';
  }

  public static boolean isLowSurrogate(char ch) {
    return ch >= '\uDC00' && ch <= '\uDFFF';
  }

  public static boolean isISOControl(char ch) {
   return ch <= '\u001F' || (ch >= '\u007F' && ch <= '\u009F');
  }

  public static boolean isJavaIdentifierStart(char ch) {
    return isLetter(ch) || ch == '$' || ch == '_';
    //TODO: add if (getType(ch)==LETTER_NUMBER) || getType(ch)==CURRENCY_SYMBOL
  }
  
  public static boolean isJavaIdentifierStart(int c) {
    return canCastToChar(c) && isJavaIdentifierStart((char) c);
  }
  
  public static boolean isJavaIdentifierPart(char ch) {
    return isJavaIdentifierStart(ch) || isDigit(ch);
	//TODO:Check for numeric letters (such as a Roman numeral character),combining marks,non-spacing marks
	//add isIdentifierIgnorable(ch)
  }

  public static boolean isJavaIdentifierPart(int c) {
    return canCastToChar(c) && isJavaIdentifierPart((char) c);
  }

  public static int toCodePoint(char high, char low) {
    return (((high & 0x3FF) << 10) | (low & 0x3FF)) + 0x10000;
  }

  public static boolean isValidCodePoint(int codePoint) {
    return codePoint >= 0 && codePoint <= 0x10FFFF;
  }

  public static boolean isBmpCodePoint(int codePoint) {
    return codePoint >= 0 && codePoint <= 0xFFFF;
  }

  public static int charCount(int codePoint) {
    return codePoint >= 0x10000 ? 2 : 1;
  }

  public static boolean isSupplementaryCodePoint(int codePoint) {
    return codePoint >= 0x10000 && codePoint <= 0x10FFFF;
  }

  private static boolean canCastToChar(int codePoint) {
    return (codePoint >= 0 && codePoint <= 0xFFFF);
  }

  public static char[] toChars(int codePoint) {
    if (!isValidCodePoint(codePoint)) {
      throw new IllegalArgumentException();
    }
    if (isSupplementaryCodePoint(codePoint)) {
      int cpPrime = codePoint - 0x10000;
      int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
      int low = 0xDC00 | (cpPrime & 0x3FF);
      return new char[] { (char) high, (char) low };
    }
    return new char[] { (char) codePoint };
  }

  public static boolean isSurrogatePair(char high, char low) {
    return isHighSurrogate(high) && isLowSurrogate(low);
  }

  public static int codePointAt(CharSequence sequence, int offset) {
    int length = sequence.length();
    if (offset < 0 || offset >= length) {
      throw new IndexOutOfBoundsException();
    }

    char high = sequence.charAt(offset);
    if (! isHighSurrogate(high) || offset >= length) {
      return high;
    }
    char low = sequence.charAt(offset + 1);
    if (! isLowSurrogate(low)) {
      return high;
    }

    return toCodePoint(high, low);
  }

  public static int codePointBefore(CharSequence sequence, int index) {
    int length = sequence.length();
    if (index < 1 || index > length) {
      throw new IndexOutOfBoundsException();
    }
    char low = sequence.charAt(index - 1);
    if (isLowSurrogate(low) && index > 1) {
      char high = sequence.charAt(index - 2);
      if (isHighSurrogate(high)) {
        return toCodePoint(high, low);
      }
    }
    return low;
  }

  public static int codePointCount(CharSequence sequence, int start, int end) {
    int length = sequence.length();
    if (start < 0 || start > end || end > length) {
      throw new IndexOutOfBoundsException();
    }

    int count = 0;
    for (int i = start; i < end; ++i) {
      if (isHighSurrogate(sequence.charAt(i))
          && (i + 1) < end
          && isLowSurrogate(sequence.charAt(i + 1)))
      {
        ++ i;
      }
      ++ count;
    }
    return count;
  }

  public static char reverseBytes(char ch) {
    return (char) (((ch & 0xFF00) >> 8) | (ch << 8));
  }
}
