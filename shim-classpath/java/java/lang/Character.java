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

  public static final byte UNASSIGNED = 0;
  public static final byte UPPERCASE_LETTER = 1;
  public static final byte LOWERCASE_LETTER = 2;
  public static final byte TITLECASE_LETTER = 3;
  public static final byte MODIFIER_LETTER = 4;
  public static final byte OTHER_LETTER = 5;
  public static final byte NON_SPACING_MARK = 6;
  public static final byte ENCLOSING_MARK = 7;
  public static final byte COMBINING_SPACING_MARK = 8;
  public static final byte DECIMAL_DIGIT_NUMBER = 9;
  public static final byte LETTER_NUMBER = 10;
  public static final byte OTHER_NUMBER = 11;
  public static final byte SPACE_SEPARATOR = 12;
  public static final byte LINE_SEPARATOR = 13;
  public static final byte PARAGRAPH_SEPARATOR = 14;
  public static final byte CONTROL = 15;
  public static final byte FORMAT = 16;
  public static final byte PRIVATE_USE = 18;
  public static final byte SURROGATE = 19;
  public static final byte DASH_PUNCTUATION = 20;
  public static final byte START_PUNCTUATION = 21;
  public static final byte END_PUNCTUATION = 22;
  public static final byte CONNECTOR_PUNCTUATION = 23;
  public static final byte OTHER_PUNCTUATION = 24;
  public static final byte MATH_SYMBOL = 25;
  public static final byte CURRENCY_SYMBOL = 26;
  public static final byte MODIFIER_SYMBOL = 27;
  public static final byte OTHER_SYMBOL = 28;
  public static final byte INITIAL_QUOTE_PUNCTUATION = 29;
  public static final byte FINAL_QUOTE_PUNCTUATION = 30;
  public static final byte DIRECTIONALITY_UNDEFINED = -1;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;
  public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;
  public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;
  public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;
  public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;
  public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;
  public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;
  public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;
  public static final byte DIRECTIONALITY_WHITESPACE = 12;
  public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;
  public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;
  public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_ISOLATE = 19;
  public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ISOLATE = 20;
  public static final byte DIRECTIONALITY_FIRST_STRONG_ISOLATE = 21;
  public static final byte DIRECTIONALITY_POP_DIRECTIONAL_ISOLATE = 22;

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

  public static boolean isTitleCase(char c) {
    return isTitleCase((int) c);
  }

  public static boolean isTitleCase(int cp) {
    return cp == 0x01C5 || cp == 0x01C8 || cp == 0x01CB || cp == 0x01F2
        || (cp >= 0x1F88 && cp <= 0x1F8F)
        || (cp >= 0x1F98 && cp <= 0x1F9F)
        || (cp >= 0x1FA8 && cp <= 0x1FAF)
        || cp == 0x1FBC || cp == 0x1FCC || cp == 0x1FFC;
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
    return isISOControl((int) ch);
  }

  public static boolean isISOControl(int codePoint) {
    return (codePoint >= 0 && codePoint <= 0x1F) || (codePoint >= 0x7F && codePoint <= 0x9F);
  }

  public static int getType(char ch) {
    return getType((int) ch);
  }

  public static int getType(int cp) {
    if (cp < 0 || cp > 0x10FFFF) {
      return UNASSIGNED;
    }
    if (cp >= 0xD800 && cp <= 0xDFFF) {
      return SURROGATE;
    }
    if (isIdentifierIgnorable(cp)) {
      if ((cp >= 0x0000 && cp <= 0x0008) || (cp >= 0x000E && cp <= 0x001B) || (cp >= 0x007F && cp <= 0x009F)) {
        return CONTROL;
      }
      return FORMAT;
    }
    if (isISOControl(cp)) {
      return CONTROL;
    }
    if (isDigit(cp)) {
      return DECIMAL_DIGIT_NUMBER;
    }
    if (isUpperCase(cp)) {
      return UPPERCASE_LETTER;
    }
    if (isLowerCase(cp)) {
      return LOWERCASE_LETTER;
    }
    if (isTitleCase(cp)) {
      return TITLECASE_LETTER;
    }
    if (cp == '_' || cp == 0x203F || cp == 0x2040 || cp == 0x2054 || cp == 0xFE33 || cp == 0xFE34 || cp == 0xFE4D || cp == 0xFE4E || cp == 0xFE4F || cp == 0xFF3F) {
      return CONNECTOR_PUNCTUATION;
    }
    if (cp == '$' || cp == 0x00A2 || cp == 0x00A3 || cp == 0x00A4 || cp == 0x00A5 || cp == 0x20AC) {
      return CURRENCY_SYMBOL;
    }
    if (cp >= 0x2160 && cp <= 0x2188) {
      return LETTER_NUMBER;
    }
    if (cp == ' ' || cp == 0x00A0 || (cp >= 0x2000 && cp <= 0x200A) || cp == 0x202F || cp == 0x205F || cp == 0x3000) {
      return SPACE_SEPARATOR;
    }
    if (cp == 0x2028) {
      return LINE_SEPARATOR;
    }
    if (cp == 0x2029) {
      return PARAGRAPH_SEPARATOR;
    }
    if (isLetter(cp)) {
      return OTHER_LETTER;
    }
    return OTHER_PUNCTUATION;
  }

  public static boolean isIdentifierIgnorable(char ch) {
    return isIdentifierIgnorable((int) ch);
  }

  public static boolean isIdentifierIgnorable(int codePoint) {
    return (codePoint >= 0x0000 && codePoint <= 0x0008)
        || (codePoint >= 0x000E && codePoint <= 0x001B)
        || (codePoint >= 0x007F && codePoint <= 0x009F)
        || (codePoint >= 0x200C && codePoint <= 0x200F)
        || (codePoint >= 0x202A && codePoint <= 0x202E)
        || (codePoint >= 0x2060 && codePoint <= 0x206F)
        || (codePoint == 0xFEFF)
        || (codePoint >= 0xFFF9 && codePoint <= 0xFFFB)
        || (codePoint >= 0x1D173 && codePoint <= 0x1D17A)
        || (codePoint >= 0xE0001 && codePoint <= 0xE007F);
  }

  public static boolean isJavaIdentifierStart(char ch) {
    return isJavaIdentifierStart((int) ch);
  }

  public static boolean isJavaIdentifierStart(int codePoint) {
    int type = getType(codePoint);
    return type == UPPERCASE_LETTER
        || type == LOWERCASE_LETTER
        || type == TITLECASE_LETTER
        || type == MODIFIER_LETTER
        || type == OTHER_LETTER
        || type == LETTER_NUMBER
        || type == CURRENCY_SYMBOL
        || type == CONNECTOR_PUNCTUATION;
  }

  public static boolean isJavaIdentifierPart(char ch) {
    return isJavaIdentifierPart((int) ch);
  }

  public static boolean isJavaIdentifierPart(int codePoint) {
    int type = getType(codePoint);
    return isJavaIdentifierStart(codePoint)
        || type == DECIMAL_DIGIT_NUMBER
        || type == COMBINING_SPACING_MARK
        || type == NON_SPACING_MARK
        || isIdentifierIgnorable(codePoint);
  }

  public static boolean isUnicodeIdentifierStart(char ch) {
    return isUnicodeIdentifierStart((int) ch);
  }

  public static boolean isUnicodeIdentifierStart(int codePoint) {
    int type = getType(codePoint);
    return type == UPPERCASE_LETTER
        || type == LOWERCASE_LETTER
        || type == TITLECASE_LETTER
        || type == MODIFIER_LETTER
        || type == OTHER_LETTER
        || type == LETTER_NUMBER;
  }

  public static boolean isUnicodeIdentifierPart(char ch) {
    return isUnicodeIdentifierPart((int) ch);
  }

  public static boolean isUnicodeIdentifierPart(int codePoint) {
    int type = getType(codePoint);
    return isUnicodeIdentifierStart(codePoint)
        || type == CONNECTOR_PUNCTUATION
        || type == DECIMAL_DIGIT_NUMBER
        || type == COMBINING_SPACING_MARK
        || type == NON_SPACING_MARK
        || isIdentifierIgnorable(codePoint);
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

  public static boolean isEmojiModifier(int codePoint) {
    return codePoint >= 0x1F3FB && codePoint <= 0x1F3FF;
  }

  public static boolean isEmojiComponent(int codePoint) {
    if (codePoint == 0x23 || codePoint == 0x2A) return true;
    if (codePoint >= 0x30 && codePoint <= 0x39) return true;
    if (codePoint == 0x200D || codePoint == 0x20E3 || codePoint == 0xFE0F) return true;
    if (codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF) return true;
    if (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF) return true;
    if (codePoint >= 0xE0020 && codePoint <= 0xE007F) return true;
    return false;
  }

  public static boolean isEmojiModifierBase(int cp) {
    if (cp == 0x261D || cp == 0x26F9) return true;
    if (cp >= 0x270A && cp <= 0x270D) return true;
    if (cp == 0x1F385 || cp == 0x1F3C7 || cp == 0x1F4AA || cp == 0x1F57A) return true;
    if (cp >= 0x1F3C2 && cp <= 0x1F3C4) return true;
    if (cp >= 0x1F3CA && cp <= 0x1F3CC) return true;
    if (cp >= 0x1F442 && cp <= 0x1F443) return true;
    if (cp >= 0x1F446 && cp <= 0x1F450) return true;
    if (cp >= 0x1F466 && cp <= 0x1F478) return true;
    if (cp == 0x1F47C || cp == 0x1F48F || cp == 0x1F491) return true;
    if (cp >= 0x1F481 && cp <= 0x1F483) return true;
    if (cp >= 0x1F485 && cp <= 0x1F487) return true;
    if (cp >= 0x1F574 && cp <= 0x1F575) return true;
    if (cp == 0x1F590 || cp == 0x1F595 || cp == 0x1F596) return true;
    if (cp >= 0x1F645 && cp <= 0x1F647) return true;
    if (cp >= 0x1F64B && cp <= 0x1F64F) return true;
    if (cp == 0x1F6A3 || cp == 0x1F6C0 || cp == 0x1F6CC) return true;
    if (cp >= 0x1F6B4 && cp <= 0x1F6B6) return true;
    if (cp == 0x1F90C || cp == 0x1F90F || cp == 0x1F926 || cp == 0x1F977 || cp == 0x1F9BB) return true;
    if (cp >= 0x1F918 && cp <= 0x1F91F) return true;
    if (cp >= 0x1F930 && cp <= 0x1F93E) return true;
    if (cp >= 0x1F9B5 && cp <= 0x1F9B6) return true;
    if (cp >= 0x1F9B8 && cp <= 0x1F9B9) return true;
    if (cp >= 0x1F9CD && cp <= 0x1F9CF) return true;
    if (cp >= 0x1F9D1 && cp <= 0x1F9DD) return true;
    if (cp >= 0x1FAF0 && cp <= 0x1FAF8) return true;
    return false;
  }

  public static boolean isExtendedPictographic(int cp) {
    if (cp == 0x00A9 || cp == 0x00AE || cp == 0x203C || cp == 0x2049 || cp == 0x2122 || cp == 0x2139) return true;
    if (cp >= 0x2194 && cp <= 0x2199) return true;
    if (cp == 0x21A9 || cp == 0x21AA || cp == 0x2328 || cp == 0x23CF || cp == 0x24C2) return true;
    if (cp >= 0x231A && cp <= 0x231B) return true;
    if (cp >= 0x23E9 && cp <= 0x23F3) return true;
    if (cp >= 0x23F8 && cp <= 0x23FA) return true;
    if (cp >= 0x25AA && cp <= 0x25AB) return true;
    if (cp == 0x25B6 || cp == 0x25C0) return true;
    if (cp >= 0x25FB && cp <= 0x25FE) return true;
    if (cp >= 0x2600 && cp <= 0x2604) return true;
    if (cp >= 0x2614 && cp <= 0x2615) return true;
    if (cp >= 0x2648 && cp <= 0x2653) return true;
    if (cp >= 0x26AA && cp <= 0x26AB) return true;
    if (cp >= 0x26BD && cp <= 0x26BE) return true;
    if (cp >= 0x26C4 && cp <= 0x26C5) return true;
    if (cp >= 0x2708 && cp <= 0x270D) return true;
    if (cp >= 0x2753 && cp <= 0x2755) return true;
    if (cp >= 0x2763 && cp <= 0x2764) return true;
    if (cp >= 0x2795 && cp <= 0x2797) return true;
    if (cp == 0x260E || cp == 0x2611 || cp == 0x2618 || cp == 0x261D || cp == 0x2620 || cp == 0x2622 || cp == 0x2623) return true;
    if (cp == 0x2626 || cp == 0x262A || cp == 0x262E || cp == 0x262F || cp == 0x2638 || cp == 0x2639 || cp == 0x263A) return true;
    if (cp == 0x2640 || cp == 0x2642 || cp == 0x265F || cp == 0x2660 || cp == 0x2663 || cp == 0x2665 || cp == 0x2666) return true;
    if (cp == 0x2668 || cp == 0x267B || cp == 0x267E || cp == 0x267F || cp == 0x2692 || cp == 0x2693 || cp == 0x2694) return true;
    if (cp == 0x2695 || cp == 0x2696 || cp == 0x2697 || cp == 0x2699 || cp == 0x269B || cp == 0x269C || cp == 0x26A0) return true;
    if (cp == 0x26A1 || cp == 0x26A7 || cp == 0x26B0 || cp == 0x26B1 || cp == 0x26C8 || cp == 0x26CE || cp == 0x26CF) return true;
    if (cp == 0x26D1 || cp == 0x26D3 || cp == 0x26D4 || cp == 0x26E9 || cp == 0x26EA || cp == 0x26F0 || cp == 0x26F1) return true;
    if (cp == 0x26F2 || cp == 0x26F3 || cp == 0x26F4 || cp == 0x26F5 || cp == 0x26F7 || cp == 0x26F8 || cp == 0x26F9) return true;
    if (cp == 0x26FA || cp == 0x26FD) return true;
    if (cp == 0x2702 || cp == 0x2705 || cp == 0x270F || cp == 0x2712 || cp == 0x2714 || cp == 0x2716 || cp == 0x271D) return true;
    if (cp == 0x2721 || cp == 0x2728 || cp == 0x2733 || cp == 0x2734 || cp == 0x2744 || cp == 0x2747 || cp == 0x274C) return true;
    if (cp == 0x274E || cp == 0x2757 || cp == 0x27A1 || cp == 0x27B0 || cp == 0x27BF) return true;
    if (cp >= 0x2B05 && cp <= 0x2B07) return true;
    if (cp >= 0x2B1B && cp <= 0x2B1C) return true;
    if (cp == 0x2B50 || cp == 0x2B55 || cp == 0x3030 || cp == 0x303D || cp == 0x3297 || cp == 0x3299) return true;
    if (cp >= 0x1F000 && cp <= 0x1FAFF) {
      if (cp >= 0x1F1E6 && cp <= 0x1F1FF) return false;
      if (cp >= 0x1F3FB && cp <= 0x1F3FF) return false;
      return true;
    }
    return false;
  }

  public static boolean isEmojiPresentation(int cp) {
    if (cp >= 0x231A && cp <= 0x231B) return true;
    if (cp >= 0x23E9 && cp <= 0x23EC) return true;
    if (cp == 0x23F0 || cp == 0x23F3) return true;
    if (cp >= 0x25FD && cp <= 0x25FE) return true;
    if (cp >= 0x2614 && cp <= 0x2615) return true;
    if (cp >= 0x2648 && cp <= 0x2653) return true;
    if (cp == 0x267F || cp == 0x2693 || cp == 0x26A1 || cp == 0x26AA || cp == 0x26AB) return true;
    if (cp >= 0x26BD && cp <= 0x26BE) return true;
    if (cp >= 0x26C4 && cp <= 0x26C5) return true;
    if (cp == 0x26CE || cp == 0x26D4 || cp == 0x26EA || cp == 0x26F2 || cp == 0x26F3 || cp == 0x26F5 || cp == 0x26FA || cp == 0x26FD) return true;
    if (cp == 0x2705 || cp == 0x270A || cp == 0x270B || cp == 0x2728 || cp == 0x274C || cp == 0x274E) return true;
    if (cp >= 0x2753 && cp <= 0x2755) return true;
    if (cp == 0x2757 || cp == 0x27B0 || cp == 0x27BF) return true;
    if (cp >= 0x2795 && cp <= 0x2797) return true;
    if (cp >= 0x2B1B && cp <= 0x2B1C) return true;
    if (cp == 0x2B50 || cp == 0x2B55) return true;
    if (cp >= 0x1F1E6 && cp <= 0x1F1FF) return true;
    if (cp >= 0x1F300 && cp <= 0x1F64F) {
      if (cp >= 0x1F3FB && cp <= 0x1F3FF) return true;
      return isExtendedPictographic(cp);
    }
    if (cp >= 0x1F680 && cp <= 0x1FAFF) {
      return isExtendedPictographic(cp);
    }
    return false;
  }

  public static boolean isEmoji(int cp) {
    if (isEmojiComponent(cp)) {
      if (cp == 0x200D || cp == 0x20E3 || cp == 0xFE0F || (cp >= 0xE0020 && cp <= 0xE007F)) {
        return false;
      }
      return true;
    }
    if (isEmojiPresentation(cp)) return true;
    if (isExtendedPictographic(cp)) return true;
    return false;
  }
}
