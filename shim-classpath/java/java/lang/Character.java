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
    return (char) toLowerCase((int) c);
  }

  public static int toLowerCase(int codePoint) {
    if (codePoint >= 'A' && codePoint <= 'Z') return codePoint + ('a' - 'A');
    if (codePoint < 128) return codePoint;
    return CharacterMappings.lower(codePoint);
  }

  public static char toUpperCase(char c) {
    return (char) toUpperCase((int) c);
  }

  public static int toUpperCase(int codePoint) {
    if (codePoint >= 'a' && codePoint <= 'z') return codePoint - ('a' - 'A');
    if (codePoint < 128) return codePoint;
    return CharacterMappings.upper(codePoint);
  }

  public static char toTitleCase(char c) {
    return (char) toTitleCase((int) c);
  }

  public static int toTitleCase(int codePoint) {
    if (codePoint < 128) return toUpperCase(codePoint);
    return CharacterMappings.title(codePoint);
  }

  public static boolean isDigit(char c) {
    return isDigit((int) c);
  }

  public static boolean isDigit(int c) {
    return getType(c) == DECIMAL_DIGIT_NUMBER;
  }

  public static int digit(char c, int radix) {
    return digit((int) c, radix);
  }

  public static int digit(int codePoint, int radix) {
    if (radix < MIN_RADIX || radix > MAX_RADIX) return -1;
    int value = latinDigit(codePoint);
    if (value < 0) value = CharacterMappings.decimal(codePoint);
    return value >= 0 && value < radix ? value : -1;
  }

  public static int getNumericValue(char c) {
    return getNumericValue((int) c);
  }

  public static int getNumericValue(int codePoint) {
    int value = latinDigit(codePoint);
    return value >= 0 ? value : CharacterMappings.numeric(codePoint);
  }

  private static int latinDigit(int codePoint) {
    if (codePoint >= '0' && codePoint <= '9') return codePoint - '0';
    if (codePoint >= 'a' && codePoint <= 'z') return codePoint - 'a' + 10;
    if (codePoint >= 'A' && codePoint <= 'Z') return codePoint - 'A' + 10;
    if (codePoint >= 0xff21 && codePoint <= 0xff3a) return codePoint - 0xff21 + 10;
    if (codePoint >= 0xff41 && codePoint <= 0xff5a) return codePoint - 0xff41 + 10;
    return -1;
  }

  public static char forDigit(int digit, int radix) {
    if (MIN_RADIX <= radix && radix <= MAX_RADIX) {
      if (0 <= digit && digit < radix) {
        return (char) (digit < 10 ? digit + '0' : digit + 'a' - 10);
      }
    }
    return 0;
  }

  public static byte getDirectionality(char c) {
    return getDirectionality((int) c);
  }

  public static byte getDirectionality(int codePoint) {
    return CharacterBidiData.directionality(codePoint);
  }

  public static boolean isMirrored(char c) {
    return isMirrored((int) c);
  }

  public static boolean isMirrored(int codePoint) {
    return CharacterBidiData.mirrored(codePoint);
  }

  public static boolean isLetter(int c) {
    int type = getType(c);
    return type >= UPPERCASE_LETTER && type <= OTHER_LETTER;
  }

  public static boolean isLetter(char c) {
    return isLetter((int) c);
  }

  public static boolean isLetterOrDigit(char c) {
    return isLetterOrDigit((int) c);
  }

  public static boolean isLetterOrDigit(int c) {
    int type = getType(c);
    return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER) || type == DECIMAL_DIGIT_NUMBER;
  }

  public static boolean isLowerCase(int c) {
    return getType(c) == LOWERCASE_LETTER
        || CharacterData.hasProperty(c, CharacterData.OTHER_LOWERCASE);
  }

  public static boolean isLowerCase(char c) {
    return isLowerCase((int) c);
  }

  public static boolean isUpperCase(char c) {
    return isUpperCase((int) c);
  }

  public static boolean isUpperCase(int c) {
    return getType(c) == UPPERCASE_LETTER
        || CharacterData.hasProperty(c, CharacterData.OTHER_UPPERCASE);
  }

  public static boolean isTitleCase(char c) {
    return isTitleCase((int) c);
  }

  public static boolean isTitleCase(int cp) {
    return getType(cp) == TITLECASE_LETTER;
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
    return isSpaceChar((int) c);
  }

  public static boolean isSpaceChar(int codePoint) {
    int type = getType(codePoint);
    return type == SPACE_SEPARATOR || type == LINE_SEPARATOR || type == PARAGRAPH_SEPARATOR;
  }

  public static boolean isDefined(char c) {
    return isDefined((int) c);
  }

  public static boolean isDefined(int codePoint) {
    return getType(codePoint) != UNASSIGNED;
  }

  public static boolean isAlphabetic(int codePoint) {
    int type = getType(codePoint);
    return (type >= UPPERCASE_LETTER && type <= OTHER_LETTER) || type == LETTER_NUMBER
        || CharacterData.hasProperty(codePoint, CharacterData.OTHER_ALPHABETIC);
  }

  public static boolean isIdeographic(int codePoint) {
    return CharacterData.hasProperty(codePoint, CharacterData.IDEOGRAPHIC);
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
    return CharacterData.getType(cp);
  }

  public static boolean isIdentifierIgnorable(char ch) {
    return isIdentifierIgnorable((int) ch);
  }

  public static boolean isIdentifierIgnorable(int codePoint) {
    return (codePoint >= 0x0000 && codePoint <= 0x0008)
        || (codePoint >= 0x000E && codePoint <= 0x001B)
        || (codePoint >= 0x007F && codePoint <= 0x009F)
        || getType(codePoint) == FORMAT;
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
        || type == LETTER_NUMBER
        || CharacterData.hasProperty(codePoint, CharacterData.OTHER_ID_START);
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
        || isIdentifierIgnorable(codePoint)
        || CharacterData.hasProperty(codePoint, CharacterData.OTHER_ID_CONTINUE);
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
    return CharacterEmojiData.hasProperty(codePoint, CharacterEmojiData.EMOJI_MODIFIER);
  }

  public static boolean isEmojiComponent(int codePoint) {
    return CharacterEmojiData.hasProperty(codePoint, CharacterEmojiData.EMOJI_COMPONENT);
  }

  public static boolean isEmojiModifierBase(int codePoint) {
    return CharacterEmojiData.hasProperty(codePoint, CharacterEmojiData.EMOJI_MODIFIER_BASE);
  }

  public static boolean isExtendedPictographic(int codePoint) {
    return CharacterEmojiData.hasProperty(codePoint, CharacterEmojiData.EXTENDED_PICTOGRAPHIC);
  }

  public static boolean isEmojiPresentation(int codePoint) {
    return CharacterEmojiData.hasProperty(codePoint, CharacterEmojiData.EMOJI_PRESENTATION);
  }

  public static boolean isEmoji(int codePoint) {
    return CharacterEmojiData.hasProperty(codePoint, CharacterEmojiData.EMOJI);
  }

  public static int toChars(int codePoint, char[] dst, int dstIndex) {
    if (isBmpCodePoint(codePoint)) {
      dst[dstIndex] = (char) codePoint;
      return 1;
    }
    if (!isValidCodePoint(codePoint)) {
      throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
    }
    dst[dstIndex + 1] = lowSurrogate(codePoint);
    dst[dstIndex] = highSurrogate(codePoint);
    return 2;
  }

  public static int codePointAt(char[] a, int index) {
    return codePointAt(a, index, a.length);
  }

  public static int codePointAt(char[] a, int index, int limit) {
    if (index >= limit || index < 0 || limit > a.length) {
      throw new IndexOutOfBoundsException();
    }
    char c1 = a[index];
    if (isHighSurrogate(c1) && ++index < limit) {
      char c2 = a[index];
      if (isLowSurrogate(c2)) {
        return toCodePoint(c1, c2);
      }
    }
    return c1;
  }

  public static int codePointBefore(char[] a, int index) {
    return codePointBefore(a, index, 0);
  }

  public static int codePointBefore(char[] a, int index, int start) {
    if (index <= start || start < 0 || index > a.length) {
      throw new IndexOutOfBoundsException();
    }
    char c2 = a[--index];
    if (isLowSurrogate(c2) && index > start) {
      char c1 = a[--index];
      if (isHighSurrogate(c1)) {
        return toCodePoint(c1, c2);
      }
    }
    return c2;
  }

  public static int codePointCount(char[] a, int offset, int count) {
    if (count > a.length - offset || offset < 0 || count < 0) {
      throw new IndexOutOfBoundsException();
    }
    int endIndex = offset + count;
    int n = count;
    for (int i = offset; i < endIndex; ) {
      if (isHighSurrogate(a[i++]) && i < endIndex && isLowSurrogate(a[i])) {
        n--;
        i++;
      }
    }
    return n;
  }

  public static int offsetByCodePoints(CharSequence seq, int index, int codePointOffset) {
    int length = seq.length();
    if (index < 0 || index > length) {
      throw new IndexOutOfBoundsException();
    }
    int x = index;
    if (codePointOffset >= 0) {
      int i;
      for (i = 0; x < length && i < codePointOffset; i++) {
        if (isHighSurrogate(seq.charAt(x++)) && x < length && isLowSurrogate(seq.charAt(x))) {
          x++;
        }
      }
      if (i < codePointOffset) {
        throw new IndexOutOfBoundsException();
      }
    } else {
      int i;
      for (i = codePointOffset; x > 0 && i < 0; i++) {
        if (isLowSurrogate(seq.charAt(--x)) && x > 0 && isHighSurrogate(seq.charAt(x - 1))) {
          x--;
        }
      }
      if (i < 0) {
        throw new IndexOutOfBoundsException();
      }
    }
    return x;
  }

  public static int offsetByCodePoints(char[] a, int start, int count, int index, int codePointOffset) {
    if (count > a.length - start || start < 0 || count < 0 || index < start || index > start + count) {
      throw new IndexOutOfBoundsException();
    }
    int x = index;
    if (codePointOffset >= 0) {
      int limit = start + count;
      int i;
      for (i = 0; x < limit && i < codePointOffset; i++) {
        if (isHighSurrogate(a[x++]) && x < limit && isLowSurrogate(a[x])) {
          x++;
        }
      }
      if (i < codePointOffset) {
        throw new IndexOutOfBoundsException();
      }
    } else {
      int i;
      for (i = codePointOffset; x > start && i < 0; i++) {
        if (isLowSurrogate(a[--x]) && x > start && isHighSurrogate(a[x - 1])) {
          x--;
        }
      }
      if (i < 0) {
        throw new IndexOutOfBoundsException();
      }
    }
    return x;
  }

  @Deprecated
  public static boolean isJavaLetter(char ch) {
    return isJavaIdentifierStart(ch);
  }

  @Deprecated
  public static boolean isJavaLetterOrDigit(char ch) {
    return isJavaIdentifierPart(ch);
  }

  @Deprecated
  public static boolean isSpace(char ch) {
    return ch <= 0x0020 && (((1L << 0x09) | (1L << 0x0A) | (1L << 0x0C) | (1L << 0x0D) | (1L << 0x20)) >> ch & 1L) != 0;
  }
}
