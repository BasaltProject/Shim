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

public final class Character implements java.io.Serializable, Comparable<Character>, java.lang.constant.Constable {
  private static final long serialVersionUID = 3786198910865385080L;

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
    if (!isValidCodePoint(codePoint)) {
      throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
    }
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
    return ((high << 10) + low) + (0x10000 - (0xD800 << 10) - 0xDC00);
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
      throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
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
    char high = sequence.charAt(offset);
    int length = sequence.length();
    if (! isHighSurrogate(high) || offset + 1 >= length) {
      return high;
    }
    char low = sequence.charAt(offset + 1);
    if (! isLowSurrogate(low)) {
      return high;
    }

    return toCodePoint(high, low);
  }

  public static int codePointBefore(CharSequence sequence, int index) {
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
    java.util.Objects.checkFromToIndex(start, end, sequence.length());

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
    int limit = a.length;
    char c1 = a[index];
    if (isHighSurrogate(c1) && ++index < limit) {
      char c2 = a[index];
      if (isLowSurrogate(c2)) {
        return toCodePoint(c1, c2);
      }
    }
    return c1;
  }

  public static int codePointAt(char[] a, int index, int limit) {
    if (index >= limit || index < 0 || limit < 0 || limit > a.length) {
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
    char c2 = a[--index];
    if (isLowSurrogate(c2) && index > 0) {
      char c1 = a[--index];
      if (isHighSurrogate(c1)) {
        return toCodePoint(c1, c2);
      }
    }
    return c2;
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
    java.util.Objects.checkFromIndexSize(offset, count, a.length);
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

  public static class Subset {
    private String name;

    protected Subset(String name) {
      if (name == null) {
        throw new NullPointerException("name");
      }
      this.name = name;
    }

    public final boolean equals(Object obj) {
      return (this == obj);
    }

    public final int hashCode() {
      return super.hashCode();
    }

    public final String toString() {
      return name;
    }
  }

  public static final class UnicodeBlock extends Subset {
    private static final java.util.Map<String, UnicodeBlock> map = new java.util.HashMap<String, UnicodeBlock>(700);

    private UnicodeBlock(String idName, String... aliases) {
      super(idName);
      map.put(idName, this);
      for (String alias : aliases) {
        map.put(alias, this);
      }
    }

    public static final UnicodeBlock BASIC_LATIN = new UnicodeBlock("BASIC_LATIN", "BASIC LATIN", "BASICLATIN");
    public static final UnicodeBlock LATIN_1_SUPPLEMENT = new UnicodeBlock("LATIN_1_SUPPLEMENT",
        "LATIN-1 SUPPLEMENT", "LATIN-1SUPPLEMENT");
    public static final UnicodeBlock LATIN_EXTENDED_A = new UnicodeBlock("LATIN_EXTENDED_A", "LATIN EXTENDED-A",
        "LATINEXTENDED-A");
    public static final UnicodeBlock LATIN_EXTENDED_B = new UnicodeBlock("LATIN_EXTENDED_B", "LATIN EXTENDED-B",
        "LATINEXTENDED-B");
    public static final UnicodeBlock IPA_EXTENSIONS = new UnicodeBlock("IPA_EXTENSIONS", "IPA EXTENSIONS",
        "IPAEXTENSIONS");
    public static final UnicodeBlock SPACING_MODIFIER_LETTERS = new UnicodeBlock("SPACING_MODIFIER_LETTERS",
        "SPACING MODIFIER LETTERS", "SPACINGMODIFIERLETTERS");
    public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS",
        "COMBINING DIACRITICAL MARKS", "COMBININGDIACRITICALMARKS");
    public static final UnicodeBlock GREEK = new UnicodeBlock("GREEK", "GREEK AND COPTIC", "GREEKANDCOPTIC");
    public static final UnicodeBlock CYRILLIC = new UnicodeBlock("CYRILLIC");
    public static final UnicodeBlock ARMENIAN = new UnicodeBlock("ARMENIAN");
    public static final UnicodeBlock HEBREW = new UnicodeBlock("HEBREW");
    public static final UnicodeBlock ARABIC = new UnicodeBlock("ARABIC");
    public static final UnicodeBlock DEVANAGARI = new UnicodeBlock("DEVANAGARI");
    public static final UnicodeBlock BENGALI = new UnicodeBlock("BENGALI");
    public static final UnicodeBlock GURMUKHI = new UnicodeBlock("GURMUKHI");
    public static final UnicodeBlock GUJARATI = new UnicodeBlock("GUJARATI");
    public static final UnicodeBlock ORIYA = new UnicodeBlock("ORIYA");
    public static final UnicodeBlock TAMIL = new UnicodeBlock("TAMIL");
    public static final UnicodeBlock TELUGU = new UnicodeBlock("TELUGU");
    public static final UnicodeBlock KANNADA = new UnicodeBlock("KANNADA");
    public static final UnicodeBlock MALAYALAM = new UnicodeBlock("MALAYALAM");
    public static final UnicodeBlock THAI = new UnicodeBlock("THAI");
    public static final UnicodeBlock LAO = new UnicodeBlock("LAO");
    public static final UnicodeBlock TIBETAN = new UnicodeBlock("TIBETAN");
    public static final UnicodeBlock GEORGIAN = new UnicodeBlock("GEORGIAN");
    public static final UnicodeBlock HANGUL_JAMO = new UnicodeBlock("HANGUL_JAMO", "HANGUL JAMO", "HANGULJAMO");
    public static final UnicodeBlock LATIN_EXTENDED_ADDITIONAL = new UnicodeBlock("LATIN_EXTENDED_ADDITIONAL",
        "LATIN EXTENDED ADDITIONAL", "LATINEXTENDEDADDITIONAL");
    public static final UnicodeBlock GREEK_EXTENDED = new UnicodeBlock("GREEK_EXTENDED", "GREEK EXTENDED",
        "GREEKEXTENDED");
    public static final UnicodeBlock GENERAL_PUNCTUATION = new UnicodeBlock("GENERAL_PUNCTUATION",
        "GENERAL PUNCTUATION", "GENERALPUNCTUATION");
    public static final UnicodeBlock SUPERSCRIPTS_AND_SUBSCRIPTS = new UnicodeBlock("SUPERSCRIPTS_AND_SUBSCRIPTS",
        "SUPERSCRIPTS AND SUBSCRIPTS", "SUPERSCRIPTSANDSUBSCRIPTS");
    public static final UnicodeBlock CURRENCY_SYMBOLS = new UnicodeBlock("CURRENCY_SYMBOLS", "CURRENCY SYMBOLS",
        "CURRENCYSYMBOLS");
    public static final UnicodeBlock COMBINING_MARKS_FOR_SYMBOLS = new UnicodeBlock("COMBINING_MARKS_FOR_SYMBOLS",
        "COMBINING DIACRITICAL MARKS FOR SYMBOLS", "COMBINING MARKS FOR SYMBOLS", "COMBININGDIACRITICALMARKSFORSYMBOLS", "COMBININGMARKSFORSYMBOLS");
    public static final UnicodeBlock LETTERLIKE_SYMBOLS = new UnicodeBlock("LETTERLIKE_SYMBOLS",
        "LETTERLIKE SYMBOLS", "LETTERLIKESYMBOLS");
    public static final UnicodeBlock NUMBER_FORMS = new UnicodeBlock("NUMBER_FORMS", "NUMBER FORMS", "NUMBERFORMS");
    public static final UnicodeBlock ARROWS = new UnicodeBlock("ARROWS");
    public static final UnicodeBlock MATHEMATICAL_OPERATORS = new UnicodeBlock("MATHEMATICAL_OPERATORS",
        "MATHEMATICAL OPERATORS", "MATHEMATICALOPERATORS");
    public static final UnicodeBlock MISCELLANEOUS_TECHNICAL = new UnicodeBlock("MISCELLANEOUS_TECHNICAL",
        "MISCELLANEOUS TECHNICAL", "MISCELLANEOUSTECHNICAL");
    public static final UnicodeBlock CONTROL_PICTURES = new UnicodeBlock("CONTROL_PICTURES", "CONTROL PICTURES",
        "CONTROLPICTURES");
    public static final UnicodeBlock OPTICAL_CHARACTER_RECOGNITION = new UnicodeBlock("OPTICAL_CHARACTER_RECOGNITION", "OPTICAL CHARACTER RECOGNITION", "OPTICALCHARACTERRECOGNITION");
    public static final UnicodeBlock ENCLOSED_ALPHANUMERICS = new UnicodeBlock("ENCLOSED_ALPHANUMERICS",
        "ENCLOSED ALPHANUMERICS", "ENCLOSEDALPHANUMERICS");
    public static final UnicodeBlock BOX_DRAWING = new UnicodeBlock("BOX_DRAWING", "BOX DRAWING", "BOXDRAWING");
    public static final UnicodeBlock BLOCK_ELEMENTS = new UnicodeBlock("BLOCK_ELEMENTS", "BLOCK ELEMENTS",
        "BLOCKELEMENTS");
    public static final UnicodeBlock GEOMETRIC_SHAPES = new UnicodeBlock("GEOMETRIC_SHAPES", "GEOMETRIC SHAPES",
        "GEOMETRICSHAPES");
    public static final UnicodeBlock MISCELLANEOUS_SYMBOLS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS",
        "MISCELLANEOUS SYMBOLS", "MISCELLANEOUSSYMBOLS");
    public static final UnicodeBlock DINGBATS = new UnicodeBlock("DINGBATS");
    public static final UnicodeBlock CJK_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("CJK_SYMBOLS_AND_PUNCTUATION",
        "CJK SYMBOLS AND PUNCTUATION", "CJKSYMBOLSANDPUNCTUATION");
    public static final UnicodeBlock HIRAGANA = new UnicodeBlock("HIRAGANA");
    public static final UnicodeBlock KATAKANA = new UnicodeBlock("KATAKANA");
    public static final UnicodeBlock BOPOMOFO = new UnicodeBlock("BOPOMOFO");
    public static final UnicodeBlock HANGUL_COMPATIBILITY_JAMO = new UnicodeBlock("HANGUL_COMPATIBILITY_JAMO",
        "HANGUL COMPATIBILITY JAMO", "HANGULCOMPATIBILITYJAMO");
    public static final UnicodeBlock KANBUN = new UnicodeBlock("KANBUN");
    public static final UnicodeBlock ENCLOSED_CJK_LETTERS_AND_MONTHS = new UnicodeBlock("ENCLOSED_CJK_LETTERS_AND_MONTHS", "ENCLOSED CJK LETTERS AND MONTHS", "ENCLOSEDCJKLETTERSANDMONTHS");
    public static final UnicodeBlock CJK_COMPATIBILITY = new UnicodeBlock("CJK_COMPATIBILITY", "CJK COMPATIBILITY",
        "CJKCOMPATIBILITY");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS",
        "CJK UNIFIED IDEOGRAPHS", "CJKUNIFIEDIDEOGRAPHS");
    public static final UnicodeBlock HANGUL_SYLLABLES = new UnicodeBlock("HANGUL_SYLLABLES", "HANGUL SYLLABLES",
        "HANGULSYLLABLES");
    public static final UnicodeBlock PRIVATE_USE_AREA = new UnicodeBlock("PRIVATE_USE_AREA", "PRIVATE USE AREA",
        "PRIVATEUSEAREA");
    public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS",
        "CJK COMPATIBILITY IDEOGRAPHS", "CJKCOMPATIBILITYIDEOGRAPHS");
    public static final UnicodeBlock ALPHABETIC_PRESENTATION_FORMS = new UnicodeBlock("ALPHABETIC_PRESENTATION_FORMS", "ALPHABETIC PRESENTATION FORMS", "ALPHABETICPRESENTATIONFORMS");
    public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_A = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_A",
        "ARABIC PRESENTATION FORMS-A", "ARABICPRESENTATIONFORMS-A");
    public static final UnicodeBlock COMBINING_HALF_MARKS = new UnicodeBlock("COMBINING_HALF_MARKS",
        "COMBINING HALF MARKS", "COMBININGHALFMARKS");
    public static final UnicodeBlock CJK_COMPATIBILITY_FORMS = new UnicodeBlock("CJK_COMPATIBILITY_FORMS",
        "CJK COMPATIBILITY FORMS", "CJKCOMPATIBILITYFORMS");
    public static final UnicodeBlock SMALL_FORM_VARIANTS = new UnicodeBlock("SMALL_FORM_VARIANTS",
        "SMALL FORM VARIANTS", "SMALLFORMVARIANTS");
    public static final UnicodeBlock ARABIC_PRESENTATION_FORMS_B = new UnicodeBlock("ARABIC_PRESENTATION_FORMS_B",
        "ARABIC PRESENTATION FORMS-B", "ARABICPRESENTATIONFORMS-B");
    public static final UnicodeBlock HALFWIDTH_AND_FULLWIDTH_FORMS = new UnicodeBlock("HALFWIDTH_AND_FULLWIDTH_FORMS", "HALFWIDTH AND FULLWIDTH FORMS", "HALFWIDTHANDFULLWIDTHFORMS");
    public static final UnicodeBlock SPECIALS = new UnicodeBlock("SPECIALS");
    public static final UnicodeBlock SURROGATES_AREA = new UnicodeBlock("SURROGATES_AREA");
    public static final UnicodeBlock SYRIAC = new UnicodeBlock("SYRIAC");
    public static final UnicodeBlock THAANA = new UnicodeBlock("THAANA");
    public static final UnicodeBlock SINHALA = new UnicodeBlock("SINHALA");
    public static final UnicodeBlock MYANMAR = new UnicodeBlock("MYANMAR");
    public static final UnicodeBlock ETHIOPIC = new UnicodeBlock("ETHIOPIC");
    public static final UnicodeBlock CHEROKEE = new UnicodeBlock("CHEROKEE");
    public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS", "UNIFIED CANADIAN ABORIGINAL SYLLABICS", "UNIFIEDCANADIANABORIGINALSYLLABICS");
    public static final UnicodeBlock OGHAM = new UnicodeBlock("OGHAM");
    public static final UnicodeBlock RUNIC = new UnicodeBlock("RUNIC");
    public static final UnicodeBlock KHMER = new UnicodeBlock("KHMER");
    public static final UnicodeBlock MONGOLIAN = new UnicodeBlock("MONGOLIAN");
    public static final UnicodeBlock BRAILLE_PATTERNS = new UnicodeBlock("BRAILLE_PATTERNS", "BRAILLE PATTERNS",
        "BRAILLEPATTERNS");
    public static final UnicodeBlock CJK_RADICALS_SUPPLEMENT = new UnicodeBlock("CJK_RADICALS_SUPPLEMENT",
        "CJK RADICALS SUPPLEMENT", "CJKRADICALSSUPPLEMENT");
    public static final UnicodeBlock KANGXI_RADICALS = new UnicodeBlock("KANGXI_RADICALS", "KANGXI RADICALS",
        "KANGXIRADICALS");
    public static final UnicodeBlock IDEOGRAPHIC_DESCRIPTION_CHARACTERS = new UnicodeBlock("IDEOGRAPHIC_DESCRIPTION_CHARACTERS", "IDEOGRAPHIC DESCRIPTION CHARACTERS", "IDEOGRAPHICDESCRIPTIONCHARACTERS");
    public static final UnicodeBlock BOPOMOFO_EXTENDED = new UnicodeBlock("BOPOMOFO_EXTENDED", "BOPOMOFO EXTENDED",
        "BOPOMOFOEXTENDED");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A", "CJK UNIFIED IDEOGRAPHS EXTENSION A", "CJKUNIFIEDIDEOGRAPHSEXTENSIONA");
    public static final UnicodeBlock YI_SYLLABLES = new UnicodeBlock("YI_SYLLABLES", "YI SYLLABLES", "YISYLLABLES");
    public static final UnicodeBlock YI_RADICALS = new UnicodeBlock("YI_RADICALS", "YI RADICALS", "YIRADICALS");
    public static final UnicodeBlock CYRILLIC_SUPPLEMENTARY = new UnicodeBlock("CYRILLIC_SUPPLEMENTARY",
        "CYRILLIC SUPPLEMENT", "CYRILLIC SUPPLEMENTARY", "CYRILLICSUPPLEMENT", "CYRILLICSUPPLEMENTARY");
    public static final UnicodeBlock TAGALOG = new UnicodeBlock("TAGALOG");
    public static final UnicodeBlock HANUNOO = new UnicodeBlock("HANUNOO");
    public static final UnicodeBlock BUHID = new UnicodeBlock("BUHID");
    public static final UnicodeBlock TAGBANWA = new UnicodeBlock("TAGBANWA");
    public static final UnicodeBlock LIMBU = new UnicodeBlock("LIMBU");
    public static final UnicodeBlock TAI_LE = new UnicodeBlock("TAI_LE", "TAI LE", "TAILE");
    public static final UnicodeBlock KHMER_SYMBOLS = new UnicodeBlock("KHMER_SYMBOLS", "KHMER SYMBOLS", "KHMERSYMBOLS");
    public static final UnicodeBlock PHONETIC_EXTENSIONS = new UnicodeBlock("PHONETIC_EXTENSIONS",
        "PHONETIC EXTENSIONS", "PHONETICEXTENSIONS");
    public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A", "MISCELLANEOUS MATHEMATICAL SYMBOLS-A", "MISCELLANEOUSMATHEMATICALSYMBOLS-A");
    public static final UnicodeBlock SUPPLEMENTAL_ARROWS_A = new UnicodeBlock("SUPPLEMENTAL_ARROWS_A",
        "SUPPLEMENTAL ARROWS-A", "SUPPLEMENTALARROWS-A");
    public static final UnicodeBlock SUPPLEMENTAL_ARROWS_B = new UnicodeBlock("SUPPLEMENTAL_ARROWS_B",
        "SUPPLEMENTAL ARROWS-B", "SUPPLEMENTALARROWS-B");
    public static final UnicodeBlock MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B = new UnicodeBlock("MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B", "MISCELLANEOUS MATHEMATICAL SYMBOLS-B", "MISCELLANEOUSMATHEMATICALSYMBOLS-B");
    public static final UnicodeBlock SUPPLEMENTAL_MATHEMATICAL_OPERATORS = new UnicodeBlock("SUPPLEMENTAL_MATHEMATICAL_OPERATORS", "SUPPLEMENTAL MATHEMATICAL OPERATORS", "SUPPLEMENTALMATHEMATICALOPERATORS");
    public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_ARROWS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_ARROWS", "MISCELLANEOUS SYMBOLS AND ARROWS", "MISCELLANEOUSSYMBOLSANDARROWS");
    public static final UnicodeBlock KATAKANA_PHONETIC_EXTENSIONS = new UnicodeBlock("KATAKANA_PHONETIC_EXTENSIONS",
        "KATAKANA PHONETIC EXTENSIONS", "KATAKANAPHONETICEXTENSIONS");
    public static final UnicodeBlock YIJING_HEXAGRAM_SYMBOLS = new UnicodeBlock("YIJING_HEXAGRAM_SYMBOLS",
        "YIJING HEXAGRAM SYMBOLS", "YIJINGHEXAGRAMSYMBOLS");
    public static final UnicodeBlock VARIATION_SELECTORS = new UnicodeBlock("VARIATION_SELECTORS",
        "VARIATION SELECTORS", "VARIATIONSELECTORS");
    public static final UnicodeBlock LINEAR_B_SYLLABARY = new UnicodeBlock("LINEAR_B_SYLLABARY",
        "LINEAR B SYLLABARY", "LINEARBSYLLABARY");
    public static final UnicodeBlock LINEAR_B_IDEOGRAMS = new UnicodeBlock("LINEAR_B_IDEOGRAMS",
        "LINEAR B IDEOGRAMS", "LINEARBIDEOGRAMS");
    public static final UnicodeBlock AEGEAN_NUMBERS = new UnicodeBlock("AEGEAN_NUMBERS", "AEGEAN NUMBERS",
        "AEGEANNUMBERS");
    public static final UnicodeBlock OLD_ITALIC = new UnicodeBlock("OLD_ITALIC", "OLD ITALIC", "OLDITALIC");
    public static final UnicodeBlock GOTHIC = new UnicodeBlock("GOTHIC");
    public static final UnicodeBlock UGARITIC = new UnicodeBlock("UGARITIC");
    public static final UnicodeBlock DESERET = new UnicodeBlock("DESERET");
    public static final UnicodeBlock SHAVIAN = new UnicodeBlock("SHAVIAN");
    public static final UnicodeBlock OSMANYA = new UnicodeBlock("OSMANYA");
    public static final UnicodeBlock CYPRIOT_SYLLABARY = new UnicodeBlock("CYPRIOT_SYLLABARY", "CYPRIOT SYLLABARY",
        "CYPRIOTSYLLABARY");
    public static final UnicodeBlock BYZANTINE_MUSICAL_SYMBOLS = new UnicodeBlock("BYZANTINE_MUSICAL_SYMBOLS",
        "BYZANTINE MUSICAL SYMBOLS", "BYZANTINEMUSICALSYMBOLS");
    public static final UnicodeBlock MUSICAL_SYMBOLS = new UnicodeBlock("MUSICAL_SYMBOLS", "MUSICAL SYMBOLS",
        "MUSICALSYMBOLS");
    public static final UnicodeBlock TAI_XUAN_JING_SYMBOLS = new UnicodeBlock("TAI_XUAN_JING_SYMBOLS",
        "TAI XUAN JING SYMBOLS", "TAIXUANJINGSYMBOLS");
    public static final UnicodeBlock MATHEMATICAL_ALPHANUMERIC_SYMBOLS = new UnicodeBlock("MATHEMATICAL_ALPHANUMERIC_SYMBOLS", "MATHEMATICAL ALPHANUMERIC SYMBOLS", "MATHEMATICALALPHANUMERICSYMBOLS");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B", "CJK UNIFIED IDEOGRAPHS EXTENSION B", "CJKUNIFIEDIDEOGRAPHSEXTENSIONB");
    public static final UnicodeBlock CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT = new UnicodeBlock("CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT", "CJK COMPATIBILITY IDEOGRAPHS SUPPLEMENT", "CJKCOMPATIBILITYIDEOGRAPHSSUPPLEMENT");
    public static final UnicodeBlock TAGS = new UnicodeBlock("TAGS");
    public static final UnicodeBlock VARIATION_SELECTORS_SUPPLEMENT = new UnicodeBlock("VARIATION_SELECTORS_SUPPLEMENT", "VARIATION SELECTORS SUPPLEMENT", "VARIATIONSELECTORSSUPPLEMENT");
    public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_A = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_A", "SUPPLEMENTARY PRIVATE USE AREA-A", "SUPPLEMENTARYPRIVATEUSEAREA-A");
    public static final UnicodeBlock SUPPLEMENTARY_PRIVATE_USE_AREA_B = new UnicodeBlock("SUPPLEMENTARY_PRIVATE_USE_AREA_B", "SUPPLEMENTARY PRIVATE USE AREA-B", "SUPPLEMENTARYPRIVATEUSEAREA-B");
    public static final UnicodeBlock HIGH_SURROGATES = new UnicodeBlock("HIGH_SURROGATES", "HIGH SURROGATES",
        "HIGHSURROGATES");
    public static final UnicodeBlock HIGH_PRIVATE_USE_SURROGATES = new UnicodeBlock("HIGH_PRIVATE_USE_SURROGATES",
        "HIGH PRIVATE USE SURROGATES", "HIGHPRIVATEUSESURROGATES");
    public static final UnicodeBlock LOW_SURROGATES = new UnicodeBlock("LOW_SURROGATES", "LOW SURROGATES",
        "LOWSURROGATES");
    public static final UnicodeBlock ARABIC_SUPPLEMENT = new UnicodeBlock("ARABIC_SUPPLEMENT", "ARABIC SUPPLEMENT",
        "ARABICSUPPLEMENT");
    public static final UnicodeBlock NKO = new UnicodeBlock("NKO");
    public static final UnicodeBlock SAMARITAN = new UnicodeBlock("SAMARITAN");
    public static final UnicodeBlock MANDAIC = new UnicodeBlock("MANDAIC");
    public static final UnicodeBlock ETHIOPIC_SUPPLEMENT = new UnicodeBlock("ETHIOPIC_SUPPLEMENT",
        "ETHIOPIC SUPPLEMENT", "ETHIOPICSUPPLEMENT");
    public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED", "UNIFIED CANADIAN ABORIGINAL SYLLABICS EXTENDED", "UNIFIEDCANADIANABORIGINALSYLLABICSEXTENDED");
    public static final UnicodeBlock NEW_TAI_LUE = new UnicodeBlock("NEW_TAI_LUE", "NEW TAI LUE", "NEWTAILUE");
    public static final UnicodeBlock BUGINESE = new UnicodeBlock("BUGINESE");
    public static final UnicodeBlock TAI_THAM = new UnicodeBlock("TAI_THAM", "TAI THAM", "TAITHAM");
    public static final UnicodeBlock BALINESE = new UnicodeBlock("BALINESE");
    public static final UnicodeBlock SUNDANESE = new UnicodeBlock("SUNDANESE");
    public static final UnicodeBlock BATAK = new UnicodeBlock("BATAK");
    public static final UnicodeBlock LEPCHA = new UnicodeBlock("LEPCHA");
    public static final UnicodeBlock OL_CHIKI = new UnicodeBlock("OL_CHIKI", "OL CHIKI", "OLCHIKI");
    public static final UnicodeBlock VEDIC_EXTENSIONS = new UnicodeBlock("VEDIC_EXTENSIONS", "VEDIC EXTENSIONS",
        "VEDICEXTENSIONS");
    public static final UnicodeBlock PHONETIC_EXTENSIONS_SUPPLEMENT = new UnicodeBlock("PHONETIC_EXTENSIONS_SUPPLEMENT", "PHONETIC EXTENSIONS SUPPLEMENT", "PHONETICEXTENSIONSSUPPLEMENT");
    public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_SUPPLEMENT = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_SUPPLEMENT", "COMBINING DIACRITICAL MARKS SUPPLEMENT", "COMBININGDIACRITICALMARKSSUPPLEMENT");
    public static final UnicodeBlock GLAGOLITIC = new UnicodeBlock("GLAGOLITIC");
    public static final UnicodeBlock LATIN_EXTENDED_C = new UnicodeBlock("LATIN_EXTENDED_C", "LATIN EXTENDED-C",
        "LATINEXTENDED-C");
    public static final UnicodeBlock COPTIC = new UnicodeBlock("COPTIC");
    public static final UnicodeBlock GEORGIAN_SUPPLEMENT = new UnicodeBlock("GEORGIAN_SUPPLEMENT",
        "GEORGIAN SUPPLEMENT", "GEORGIANSUPPLEMENT");
    public static final UnicodeBlock TIFINAGH = new UnicodeBlock("TIFINAGH");
    public static final UnicodeBlock ETHIOPIC_EXTENDED = new UnicodeBlock("ETHIOPIC_EXTENDED", "ETHIOPIC EXTENDED",
        "ETHIOPICEXTENDED");
    public static final UnicodeBlock CYRILLIC_EXTENDED_A = new UnicodeBlock("CYRILLIC_EXTENDED_A",
        "CYRILLIC EXTENDED-A", "CYRILLICEXTENDED-A");
    public static final UnicodeBlock SUPPLEMENTAL_PUNCTUATION = new UnicodeBlock("SUPPLEMENTAL_PUNCTUATION",
        "SUPPLEMENTAL PUNCTUATION", "SUPPLEMENTALPUNCTUATION");
    public static final UnicodeBlock CJK_STROKES = new UnicodeBlock("CJK_STROKES", "CJK STROKES", "CJKSTROKES");
    public static final UnicodeBlock LISU = new UnicodeBlock("LISU");
    public static final UnicodeBlock VAI = new UnicodeBlock("VAI");
    public static final UnicodeBlock CYRILLIC_EXTENDED_B = new UnicodeBlock("CYRILLIC_EXTENDED_B",
        "CYRILLIC EXTENDED-B", "CYRILLICEXTENDED-B");
    public static final UnicodeBlock BAMUM = new UnicodeBlock("BAMUM");
    public static final UnicodeBlock MODIFIER_TONE_LETTERS = new UnicodeBlock("MODIFIER_TONE_LETTERS",
        "MODIFIER TONE LETTERS", "MODIFIERTONELETTERS");
    public static final UnicodeBlock LATIN_EXTENDED_D = new UnicodeBlock("LATIN_EXTENDED_D", "LATIN EXTENDED-D",
        "LATINEXTENDED-D");
    public static final UnicodeBlock SYLOTI_NAGRI = new UnicodeBlock("SYLOTI_NAGRI", "SYLOTI NAGRI", "SYLOTINAGRI");
    public static final UnicodeBlock COMMON_INDIC_NUMBER_FORMS = new UnicodeBlock("COMMON_INDIC_NUMBER_FORMS",
        "COMMON INDIC NUMBER FORMS", "COMMONINDICNUMBERFORMS");
    public static final UnicodeBlock PHAGS_PA = new UnicodeBlock("PHAGS_PA", "PHAGS-PA");
    public static final UnicodeBlock SAURASHTRA = new UnicodeBlock("SAURASHTRA");
    public static final UnicodeBlock DEVANAGARI_EXTENDED = new UnicodeBlock("DEVANAGARI_EXTENDED",
        "DEVANAGARI EXTENDED", "DEVANAGARIEXTENDED");
    public static final UnicodeBlock KAYAH_LI = new UnicodeBlock("KAYAH_LI", "KAYAH LI", "KAYAHLI");
    public static final UnicodeBlock REJANG = new UnicodeBlock("REJANG");
    public static final UnicodeBlock HANGUL_JAMO_EXTENDED_A = new UnicodeBlock("HANGUL_JAMO_EXTENDED_A",
        "HANGUL JAMO EXTENDED-A", "HANGULJAMOEXTENDED-A");
    public static final UnicodeBlock JAVANESE = new UnicodeBlock("JAVANESE");
    public static final UnicodeBlock CHAM = new UnicodeBlock("CHAM");
    public static final UnicodeBlock MYANMAR_EXTENDED_A = new UnicodeBlock("MYANMAR_EXTENDED_A",
        "MYANMAR EXTENDED-A", "MYANMAREXTENDED-A");
    public static final UnicodeBlock TAI_VIET = new UnicodeBlock("TAI_VIET", "TAI VIET", "TAIVIET");
    public static final UnicodeBlock ETHIOPIC_EXTENDED_A = new UnicodeBlock("ETHIOPIC_EXTENDED_A",
        "ETHIOPIC EXTENDED-A", "ETHIOPICEXTENDED-A");
    public static final UnicodeBlock MEETEI_MAYEK = new UnicodeBlock("MEETEI_MAYEK", "MEETEI MAYEK", "MEETEIMAYEK");
    public static final UnicodeBlock HANGUL_JAMO_EXTENDED_B = new UnicodeBlock("HANGUL_JAMO_EXTENDED_B",
        "HANGUL JAMO EXTENDED-B", "HANGULJAMOEXTENDED-B");
    public static final UnicodeBlock VERTICAL_FORMS = new UnicodeBlock("VERTICAL_FORMS", "VERTICAL FORMS",
        "VERTICALFORMS");
    public static final UnicodeBlock ANCIENT_GREEK_NUMBERS = new UnicodeBlock("ANCIENT_GREEK_NUMBERS",
        "ANCIENT GREEK NUMBERS", "ANCIENTGREEKNUMBERS");
    public static final UnicodeBlock ANCIENT_SYMBOLS = new UnicodeBlock("ANCIENT_SYMBOLS", "ANCIENT SYMBOLS",
        "ANCIENTSYMBOLS");
    public static final UnicodeBlock PHAISTOS_DISC = new UnicodeBlock("PHAISTOS_DISC", "PHAISTOS DISC", "PHAISTOSDISC");
    public static final UnicodeBlock LYCIAN = new UnicodeBlock("LYCIAN");
    public static final UnicodeBlock CARIAN = new UnicodeBlock("CARIAN");
    public static final UnicodeBlock OLD_PERSIAN = new UnicodeBlock("OLD_PERSIAN", "OLD PERSIAN", "OLDPERSIAN");
    public static final UnicodeBlock IMPERIAL_ARAMAIC = new UnicodeBlock("IMPERIAL_ARAMAIC", "IMPERIAL ARAMAIC",
        "IMPERIALARAMAIC");
    public static final UnicodeBlock PHOENICIAN = new UnicodeBlock("PHOENICIAN");
    public static final UnicodeBlock LYDIAN = new UnicodeBlock("LYDIAN");
    public static final UnicodeBlock KHAROSHTHI = new UnicodeBlock("KHAROSHTHI");
    public static final UnicodeBlock OLD_SOUTH_ARABIAN = new UnicodeBlock("OLD_SOUTH_ARABIAN", "OLD SOUTH ARABIAN",
        "OLDSOUTHARABIAN");
    public static final UnicodeBlock AVESTAN = new UnicodeBlock("AVESTAN");
    public static final UnicodeBlock INSCRIPTIONAL_PARTHIAN = new UnicodeBlock("INSCRIPTIONAL_PARTHIAN",
        "INSCRIPTIONAL PARTHIAN", "INSCRIPTIONALPARTHIAN");
    public static final UnicodeBlock INSCRIPTIONAL_PAHLAVI = new UnicodeBlock("INSCRIPTIONAL_PAHLAVI",
        "INSCRIPTIONAL PAHLAVI", "INSCRIPTIONALPAHLAVI");
    public static final UnicodeBlock OLD_TURKIC = new UnicodeBlock("OLD_TURKIC", "OLD TURKIC", "OLDTURKIC");
    public static final UnicodeBlock RUMI_NUMERAL_SYMBOLS = new UnicodeBlock("RUMI_NUMERAL_SYMBOLS",
        "RUMI NUMERAL SYMBOLS", "RUMINUMERALSYMBOLS");
    public static final UnicodeBlock BRAHMI = new UnicodeBlock("BRAHMI");
    public static final UnicodeBlock KAITHI = new UnicodeBlock("KAITHI");
    public static final UnicodeBlock CUNEIFORM = new UnicodeBlock("CUNEIFORM");
    public static final UnicodeBlock CUNEIFORM_NUMBERS_AND_PUNCTUATION = new UnicodeBlock("CUNEIFORM_NUMBERS_AND_PUNCTUATION", "CUNEIFORM NUMBERS AND PUNCTUATION", "CUNEIFORMNUMBERSANDPUNCTUATION");
    public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS = new UnicodeBlock("EGYPTIAN_HIEROGLYPHS",
        "EGYPTIAN HIEROGLYPHS", "EGYPTIANHIEROGLYPHS");
    public static final UnicodeBlock BAMUM_SUPPLEMENT = new UnicodeBlock("BAMUM_SUPPLEMENT", "BAMUM SUPPLEMENT",
        "BAMUMSUPPLEMENT");
    public static final UnicodeBlock KANA_SUPPLEMENT = new UnicodeBlock("KANA_SUPPLEMENT", "KANA SUPPLEMENT",
        "KANASUPPLEMENT");
    public static final UnicodeBlock ANCIENT_GREEK_MUSICAL_NOTATION = new UnicodeBlock("ANCIENT_GREEK_MUSICAL_NOTATION", "ANCIENT GREEK MUSICAL NOTATION", "ANCIENTGREEKMUSICALNOTATION");
    public static final UnicodeBlock COUNTING_ROD_NUMERALS = new UnicodeBlock("COUNTING_ROD_NUMERALS",
        "COUNTING ROD NUMERALS", "COUNTINGRODNUMERALS");
    public static final UnicodeBlock MAHJONG_TILES = new UnicodeBlock("MAHJONG_TILES", "MAHJONG TILES", "MAHJONGTILES");
    public static final UnicodeBlock DOMINO_TILES = new UnicodeBlock("DOMINO_TILES", "DOMINO TILES", "DOMINOTILES");
    public static final UnicodeBlock PLAYING_CARDS = new UnicodeBlock("PLAYING_CARDS", "PLAYING CARDS", "PLAYINGCARDS");
    public static final UnicodeBlock ENCLOSED_ALPHANUMERIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_ALPHANUMERIC_SUPPLEMENT", "ENCLOSED ALPHANUMERIC SUPPLEMENT", "ENCLOSEDALPHANUMERICSUPPLEMENT");
    public static final UnicodeBlock ENCLOSED_IDEOGRAPHIC_SUPPLEMENT = new UnicodeBlock("ENCLOSED_IDEOGRAPHIC_SUPPLEMENT", "ENCLOSED IDEOGRAPHIC SUPPLEMENT", "ENCLOSEDIDEOGRAPHICSUPPLEMENT");
    public static final UnicodeBlock MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS", "MISCELLANEOUS SYMBOLS AND PICTOGRAPHS", "MISCELLANEOUSSYMBOLSANDPICTOGRAPHS");
    public static final UnicodeBlock EMOTICONS = new UnicodeBlock("EMOTICONS");
    public static final UnicodeBlock TRANSPORT_AND_MAP_SYMBOLS = new UnicodeBlock("TRANSPORT_AND_MAP_SYMBOLS",
        "TRANSPORT AND MAP SYMBOLS", "TRANSPORTANDMAPSYMBOLS");
    public static final UnicodeBlock ALCHEMICAL_SYMBOLS = new UnicodeBlock("ALCHEMICAL_SYMBOLS",
        "ALCHEMICAL SYMBOLS", "ALCHEMICALSYMBOLS");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C", "CJK UNIFIED IDEOGRAPHS EXTENSION C", "CJKUNIFIEDIDEOGRAPHSEXTENSIONC");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D", "CJK UNIFIED IDEOGRAPHS EXTENSION D", "CJKUNIFIEDIDEOGRAPHSEXTENSIOND");
    public static final UnicodeBlock ARABIC_EXTENDED_A = new UnicodeBlock("ARABIC_EXTENDED_A", "ARABIC EXTENDED-A",
        "ARABICEXTENDED-A");
    public static final UnicodeBlock SUNDANESE_SUPPLEMENT = new UnicodeBlock("SUNDANESE_SUPPLEMENT",
        "SUNDANESE SUPPLEMENT", "SUNDANESESUPPLEMENT");
    public static final UnicodeBlock MEETEI_MAYEK_EXTENSIONS = new UnicodeBlock("MEETEI_MAYEK_EXTENSIONS",
        "MEETEI MAYEK EXTENSIONS", "MEETEIMAYEKEXTENSIONS");
    public static final UnicodeBlock MEROITIC_HIEROGLYPHS = new UnicodeBlock("MEROITIC_HIEROGLYPHS",
        "MEROITIC HIEROGLYPHS", "MEROITICHIEROGLYPHS");
    public static final UnicodeBlock MEROITIC_CURSIVE = new UnicodeBlock("MEROITIC_CURSIVE", "MEROITIC CURSIVE",
        "MEROITICCURSIVE");
    public static final UnicodeBlock SORA_SOMPENG = new UnicodeBlock("SORA_SOMPENG", "SORA SOMPENG", "SORASOMPENG");
    public static final UnicodeBlock CHAKMA = new UnicodeBlock("CHAKMA");
    public static final UnicodeBlock SHARADA = new UnicodeBlock("SHARADA");
    public static final UnicodeBlock TAKRI = new UnicodeBlock("TAKRI");
    public static final UnicodeBlock MIAO = new UnicodeBlock("MIAO");
    public static final UnicodeBlock ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS = new UnicodeBlock("ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS", "ARABIC MATHEMATICAL ALPHABETIC SYMBOLS", "ARABICMATHEMATICALALPHABETICSYMBOLS");
    public static final UnicodeBlock COMBINING_DIACRITICAL_MARKS_EXTENDED = new UnicodeBlock("COMBINING_DIACRITICAL_MARKS_EXTENDED", "COMBINING DIACRITICAL MARKS EXTENDED", "COMBININGDIACRITICALMARKSEXTENDED");
    public static final UnicodeBlock MYANMAR_EXTENDED_B = new UnicodeBlock("MYANMAR_EXTENDED_B",
        "MYANMAR EXTENDED-B", "MYANMAREXTENDED-B");
    public static final UnicodeBlock LATIN_EXTENDED_E = new UnicodeBlock("LATIN_EXTENDED_E", "LATIN EXTENDED-E",
        "LATINEXTENDED-E");
    public static final UnicodeBlock COPTIC_EPACT_NUMBERS = new UnicodeBlock("COPTIC_EPACT_NUMBERS",
        "COPTIC EPACT NUMBERS", "COPTICEPACTNUMBERS");
    public static final UnicodeBlock OLD_PERMIC = new UnicodeBlock("OLD_PERMIC", "OLD PERMIC", "OLDPERMIC");
    public static final UnicodeBlock ELBASAN = new UnicodeBlock("ELBASAN");
    public static final UnicodeBlock CAUCASIAN_ALBANIAN = new UnicodeBlock("CAUCASIAN_ALBANIAN",
        "CAUCASIAN ALBANIAN", "CAUCASIANALBANIAN");
    public static final UnicodeBlock LINEAR_A = new UnicodeBlock("LINEAR_A", "LINEAR A", "LINEARA");
    public static final UnicodeBlock PALMYRENE = new UnicodeBlock("PALMYRENE");
    public static final UnicodeBlock NABATAEAN = new UnicodeBlock("NABATAEAN");
    public static final UnicodeBlock OLD_NORTH_ARABIAN = new UnicodeBlock("OLD_NORTH_ARABIAN", "OLD NORTH ARABIAN",
        "OLDNORTHARABIAN");
    public static final UnicodeBlock MANICHAEAN = new UnicodeBlock("MANICHAEAN");
    public static final UnicodeBlock PSALTER_PAHLAVI = new UnicodeBlock("PSALTER_PAHLAVI", "PSALTER PAHLAVI",
        "PSALTERPAHLAVI");
    public static final UnicodeBlock MAHAJANI = new UnicodeBlock("MAHAJANI");
    public static final UnicodeBlock SINHALA_ARCHAIC_NUMBERS = new UnicodeBlock("SINHALA_ARCHAIC_NUMBERS",
        "SINHALA ARCHAIC NUMBERS", "SINHALAARCHAICNUMBERS");
    public static final UnicodeBlock KHOJKI = new UnicodeBlock("KHOJKI");
    public static final UnicodeBlock KHUDAWADI = new UnicodeBlock("KHUDAWADI");
    public static final UnicodeBlock GRANTHA = new UnicodeBlock("GRANTHA");
    public static final UnicodeBlock TIRHUTA = new UnicodeBlock("TIRHUTA");
    public static final UnicodeBlock SIDDHAM = new UnicodeBlock("SIDDHAM");
    public static final UnicodeBlock MODI = new UnicodeBlock("MODI");
    public static final UnicodeBlock WARANG_CITI = new UnicodeBlock("WARANG_CITI", "WARANG CITI", "WARANGCITI");
    public static final UnicodeBlock PAU_CIN_HAU = new UnicodeBlock("PAU_CIN_HAU", "PAU CIN HAU", "PAUCINHAU");
    public static final UnicodeBlock MRO = new UnicodeBlock("MRO");
    public static final UnicodeBlock BASSA_VAH = new UnicodeBlock("BASSA_VAH", "BASSA VAH", "BASSAVAH");
    public static final UnicodeBlock PAHAWH_HMONG = new UnicodeBlock("PAHAWH_HMONG", "PAHAWH HMONG", "PAHAWHHMONG");
    public static final UnicodeBlock DUPLOYAN = new UnicodeBlock("DUPLOYAN");
    public static final UnicodeBlock SHORTHAND_FORMAT_CONTROLS = new UnicodeBlock("SHORTHAND_FORMAT_CONTROLS",
        "SHORTHAND FORMAT CONTROLS", "SHORTHANDFORMATCONTROLS");
    public static final UnicodeBlock MENDE_KIKAKUI = new UnicodeBlock("MENDE_KIKAKUI", "MENDE KIKAKUI", "MENDEKIKAKUI");
    public static final UnicodeBlock ORNAMENTAL_DINGBATS = new UnicodeBlock("ORNAMENTAL_DINGBATS",
        "ORNAMENTAL DINGBATS", "ORNAMENTALDINGBATS");
    public static final UnicodeBlock GEOMETRIC_SHAPES_EXTENDED = new UnicodeBlock("GEOMETRIC_SHAPES_EXTENDED",
        "GEOMETRIC SHAPES EXTENDED", "GEOMETRICSHAPESEXTENDED");
    public static final UnicodeBlock SUPPLEMENTAL_ARROWS_C = new UnicodeBlock("SUPPLEMENTAL_ARROWS_C",
        "SUPPLEMENTAL ARROWS-C", "SUPPLEMENTALARROWS-C");
    public static final UnicodeBlock CHEROKEE_SUPPLEMENT = new UnicodeBlock("CHEROKEE_SUPPLEMENT",
        "CHEROKEE SUPPLEMENT", "CHEROKEESUPPLEMENT");
    public static final UnicodeBlock HATRAN = new UnicodeBlock("HATRAN");
    public static final UnicodeBlock OLD_HUNGARIAN = new UnicodeBlock("OLD_HUNGARIAN", "OLD HUNGARIAN", "OLDHUNGARIAN");
    public static final UnicodeBlock MULTANI = new UnicodeBlock("MULTANI");
    public static final UnicodeBlock AHOM = new UnicodeBlock("AHOM");
    public static final UnicodeBlock EARLY_DYNASTIC_CUNEIFORM = new UnicodeBlock("EARLY_DYNASTIC_CUNEIFORM",
        "EARLY DYNASTIC CUNEIFORM", "EARLYDYNASTICCUNEIFORM");
    public static final UnicodeBlock ANATOLIAN_HIEROGLYPHS = new UnicodeBlock("ANATOLIAN_HIEROGLYPHS",
        "ANATOLIAN HIEROGLYPHS", "ANATOLIANHIEROGLYPHS");
    public static final UnicodeBlock SUTTON_SIGNWRITING = new UnicodeBlock("SUTTON_SIGNWRITING",
        "SUTTON SIGNWRITING", "SUTTONSIGNWRITING");
    public static final UnicodeBlock SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS = new UnicodeBlock("SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS", "SUPPLEMENTAL SYMBOLS AND PICTOGRAPHS", "SUPPLEMENTALSYMBOLSANDPICTOGRAPHS");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E", "CJK UNIFIED IDEOGRAPHS EXTENSION E", "CJKUNIFIEDIDEOGRAPHSEXTENSIONE");
    public static final UnicodeBlock SYRIAC_SUPPLEMENT = new UnicodeBlock("SYRIAC_SUPPLEMENT", "SYRIAC SUPPLEMENT",
        "SYRIACSUPPLEMENT");
    public static final UnicodeBlock CYRILLIC_EXTENDED_C = new UnicodeBlock("CYRILLIC_EXTENDED_C",
        "CYRILLIC EXTENDED-C", "CYRILLICEXTENDED-C");
    public static final UnicodeBlock OSAGE = new UnicodeBlock("OSAGE");
    public static final UnicodeBlock NEWA = new UnicodeBlock("NEWA");
    public static final UnicodeBlock MONGOLIAN_SUPPLEMENT = new UnicodeBlock("MONGOLIAN_SUPPLEMENT",
        "MONGOLIAN SUPPLEMENT", "MONGOLIANSUPPLEMENT");
    public static final UnicodeBlock MARCHEN = new UnicodeBlock("MARCHEN");
    public static final UnicodeBlock IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION = new UnicodeBlock("IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION", "IDEOGRAPHIC SYMBOLS AND PUNCTUATION", "IDEOGRAPHICSYMBOLSANDPUNCTUATION");
    public static final UnicodeBlock TANGUT = new UnicodeBlock("TANGUT");
    public static final UnicodeBlock TANGUT_COMPONENTS = new UnicodeBlock("TANGUT_COMPONENTS", "TANGUT COMPONENTS",
        "TANGUTCOMPONENTS");
    public static final UnicodeBlock KANA_EXTENDED_A = new UnicodeBlock("KANA_EXTENDED_A", "KANA EXTENDED-A",
        "KANAEXTENDED-A");
    public static final UnicodeBlock GLAGOLITIC_SUPPLEMENT = new UnicodeBlock("GLAGOLITIC_SUPPLEMENT",
        "GLAGOLITIC SUPPLEMENT", "GLAGOLITICSUPPLEMENT");
    public static final UnicodeBlock ADLAM = new UnicodeBlock("ADLAM");
    public static final UnicodeBlock MASARAM_GONDI = new UnicodeBlock("MASARAM_GONDI", "MASARAM GONDI", "MASARAMGONDI");
    public static final UnicodeBlock ZANABAZAR_SQUARE = new UnicodeBlock("ZANABAZAR_SQUARE", "ZANABAZAR SQUARE",
        "ZANABAZARSQUARE");
    public static final UnicodeBlock NUSHU = new UnicodeBlock("NUSHU");
    public static final UnicodeBlock SOYOMBO = new UnicodeBlock("SOYOMBO");
    public static final UnicodeBlock BHAIKSUKI = new UnicodeBlock("BHAIKSUKI");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F", "CJK UNIFIED IDEOGRAPHS EXTENSION F", "CJKUNIFIEDIDEOGRAPHSEXTENSIONF");
    public static final UnicodeBlock GEORGIAN_EXTENDED = new UnicodeBlock("GEORGIAN_EXTENDED", "GEORGIAN EXTENDED",
        "GEORGIANEXTENDED");
    public static final UnicodeBlock HANIFI_ROHINGYA = new UnicodeBlock("HANIFI_ROHINGYA", "HANIFI ROHINGYA",
        "HANIFIROHINGYA");
    public static final UnicodeBlock OLD_SOGDIAN = new UnicodeBlock("OLD_SOGDIAN", "OLD SOGDIAN", "OLDSOGDIAN");
    public static final UnicodeBlock SOGDIAN = new UnicodeBlock("SOGDIAN");
    public static final UnicodeBlock DOGRA = new UnicodeBlock("DOGRA");
    public static final UnicodeBlock GUNJALA_GONDI = new UnicodeBlock("GUNJALA_GONDI", "GUNJALA GONDI", "GUNJALAGONDI");
    public static final UnicodeBlock MAKASAR = new UnicodeBlock("MAKASAR");
    public static final UnicodeBlock MEDEFAIDRIN = new UnicodeBlock("MEDEFAIDRIN");
    public static final UnicodeBlock MAYAN_NUMERALS = new UnicodeBlock("MAYAN_NUMERALS", "MAYAN NUMERALS",
        "MAYANNUMERALS");
    public static final UnicodeBlock INDIC_SIYAQ_NUMBERS = new UnicodeBlock("INDIC_SIYAQ_NUMBERS",
        "INDIC SIYAQ NUMBERS", "INDICSIYAQNUMBERS");
    public static final UnicodeBlock CHESS_SYMBOLS = new UnicodeBlock("CHESS_SYMBOLS", "CHESS SYMBOLS", "CHESSSYMBOLS");
    public static final UnicodeBlock ELYMAIC = new UnicodeBlock("ELYMAIC");
    public static final UnicodeBlock NANDINAGARI = new UnicodeBlock("NANDINAGARI");
    public static final UnicodeBlock TAMIL_SUPPLEMENT = new UnicodeBlock("TAMIL_SUPPLEMENT", "TAMIL SUPPLEMENT",
        "TAMILSUPPLEMENT");
    public static final UnicodeBlock EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS = new UnicodeBlock("EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS", "EGYPTIAN HIEROGLYPH FORMAT CONTROLS", "EGYPTIANHIEROGLYPHFORMATCONTROLS");
    public static final UnicodeBlock SMALL_KANA_EXTENSION = new UnicodeBlock("SMALL_KANA_EXTENSION",
        "SMALL KANA EXTENSION", "SMALLKANAEXTENSION");
    public static final UnicodeBlock NYIAKENG_PUACHUE_HMONG = new UnicodeBlock("NYIAKENG_PUACHUE_HMONG",
        "NYIAKENG PUACHUE HMONG", "NYIAKENGPUACHUEHMONG");
    public static final UnicodeBlock WANCHO = new UnicodeBlock("WANCHO");
    public static final UnicodeBlock OTTOMAN_SIYAQ_NUMBERS = new UnicodeBlock("OTTOMAN_SIYAQ_NUMBERS",
        "OTTOMAN SIYAQ NUMBERS", "OTTOMANSIYAQNUMBERS");
    public static final UnicodeBlock SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A = new UnicodeBlock("SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A", "SYMBOLS AND PICTOGRAPHS EXTENDED-A", "SYMBOLSANDPICTOGRAPHSEXTENDED-A");
    public static final UnicodeBlock YEZIDI = new UnicodeBlock("YEZIDI");
    public static final UnicodeBlock CHORASMIAN = new UnicodeBlock("CHORASMIAN");
    public static final UnicodeBlock DIVES_AKURU = new UnicodeBlock("DIVES_AKURU", "DIVES AKURU", "DIVESAKURU");
    public static final UnicodeBlock LISU_SUPPLEMENT = new UnicodeBlock("LISU_SUPPLEMENT", "LISU SUPPLEMENT",
        "LISUSUPPLEMENT");
    public static final UnicodeBlock KHITAN_SMALL_SCRIPT = new UnicodeBlock("KHITAN_SMALL_SCRIPT",
        "KHITAN SMALL SCRIPT", "KHITANSMALLSCRIPT");
    public static final UnicodeBlock TANGUT_SUPPLEMENT = new UnicodeBlock("TANGUT_SUPPLEMENT", "TANGUT SUPPLEMENT",
        "TANGUTSUPPLEMENT");
    public static final UnicodeBlock SYMBOLS_FOR_LEGACY_COMPUTING = new UnicodeBlock("SYMBOLS_FOR_LEGACY_COMPUTING",
        "SYMBOLS FOR LEGACY COMPUTING", "SYMBOLSFORLEGACYCOMPUTING");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G", "CJK UNIFIED IDEOGRAPHS EXTENSION G", "CJKUNIFIEDIDEOGRAPHSEXTENSIONG");
    public static final UnicodeBlock ARABIC_EXTENDED_B = new UnicodeBlock("ARABIC_EXTENDED_B", "ARABIC EXTENDED-B",
        "ARABICEXTENDED-B");
    public static final UnicodeBlock VITHKUQI = new UnicodeBlock("VITHKUQI");
    public static final UnicodeBlock LATIN_EXTENDED_F = new UnicodeBlock("LATIN_EXTENDED_F", "LATIN EXTENDED-F",
        "LATINEXTENDED-F");
    public static final UnicodeBlock OLD_UYGHUR = new UnicodeBlock("OLD_UYGHUR", "OLD UYGHUR", "OLDUYGHUR");
    public static final UnicodeBlock UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_A = new UnicodeBlock("UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_A", "UNIFIED CANADIAN ABORIGINAL SYLLABICS EXTENDED-A", "UNIFIEDCANADIANABORIGINALSYLLABICSEXTENDED-A");
    public static final UnicodeBlock CYPRO_MINOAN = new UnicodeBlock("CYPRO_MINOAN", "CYPRO-MINOAN");
    public static final UnicodeBlock TANGSA = new UnicodeBlock("TANGSA");
    public static final UnicodeBlock KANA_EXTENDED_B = new UnicodeBlock("KANA_EXTENDED_B", "KANA EXTENDED-B",
        "KANAEXTENDED-B");
    public static final UnicodeBlock ZNAMENNY_MUSICAL_NOTATION = new UnicodeBlock("ZNAMENNY_MUSICAL_NOTATION",
        "ZNAMENNY MUSICAL NOTATION", "ZNAMENNYMUSICALNOTATION");
    public static final UnicodeBlock LATIN_EXTENDED_G = new UnicodeBlock("LATIN_EXTENDED_G", "LATIN EXTENDED-G",
        "LATINEXTENDED-G");
    public static final UnicodeBlock TOTO = new UnicodeBlock("TOTO");
    public static final UnicodeBlock ETHIOPIC_EXTENDED_B = new UnicodeBlock("ETHIOPIC_EXTENDED_B",
        "ETHIOPIC EXTENDED-B", "ETHIOPICEXTENDED-B");
    public static final UnicodeBlock ARABIC_EXTENDED_C = new UnicodeBlock("ARABIC_EXTENDED_C", "ARABIC EXTENDED-C",
        "ARABICEXTENDED-C");
    public static final UnicodeBlock DEVANAGARI_EXTENDED_A = new UnicodeBlock("DEVANAGARI_EXTENDED_A",
        "DEVANAGARI EXTENDED-A", "DEVANAGARIEXTENDED-A");
    public static final UnicodeBlock KAWI = new UnicodeBlock("KAWI");
    public static final UnicodeBlock KAKTOVIK_NUMERALS = new UnicodeBlock("KAKTOVIK_NUMERALS", "KAKTOVIK NUMERALS",
        "KAKTOVIKNUMERALS");
    public static final UnicodeBlock CYRILLIC_EXTENDED_D = new UnicodeBlock("CYRILLIC_EXTENDED_D",
        "CYRILLIC EXTENDED-D", "CYRILLICEXTENDED-D");
    public static final UnicodeBlock NAG_MUNDARI = new UnicodeBlock("NAG_MUNDARI", "NAG MUNDARI", "NAGMUNDARI");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_H = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_H", "CJK UNIFIED IDEOGRAPHS EXTENSION H", "CJKUNIFIEDIDEOGRAPHSEXTENSIONH");
    public static final UnicodeBlock CJK_UNIFIED_IDEOGRAPHS_EXTENSION_I = new UnicodeBlock("CJK_UNIFIED_IDEOGRAPHS_EXTENSION_I", "CJK UNIFIED IDEOGRAPHS EXTENSION I", "CJKUNIFIEDIDEOGRAPHSEXTENSIONI");
    public static final UnicodeBlock TODHRI = new UnicodeBlock("TODHRI");
    public static final UnicodeBlock GARAY = new UnicodeBlock("GARAY");
    public static final UnicodeBlock TULU_TIGALARI = new UnicodeBlock("TULU_TIGALARI", "TULU-TIGALARI");
    public static final UnicodeBlock MYANMAR_EXTENDED_C = new UnicodeBlock("MYANMAR_EXTENDED_C",
        "MYANMAR EXTENDED-C", "MYANMAREXTENDED-C");
    public static final UnicodeBlock SUNUWAR = new UnicodeBlock("SUNUWAR");
    public static final UnicodeBlock EGYPTIAN_HIEROGLYPHS_EXTENDED_A = new UnicodeBlock("EGYPTIAN_HIEROGLYPHS_EXTENDED_A", "EGYPTIAN HIEROGLYPHS EXTENDED-A", "EGYPTIANHIEROGLYPHSEXTENDED-A");
    public static final UnicodeBlock GURUNG_KHEMA = new UnicodeBlock("GURUNG_KHEMA", "GURUNG KHEMA", "GURUNGKHEMA");
    public static final UnicodeBlock KIRAT_RAI = new UnicodeBlock("KIRAT_RAI", "KIRAT RAI", "KIRATRAI");
    public static final UnicodeBlock SYMBOLS_FOR_LEGACY_COMPUTING_SUPPLEMENT = new UnicodeBlock("SYMBOLS_FOR_LEGACY_COMPUTING_SUPPLEMENT", "SYMBOLS FOR LEGACY COMPUTING SUPPLEMENT", "SYMBOLSFORLEGACYCOMPUTINGSUPPLEMENT");
    public static final UnicodeBlock OL_ONAL = new UnicodeBlock("OL_ONAL", "OL ONAL", "OLONAL");

    private static final String kStarts =
        "0000000000800001000001800002500002b00003000003700004000005000005300005900006000007000007500007800007" +
        "c00008000008400008600008700008a0000900000980000a00000a80000b00000b80000c00000c80000d00000d80000e0000" +
        "0e80000f000010000010a00011000012000013800013a00014000016800016a0001700001720001740001760001780001800" +
        "0018b00019000019500019800019e0001a00001a20001ab0001b00001b80001bc0001c00001c50001c80001c90001cc0001c" +
        "d0001d00001d80001dc0001e00001f000020000020700020a00020d000210000215000219000220000230000240000244000" +
        "24600025000025800025a00026000027000027c00027f0002800002900002980002a00002b00002c00002c60002c80002d00" +
        "002d30002d80002de0002e00002e80002f00002ff00030000030400030a00031000031300031900031a00031c00031f00032" +
        "00003300003400004dc0004e0000a00000a49000a4d000a50000a64000a6a000a70000a72000a80000a83000a84000a88000" +
        "a8e000a90000a93000a96000a98000a9e000aa0000aa6000aa8000aae000ab0000ab3000ab7000abc000ac0000d7b000d800" +
        "00db8000dc0000e00000f90000fb0000fb5000fe0000fe1000fe2000fe3000fe5000fe7000ff0000fff00100000100800101" +
        "000101400101900101d00102800102a00102e00103000103300103500103800103a00104000104500104800104b001050001" +
        "05300105700105c00106000107800108000108400108600108800108e00109000109200109800109a0010a00010a60010a80" +
        "010ac0010b00010b40010b60010b80010c00010c80010d00010d40010e60010e80010ec0010f00010f30010f70010fb0010f" +
        "e00110000110800110d00111000111500111800111e00112000112800112b001130001138001140001148001158001160001" +
        "16600116800116d00117000118000118a00119000119a0011a00011a50011ab0011ac0011b00011bc0011c00011c70011d00" +
        "011d60011ee0011f00011fb0011fc0012000012400012480012f90013000013430013460014400016100016800016a40016a" +
        "70016ad0016b00016d40016e40016f00016fe0017000018800018b00018d0001aff001b00001b10001b13001b17001bc0001" +
        "bca001cc0001cf0001d00001d10001d20001d2c001d2e001d30001d36001d40001d80001df0001e00001e03001e10001e290" +
        "01e2c001e4d001e5d001e7e001e80001e90001ec7001ed0001ee0001f00001f03001f0a001f10001f20001f30001f60001f6" +
        "5001f68001f70001f78001f80001f90001fa0001fa7001fb0002000002a70002b74002b82002ceb002ebf002f80003000003" +
        "13500e00000e01000f0000100000";
    private static final String kEnds =
        "00007f0000ff00017f00024f0002af0002ff00036f0003ff0004ff00052f00058f0005ff0006ff00074f00077f0007bf0007" +
        "ff00083f00085f00086f00089f0008ff00097f0009ff000a7f000aff000b7f000bff000c7f000cff000d7f000dff000e7f00" +
        "0eff000fff00109f0010ff0011ff00137f00139f0013ff00167f00169f0016ff00171f00173f00175f00177f0017ff0018af" +
        "0018ff00194f00197f0019df0019ff001a1f001aaf001aff001b7f001bbf001bff001c4f001c7f001c8f001cbf001ccf001c" +
        "ff001d7f001dbf001dff001eff001fff00206f00209f0020cf0020ff00214f00218f0021ff0022ff0023ff00243f00245f00" +
        "24ff00257f00259f0025ff0026ff0027bf0027ef0027ff0028ff00297f0029ff002aff002bff002c5f002c7f002cff002d2f" +
        "002d7f002ddf002dff002e7f002eff002fdf002fff00303f00309f0030ff00312f00318f00319f0031bf0031ef0031ff0032" +
        "ff0033ff004dbf004dff009fff00a48f00a4cf00a4ff00a63f00a69f00a6ff00a71f00a7ff00a82f00a83f00a87f00a8df00" +
        "a8ff00a92f00a95f00a97f00a9df00a9ff00aa5f00aa7f00aadf00aaff00ab2f00ab6f00abbf00abff00d7af00d7ff00db7f" +
        "00dbff00dfff00f8ff00faff00fb4f00fdff00fe0f00fe1f00fe2f00fe4f00fe6f00feff00ffef00ffff01007f0100ff0101" +
        "3f01018f0101cf0101ff01029f0102df0102ff01032f01034f01037f01039f0103df01044f01047f0104af0104ff01052f01" +
        "056f0105bf0105ff01077f0107bf01083f01085f01087f0108af0108ff01091f01093f01099f0109ff010a5f010a7f010a9f" +
        "010aff010b3f010b5f010b7f010baf010c4f010cff010d3f010d8f010e7f010ebf010eff010f2f010f6f010faf010fdf010f" +
        "ff01107f0110cf0110ff01114f01117f0111df0111ff01124f0112af0112ff01137f0113ff01147f0114df0115ff01165f01" +
        "167f0116cf0116ff01174f01184f0118ff01195f0119ff011a4f011aaf011abf011aff011b5f011bff011c6f011cbf011d5f" +
        "011daf011eff011f5f011fbf011fff0123ff01247f01254f012fff01342f01345f0143ff01467f01613f016a3f016a6f016a" +
        "cf016aff016b8f016d7f016e9f016f9f016fff0187ff018aff018cff018d7f01afff01b0ff01b12f01b16f01b2ff01bc9f01" +
        "bcaf01cebf01cfcf01d0ff01d1ff01d24f01d2df01d2ff01d35f01d37f01d7ff01daaf01dfff01e02f01e08f01e14f01e2bf" +
        "01e2ff01e4ff01e5ff01e7ff01e8df01e95f01ecbf01ed4f01eeff01f02f01f09f01f0ff01f1ff01f2ff01f5ff01f64f01f6" +
        "7f01f6ff01f77f01f7ff01f8ff01f9ff01fa6f01faff01fbff02a6df02b73f02b81f02ceaf02ebef02ee5f02fa1f03134f03" +
        "23af0e007f0e01ef0fffff10ffff";
    private static final UnicodeBlock[] kBlocks = {
        BASIC_LATIN, LATIN_1_SUPPLEMENT, LATIN_EXTENDED_A, LATIN_EXTENDED_B, IPA_EXTENSIONS,
        SPACING_MODIFIER_LETTERS, COMBINING_DIACRITICAL_MARKS, GREEK, CYRILLIC, CYRILLIC_SUPPLEMENTARY,
        ARMENIAN, HEBREW, ARABIC, SYRIAC, ARABIC_SUPPLEMENT, THAANA, NKO, SAMARITAN, MANDAIC,
        SYRIAC_SUPPLEMENT, ARABIC_EXTENDED_B, ARABIC_EXTENDED_A, DEVANAGARI, BENGALI, GURMUKHI, GUJARATI,
        ORIYA, TAMIL, TELUGU, KANNADA, MALAYALAM, SINHALA, THAI, LAO, TIBETAN, MYANMAR, GEORGIAN,
        HANGUL_JAMO, ETHIOPIC, ETHIOPIC_SUPPLEMENT, CHEROKEE, UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS, OGHAM,
        RUNIC, TAGALOG, HANUNOO, BUHID, TAGBANWA, KHMER, MONGOLIAN,
        UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED, LIMBU, TAI_LE, NEW_TAI_LUE, KHMER_SYMBOLS, BUGINESE,
        TAI_THAM, COMBINING_DIACRITICAL_MARKS_EXTENDED, BALINESE, SUNDANESE, BATAK, LEPCHA, OL_CHIKI,
        CYRILLIC_EXTENDED_C, GEORGIAN_EXTENDED, SUNDANESE_SUPPLEMENT, VEDIC_EXTENSIONS, PHONETIC_EXTENSIONS,
        PHONETIC_EXTENSIONS_SUPPLEMENT, COMBINING_DIACRITICAL_MARKS_SUPPLEMENT, LATIN_EXTENDED_ADDITIONAL,
        GREEK_EXTENDED, GENERAL_PUNCTUATION, SUPERSCRIPTS_AND_SUBSCRIPTS, CURRENCY_SYMBOLS,
        COMBINING_MARKS_FOR_SYMBOLS, LETTERLIKE_SYMBOLS, NUMBER_FORMS, ARROWS, MATHEMATICAL_OPERATORS,
        MISCELLANEOUS_TECHNICAL, CONTROL_PICTURES, OPTICAL_CHARACTER_RECOGNITION, ENCLOSED_ALPHANUMERICS,
        BOX_DRAWING, BLOCK_ELEMENTS, GEOMETRIC_SHAPES, MISCELLANEOUS_SYMBOLS, DINGBATS,
        MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A, SUPPLEMENTAL_ARROWS_A, BRAILLE_PATTERNS, SUPPLEMENTAL_ARROWS_B,
        MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B, SUPPLEMENTAL_MATHEMATICAL_OPERATORS,
        MISCELLANEOUS_SYMBOLS_AND_ARROWS, GLAGOLITIC, LATIN_EXTENDED_C, COPTIC, GEORGIAN_SUPPLEMENT,
        TIFINAGH, ETHIOPIC_EXTENDED, CYRILLIC_EXTENDED_A, SUPPLEMENTAL_PUNCTUATION, CJK_RADICALS_SUPPLEMENT,
        KANGXI_RADICALS, IDEOGRAPHIC_DESCRIPTION_CHARACTERS, CJK_SYMBOLS_AND_PUNCTUATION, HIRAGANA, KATAKANA,
        BOPOMOFO, HANGUL_COMPATIBILITY_JAMO, KANBUN, BOPOMOFO_EXTENDED, CJK_STROKES,
        KATAKANA_PHONETIC_EXTENSIONS, ENCLOSED_CJK_LETTERS_AND_MONTHS, CJK_COMPATIBILITY,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, YIJING_HEXAGRAM_SYMBOLS, CJK_UNIFIED_IDEOGRAPHS, YI_SYLLABLES,
        YI_RADICALS, LISU, VAI, CYRILLIC_EXTENDED_B, BAMUM, MODIFIER_TONE_LETTERS, LATIN_EXTENDED_D,
        SYLOTI_NAGRI, COMMON_INDIC_NUMBER_FORMS, PHAGS_PA, SAURASHTRA, DEVANAGARI_EXTENDED, KAYAH_LI, REJANG,
        HANGUL_JAMO_EXTENDED_A, JAVANESE, MYANMAR_EXTENDED_B, CHAM, MYANMAR_EXTENDED_A, TAI_VIET,
        MEETEI_MAYEK_EXTENSIONS, ETHIOPIC_EXTENDED_A, LATIN_EXTENDED_E, CHEROKEE_SUPPLEMENT, MEETEI_MAYEK,
        HANGUL_SYLLABLES, HANGUL_JAMO_EXTENDED_B, HIGH_SURROGATES, HIGH_PRIVATE_USE_SURROGATES,
        LOW_SURROGATES, PRIVATE_USE_AREA, CJK_COMPATIBILITY_IDEOGRAPHS, ALPHABETIC_PRESENTATION_FORMS,
        ARABIC_PRESENTATION_FORMS_A, VARIATION_SELECTORS, VERTICAL_FORMS, COMBINING_HALF_MARKS,
        CJK_COMPATIBILITY_FORMS, SMALL_FORM_VARIANTS, ARABIC_PRESENTATION_FORMS_B,
        HALFWIDTH_AND_FULLWIDTH_FORMS, SPECIALS, LINEAR_B_SYLLABARY, LINEAR_B_IDEOGRAMS, AEGEAN_NUMBERS,
        ANCIENT_GREEK_NUMBERS, ANCIENT_SYMBOLS, PHAISTOS_DISC, LYCIAN, CARIAN, COPTIC_EPACT_NUMBERS,
        OLD_ITALIC, GOTHIC, OLD_PERMIC, UGARITIC, OLD_PERSIAN, DESERET, SHAVIAN, OSMANYA, OSAGE, ELBASAN,
        CAUCASIAN_ALBANIAN, VITHKUQI, TODHRI, LINEAR_A, LATIN_EXTENDED_F, CYPRIOT_SYLLABARY,
        IMPERIAL_ARAMAIC, PALMYRENE, NABATAEAN, HATRAN, PHOENICIAN, LYDIAN, MEROITIC_HIEROGLYPHS,
        MEROITIC_CURSIVE, KHAROSHTHI, OLD_SOUTH_ARABIAN, OLD_NORTH_ARABIAN, MANICHAEAN, AVESTAN,
        INSCRIPTIONAL_PARTHIAN, INSCRIPTIONAL_PAHLAVI, PSALTER_PAHLAVI, OLD_TURKIC, OLD_HUNGARIAN,
        HANIFI_ROHINGYA, GARAY, RUMI_NUMERAL_SYMBOLS, YEZIDI, ARABIC_EXTENDED_C, OLD_SOGDIAN, SOGDIAN,
        OLD_UYGHUR, CHORASMIAN, ELYMAIC, BRAHMI, KAITHI, SORA_SOMPENG, CHAKMA, MAHAJANI, SHARADA,
        SINHALA_ARCHAIC_NUMBERS, KHOJKI, MULTANI, KHUDAWADI, GRANTHA, TULU_TIGALARI, NEWA, TIRHUTA, SIDDHAM,
        MODI, MONGOLIAN_SUPPLEMENT, TAKRI, MYANMAR_EXTENDED_C, AHOM, DOGRA, WARANG_CITI, DIVES_AKURU,
        NANDINAGARI, ZANABAZAR_SQUARE, SOYOMBO, UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED_A,
        PAU_CIN_HAU, DEVANAGARI_EXTENDED_A, SUNUWAR, BHAIKSUKI, MARCHEN, MASARAM_GONDI, GUNJALA_GONDI,
        MAKASAR, KAWI, LISU_SUPPLEMENT, TAMIL_SUPPLEMENT, CUNEIFORM, CUNEIFORM_NUMBERS_AND_PUNCTUATION,
        EARLY_DYNASTIC_CUNEIFORM, CYPRO_MINOAN, EGYPTIAN_HIEROGLYPHS, EGYPTIAN_HIEROGLYPH_FORMAT_CONTROLS,
        EGYPTIAN_HIEROGLYPHS_EXTENDED_A, ANATOLIAN_HIEROGLYPHS, GURUNG_KHEMA, BAMUM_SUPPLEMENT, MRO, TANGSA,
        BASSA_VAH, PAHAWH_HMONG, KIRAT_RAI, MEDEFAIDRIN, MIAO, IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION, TANGUT,
        TANGUT_COMPONENTS, KHITAN_SMALL_SCRIPT, TANGUT_SUPPLEMENT, KANA_EXTENDED_B, KANA_SUPPLEMENT,
        KANA_EXTENDED_A, SMALL_KANA_EXTENSION, NUSHU, DUPLOYAN, SHORTHAND_FORMAT_CONTROLS,
        SYMBOLS_FOR_LEGACY_COMPUTING_SUPPLEMENT, ZNAMENNY_MUSICAL_NOTATION, BYZANTINE_MUSICAL_SYMBOLS,
        MUSICAL_SYMBOLS, ANCIENT_GREEK_MUSICAL_NOTATION, KAKTOVIK_NUMERALS, MAYAN_NUMERALS,
        TAI_XUAN_JING_SYMBOLS, COUNTING_ROD_NUMERALS, MATHEMATICAL_ALPHANUMERIC_SYMBOLS, SUTTON_SIGNWRITING,
        LATIN_EXTENDED_G, GLAGOLITIC_SUPPLEMENT, CYRILLIC_EXTENDED_D, NYIAKENG_PUACHUE_HMONG, TOTO, WANCHO,
        NAG_MUNDARI, OL_ONAL, ETHIOPIC_EXTENDED_B, MENDE_KIKAKUI, ADLAM, INDIC_SIYAQ_NUMBERS,
        OTTOMAN_SIYAQ_NUMBERS, ARABIC_MATHEMATICAL_ALPHABETIC_SYMBOLS, MAHJONG_TILES, DOMINO_TILES,
        PLAYING_CARDS, ENCLOSED_ALPHANUMERIC_SUPPLEMENT, ENCLOSED_IDEOGRAPHIC_SUPPLEMENT,
        MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS, EMOTICONS, ORNAMENTAL_DINGBATS, TRANSPORT_AND_MAP_SYMBOLS,
        ALCHEMICAL_SYMBOLS, GEOMETRIC_SHAPES_EXTENDED, SUPPLEMENTAL_ARROWS_C,
        SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS, CHESS_SYMBOLS, SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A,
        SYMBOLS_FOR_LEGACY_COMPUTING, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_I,
        CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_G,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_H, TAGS, VARIATION_SELECTORS_SUPPLEMENT,
        SUPPLEMENTARY_PRIVATE_USE_AREA_A, SUPPLEMENTARY_PRIVATE_USE_AREA_B
    };

    private static final int[] kStartValues = decodeAll(kStarts, 6, kBlocks.length);
    private static final int[] kEndValues = decodeAll(kEnds, 6, kBlocks.length);

    private static int[] decodeAll(String table, int width, int count) {
      char[] chars = table.toCharArray();
      int[] values = new int[count];
      for (int i = 0; i < count; i++) {
        values[i] = hex(chars, width * i, 6);
      }
      return values;
    }

    private static int hex(char[] chars, int offset, int digits) {
      int value = 0;
      for (int i = 0; i < digits; i++) {
        value = (value << 4) | java.lang.Character.digit(chars[offset + i], 16);
      }
      return value;
    }

    public static UnicodeBlock of(char c) {
      return of((int) c);
    }

    public static UnicodeBlock of(int codePoint) {
      if (!isValidCodePoint(codePoint)) {
        throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
      }
      int lo = 0;
      int hi = kBlocks.length - 1;
      while (lo <= hi) {
        int mid = (lo + hi) >>> 1;
        if (kStartValues[mid] > codePoint) {
          hi = mid - 1;
        } else if (kEndValues[mid] < codePoint) {
          lo = mid + 1;
        } else {
          return kBlocks[mid];
        }
      }
      return null;
    }

    public static final UnicodeBlock forName(String blockName) {
      UnicodeBlock block = map.get(blockName.toUpperCase(java.util.Locale.US));
      if (block == null) {
        throw new IllegalArgumentException("Not a valid block name: " + blockName);
      }
      return block;
    }
  }

  public static enum UnicodeScript {
    COMMON, LATIN, GREEK, CYRILLIC, ARMENIAN, HEBREW, ARABIC, SYRIAC, THAANA, DEVANAGARI, BENGALI, GURMUKHI,
    GUJARATI, ORIYA, TAMIL, TELUGU, KANNADA, MALAYALAM, SINHALA, THAI, LAO, TIBETAN, MYANMAR, GEORGIAN,
    HANGUL, ETHIOPIC, CHEROKEE, CANADIAN_ABORIGINAL, OGHAM, RUNIC, KHMER, MONGOLIAN, HIRAGANA, KATAKANA,
    BOPOMOFO, HAN, YI, OLD_ITALIC, GOTHIC, DESERET, INHERITED, TAGALOG, HANUNOO, BUHID, TAGBANWA, LIMBU,
    TAI_LE, LINEAR_B, UGARITIC, SHAVIAN, OSMANYA, CYPRIOT, BRAILLE, BUGINESE, COPTIC, NEW_TAI_LUE,
    GLAGOLITIC, TIFINAGH, SYLOTI_NAGRI, OLD_PERSIAN, KHAROSHTHI, BALINESE, CUNEIFORM, PHOENICIAN, PHAGS_PA,
    NKO, SUNDANESE, BATAK, LEPCHA, OL_CHIKI, VAI, SAURASHTRA, KAYAH_LI, REJANG, LYCIAN, CARIAN, LYDIAN, CHAM,
    TAI_THAM, TAI_VIET, AVESTAN, EGYPTIAN_HIEROGLYPHS, SAMARITAN, MANDAIC, LISU, BAMUM, JAVANESE,
    MEETEI_MAYEK, IMPERIAL_ARAMAIC, OLD_SOUTH_ARABIAN, INSCRIPTIONAL_PARTHIAN, INSCRIPTIONAL_PAHLAVI,
    OLD_TURKIC, BRAHMI, KAITHI, MEROITIC_HIEROGLYPHS, MEROITIC_CURSIVE, SORA_SOMPENG, CHAKMA, SHARADA, TAKRI,
    MIAO, CAUCASIAN_ALBANIAN, BASSA_VAH, DUPLOYAN, ELBASAN, GRANTHA, PAHAWH_HMONG, KHOJKI, LINEAR_A,
    MAHAJANI, MANICHAEAN, MENDE_KIKAKUI, MODI, MRO, OLD_NORTH_ARABIAN, NABATAEAN, PALMYRENE, PAU_CIN_HAU,
    OLD_PERMIC, PSALTER_PAHLAVI, SIDDHAM, KHUDAWADI, TIRHUTA, WARANG_CITI, AHOM, ANATOLIAN_HIEROGLYPHS,
    HATRAN, MULTANI, OLD_HUNGARIAN, SIGNWRITING, ADLAM, BHAIKSUKI, MARCHEN, NEWA, OSAGE, TANGUT,
    MASARAM_GONDI, NUSHU, SOYOMBO, ZANABAZAR_SQUARE, HANIFI_ROHINGYA, OLD_SOGDIAN, SOGDIAN, DOGRA,
    GUNJALA_GONDI, MAKASAR, MEDEFAIDRIN, ELYMAIC, NANDINAGARI, NYIAKENG_PUACHUE_HMONG, WANCHO, YEZIDI,
    CHORASMIAN, DIVES_AKURU, KHITAN_SMALL_SCRIPT, VITHKUQI, OLD_UYGHUR, CYPRO_MINOAN, TANGSA, TOTO, KAWI,
    NAG_MUNDARI, TODHRI, GARAY, TULU_TIGALARI, SUNUWAR, GURUNG_KHEMA, KIRAT_RAI, OL_ONAL, UNKNOWN;

    private static final String kStarts =
        "00000000000004100100005b00000006100100007b0000000aa0010000ab0000000ba0010000bb0000000c00010000d70000" +
        "000d80010000f70000000f80010002b90000002e00010002e50000002ea0220002ec00000030002800037000200037400000" +
        "03750020003780aa00037a00200037e00000037f0020003800aa000384002000385000000386002000387000000388002000" +
        "38b0aa00038c00200038d0aa00038e0020003a20aa0003a30020003e20360003f00020004000030004850280004870030005" +
        "300aa0005310040005570aa00055900400058b0aa00058d0040005900aa0005910050005c80aa0005d00050005eb0aa0005e" +
        "f0050005f50aa00060000600060500000060600600060c00000060d00600061b00000061c00600061f000000620006000640" +
        "00000064100600064b0280006560060006700280006710060006dd0000006de00600070000700070e0aa00070f00700074b0" +
        "aa00074d0070007500060007800080007b20aa0007c00410007fb0aa0007fd04100080005200082e0aa00083005200083f0a" +
        "a00084005300085c0aa00085e05300085f0aa00086000700086b0aa00087000600088f0aa0008900060008920aa000897006" +
        "0008e20000008e300600090000900095102800095500900096400000096600900098000a0009840aa00098500a00098d0aa0" +
        "0098f00a0009910aa00099300a0009a90aa0009aa00a0009b10aa0009b200a0009b30aa0009b600a0009ba0aa0009bc00a00" +
        "09c50aa0009c700a0009c90aa0009cb00a0009cf0aa0009d700a0009d80aa0009dc00a0009de0aa0009df00a0009e40aa000" +
        "9e600a0009ff0aa000a0100b000a040aa000a0500b000a0b0aa000a0f00b000a110aa000a1300b000a290aa000a2a00b000a" +
        "310aa000a3200b000a340aa000a3500b000a370aa000a3800b000a3a0aa000a3c00b000a3d0aa000a3e00b000a430aa000a4" +
        "700b000a490aa000a4b00b000a4e0aa000a5100b000a520aa000a5900b000a5d0aa000a5e00b000a5f0aa000a6600b000a77" +
        "0aa000a8100c000a840aa000a8500c000a8e0aa000a8f00c000a920aa000a9300c000aa90aa000aaa00c000ab10aa000ab20" +
        "0c000ab40aa000ab500c000aba0aa000abc00c000ac60aa000ac700c000aca0aa000acb00c000ace0aa000ad000c000ad10a" +
        "a000ae000c000ae40aa000ae600c000af20aa000af900c000b000aa000b0100d000b040aa000b0500d000b0d0aa000b0f00d" +
        "000b110aa000b1300d000b290aa000b2a00d000b310aa000b3200d000b340aa000b3500d000b3a0aa000b3c00d000b450aa0" +
        "00b4700d000b490aa000b4b00d000b4e0aa000b5500d000b580aa000b5c00d000b5e0aa000b5f00d000b640aa000b6600d00" +
        "0b780aa000b8200e000b840aa000b8500e000b8b0aa000b8e00e000b910aa000b9200e000b960aa000b9900e000b9b0aa000" +
        "b9c00e000b9d0aa000b9e00e000ba00aa000ba300e000ba50aa000ba800e000bab0aa000bae00e000bba0aa000bbe00e000b" +
        "c30aa000bc600e000bc90aa000bca00e000bce0aa000bd000e000bd10aa000bd700e000bd80aa000be600e000bfb0aa000c0" +
        "000f000c0d0aa000c0e00f000c110aa000c1200f000c290aa000c2a00f000c3a0aa000c3c00f000c450aa000c4600f000c49" +
        "0aa000c4a00f000c4e0aa000c5500f000c570aa000c5800f000c5b0aa000c5d00f000c5e0aa000c6000f000c640aa000c660" +
        "0f000c700aa000c7700f000c80010000c8d0aa000c8e010000c910aa000c92010000ca90aa000caa010000cb40aa000cb501" +
        "0000cba0aa000cbc010000cc50aa000cc6010000cc90aa000cca010000cce0aa000cd5010000cd70aa000cdd010000cdf0aa" +
        "000ce0010000ce40aa000ce6010000cf00aa000cf1010000cf40aa000d00011000d0d0aa000d0e011000d110aa000d120110" +
        "00d450aa000d46011000d490aa000d4a011000d500aa000d54011000d640aa000d66011000d800aa000d81012000d840aa00" +
        "0d85012000d970aa000d9a012000db20aa000db3012000dbc0aa000dbd012000dbe0aa000dc0012000dc70aa000dca012000" +
        "dcb0aa000dcf012000dd50aa000dd6012000dd70aa000dd8012000de00aa000de6012000df00aa000df2012000df50aa000e" +
        "01013000e3b0aa000e3f000000e40013000e5c0aa000e81014000e830aa000e84014000e850aa000e86014000e8b0aa000e8" +
        "c014000ea40aa000ea5014000ea60aa000ea7014000ebe0aa000ec0014000ec50aa000ec6014000ec70aa000ec8014000ecf" +
        "0aa000ed0014000eda0aa000edc014000ee00aa000f00015000f480aa000f49015000f6d0aa000f71015000f980aa000f990" +
        "15000fbd0aa000fbe015000fcd0aa000fce015000fd5000000fd9015000fdb0aa0010000160010a00170010c60aa0010c701" +
        "70010c80aa0010cd0170010ce0aa0010d00170010fb0000010fc0170011000180012000190012490aa00124a01900124e0aa" +
        "0012500190012570aa0012580190012590aa00125a01900125e0aa0012600190012890aa00128a01900128e0aa0012900190" +
        "012b10aa0012b20190012b60aa0012b80190012bf0aa0012c00190012c10aa0012c20190012c60aa0012c80190012d70aa00" +
        "12d80190013110aa0013120190013160aa00131801900135b0aa00135d01900137d0aa00138001900139a0aa0013a001a001" +
        "3f60aa0013f801a0013fe0aa00140001b00168001c00169d0aa0016a001d0016eb0000016ee01d0016f90aa0017000290017" +
        "160aa00171f02900172002a0017350000017370aa00174002b0017540aa00176002c00176d0aa00176e02c0017710aa00177" +
        "202c0017740aa00178001e0017de0aa0017e001e0017ea0aa0017f001e0017fa0aa00180001f00180200000180401f001805" +
        "00000180601f00181a0aa00182001f0018790aa00188001f0018ab0aa0018b001b0018f60aa00190002d00191f0aa0019200" +
        "2d00192c0aa00193002d00193c0aa00194002d0019410aa00194402d00195002e00196e0aa00197002e0019750aa00198003" +
        "70019ac0aa0019b00370019ca0aa0019d00370019db0aa0019de0370019e001e001a00035001a1c0aa001a1e035001a2004e" +
        "001a5f0aa001a6004e001a7d0aa001a7f04e001a8a0aa001a9004e001a9a0aa001aa004e001aae0aa001ab0028001acf0aa0" +
        "01b0003d001b4d0aa001b4e03d001b80042001bc0043001bf40aa001bfc043001c00044001c380aa001c3b044001c4a0aa00" +
        "1c4d044001c50045001c80003001c8b0aa001c90017001cbb0aa001cbd017001cc0042001cc80aa001cd0028001cd3000001" +
        "cd4028001ce1000001ce2028001ce9000001ced028001cee000001cf4028001cf5000001cf8028001cfa000001cfb0aa001d" +
        "00001001d26002001d2b003001d2c001001d5d002001d62001001d66002001d6b001001d78003001d79001001dbf002001dc" +
        "0028001e00001001f00002001f160aa001f18002001f1e0aa001f20002001f460aa001f48002001f4e0aa001f50002001f58" +
        "0aa001f59002001f5a0aa001f5b002001f5c0aa001f5d002001f5e0aa001f5f002001f7e0aa001f80002001fb50aa001fb60" +
        "02001fc50aa001fc6002001fd40aa001fd6002001fdc0aa001fdd002001ff00aa001ff2002001ff50aa001ff6002001fff0a" +
        "a00200000000200c02800200e0000020650aa0020660000020710010020720aa00207400000207f00100208000000208f0aa" +
        "00209000100209d0aa0020a00000020c10aa0020d00280020f10aa00210000000212600200212700000212a00100212c0000" +
        "0213200100213300000214e00100214f00000216000100218900000218c0aa00219000000242a0aa00244000000244b0aa00" +
        "2460000002800034002900000002b740aa002b76000002b960aa002b97000002c00038002c60001002c80036002cf40aa002" +
        "cf9036002d00017002d260aa002d27017002d280aa002d2d017002d2e0aa002d30039002d680aa002d6f039002d710aa002d" +
        "7f039002d80019002d970aa002da0019002da70aa002da8019002daf0aa002db0019002db70aa002db8019002dbf0aa002dc" +
        "0019002dc70aa002dc8019002dcf0aa002dd0019002dd70aa002dd8019002ddf0aa002de0003002e00000002e5e0aa002e80" +
        "023002e9a0aa002e9b023002ef40aa002f00023002fd60aa002ff00000030050230030060000030070230030080000030210" +
        "2300302a02800302e01800303000000303802300303c0000030400aa0030410200030970aa00309902800309b00000309d02" +
        "00030a00000030a10210030fb0000030fd0210031000aa0031050220031300aa00313101800318f0aa0031900000031a0022" +
        "0031c00000031e60aa0031ef0000031f002100320001800321f0aa00322000000326001800327f0000032d00210032ff0000" +
        "03300021003358000003400023004dc0000004e0002300a00002400a48d0aa00a49002400a4c70aa00a4d005400a50004600" +
        "a62c0aa00a64000300a6a005500a6f80aa00a70000000a72200100a78800000a78b00100a7ce0aa00a7d000100a7d20aa00a" +
        "7d300100a7d40aa00a7d500100a7dd0aa00a7f200100a80003a00a82d0aa00a83000000a83a0aa00a84004000a8780aa00a8" +
        "8004700a8c60aa00a8ce04700a8da0aa00a8e000900a90004800a92e00000a92f04800a93004900a9540aa00a95f04900a96" +
        "001800a97d0aa00a98005600a9ce0aa00a9cf00000a9d005600a9da0aa00a9de05600a9e001600a9ff0aa00aa0004d00aa37" +
        "0aa00aa4004d00aa4e0aa00aa5004d00aa5a0aa00aa5c04d00aa6001600aa8004f00aac30aa00aadb04f00aae005700aaf70" +
        "aa00ab0101900ab070aa00ab0901900ab0f0aa00ab1101900ab170aa00ab2001900ab270aa00ab2801900ab2f0aa00ab3000" +
        "100ab5b00000ab5c00100ab6500200ab6600100ab6a00000ab6c0aa00ab7001a00abc005700abee0aa00abf005700abfa0aa" +
        "00ac0001800d7a40aa00d7b001800d7c70aa00d7cb01800d7fc0aa00f90002300fa6e0aa00fa7002300fada0aa00fb000010" +
        "0fb070aa00fb1300400fb180aa00fb1d00500fb370aa00fb3800500fb3d0aa00fb3e00500fb3f0aa00fb4000500fb420aa00" +
        "fb4300500fb450aa00fb4600500fb5000600fbc30aa00fbd300600fd3e00000fd4000600fd900aa00fd9200600fdc80aa00f" +
        "dcf00600fdd00aa00fdf000600fe0002800fe1000000fe1a0aa00fe2002800fe2e00300fe3000000fe530aa00fe5400000fe" +
        "670aa00fe6800000fe6c0aa00fe7000600fe750aa00fe7600600fefd0aa00feff00000ff000aa00ff0100000ff2100100ff3" +
        "b00000ff4100100ff5b00000ff6602100ff7000000ff7102100ff9e00000ffa001800ffbf0aa00ffc201800ffc80aa00ffca" +
        "01800ffd00aa00ffd201800ffd80aa00ffda01800ffdd0aa00ffe000000ffe70aa00ffe800000ffef0aa00fff900000fffe0" +
        "aa01000002f01000c0aa01000d02f0100270aa01002802f01003b0aa01003c02f01003e0aa01003f02f01004e0aa01005002" +
        "f01005e0aa01008002f0100fb0aa0101000000101030aa0101070000101340aa01013700001014000201018f0aa010190000" +
        "01019d0aa0101a00020101a10aa0101d00000101fd0280101fe0aa01028004a01029d0aa0102a004b0102d10aa0102e00280" +
        "102e10000102fc0aa0103000250103240aa01032d02501033002601034b0aa01035007701037b0aa01038003001039e0aa01" +
        "039f0300103a003b0103c40aa0103c803b0103d60aa01040002701045003101048003201049e0aa0104a00320104aa0aa010" +
        "4b00870104d40aa0104d80870104fc0aa0105000690105280aa0105300660105640aa01056f06601057009c01057b0aa0105" +
        "7c09c01058b0aa01058c09c0105930aa01059409c0105960aa01059709c0105a20aa0105a309c0105b20aa0105b309c0105b" +
        "a0aa0105bb09c0105bd0aa0105c00a30105f40aa01060006d0107370aa01074006d0107560aa01076006d0107680aa010780" +
        "0010107860aa0107870010107b10aa0107b20010107bb0aa0108000330108060aa0108080330108090aa01080a0330108360" +
        "aa0108370330108390aa01083c03301083d0aa01083f0330108400580108560aa01085705801086007501088007401089f0a" +
        "a0108a70740108b00aa0108e007f0108f30aa0108f407f0108f60aa0108fb07f01090003f01091c0aa01091f03f01092004c" +
        "01093a0aa01093f04c0109400aa01098005f0109a00600109b80aa0109bc0600109d00aa0109d2060010a0003c010a040aa0" +
        "10a0503c010a070aa010a0c03c010a140aa010a1503c010a180aa010a1903c010a360aa010a3803c010a3b0aa010a3f03c01" +
        "0a490aa010a5003c010a590aa010a60059010a80073010aa00aa010ac006f010ae70aa010aeb06f010af70aa010b00050010" +
        "b360aa010b39050010b4005a010b560aa010b5805a010b6005b010b730aa010b7805b010b80078010b920aa010b99078010b" +
        "9d0aa010ba9078010bb00aa010c0005c010c490aa010c80081010cb30aa010cc0081010cf30aa010cfa081010d0008d010d2" +
        "80aa010d3008d010d3a0aa010d400a4010d660aa010d690a4010d860aa010d8e0a4010d900aa010e60006010e7f0aa010e80" +
        "098010eaa0aa010eab098010eae0aa010eb0098010eb20aa010ec2006010ec50aa010efc006010f0008e010f280aa010f300" +
        "8f010f5a0aa010f7009d010f8a0aa010fb0099010fcc0aa010fe0094010ff70aa01100005d01104e0aa01105205d0110760a" +
        "a01107f05d01108005e0110c30aa0110cd05e0110ce0aa0110d00610110e90aa0110f00610110fa0aa0111000620111350aa" +
        "0111360620111480aa01115006e0111770aa0111800630111e00aa0111e10120111f50aa01120006c0112120aa01121306c0" +
        "112420aa0112800800112870aa0112880800112890aa01128a08001128e0aa01128f08001129e0aa01129f0800112aa0aa01" +
        "12b007a0112eb0aa0112f007a0112fa0aa01130006a0113040aa01130506a01130d0aa01130f06a0113110aa01131306a011" +
        "3290aa01132a06a0113310aa01133206a0113340aa01133506a01133a0aa01133b02801133c06a0113450aa01134706a0113" +
        "490aa01134b06a01134e0aa01135006a0113510aa01135706a0113580aa01135d06a0113640aa01136606a01136d0aa01137" +
        "006a0113750aa0113800a501138a0aa01138b0a501138c0aa01138e0a501138f0aa0113900a50113b60aa0113b70a50113c1" +
        "0aa0113c20a50113c30aa0113c50a50113c60aa0113c70a50113cb0aa0113cc0a50113d60aa0113d70a50113d90aa0113e10" +
        "a50113e30aa01140008601145c0aa01145d0860114620aa01148007b0114c80aa0114d007b0114da0aa0115800790115b60a" +
        "a0115b80790115de0aa0116000710116450aa01165007101165a0aa01166001f01166d0aa0116800640116ba0aa0116c0064" +
        "0116ca0aa0116d00160116e40aa01170007d01171b0aa01171d07d01172c0aa01173007d0117470aa01180009001183c0aa0" +
        "118a007c0118f30aa0118ff07c01190009a0119070aa01190909a01190a0aa01190c09a0119140aa01191509a0119170aa01" +
        "191809a0119360aa01193709a0119390aa01193b09a0119470aa01195009a01195a0aa0119a00950119a80aa0119aa095011" +
        "9d80aa0119da0950119e50aa011a0008c011a480aa011a5008b011aa30aa011ab001b011ac0076011af90aa011b00009011b" +
        "0a0aa011bc00a6011be20aa011bf00a6011bfa0aa011c00084011c090aa011c0a084011c370aa011c38084011c460aa011c5" +
        "0084011c6d0aa011c70085011c900aa011c92085011ca80aa011ca9085011cb70aa011d00089011d070aa011d08089011d0a" +
        "0aa011d0b089011d370aa011d3a089011d3b0aa011d3c089011d3e0aa011d3f089011d480aa011d50089011d5a0aa011d600" +
        "91011d660aa011d67091011d690aa011d6a091011d8f0aa011d90091011d920aa011d93091011d990aa011da0091011daa0a" +
        "a011ee0092011ef90aa011f000a1011f110aa011f120a1011f3b0aa011f3e0a1011f5b0aa011fb0054011fb10aa011fc000e" +
        "011ff20aa011fff00e01200003e01239a0aa01240003e01246f0aa01247003e0124750aa01248003e0125440aa012f9009e0" +
        "12ff30aa0130000510134560aa0134600510143fb0aa01440007e0146470aa0161000a701613a0aa016800055016a390aa01" +
        "6a40072016a5f0aa016a60072016a6a0aa016a6e072016a7009f016abf0aa016ac009f016aca0aa016ad0067016aee0aa016" +
        "af0067016af60aa016b0006b016b460aa016b5006b016b5a0aa016b5b06b016b620aa016b6306b016b780aa016b7d06b016b" +
        "900aa016d400a8016d7a0aa016e40093016e9b0aa016f00065016f4b0aa016f4f065016f880aa016f8f065016fa00aa016fe" +
        "0088016fe108a016fe2023016fe409b016fe50aa016ff0023016ff20aa0170000880187f80aa018800088018b0009b018cd6" +
        "0aa018cff09b018d00088018d090aa01aff002101aff40aa01aff502101affc0aa01affd02101afff0aa01b00002101b0010" +
        "2001b12002101b1230aa01b13202001b1330aa01b15002001b1530aa01b15502101b1560aa01b16402101b1680aa01b17008" +
        "a01b2fc0aa01bc0006801bc6b0aa01bc7006801bc7d0aa01bc8006801bc890aa01bc9006801bc9a0aa01bc9c06801bca0000" +
        "01bca40aa01cc0000001ccfa0aa01cd0000001ceb40aa01cf0002801cf2e0aa01cf3002801cf470aa01cf5000001cfc40aa0" +
        "1d00000001d0f60aa01d10000001d1270aa01d12900001d16702801d16a00001d17b02801d18300001d18502801d18c00001" +
        "d1aa02801d1ae00001d1eb0aa01d20000201d2460aa01d2c000001d2d40aa01d2e000001d2f40aa01d30000001d3570aa01d" +
        "36000001d3790aa01d40000001d4550aa01d45600001d49d0aa01d49e00001d4a00aa01d4a200001d4a30aa01d4a500001d4" +
        "a70aa01d4a900001d4ad0aa01d4ae00001d4ba0aa01d4bb00001d4bc0aa01d4bd00001d4c40aa01d4c500001d5060aa01d50" +
        "700001d50b0aa01d50d00001d5150aa01d51600001d51d0aa01d51e00001d53a0aa01d53b00001d53f0aa01d54000001d545" +
        "0aa01d54600001d5470aa01d54a00001d5510aa01d55200001d6a60aa01d6a800001d7cc0aa01d7ce00001d80008201da8c0" +
        "aa01da9b08201daa00aa01daa108201dab00aa01df0000101df1f0aa01df2500101df2b0aa01e00003801e0070aa01e00803" +
        "801e0190aa01e01b03801e0220aa01e02303801e0250aa01e02603801e02b0aa01e03000301e06e0aa01e08f00301e0900aa" +
        "01e10009601e12d0aa01e13009601e13e0aa01e14009601e14a0aa01e14e09601e1500aa01e2900a001e2af0aa01e2c00970" +
        "1e2fa0aa01e2ff09701e3000aa01e4d00a201e4fa0aa01e5d00a901e5fb0aa01e5ff0a901e6000aa01e7e001901e7e70aa01" +
        "e7e801901e7ec0aa01e7ed01901e7ef0aa01e7f001901e7ff0aa01e80007001e8c50aa01e8c707001e8d70aa01e90008301e" +
        "94c0aa01e95008301e95a0aa01e95e08301e9600aa01ec7100001ecb50aa01ed0100001ed3e0aa01ee0000601ee040aa01ee" +
        "0500601ee200aa01ee2100601ee230aa01ee2400601ee250aa01ee2700601ee280aa01ee2900601ee330aa01ee3400601ee3" +
        "80aa01ee3900601ee3a0aa01ee3b00601ee3c0aa01ee4200601ee430aa01ee4700601ee480aa01ee4900601ee4a0aa01ee4b" +
        "00601ee4c0aa01ee4d00601ee500aa01ee5100601ee530aa01ee5400601ee550aa01ee5700601ee580aa01ee5900601ee5a0" +
        "aa01ee5b00601ee5c0aa01ee5d00601ee5e0aa01ee5f00601ee600aa01ee6100601ee630aa01ee6400601ee650aa01ee6700" +
        "601ee6b0aa01ee6c00601ee730aa01ee7400601ee780aa01ee7900601ee7d0aa01ee7e00601ee7f0aa01ee8000601ee8a0aa" +
        "01ee8b00601ee9c0aa01eea100601eea40aa01eea500601eeaa0aa01eeab00601eebc0aa01eef000601eef20aa01f0000000" +
        "1f02c0aa01f03000001f0940aa01f0a000001f0af0aa01f0b100001f0c00aa01f0c100001f0d00aa01f0d100001f0f60aa01" +
        "f10000001f1ae0aa01f1e600001f20002001f20100001f2030aa01f21000001f23c0aa01f24000001f2490aa01f25000001f" +
        "2520aa01f26000001f2660aa01f30000001f6d80aa01f6dc00001f6ed0aa01f6f000001f6fd0aa01f70000001f7770aa01f7" +
        "7b00001f7da0aa01f7e000001f7ec0aa01f7f000001f7f10aa01f80000001f80c0aa01f81000001f8480aa01f85000001f85" +
        "a0aa01f86000001f8880aa01f89000001f8ae0aa01f8b000001f8bc0aa01f8c000001f8c20aa01f90000001fa540aa01fa60" +
        "00001fa6e0aa01fa7000001fa7d0aa01fa8000001fa8a0aa01fa8f00001fac70aa01face00001fadd0aa01fadf00001faea0" +
        "aa01faf000001faf90aa01fb0000001fb930aa01fb9400001fbfa0aa02000002302a6e00aa02a70002302b73a0aa02b74002" +
        "302b81e0aa02b82002302cea20aa02ceb002302ebe10aa02ebf002302ee5e0aa02f80002302fa1e0aa03000002303134b0aa" +
        "0313500230323b00aa0e00010000e00020aa0e00200000e00800aa0e01000280e01f00aa";
    private static final java.util.HashMap<String, UnicodeScript> aliases =
        new java.util.HashMap<String, UnicodeScript>(512);

    static {
      aliases.put("ZYYY", COMMON);
      aliases.put("LATN", LATIN);
      aliases.put("GREK", GREEK);
      aliases.put("CYRL", CYRILLIC);
      aliases.put("ARMN", ARMENIAN);
      aliases.put("HEBR", HEBREW);
      aliases.put("ARAB", ARABIC);
      aliases.put("SYRC", SYRIAC);
      aliases.put("THAA", THAANA);
      aliases.put("DEVA", DEVANAGARI);
      aliases.put("BENG", BENGALI);
      aliases.put("GURU", GURMUKHI);
      aliases.put("GUJR", GUJARATI);
      aliases.put("ORYA", ORIYA);
      aliases.put("TAML", TAMIL);
      aliases.put("TELU", TELUGU);
      aliases.put("KNDA", KANNADA);
      aliases.put("MLYM", MALAYALAM);
      aliases.put("SINH", SINHALA);
      aliases.put("THAI", THAI);
      aliases.put("LAOO", LAO);
      aliases.put("TIBT", TIBETAN);
      aliases.put("MYMR", MYANMAR);
      aliases.put("GEOR", GEORGIAN);
      aliases.put("HANG", HANGUL);
      aliases.put("ETHI", ETHIOPIC);
      aliases.put("CHER", CHEROKEE);
      aliases.put("CANS", CANADIAN_ABORIGINAL);
      aliases.put("OGAM", OGHAM);
      aliases.put("RUNR", RUNIC);
      aliases.put("KHMR", KHMER);
      aliases.put("MONG", MONGOLIAN);
      aliases.put("HIRA", HIRAGANA);
      aliases.put("KANA", KATAKANA);
      aliases.put("BOPO", BOPOMOFO);
      aliases.put("HANI", HAN);
      aliases.put("YIII", YI);
      aliases.put("ITAL", OLD_ITALIC);
      aliases.put("GOTH", GOTHIC);
      aliases.put("DSRT", DESERET);
      aliases.put("ZINH", INHERITED);
      aliases.put("TGLG", TAGALOG);
      aliases.put("HANO", HANUNOO);
      aliases.put("BUHD", BUHID);
      aliases.put("TAGB", TAGBANWA);
      aliases.put("LIMB", LIMBU);
      aliases.put("TALE", TAI_LE);
      aliases.put("LINB", LINEAR_B);
      aliases.put("UGAR", UGARITIC);
      aliases.put("SHAW", SHAVIAN);
      aliases.put("OSMA", OSMANYA);
      aliases.put("CPRT", CYPRIOT);
      aliases.put("BRAI", BRAILLE);
      aliases.put("BUGI", BUGINESE);
      aliases.put("COPT", COPTIC);
      aliases.put("TALU", NEW_TAI_LUE);
      aliases.put("GLAG", GLAGOLITIC);
      aliases.put("TFNG", TIFINAGH);
      aliases.put("SYLO", SYLOTI_NAGRI);
      aliases.put("XPEO", OLD_PERSIAN);
      aliases.put("KHAR", KHAROSHTHI);
      aliases.put("BALI", BALINESE);
      aliases.put("XSUX", CUNEIFORM);
      aliases.put("PHNX", PHOENICIAN);
      aliases.put("PHAG", PHAGS_PA);
      aliases.put("NKOO", NKO);
      aliases.put("SUND", SUNDANESE);
      aliases.put("BATK", BATAK);
      aliases.put("LEPC", LEPCHA);
      aliases.put("OLCK", OL_CHIKI);
      aliases.put("VAII", VAI);
      aliases.put("SAUR", SAURASHTRA);
      aliases.put("KALI", KAYAH_LI);
      aliases.put("RJNG", REJANG);
      aliases.put("LYCI", LYCIAN);
      aliases.put("CARI", CARIAN);
      aliases.put("LYDI", LYDIAN);
      aliases.put("CHAM", CHAM);
      aliases.put("LANA", TAI_THAM);
      aliases.put("TAVT", TAI_VIET);
      aliases.put("AVST", AVESTAN);
      aliases.put("EGYP", EGYPTIAN_HIEROGLYPHS);
      aliases.put("SAMR", SAMARITAN);
      aliases.put("MAND", MANDAIC);
      aliases.put("LISU", LISU);
      aliases.put("BAMU", BAMUM);
      aliases.put("JAVA", JAVANESE);
      aliases.put("MTEI", MEETEI_MAYEK);
      aliases.put("ARMI", IMPERIAL_ARAMAIC);
      aliases.put("SARB", OLD_SOUTH_ARABIAN);
      aliases.put("PRTI", INSCRIPTIONAL_PARTHIAN);
      aliases.put("PHLI", INSCRIPTIONAL_PAHLAVI);
      aliases.put("ORKH", OLD_TURKIC);
      aliases.put("BRAH", BRAHMI);
      aliases.put("KTHI", KAITHI);
      aliases.put("MERO", MEROITIC_HIEROGLYPHS);
      aliases.put("MERC", MEROITIC_CURSIVE);
      aliases.put("SORA", SORA_SOMPENG);
      aliases.put("CAKM", CHAKMA);
      aliases.put("SHRD", SHARADA);
      aliases.put("TAKR", TAKRI);
      aliases.put("PLRD", MIAO);
      aliases.put("AGHB", CAUCASIAN_ALBANIAN);
      aliases.put("BASS", BASSA_VAH);
      aliases.put("DUPL", DUPLOYAN);
      aliases.put("ELBA", ELBASAN);
      aliases.put("GRAN", GRANTHA);
      aliases.put("HMNG", PAHAWH_HMONG);
      aliases.put("KHOJ", KHOJKI);
      aliases.put("LINA", LINEAR_A);
      aliases.put("MAHJ", MAHAJANI);
      aliases.put("MANI", MANICHAEAN);
      aliases.put("MEND", MENDE_KIKAKUI);
      aliases.put("MODI", MODI);
      aliases.put("MROO", MRO);
      aliases.put("NARB", OLD_NORTH_ARABIAN);
      aliases.put("NBAT", NABATAEAN);
      aliases.put("PALM", PALMYRENE);
      aliases.put("PAUC", PAU_CIN_HAU);
      aliases.put("PERM", OLD_PERMIC);
      aliases.put("PHLP", PSALTER_PAHLAVI);
      aliases.put("SIDD", SIDDHAM);
      aliases.put("SIND", KHUDAWADI);
      aliases.put("TIRH", TIRHUTA);
      aliases.put("WARA", WARANG_CITI);
      aliases.put("AHOM", AHOM);
      aliases.put("HLUW", ANATOLIAN_HIEROGLYPHS);
      aliases.put("HATR", HATRAN);
      aliases.put("MULT", MULTANI);
      aliases.put("HUNG", OLD_HUNGARIAN);
      aliases.put("SGNW", SIGNWRITING);
      aliases.put("ADLM", ADLAM);
      aliases.put("BHKS", BHAIKSUKI);
      aliases.put("MARC", MARCHEN);
      aliases.put("NEWA", NEWA);
      aliases.put("OSGE", OSAGE);
      aliases.put("TANG", TANGUT);
      aliases.put("GONM", MASARAM_GONDI);
      aliases.put("NSHU", NUSHU);
      aliases.put("SOYO", SOYOMBO);
      aliases.put("ZANB", ZANABAZAR_SQUARE);
      aliases.put("ROHG", HANIFI_ROHINGYA);
      aliases.put("SOGO", OLD_SOGDIAN);
      aliases.put("SOGD", SOGDIAN);
      aliases.put("DOGR", DOGRA);
      aliases.put("GONG", GUNJALA_GONDI);
      aliases.put("MAKA", MAKASAR);
      aliases.put("MEDF", MEDEFAIDRIN);
      aliases.put("ELYM", ELYMAIC);
      aliases.put("NAND", NANDINAGARI);
      aliases.put("HMNP", NYIAKENG_PUACHUE_HMONG);
      aliases.put("WCHO", WANCHO);
      aliases.put("YEZI", YEZIDI);
      aliases.put("CHRS", CHORASMIAN);
      aliases.put("DIAK", DIVES_AKURU);
      aliases.put("KITS", KHITAN_SMALL_SCRIPT);
      aliases.put("VITH", VITHKUQI);
      aliases.put("OUGR", OLD_UYGHUR);
      aliases.put("CPMN", CYPRO_MINOAN);
      aliases.put("TNSA", TANGSA);
      aliases.put("TOTO", TOTO);
      aliases.put("KAWI", KAWI);
      aliases.put("NAGM", NAG_MUNDARI);
      aliases.put("TODR", TODHRI);
      aliases.put("GARA", GARAY);
      aliases.put("TUTG", TULU_TIGALARI);
      aliases.put("SUNU", SUNUWAR);
      aliases.put("GUKH", GURUNG_KHEMA);
      aliases.put("KRAI", KIRAT_RAI);
      aliases.put("ONAO", OL_ONAL);
      aliases.put("ZZZZ", UNKNOWN);
    }

    private static final int[] kStartValues = new int[1708];
    private static final int[] kScriptIndex = new int[1708];
    private static final UnicodeScript[] kValues = values();

    static {
      char[] chars = kStarts.toCharArray();
      for (int i = 0; i < kStartValues.length; i++) {
        kStartValues[i] = hex(chars, 9 * i, 6);
        kScriptIndex[i] = hex(chars, 9 * i + 6, 3);
      }
    }

    private static int hex(char[] chars, int offset, int digits) {
      int value = 0;
      for (int i = 0; i < digits; i++) {
        value = (value << 4) | Character.digit(chars[offset + i], 16);
      }
      return value;
    }

    public static UnicodeScript of(int codePoint) {
      if (!isValidCodePoint(codePoint)) {
        throw new IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", codePoint));
      }
      int lo = 0;
      int hi = kStartValues.length - 1;
      while (lo < hi) {
        int mid = (lo + hi + 1) >>> 1;
        if (kStartValues[mid] <= codePoint) {
          lo = mid;
        } else {
          hi = mid - 1;
        }
      }
      return kValues[kScriptIndex[lo]];
    }

    public static final UnicodeScript forName(String scriptName) {
      scriptName = scriptName.toUpperCase(java.util.Locale.ENGLISH);
      UnicodeScript sc = aliases.get(scriptName);
      if (sc != null) {
        return sc;
      }
      return valueOf(scriptName);
    }
  }
}
