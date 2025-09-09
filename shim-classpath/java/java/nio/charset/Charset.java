/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Charset implements Comparable<Charset> {
  private final String canonicalName;
  private final Set<String> aliases;

  protected Charset(String canonicalName, String[] aliases) {
    this.canonicalName = Objects.requireNonNull(canonicalName, "canonicalName");
    if (aliases == null || aliases.length == 0) {
      this.aliases = Collections.emptySet();
    } else {
      Set<String> set = new HashSet<String>();
      for (String a : aliases) {
        set.add(a);
      }
      this.aliases = Collections.unmodifiableSet(set);
    }
  }

  public static Charset defaultCharset() {
    return StandardCharsets.UTF_8;
  }

  public static Charset forName(String charsetName) {
    Objects.requireNonNull(charsetName, "charsetName");
    String norm = charsetName.toUpperCase(Locale.ROOT);
    if (norm.equals("UTF-8") || norm.equals("UTF8")) {
      return StandardCharsets.UTF_8;
    }
    if (norm.equals("ISO-8859-1") || norm.equals("ISO8859_1") || norm.equals("LATIN1")) {
      return StandardCharsets.ISO_8859_1;
    }
    if (norm.equals("US-ASCII") || norm.equals("ASCII")) {
      return StandardCharsets.US_ASCII;
    }
    if (norm.equals("UTF-16") || norm.equals("UTF16")) {
      return StandardCharsets.UTF_16;
    }
    if (norm.equals("UTF-16BE") || norm.equals("UTF16BE")) {
      return StandardCharsets.UTF_16BE;
    }
    if (norm.equals("UTF-16LE") || norm.equals("UTF16LE")) {
      return StandardCharsets.UTF_16LE;
    }
    throw new UnsupportedCharsetException(charsetName);
  }

  public static boolean isSupported(String charsetName) {
    try {
      forName(charsetName);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  public static SortedMap<String, Charset> availableCharsets() {
    TreeMap<String, Charset> map = new TreeMap<String, Charset>(String.CASE_INSENSITIVE_ORDER);
    map.put(StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8);
    map.put(StandardCharsets.ISO_8859_1.name(), StandardCharsets.ISO_8859_1);
    map.put(StandardCharsets.US_ASCII.name(), StandardCharsets.US_ASCII);
    map.put(StandardCharsets.UTF_16.name(), StandardCharsets.UTF_16);
    map.put(StandardCharsets.UTF_16BE.name(), StandardCharsets.UTF_16BE);
    map.put(StandardCharsets.UTF_16LE.name(), StandardCharsets.UTF_16LE);
    return Collections.unmodifiableSortedMap(map);
  }

  public String name() {
    return canonicalName;
  }

  public Set<String> aliases() {
    return aliases;
  }

  public String displayName() {
    return canonicalName;
  }

  public boolean isRegistered() {
    return true;
  }

  public boolean canEncode() {
    return true;
  }

  public abstract CharsetDecoder newDecoder();

  public abstract CharsetEncoder newEncoder();

  public ByteBuffer encode(String str) {
    byte[] bytes;
    try {
      bytes = str.getBytes(canonicalName);
    } catch (UnsupportedEncodingException e) {
      bytes = str.getBytes();
    }
    return ByteBuffer.wrap(bytes);
  }

  public ByteBuffer encode(CharBuffer cb) {
    return encode(cb.toString());
  }

  public CharBuffer decode(ByteBuffer bb) {
    byte[] b = new byte[bb.remaining()];
    bb.get(b);
    String s;
    try {
      s = new String(b, canonicalName);
    } catch (UnsupportedEncodingException e) {
      s = new String(b);
    }
    return CharBuffer.wrap(s);
  }

  @Override
  public int compareTo(Charset other) {
    return canonicalName.compareToIgnoreCase(other.canonicalName);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other instanceof Charset) {
      return canonicalName.equalsIgnoreCase(((Charset) other).canonicalName);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return canonicalName.toUpperCase(Locale.ROOT).hashCode();
  }

  @Override
  public String toString() {
    return canonicalName;
  }
}
