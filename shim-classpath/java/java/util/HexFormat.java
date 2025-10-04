/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.IOException;

public final class HexFormat {
  private static final char[] LOWER_DIGITS = "0123456789abcdef".toCharArray();
  private static final char[] UPPER_DIGITS = "0123456789ABCDEF".toCharArray();
  private static final HexFormat DEFAULT_LOWER = new HexFormat("", "", "", false);

  private final String delimiter;
  private final String prefix;
  private final String suffix;
  private final boolean uppercase;
  private final char[] digits;

  private HexFormat(String delimiter, String prefix, String suffix, boolean uppercase) {
    this.delimiter = Objects.requireNonNull(delimiter, "delimiter");
    this.prefix = Objects.requireNonNull(prefix, "prefix");
    this.suffix = Objects.requireNonNull(suffix, "suffix");
    this.uppercase = uppercase;
    this.digits = uppercase ? UPPER_DIGITS : LOWER_DIGITS;
  }

  public static HexFormat of() {
    return DEFAULT_LOWER;
  }

  public static HexFormat ofDelimiter(String delimiter) {
    return new HexFormat(delimiter, "", "", false);
  }

  public HexFormat withDelimiter(String delimiter) {
    return new HexFormat(delimiter, prefix, suffix, uppercase);
  }

  public HexFormat withPrefix(String prefix) {
    return new HexFormat(delimiter, prefix, suffix, uppercase);
  }

  public HexFormat withSuffix(String suffix) {
    return new HexFormat(delimiter, prefix, suffix, uppercase);
  }

  public HexFormat withUpperCase() {
    return uppercase ? this : new HexFormat(delimiter, prefix, suffix, true);
  }

  public HexFormat withLowerCase() {
    return !uppercase ? this : new HexFormat(delimiter, prefix, suffix, false);
  }

  public String delimiter() {
    return delimiter;
  }

  public String prefix() {
    return prefix;
  }

  public String suffix() {
    return suffix;
  }

  public boolean isUpperCase() {
    return uppercase;
  }

  public char toHighHexDigit(int value) {
    return digits[(value >> 4) & 0xF];
  }

  public char toLowHexDigit(int value) {
    return digits[value & 0xF];
  }

  public String toHexDigits(byte value) {
    return new String(new char[] { toHighHexDigit(value), toLowHexDigit(value) });
  }

  public String toHexDigits(char value) {
    return toHexDigits((short) value);
  }

  public String toHexDigits(short value) {
    char[] buf = new char[4];
    buf[0] = digits[(value >> 12) & 0xF];
    buf[1] = digits[(value >> 8) & 0xF];
    buf[2] = digits[(value >> 4) & 0xF];
    buf[3] = digits[value & 0xF];
    return new String(buf);
  }

  public String toHexDigits(int value) {
    char[] buf = new char[8];
    for (int i = 7; i >= 0; --i) {
      buf[i] = digits[value & 0xF];
      value >>>= 4;
    }
    return new String(buf);
  }

  public String toHexDigits(long value) {
    char[] buf = new char[16];
    for (int i = 15; i >= 0; --i) {
      buf[i] = digits[(int) (value & 0xFL)];
      value >>>= 4;
    }
    return new String(buf);
  }

  public String formatHex(byte[] bytes) {
    return formatHex(bytes, 0, bytes.length);
  }

  public String formatHex(byte[] bytes, int fromIndex, int toIndex) {
    Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);
    StringBuilder sb = new StringBuilder();
    try {
      formatHex(sb, bytes, fromIndex, toIndex);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return sb.toString();
  }

  public <A extends Appendable> A formatHex(A out, byte[] bytes) throws IOException {
    return formatHex(out, bytes, 0, bytes.length);
  }

  public <A extends Appendable> A formatHex(A out, byte[] bytes, int fromIndex, int toIndex) throws IOException {
    Objects.requireNonNull(out, "out");
    Objects.checkFromToIndex(fromIndex, toIndex, bytes.length);
    for (int i = fromIndex; i < toIndex; ++i) {
      if (i > fromIndex && !delimiter.isEmpty()) {
        out.append(delimiter);
      }
      if (!prefix.isEmpty()) {
        out.append(prefix);
      }
      byte b = bytes[i];
      out.append(toHighHexDigit(b));
      out.append(toLowHexDigit(b));
      if (!suffix.isEmpty()) {
        out.append(suffix);
      }
    }
    return out;
  }

  public byte[] parseHex(CharSequence string) {
    Objects.requireNonNull(string, "string");
    return parseHex(string, 0, string.length());
  }

