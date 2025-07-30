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
  implements Comparable<String>, CharSequence, Serializable
{
  private static final String UTF_8_ENCODING = "UTF-8";
  private static final String ISO_8859_1_ENCODING = "ISO-8859-1";
  private static final String LATIN_1_ENCODING = "LATIN-1";
  private static final String DEFAULT_ENCODING = UTF_8_ENCODING;

  public static Comparator<String> CASE_INSENSITIVE_ORDER
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

  public String() {
    this(new char[0], 0, 0);
  }

  public String(char[] data, int offset, int length, boolean copy) {
    this((Object) data, offset, length, copy);
  }

  public String(char[] data, int offset, int length) {
    this(data, offset, length, true);
  }

  public String(char[] data) {
    this(data, 0, data.length);
  }

  public String(byte bytes[], int offset, int length, String charsetName)
    throws UnsupportedEncodingException
  {
    this(bytes, offset, length);
    if (! (charsetName.equalsIgnoreCase(UTF_8_ENCODING)
           || charsetName.equalsIgnoreCase(ISO_8859_1_ENCODING)))
    {
      throw new UnsupportedEncodingException(charsetName);
    }
  }

  public String(byte[] data, int offset, int length, boolean copy) {
    this((Object) data, offset, length, copy);
  }

  public String(byte[] data, int offset, int length) {
    this(data, offset, length, true);
  }

  public String(byte[] data) {
    this(data, 0, data.length);
  }

  public String(String s) {
    this(s.toCharArray());
  }

  public String(byte[] data, String charset)
    throws UnsupportedEncodingException
  {
    this(data, 0, data.length, charset);
  }

  public String(byte bytes[], int highByte, int offset, int length) {
    if (offset < 0 )
      throw new StringIndexOutOfBoundsException(offset);
    else if (offset + length > bytes.length)
      throw new StringIndexOutOfBoundsException(offset + length);
    else if (length < 0)
      throw new StringIndexOutOfBoundsException(length);

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

    if (offset < 0 )
      throw new StringIndexOutOfBoundsException(offset);
    else if (offset + length > l)
      throw new StringIndexOutOfBoundsException(offset + length);
    else if (length < 0)
      throw new StringIndexOutOfBoundsException(length);

    if(!copy && Utf8.test(data)) copy = true;

    if (copy) {
      Object c;
      if (data instanceof char[]) {
        c = new char[length];
        System.arraycopy(data, offset, c, 0, length);
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
      int h = 0;
      for (int i = 0; i < length; ++i) h = (h * 31) + charAt(i);
      hashCode = h;
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

    int idx = 0;
    int result;

    int end = (length < s.length ? length : s.length);

    while (idx < end) {
      if ((result = charAt(idx) - s.charAt(idx)) != 0) {
        return result;
      }
      idx++;
    }
    return length - s.length;
  }

  public int compareToIgnoreCase(String s) {
    if (this == s) return 0;

    int idx = 0;
    int result;

    int end = (length < s.length ? length : s.length);

    while (idx < end) {
      if ((result =
           Character.toLowerCase(charAt(idx)) -
           Character.toLowerCase(s.charAt(idx))) != 0) {
        return result;
      }
      idx++;
    }
    return length - s.length;
  }

  public String trim() {
    int start = -1;
    for (int i = 0; i < length; ++i) {
      char c = charAt(i);
      if (start == -1 && ! Character.isWhitespace(c)) {
        start = i;
        break;
      }
    }

    int end = -1;
    for (int i = length - 1; i >= 0; --i) {
      char c = charAt(i);
      if (end == -1 && ! Character.isWhitespace(c)) {
        end = i + 1;
        break;
      }
    }

    if (start >= end) {
      return "";
    } else {
      return substring(start, end);
    }
  }

  public String toLowerCase() {
    for (int j = 0; j < length; ++j) {
      char ch = charAt(j);
      if (Character.toLowerCase(ch) != ch) {
        char[] b = new char[length];
        for (int i = 0; i < length; ++i) {
          b[i] = Character.toLowerCase(charAt(i));
        }
        return new String(b, 0, length, false);
      }
    }
    return this;
  }

  public String toUpperCase() {
    for (int j = 0; j < length; ++j) {
      char ch = charAt(j);
      if (Character.toUpperCase(ch) != ch) {
        char[] b = new char[length];
        for (int i = 0; i < length; ++i) {
          b[i] = Character.toUpperCase(charAt(i));
        }
        return new String(b, 0, length, false);
      }
    }
    return this;
  }

  public int indexOf(int c) {
    return indexOf(c, 0);
  }

  public int indexOf(int c, int start) {
    for (int i = start; i < length; ++i) {
      if (charAt(i) == c) {
        return i;
      }
    }

    return -1;
  }

  public int lastIndexOf(int ch) {
    return lastIndexOf(ch, length-1);
  }

  public int indexOf(String s) {
    return indexOf(s, 0);
  }

  public int indexOf(String s, int start) {
    if (s.length == 0) return start;

    for (int i = start; i < length - s.length + 1; ++i) {
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

  public int lastIndexOf(String s) {
    return lastIndexOf(s, length - s.length);
  }

  public int lastIndexOf(String s, int lastIndex) {
    if (s.length == 0) return lastIndex;

    for (int i = Math.min(length - s.length, lastIndex); i >= 0; --i) {
      int j = 0;
      for (; j < s.length && i + j < length; ++j) {
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
    if (start < 0)
      throw new StringIndexOutOfBoundsException(start);
    else if (end > length)
      throw new StringIndexOutOfBoundsException(end);
    int newLen = end - start;
    if (newLen < 0)
      throw new StringIndexOutOfBoundsException(newLen);

    if (start == 0 && end == length)
      return this;
    else if (end - start == 0)
      return "";
    else
      return new String(data, offset + start, newLen, false);
  }

  public boolean startsWith(String s) {
    if (length >= s.length) {
      return substring(0, s.length).compareTo(s) == 0;
    } else {
      return false;
    }
  }

  public boolean startsWith(String s, int start) {
    if (length >= s.length + start) {
      return substring(start, s.length).compareTo(s) == 0;
    } else {
      return false;
    }
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
    if (srcOffset < 0)
      throw new StringIndexOutOfBoundsException(srcOffset);
    else if (srcOffset + srcLength > length)
      throw new StringIndexOutOfBoundsException(srcOffset + srcLength);
    else if (srcLength < 0)
      throw new StringIndexOutOfBoundsException(srcLength);

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
    try {
      return getBytes(DEFAULT_ENCODING);
    } catch (java.io.UnsupportedEncodingException ex) {
      throw new RuntimeException(
        "Default '" + DEFAULT_ENCODING + "' encoding not handled", ex);
    }
  }

  public byte[] getBytes(String format)
    throws java.io.UnsupportedEncodingException
  {
    if(data instanceof byte[]) {
      byte[] b = new byte[length];
      getBytes(0, length, b, 0);
      return b;
    }
    String fmt = format.trim().toUpperCase();
    if (DEFAULT_ENCODING.equals(fmt)) {
      return Utf8.encode((char[])data, offset, length);
    } else if (ISO_8859_1_ENCODING.equals(fmt) || LATIN_1_ENCODING.equals(fmt)) {
      return Iso88591.encode((char[])data, offset, length);
    } else {
      throw new java.io.UnsupportedEncodingException(
        "Encoding " + format + " not supported");
    }
  }

  public void getChars(int srcOffset, int srcEnd,
                       char[] dst, int dstOffset)
  {
    if (srcOffset < 0)
      throw new StringIndexOutOfBoundsException(srcOffset);
    else if (srcEnd > length)
      throw new StringIndexOutOfBoundsException(srcEnd);

    int srcLength = srcEnd-srcOffset;
    if (data instanceof char[]) {
      char[] src = (char[]) data;
      System.arraycopy(src, offset + srcOffset, dst, dstOffset, srcLength);
    } else {
      byte[] src = (byte[]) data;
      for (int i = 0; i < srcLength; ++i) {
        dst[i + dstOffset] = (char) src[i + offset + srcOffset];
      }
    }
  }

  public char[] toCharArray() {
    char[] b = new char[length];
    getChars(0, length, b, 0);
    return b;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index > length) {
      throw new StringIndexOutOfBoundsException(index);
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

    int targetSize = target.length();

    StringBuilder returnValue = new StringBuilder();
    String unhandled = this;

    int index = -1;
    while ((index = unhandled.indexOf(targetString)) != -1) {
      returnValue.append(unhandled.substring(0, index)).append(replaceString);
      unhandled = unhandled.substring(index + targetSize,
                                      unhandled.length());
    }

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

  public static String valueOf(byte v) {
    return Byte.toString(v);
  }

  public static String valueOf(short v) {
    return Short.toString(v);
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
    return new String(data, offset, length);
  }

  public static String valueOf(char[] data) {
    return valueOf(data, 0, data.length);
  }

  public int lastIndexOf(int ch, int lastIndex) {
    if (lastIndex >= length) {
      lastIndex = length - 1;
    }
    for (int i = lastIndex ; i >= 0; --i) {
      if (charAt(i) == ch) {
        return i;
      }
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
    String a = substring(thisOffset, thisOffset + length);
    String b = match.substring(matchOffset, matchOffset + length);
    if (ignoreCase) {
      return a.equalsIgnoreCase(b);
    } else {
      return a.equals(b);
    }
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public boolean contains(CharSequence match) {
    return indexOf(match.toString()) != -1;
  }

  public int codePointAt(int offset) {
    return Character.codePointAt(this, offset);
  }

  public int codePointCount(int start, int end) {
    return Character.codePointCount(this, start, end);
  }

  public String toUpperCase(Locale locale) {
    if (locale == Locale.ENGLISH) {
      return toUpperCase();
    } else {
      throw new UnsupportedOperationException("toUpperCase("+locale+')');
    }
  }

  public String toLowerCase(Locale locale) {
    if (locale == Locale.ENGLISH) {
      return toLowerCase();
    } else {
      throw new UnsupportedOperationException("toLowerCase("+locale+')');
    }
  }

  public int codePointBefore(int index) {
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
    long total = (long) length * (long) count;
    if (total > Integer.MAX_VALUE) {
      throw new OutOfMemoryError("Required array size too large");
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
        int toRemove = Math.min(-n, left);
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
}
