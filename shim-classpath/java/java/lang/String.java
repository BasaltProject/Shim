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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;

import shim.Iso88591;
import shim.Utf8;

public final class String
  implements Comparable<String>, CharSequence, Serializable,
             java.lang.constant.Constable, java.lang.constant.ConstantDesc
{
  private static final long serialVersionUID = -6849794470754667710L;
  private static final java.io.ObjectStreamField[] serialPersistentFields = new java.io.ObjectStreamField[0];
  private static final String UTF_8_ENCODING = "UTF-8";
  private static final String ISO_8859_1_ENCODING = "ISO-8859-1";
  private static final String LATIN_1_ENCODING = "LATIN-1";
  private static final String DEFAULT_ENCODING = UTF_8_ENCODING;

  public static final Comparator<String> CASE_INSENSITIVE_ORDER
    = new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
      return a.compareToIgnoreCase(b);
    }
  };

  private final Object data;
  private final int offset;
  private final int length;
  private int hashCode;

  private static StringIndexOutOfBoundsException indexError(int index, int length) {
    return new StringIndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
  }

  private static void checkFromToIndex(int from, int to, int length) {
    if (from < 0 || from > to || to > length) {
      throw new StringIndexOutOfBoundsException("Range [" + from + ", " + to + ") out of bounds for length "
                                                + length);
    }
  }

  private static void checkFromIndexSize(int from, int size, int length) {
    if ((length | from | size) < 0 || size > length - from) {
      throw new StringIndexOutOfBoundsException("Range [" + from + ", " + from + " + " + size
                                                + ") out of bounds for length " + length);
    }
  }

  public String() {
    this(new char[0], 0, 0);
  }

  String(char[] data, int offset, int length, boolean copy) {
    this((Object) data, offset, length, copy);
  }

  public String(char[] data, int offset, int length) {
    this(data, offset, length, true);
  }

  public String(char[] data) {
    this(data, 0, data.length);
  }

  public String(byte[] bytes, int offset, int length, String charsetName)
    throws UnsupportedEncodingException
  {
    this(decodeBytes(bytes, offset, length, sun.nio.cs.StringCoding.lookup(charsetName)));
  }

  String(byte[] data, int offset, int length, boolean copy) {
    this((Object) data, offset, length, copy);
  }

  public String(byte[] data, int offset, int length) {
    this(decodeBytes(data, offset, length, sun.nio.cs.UTF_8.INSTANCE));
  }

  public String(StringBuilder builder) {
    this(builder.toString());
  }

  public String(StringBuffer buffer) {
    this(buffer.toString());
  }

  public String(int[] codePoints, int offset, int count) {
    this(codePointChars(codePoints, offset, count));
  }

  @Deprecated
  public String(byte[] ascii, int hibyte) {
    this(ascii, hibyte, 0, ascii.length);
  }

  private static char[] codePointChars(int[] codePoints, int offset, int count) {
    if (offset < 0 || count < 0 || offset > codePoints.length - count) {
      throw new StringIndexOutOfBoundsException("offset " + offset + ", count " + count + ", length "
                                                + codePoints.length);
    }
    int length = 0;
    for (int i = offset; i < offset + count; ++i) {
      int c = codePoints[i];
      if (!Character.isValidCodePoint(c)) throw new IllegalArgumentException(Integer.toString(c));
      length += Character.charCount(c);
    }
    char[] result = new char[length];
    int position = 0;
    for (int i = offset; i < offset + count; ++i) {
      position += Character.toChars(codePoints[i], result, position);
    }
    return result;
  }

  public String(byte[] data) {
    this(data, 0, data.length);
  }

  public String(String s) {
    this(s.toCharArray());
  }

  // The source is already decoded and immutable. A slice must not scan its entire
  // backing array again as the raw-byte constructor does to recognize UTF-8.
  // Bounds are checked by substring before entering this trusted storage path.
  private String(String source, int start, int length) {
    this.data = source.data;
    this.offset = source.offset + start;
    this.length = length;
  }

  public String(byte[] data, String charset)
    throws UnsupportedEncodingException
  {
    this(data, 0, data.length, charset);
  }

  public String(byte[] bytes, int offset, int length, java.nio.charset.Charset charset) {
    this(decodeBytes(bytes, offset, length, charset));
  }

  public String(byte[] bytes, java.nio.charset.Charset charset) {
    this(bytes, 0, bytes.length, charset);
  }

  private static char[] decodeBytes(byte[] bytes, int offset, int length, java.nio.charset.Charset charset) {
    if (charset == null) throw new NullPointerException("charset");
    if (bytes == null) throw new NullPointerException();
    if (offset < 0 || length < 0 || offset > bytes.length - length) {
      throw new StringIndexOutOfBoundsException("Range [" + offset + ", " + offset + " + " + length
                                                + ") out of bounds for length " + bytes.length);
    }
    if (charset == sun.nio.cs.UTF_8.INSTANCE || charset == sun.nio.cs.US_ASCII.INSTANCE) {
      char[] ascii = decodeAsciiBytes(bytes, offset, length);
      if (ascii != null) return ascii;
    }
    return sun.nio.cs.StringCoding.decode(charset, bytes, offset, length);
  }

  public String(byte bytes[], int highByte, int offset, int length) {
    checkFromIndexSize(offset, length, bytes.length);

    char[] c = new char[length];
    int mask = highByte << 8;
    for (int i = 0; i < length; ++i) {
      c[i] = (char) ((bytes[offset + i] & 0xFF) | mask);
    }

    this.data = c;
    this.offset = 0;
    this.length = length;
  }

  private String(Object data, int offset, int length, boolean copy) {
    int l;
    if (data instanceof char[]) {
      l = ((char[]) data).length;
    } else {
      l = ((byte[]) data).length;
    }

    checkFromIndexSize(offset, length, l);

    if(!copy && Utf8.test(data)) copy = true;

    if (copy) {
      Object c;
      if (data instanceof char[]) {
        c = copyCharStorage((char[]) data, offset, length);
      } else {
        c = Utf8.decode((byte[])data, offset, length);
        if(c instanceof char[]) length = ((char[])c).length;
        if (c == null) {
          throw new RuntimeException
            ("unable to parse \"" + new String(data, offset, length, false)
             + "\"");
        }
      }

      this.data = c;
      this.offset = 0;
      this.length = length;
    } else {
      this.data = data;
      this.offset = offset;
      this.length = length;
    }
  }

  // Private storage helpers: callers validate ranges before entering the VM.
  private static native char[] decodeAsciiBytes(byte[] source, int offset, int length);
  private static native Object copyCharStorage(char[] source, int offset, int length);
  private static native void widenBytes(byte[] source, int offset, char[] target, int targetOffset, int length);
  private native int indexOfString(String target, int start);
  private native int indexOfCodePoint(int codePoint, int start);
  private native int hashStorage();
  private native int compareStorage(String target);

  @Override
  public String toString() {
    return this;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = hashStorage();
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof String) {
      String s = (String) o;
      return s.length == length && compareTo(s) == 0;
    } else {
      return false;
    }
  }

  public boolean equalsIgnoreCase(String s) {
    if (this == s) {
      return true;
    } else {
      return s != null && s.length == length && compareToIgnoreCase(s) == 0;
    }
  }

  @Override
  public int compareTo(String s) {
    if (this == s) return 0;

    if (s == null) throw new NullPointerException();
    return compareStorage(s);
  }

  private boolean isLatin1() {
    for (int i = 0; i < length; ++i) {
      if (charAt(i) > 0xFF) return false;
    }
    return true;
  }

  public int compareToIgnoreCase(String s) {
    if (this == s) return 0;
    boolean byCodePoint = !isLatin1() && !s.isLatin1();
    int i = 0;
    int j = 0;
    while (i < length && j < s.length) {
      int c1;
      int c2;
      if (byCodePoint) {
        c1 = codePointAt(i);
        c2 = s.codePointAt(j);
        i += Character.charCount(c1);
        j += Character.charCount(c2);
      } else {
        c1 = charAt(i++);
        c2 = s.charAt(j++);
      }
      if (c1 != c2) {
        c1 = Character.toUpperCase(c1);
        c2 = Character.toUpperCase(c2);
        if (c1 != c2) {
          c1 = Character.toLowerCase(c1);
          c2 = Character.toLowerCase(c2);
          if (c1 != c2) {
            return c1 - c2;
          }
        }
      }
    }
    return (length - i) - (s.length - j);
  }

  public String trim() {
    int start = 0;
    int end = length;
    while (start < end && charAt(start) <= ' ') {
      start++;
    }
    while (start < end && charAt(end - 1) <= ' ') {
      end--;
    }
    return start == 0 && end == length ? this : substring(start, end);
  }

  private String turkic(boolean upper) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      char c = charAt(i);
      if (upper) {
        sb.append(c == 'i' ? '\u0130' : c == '\u0131' ? 'I' : c);
      } else if (c == 'I') {
        if (i + 1 < length && charAt(i + 1) == '\u0307') {
          sb.append('i');
          ++i;
        } else {
          sb.append('\u0131');
        }
      } else if (c == '\u0130') {
        sb.append('i');
      } else {
        sb.append(c);
      }
    }
    return upper ? sb.toString().toUpperCase() : sb.toString().toLowerCase();
  }

  public String toLowerCase() {
    StringBuilder out = null;
    for (int i = 0; i < length; ) {
      int cp = codePointAt(i);
      int n = Character.charCount(cp);
      String mapped = null;
      if (cp == 0x130) {
        mapped = "i\u0307";
      } else if (cp == 0x3A3) {
        mapped = finalSigma(i) ? "\u03C2" : "\u03C3";
      } else {
        int lower = Character.toLowerCase(cp);
        if (lower != cp) {
          mapped = new String(Character.toChars(lower));
        }
      }
      if (mapped != null && out == null) {
        out = new StringBuilder(length + 4).append(this, 0, i);
      }
      if (out != null) {
        if (mapped != null) {
          out.append(mapped);
        } else {
          out.append(this, i, i + n);
        }
      }
      i += n;
    }
    return out == null ? this : out.toString();
  }

  private boolean finalSigma(int index) {
    boolean before = false;
    for (int i = index; i > 0; ) {
      int cp = codePointBefore(i);
      i -= Character.charCount(cp);
      if (isCaseIgnorable(cp)) continue;
      before = isCased(cp);
      break;
    }
    if (!before) return false;
    for (int i = index + 1; i < length; ) {
      int cp = codePointAt(i);
      i += Character.charCount(cp);
      if (isCaseIgnorable(cp)) continue;
      return !isCased(cp);
    }
    return true;
  }

  private static boolean isCased(int cp) {
    return Character.isLowerCase(cp) || Character.isUpperCase(cp) || Character.isTitleCase(cp);
  }

  private static boolean isCaseIgnorable(int cp) {
    int type = Character.getType(cp);
    return cp == '\'' || cp == '.' || cp == ':' || cp == 0xB7 || cp == 0x2019 || type == Character.NON_SPACING_MARK
        || type == Character.ENCLOSING_MARK || type == Character.FORMAT || type == Character.MODIFIER_LETTER
        || type == Character.MODIFIER_SYMBOL;
  }

  private static String upperSpecial(int cp) {
    switch (cp) {
    case 0xDF: return "SS";
    case 0x149: return "\u02BCN";
    case 0x1F0: return "J\u030C";
    case 0x390: return "\u0399\u0308\u0301";
    case 0x3B0: return "\u03A5\u0308\u0301";
    case 0x587: return "\u0535\u0552";
    case 0x1E96: return "H\u0331";
    case 0x1E97: return "T\u0308";
    case 0x1E98: return "W\u030A";
    case 0x1E99: return "Y\u030A";
    case 0x1E9A: return "A\u02BE";
    case 0xFB00: return "FF";
    case 0xFB01: return "FI";
    case 0xFB02: return "FL";
    case 0xFB03: return "FFI";
    case 0xFB04: return "FFL";
    case 0xFB05: return "ST";
    case 0xFB06: return "ST";
    case 0xFB13: return "\u0544\u0546";
    case 0xFB14: return "\u0544\u0535";
    case 0xFB15: return "\u0544\u053B";
    case 0xFB16: return "\u054E\u0546";
    case 0xFB17: return "\u0544\u053D";
    default: return null;
    }
  }

  public String toUpperCase() {
    StringBuilder out = null;
    for (int i = 0; i < length; ) {
      int cp = codePointAt(i);
      int n = Character.charCount(cp);
      String mapped = upperSpecial(cp);
      if (mapped == null) {
        int upper = Character.toUpperCase(cp);
        if (upper != cp) {
          mapped = new String(Character.toChars(upper));
        }
      }
      if (mapped != null && out == null) {
        out = new StringBuilder(length + 4).append(this, 0, i);
      }
      if (out != null) {
        if (mapped != null) {
          out.append(mapped);
        } else {
          out.append(this, i, i + n);
        }
      }
      i += n;
    }
    return out == null ? this : out.toString();
  }

  public int indexOf(int c) {
    return indexOf(c, 0);
  }

  public int indexOf(int c, int start) {
    if (start < 0) start = 0;
    if (start >= length || c < 0 || c > 0x10ffff) return -1;
    return indexOfCodePoint(c, start);
  }

  public int lastIndexOf(int ch) {
    return lastIndexOf(ch, length - 1);
  }

  public int indexOf(String s) {
    return indexOf(s, 0);
  }

  public int indexOf(String s, int start) {
    if (start < 0) start = 0;
    if (s.length == 0) return Math.min(start, length);

    if (start > length - s.length) return -1;
    return indexOfString(s, start);
  }

  public int lastIndexOf(String s) {
    return lastIndexOf(s, length);
  }

  public int lastIndexOf(String s, int lastIndex) {
    int rightIndex = length - s.length;
    if (lastIndex > rightIndex) lastIndex = rightIndex;
    if (lastIndex < 0) return -1;
    if (s.length == 0) return lastIndex;

    for (int i = lastIndex; i >= 0; --i) {
      int j = 0;
      for (; j < s.length; ++j) {
        if (charAt(i + j) != s.charAt(j)) {
          break;
        }
      }
      if (j == s.length) {
        return i;
      }
    }

    return -1;
  }

  public String replace(char oldChar, char newChar) {
    if (data instanceof char[]) {
      char[] buf = new char[length];
      for (int i=0; i < length; i++) {
        if (charAt(i) == oldChar) {
          buf[i] = newChar;
        } else {
          buf[i] = charAt(i);
        }
      }
      return new String(buf, 0, length, false);
    } else if (oldChar > 0x7f || newChar > 0x7f) {
      char[] buf = new char[length];
      for (int i = 0; i < length; i++) {
        char c = charAt(i);
        buf[i] = c == oldChar ? newChar : c;
      }
      return new String(buf, 0, length, false);
    } else {
      byte[] buf = new byte[length];
      byte[] orig = (byte[])data;
      byte oldByte = (byte)oldChar;
      byte newByte = (byte)newChar;
      for (int i=0; i < length; i++) {
        if (orig[i+offset] == oldByte) {
          buf[i] = newByte;
        } else {
          buf[i] = orig[i+offset];
        }
      }
      return new String(buf, 0, length, false);
    }
  }

  public String substring(int start) {
    return substring(start, length);
  }

  public String substring(int start, int end) {
    checkFromToIndex(start, end, length);
    int newLen = end - start;

    if (start == 0 && end == length)
      return this;
    else if (end - start == 0)
      return "";
    else
      return new String(this, start, newLen);
  }

  public boolean startsWith(String s) {
    if (length >= s.length) {
      return substring(0, s.length).compareTo(s) == 0;
    } else {
      return false;
    }
  }

  public boolean startsWith(String s, int start) {
    if (start < 0 || start > length - s.length) {
      return false;
    }
    for (int i = 0; i < s.length; ++i) {
      if (charAt(start + i) != s.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  public boolean endsWith(String s) {
    if (length >= s.length) {
      return substring(length - s.length).compareTo(s) == 0;
    } else {
      return false;
    }
  }

  public String concat(String s) {
    if (s.length() == 0) {
      return this;
    } else {
      return this + s;
    }
  }

  public void getBytes(int srcOffset, int srcLength,
                       byte[] dst, int dstOffset)
  {
    checkFromToIndex(srcOffset, srcLength, length);
    srcLength -= srcOffset;
    checkFromIndexSize(dstOffset, srcLength, dst.length);

    if (data instanceof char[]) {
      char[] src = (char[]) data;
      for (int i = 0; i < srcLength; ++i) {
        dst[i + dstOffset] = (byte) src[i + offset + srcOffset];
      }
    } else {
      byte[] src = (byte[]) data;
      System.arraycopy(src, offset + srcOffset, dst, dstOffset, srcLength);
    }
  }

  public byte[] getBytes() {
    return encodeChars(sun.nio.cs.UTF_8.INSTANCE);
  }

  public byte[] getBytes(String charsetName)
    throws java.io.UnsupportedEncodingException
  {
    return encodeChars(sun.nio.cs.StringCoding.lookup(charsetName));
  }

  public byte[] getBytes(java.nio.charset.Charset charset) {
    if (charset == null) throw new NullPointerException();
    return encodeChars(charset);
  }

  private byte[] encodeChars(java.nio.charset.Charset charset) {
    if (data instanceof byte[]) {
      byte[] source = (byte[]) data;
      if (charset == sun.nio.cs.UTF_8.INSTANCE || charset == sun.nio.cs.US_ASCII.INSTANCE
          || charset == sun.nio.cs.ISO_8859_1.INSTANCE) {
        boolean ascii = true;
        for (int i = 0; i < length; ++i) {
          if (source[offset + i] < 0) {
            ascii = false;
            break;
          }
        }
        if (ascii) {
          byte[] b = new byte[length];
          System.arraycopy(source, offset, b, 0, length);
          return b;
        }
      }
      char[] chars = new char[length];
      getChars(0, length, chars, 0);
      return sun.nio.cs.StringCoding.encode(charset, chars, 0, length);
    }
    return sun.nio.cs.StringCoding.encode(charset, (char[]) data, offset, length);
  }

  public void getChars(int srcOffset, int srcEnd,
                       char[] dst, int dstOffset)
  {
    checkFromToIndex(srcOffset, srcEnd, length);
    int srcLength = srcEnd - srcOffset;
    checkFromIndexSize(dstOffset, srcLength, dst.length);
    if (data instanceof char[]) {
      char[] src = (char[]) data;
      System.arraycopy(src, offset + srcOffset, dst, dstOffset, srcLength);
    } else {
      widenBytes((byte[]) data, offset + srcOffset, dst, dstOffset, srcLength);
    }
  }

  public char[] toCharArray() {
    char[] b = new char[length];
    getChars(0, length, b, 0);
    return b;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index >= length) {
      throw indexError(index, length);
    }

    if (data instanceof char[]) {
      return ((char[]) data)[index + offset];
    } else {
      return (char) ((byte[]) data)[index + offset];
    }
  }

  public String[] split(String regex) {
    return split(regex, 0);
  }

  public String[] split(String regex, int limit) {
    // Avoid constructing a regex machine for a single literal delimiter.
    // Surrogates must retain Pattern's code-point matching semantics.
    char delimiter = 0;
    boolean literal = false;
    if (regex.length() == 1) {
      delimiter = regex.charAt(0);
      literal = "\\.^$|?*+()[]{}".indexOf(delimiter) < 0;
    } else if (regex.length() == 2 && regex.charAt(0) == '\\') {
      delimiter = regex.charAt(1);
      literal = "\\.^$|?*+()[]{}-/".indexOf(delimiter) >= 0;
    }
    if (literal && (delimiter < 0xd800 || delimiter > 0xdfff)) {
      ArrayList<String> parts = new ArrayList<String>();
      int start = 0;
      int end;
      while ((limit <= 0 || parts.size() < limit - 1)
          && (end = indexOf(delimiter, start)) >= 0) {
        parts.add(substring(start, end));
        start = end + 1;
      }
      if (start == 0) return new String[] {this};
      parts.add(substring(start));
      int size = parts.size();
      if (limit == 0) {
        while (size > 0 && parts.get(size - 1).isEmpty()) --size;
      }
      String[] result = new String[size];
      for (int i = 0; i < size; ++i) result[i] = parts.get(i);
      return result;
    }
    return Pattern.compile(regex).split(this, limit);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return substring(start, end);
  }

  public boolean matches(String regex) {
    return Pattern.matches(regex, this);
  }

  public String replaceFirst(String regex, String replacement) {
    return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
  }

  public String replaceAll(String regex, String replacement) {
    return Pattern.compile(regex).matcher(this).replaceAll(replacement);
  }

  public String replace(CharSequence target, CharSequence replace) {
    if (target.length() == 0) {
      return this.infuse(replace.toString());
    }

    String targetString = target.toString();
    String replaceString = replace.toString();

    int targetSize = targetString.length();
    int index = indexOf(targetString);
    if (index == -1) return this;

    StringBuilder returnValue = new StringBuilder();
    String unhandled = this;
    do {
      returnValue.append(unhandled.substring(0, index)).append(replaceString);
      unhandled = unhandled.substring(index + targetSize, unhandled.length());
      index = unhandled.indexOf(targetString);
    } while (index != -1);

    returnValue.append(unhandled);
    return returnValue.toString();
  }

  private String infuse(String infuseWith) {
    StringBuilder retVal = new StringBuilder();

    String me = this;
    for (int i = 0; i < me.length(); i++) {
      retVal.append(infuseWith).append(me.substring(i, i + 1));
    }

    retVal.append(infuseWith);
    return retVal.toString();
  }

  public native String intern();

  public static String format(String fmt, Object... args) {
    final Formatter formatter = new Formatter();
    final String result = formatter.format(fmt, args).toString();
    formatter.close();
    return result;
  }

  public static String format(Locale l, String fmt, Object... args) {
    final Formatter formatter = new Formatter();
    final String result = formatter.format(l, fmt, args).toString();
    formatter.close();
    return result;
  }

  public static String valueOf(Object s) {
    return s == null ? "null" : s.toString();
  }

  public static String valueOf(boolean v) {
    return Boolean.toString(v);
  }

  public static String valueOf(char v) {
    return Character.toString(v);
  }

  public static String valueOf(int v) {
    return Integer.toString(v);
  }

  public static String valueOf(long v) {
    return Long.toString(v);
  }

  public static String valueOf(float v) {
    return Float.toString(v);
  }

  public static String valueOf(double v) {
    return Double.toString(v);
  }

  public static String valueOf(char[] data, int offset, int length) {
    checkFromIndexSize(offset, length, data.length);
    return new String(data, offset, length);
  }

  public static String valueOf(char[] data) {
    return valueOf(data, 0, data.length);
  }

  public int lastIndexOf(int ch, int lastIndex) {
    if (lastIndex >= length) {
      lastIndex = length - 1;
    }
    if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
      for (int i = lastIndex; i >= 0; --i) {
        if (charAt(i) == ch) {
          return i;
        }
      }
      return -1;
    }
    if (!Character.isValidCodePoint(ch)) return -1;
    char high = Character.highSurrogate(ch);
    char low = Character.lowSurrogate(ch);
    for (int i = Math.min(lastIndex, length - 2); i >= 0; --i) {
      if (charAt(i) == high && charAt(i + 1) == low) return i;
    }
    return -1;
  }

  public boolean regionMatches(int thisOffset, String match, int matchOffset,
                               int length)
  {
    return regionMatches(false, thisOffset, match, matchOffset, length);
  }

  public boolean regionMatches(boolean ignoreCase, int thisOffset,
                               String match, int matchOffset, int length)
  {
    if (matchOffset < 0 || thisOffset < 0 || (long) thisOffset + length > this.length
        || (long) matchOffset + length > match.length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      char x = charAt(thisOffset + i);
      char y = match.charAt(matchOffset + i);
      if (x == y) {
        continue;
      }
      if (!ignoreCase) {
        return false;
      }
      char ux = Character.toUpperCase(x);
      char uy = Character.toUpperCase(y);
      if (ux != uy && Character.toLowerCase(ux) != Character.toLowerCase(uy)) {
        return false;
      }
    }
    return true;
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public boolean contains(CharSequence match) {
    return indexOf(match.toString()) != -1;
  }

  public int codePointAt(int offset) {
    if (offset < 0 || offset >= length) {
      throw indexError(offset, length);
    }
    return Character.codePointAt(this, offset);
  }

  public int codePointCount(int start, int end) {
    java.util.Objects.checkFromToIndex(start, end, length);
    return Character.codePointCount(this, start, end);
  }

  public String toUpperCase(Locale locale) {
    if (locale == null) throw new NullPointerException();
    String lang = locale.getLanguage();
    if (lang.equals("tr") || lang.equals("az")) {
      return turkic(true);
    }
    return toUpperCase();
  }

  public String toLowerCase(Locale locale) {
    if (locale == null) throw new NullPointerException();
    String lang = locale.getLanguage();
    if (lang.equals("tr") || lang.equals("az")) {
      return turkic(false);
    }
    return toLowerCase();
  }

  public int codePointBefore(int index) {
    int i = index - 1;
    if (i < 0 || i >= length) {
      throw indexError(i, length);
    }
    return Character.codePointBefore(this, index);
  }

  private int indexOfNonWhitespace() {
    int i = 0;
    while (i < length) {
      int cp = codePointAt(i);
      if (!Character.isWhitespace(cp)) {
        return i;
      }
      i += Character.charCount(cp);
    }
    return length;
  }

  private int lastIndexOfNonWhitespace() {
    int i = length;
    while (i > 0) {
      int cp = codePointBefore(i);
      if (!Character.isWhitespace(cp)) {
        return i;
      }
      i -= Character.charCount(cp);
    }
    return 0;
  }

  public String strip() {
    int left = indexOfNonWhitespace();
    if (left == length) {
      return "";
    }
    int right = lastIndexOfNonWhitespace();
    return (left == 0 && right == length) ? this : substring(left, right);
  }

  public String stripLeading() {
    int left = indexOfNonWhitespace();
    return left == 0 ? this : substring(left, length);
  }

  public String stripTrailing() {
    int right = lastIndexOfNonWhitespace();
    return right == length ? this : substring(0, right);
  }

  public boolean isBlank() {
    return indexOfNonWhitespace() == length;
  }

  public String repeat(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("count is negative: " + count);
    }
    if (count == 1) {
      return this;
    }
    if (count == 0 || length == 0) {
      return "";
    }
    boolean latin1 = true;
    for (int i = 0; i < length && latin1; i++) {
      latin1 = charAt(i) < 256;
    }
    int byteLength = latin1 ? length : length * 2;
    if (Integer.MAX_VALUE / count < byteLength) {
      throw new OutOfMemoryError("Required length exceeds implementation limit");
    }
    long total = (long) length * (long) count;
    if ((long) byteLength * count > Integer.MAX_VALUE - 2) {
      throw new OutOfMemoryError("Requested array size exceeds VM limit");
    }
    char[] chars = new char[(int) total];
    char[] src = toCharArray();
    for (int i = 0; i < count; i++) {
      System.arraycopy(src, 0, chars, i * length, length);
    }
    return new String(chars);
  }

  public <R> R transform(Function<? super String, ? extends R> f) {
    return f.apply(this);
  }

  public String formatted(Object... args) {
    return String.format(this, args);
  }

  private List<String> extractLines() {
    List<String> result = new ArrayList<String>();
    int len = length;
    int index = 0;
    while (index < len) {
      int lineStart = index;
      while (index < len && charAt(index) != '\n' && charAt(index) != '\r') {
        index++;
      }
      result.add(substring(lineStart, index));
      if (index < len) {
        if (charAt(index) == '\r' && (index + 1) < len && charAt(index + 1) == '\n') {
          index += 2;
        } else {
          index += 1;
        }
      }
    }
    return result;
  }

  public String indent(int n) {
    if (isEmpty()) {
      return "";
    }
    List<String> list = extractLines();
    if (n > 0) {
      long total = 0;
      for (int i = 0; i < list.size(); i++) {
        total += (long) list.get(i).length + n + 1;
      }
      if (total > Integer.MAX_VALUE - 2) {
        throw new OutOfMemoryError("Requested array size exceeds VM limit");
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      String line = list.get(i);
      if (n > 0) {
        for (int s = 0; s < n; s++) {
          sb.append(' ');
        }
        sb.append(line);
      } else if (n < 0) {
        int left = line.indexOfNonWhitespace();
        int toRemove = (int) Math.min(-(long) n, left);
        sb.append(line.substring(toRemove));
      } else {
        sb.append(line);
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  public String stripIndent() {
    int len = length;
    if (len == 0) {
      return "";
    }
    char last = charAt(len - 1);
    boolean hasTrailingNewline = (last == '\n' || last == '\r');
    List<String> list = extractLines();
    int outdent = hasTrailingNewline ? 0 : outdent(list);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      String line = list.get(i);
      int left = line.indexOfNonWhitespace();
      int right = line.lastIndexOfNonWhitespace();
      int from = Math.min(outdent, left);
      if (left <= right) {
        sb.append(line.substring(from, right));
      }
      if (i + 1 < list.size() || hasTrailingNewline) {
        sb.append('\n');
      }
    }
    return sb.toString();
  }

  private static int outdent(List<String> list) {
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < list.size(); i++) {
      String line = list.get(i);
      int idx = line.indexOfNonWhitespace();
      if (idx != line.length()) {
        min = Math.min(min, idx);
      }
    }
    String lastLine = list.get(list.size() - 1);
    if (lastLine.isBlank()) {
      min = Math.min(min, lastLine.length());
    }
    return min == Integer.MAX_VALUE ? 0 : min;
  }

  public String translateEscapes() {
    int len = length;
    if (len == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = charAt(i);
      if (c == '\\') {
        if (i + 1 >= len) {
          throw new IllegalArgumentException("Invalid escape sequence: \\ at end of string");
        }
        i++;
        char next = charAt(i);
        switch (next) {
          case 'b':
            sb.append('\b');
            break;
          case 't':
            sb.append('\t');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'f':
            sb.append('\f');
            break;
          case 'r':
            sb.append('\r');
            break;
          case '\"':
            sb.append('\"');
            break;
          case '\'':
            sb.append('\'');
            break;
          case '\\':
            sb.append('\\');
            break;
          case '\n':
            break;
          case '\r':
            if (i + 1 < len && charAt(i + 1) == '\n') {
              i++;
            }
            break;
          case '0': case '1': case '2': case '3':
          case '4': case '5': case '6': case '7': {
            int octal = next - '0';
            int maxDigits = (next <= '3') ? 2 : 1;
            for (int d = 0; d < maxDigits; d++) {
              if (i + 1 < len) {
                char ch = charAt(i + 1);
                if (ch >= '0' && ch <= '7') {
                  octal = (octal << 3) | (ch - '0');
                  i++;
                } else {
                  break;
                }
              }
            }
            sb.append((char) octal);
            break;
          }
          default:
            throw new IllegalArgumentException("Invalid escape sequence: \\" + next);
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  public static String join(CharSequence delimiter, CharSequence... elements) {
    Objects.requireNonNull(delimiter);
    Objects.requireNonNull(elements);
    StringJoiner joiner = new StringJoiner(delimiter);
    for (int i = 0; i < elements.length; i++) {
      joiner.add(elements[i]);
    }
    return joiner.toString();
  }

  public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
    Objects.requireNonNull(delimiter);
    Objects.requireNonNull(elements);
    StringJoiner joiner = new StringJoiner(delimiter);
    for (CharSequence cs : elements) {
      joiner.add(cs);
    }
    return joiner.toString();
  }

  public java.util.stream.Stream<String> lines() {
    class LineIterator implements java.util.Iterator<String> {
      private int next = 0;
      private final int fence = length();
      private String line = null;

      @Override
      public boolean hasNext() {
        if (line != null) {
          return true;
        }
        if (next > fence || next == fence) {
          return false;
        }
        int i = next;
        while (i < fence) {
          char c = charAt(i);
          if (c == '\n' || c == '\r') {
            line = substring(next, i);
            if (c == '\r' && i + 1 < fence && charAt(i + 1) == '\n') {
              next = i + 2;
            } else {
              next = i + 1;
            }
            return true;
          }
          i++;
        }
        line = substring(next, fence);
        next = fence;
        return true;
      }

      @Override
      public String next() {
        if (!hasNext()) {
          throw new java.util.NoSuchElementException();
        }
        String ret = line;
        line = null;
        return ret;
      }
    }
    return java.util.stream.StreamSupport.stream(
        java.util.Spliterators.spliteratorUnknownSize(new LineIterator(), java.util.Spliterator.ORDERED | java.util.Spliterator.NONNULL),
        false);
  }

  @Override
  public java.util.Optional<String> describeConstable() {
    return java.util.Optional.of(this);
  }

  @Override
  public String resolveConstantDesc(java.lang.invoke.MethodHandles.Lookup lookup) {
    return this;
  }

  private void checkBoundsBeginEnd(int begin, int end) {
    checkFromToIndex(begin, end, length);
  }

  public int indexOf(int ch, int beginIndex, int endIndex) {
    checkBoundsBeginEnd(beginIndex, endIndex);
    if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
      for (int i = beginIndex; i < endIndex; ++i) {
        if (charAt(i) == ch) return i;
      }
      return -1;
    }
    if (!Character.isValidCodePoint(ch)) return -1;
    char high = Character.highSurrogate(ch);
    char low = Character.lowSurrogate(ch);
    for (int i = beginIndex; i < endIndex - 1; ++i) {
      if (charAt(i) == high && charAt(i + 1) == low) return i;
    }
    return -1;
  }

  public int indexOf(String str, int beginIndex, int endIndex) {
    java.util.Objects.requireNonNull(str);
    checkBoundsBeginEnd(beginIndex, endIndex);
    if (str.length == 0) return beginIndex;
    for (int i = beginIndex; i <= endIndex - str.length; ++i) {
      if (startsWith(str, i)) return i;
    }
    return -1;
  }

  public int offsetByCodePoints(int index, int codePointOffset) {
    if (index < 0 || index > length) {
      throw new IndexOutOfBoundsException();
    }
    return Character.offsetByCodePoints(this, index, codePointOffset);
  }

  public boolean contentEquals(CharSequence cs) {
    if (cs.length() != length) return false;
    for (int i = 0; i < length; ++i) {
      if (charAt(i) != cs.charAt(i)) return false;
    }
    return true;
  }

  public boolean contentEquals(StringBuffer buffer) {
    return contentEquals((CharSequence) buffer);
  }

  public static String copyValueOf(char[] data, int offset, int count) {
    return valueOf(data, offset, count);
  }

  public static String copyValueOf(char[] data) {
    return valueOf(data);
  }

  public String[] splitWithDelimiters(String regex, int limit) {
    java.util.regex.Matcher matcher = Pattern.compile(regex).matcher(this);
    boolean limited = limit > 0;
    java.util.ArrayList<String> parts = new java.util.ArrayList<String>();
    int index = 0;
    int matches = 0;
    while (matcher.find()) {
      if (!limited || matches < limit - 1) {
        if (index == 0 && matcher.start() == 0 && matcher.end() == 0) continue;
        parts.add(substring(index, matcher.start()));
        index = matcher.end();
        parts.add(substring(matcher.start(), index));
        ++matches;
      } else if (matches == limit - 1) {
        parts.add(substring(index, length));
        index = matcher.end();
        ++matches;
      }
    }
    if (index == 0) return new String[] {this};
    if (!limited || matches < limit) parts.add(substring(index, length));
    int size = parts.size();
    if (limit == 0) {
      while (size > 0 && parts.get(size - 1).isEmpty()) --size;
    }
    return parts.subList(0, size).toArray(new String[size]);
  }
}