  public byte[] parseHex(CharSequence string, int fromIndex, int toIndex) {
    Objects.requireNonNull(string, "string");
    Objects.checkFromToIndex(fromIndex, toIndex, string.length());
    int length = toIndex - fromIndex;
    if (delimiter.isEmpty() && prefix.isEmpty() && suffix.isEmpty()) {
      if ((length & 1) != 0) {
        throw new IllegalArgumentException("string length not even: " + length);
      }
      int count = length / 2;
      byte[] res = new byte[count];
      for (int i = 0; i < count; ++i) {
        int idx = fromIndex + (i * 2);
        int high = fromHexDigit(string.charAt(idx));
        int low = fromHexDigit(string.charAt(idx + 1));
        res[i] = (byte) ((high << 4) | low);
      }
      return res;
    }

    if (length == 0) {
      return new byte[0];
    }

    int itemLen = prefix.length() + 2 + suffix.length();
    int delimLen = delimiter.length();
    int count = (length + delimLen) / (itemLen + delimLen);
    if (count <= 0 || (count * itemLen + (count - 1) * delimLen) != length) {
      throw new IllegalArgumentException("invalid hex format input length: " + length);
    }

    byte[] res = new byte[count];
    int pos = fromIndex;
    for (int i = 0; i < count; ++i) {
      if (i > 0) {
        for (int d = 0; d < delimLen; ++d) {
          if (string.charAt(pos++) != delimiter.charAt(d)) {
            throw new IllegalArgumentException("delimiter mismatch at index " + (pos - 1));
          }
        }
      }
      for (int p = 0; p < prefix.length(); ++p) {
        if (string.charAt(pos++) != prefix.charAt(p)) {
          throw new IllegalArgumentException("prefix mismatch at index " + (pos - 1));
        }
      }
      int high = fromHexDigit(string.charAt(pos++));
      int low = fromHexDigit(string.charAt(pos++));
      res[i] = (byte) ((high << 4) | low);
      for (int s = 0; s < suffix.length(); ++s) {
        if (string.charAt(pos++) != suffix.charAt(s)) {
          throw new IllegalArgumentException("suffix mismatch at index " + (pos - 1));
        }
      }
    }
    return res;
  }

  public byte[] parseHex(char[] chars, int fromIndex, int toIndex) {
    Objects.requireNonNull(chars, "chars");
    Objects.checkFromToIndex(fromIndex, toIndex, chars.length);
    return parseHex(new String(chars, fromIndex, toIndex - fromIndex));
  }

  public static boolean isHexDigit(int ch) {
    return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
  }

  public static int fromHexDigit(int ch) {
    if (ch >= '0' && ch <= '9') {
      return ch - '0';
    }
    if (ch >= 'A' && ch <= 'F') {
      return ch - 'A' + 10;
    }
    if (ch >= 'a' && ch <= 'f') {
      return ch - 'a' + 10;
    }
    throw new NumberFormatException("not a hexadecimal digit: \"" + (char) ch + "\" = " + ch);
  }

  public static int fromHexDigits(CharSequence string) {
    Objects.requireNonNull(string, "string");
    return fromHexDigits(string, 0, string.length());
  }

  public static int fromHexDigits(CharSequence string, int fromIndex, int toIndex) {
    Objects.requireNonNull(string, "string");
    Objects.checkFromToIndex(fromIndex, toIndex, string.length());
    int len = toIndex - fromIndex;
    if (len < 1 || len > 8) {
      throw new IllegalArgumentException("length must be between 1 and 8: " + len);
    }
    int value = 0;
    for (int i = fromIndex; i < toIndex; ++i) {
      value = (value << 4) | fromHexDigit(string.charAt(i));
    }
    return value;
  }

  public static long fromHexDigitsToLong(CharSequence string) {
    Objects.requireNonNull(string, "string");
    return fromHexDigitsToLong(string, 0, string.length());
  }

  public static long fromHexDigitsToLong(CharSequence string, int fromIndex, int toIndex) {
    Objects.requireNonNull(string, "string");
    Objects.checkFromToIndex(fromIndex, toIndex, string.length());
    int len = toIndex - fromIndex;
    if (len < 1 || len > 16) {
      throw new IllegalArgumentException("length must be between 1 and 16: " + len);
    }
    long value = 0;
    for (int i = fromIndex; i < toIndex; ++i) {
      value = (value << 4) | (long) fromHexDigit(string.charAt(i));
    }
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HexFormat)) return false;
    HexFormat other = (HexFormat) o;
    return uppercase == other.uppercase
        && delimiter.equals(other.delimiter)
        && prefix.equals(other.prefix)
        && suffix.equals(other.suffix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delimiter, prefix, suffix, uppercase);
  }

  @Override
  public String toString() {
    return "uppercase: " + uppercase
        + ", delimiter: \"" + delimiter + "\""
        + ", prefix: \"" + prefix + "\""
        + ", suffix: \"" + suffix + "\"";
  }
}
