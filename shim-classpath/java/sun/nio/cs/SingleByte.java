/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Arrays;

public final class SingleByte extends Charset implements HistoricallyNamedCharset {
  private final String historical;
  private final boolean asciiCompatible;
  private final byte replacementByte;
  private final char[] b2c;
  private final char[] encodeChars;
  private final byte[] encodeBytes;
  private final boolean[] encodeUnmappable;

  SingleByte(String name, String[] aliases, String historical, int replacement, boolean registered,
      boolean asciiCompatible, String table, String overrides) {
    super(name, aliases);
    this.historical = historical;
    this.asciiCompatible = asciiCompatible;
    this.replacementByte = (byte) replacement;
    b2c = new char[256];
    for (int i = 0; i < 256; i++) {
      b2c[i] = (char) Integer.parseInt(table.substring(i * 4, i * 4 + 4), 16);
    }
    char[] chars = new char[256 + overrides.length() / 7];
    byte[] bytes = new byte[chars.length];
    boolean[] unmappable = new boolean[chars.length];
    int n = 0;
    for (int i = 0; i < 256; i++) {
      if (b2c[i] != '�') {
        n = add(chars, bytes, unmappable, n, b2c[i], (byte) i, false);
      }
    }
    for (int i = 0; i + 7 <= overrides.length(); i += 7) {
      char c = (char) Integer.parseInt(overrides.substring(i, i + 4), 16);
      int b = Integer.parseInt(overrides.substring(i + 4, i + 6), 16);
      boolean none = overrides.charAt(i + 6) == '1';
      n = add(chars, bytes, unmappable, n, c, (byte) b, none);
    }
    int[] order = new int[n];
    for (int i = 0; i < n; i++) {
      order[i] = i;
    }
    Integer[] boxed = new Integer[n];
    for (int i = 0; i < n; i++) {
      boxed[i] = i;
    }
    final char[] keys = chars;
    Arrays.sort(boxed, new java.util.Comparator<Integer>() {
      public int compare(Integer a, Integer b) {
        return keys[a] - keys[b];
      }
    });
    encodeChars = new char[n];
    encodeBytes = new byte[n];
    encodeUnmappable = new boolean[n];
    for (int i = 0; i < n; i++) {
      encodeChars[i] = chars[boxed[i]];
      encodeBytes[i] = bytes[boxed[i]];
      encodeUnmappable[i] = unmappable[boxed[i]];
    }
  }

  private static int add(char[] chars, byte[] bytes, boolean[] unmappable, int n, char c, byte b, boolean none) {
    for (int i = 0; i < n; i++) {
      if (chars[i] == c) {
        bytes[i] = b;
        unmappable[i] = none;
        return n;
      }
    }
    chars[n] = c;
    bytes[n] = b;
    unmappable[n] = none;
    return n + 1;
  }

  public String historicalName() {
    return historical;
  }

  public boolean contains(Charset cs) {
    return cs == this || (asciiCompatible && cs.name().equals("US-ASCII"));
  }

  int lookup(char c) {
    int index = Arrays.binarySearch(encodeChars, c);
    if (index < 0 || encodeUnmappable[index]) {
      return -1;
    }
    return encodeBytes[index] & 0xff;
  }

  public CharsetDecoder newDecoder() {
    return new CharsetDecoder(this, 1.0f, 1.0f) {
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        while (src.hasRemaining()) {
          char c = b2c[src.get(src.position()) & 0xff];
          if (c == '�') {
            return CoderResult.unmappableForLength(1);
          }
          if (!dst.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          dst.put(c);
          src.position(src.position() + 1);
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }

  public CharsetEncoder newEncoder() {
    return new CharsetEncoder(this, 1.0f, 1.0f, new byte[] {replacementByte}) {
      private final Surrogate.Parser sgp = new Surrogate.Parser();

      public boolean canEncode(char c) {
        return lookup(c) >= 0;
      }

      protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        int mark = src.position();
        try {
          while (src.hasRemaining()) {
            char c = src.get();
            int b = lookup(c);
            if (b >= 0) {
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              dst.put((byte) b);
              mark++;
              continue;
            }
            if (Character.isSurrogate(c)) {
              if (sgp.parse(c, src) < 0) {
                return sgp.error();
              }
              return CoderResult.unmappableForLength(2);
            }
            return CoderResult.unmappableForLength(1);
          }
          return CoderResult.UNDERFLOW;
        } finally {
          src.position(mark);
        }
      }
    };
  }
}
