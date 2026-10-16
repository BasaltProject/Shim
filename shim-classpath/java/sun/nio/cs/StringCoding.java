/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public final class StringCoding {
  private StringCoding() { }

  public static Charset lookup(String charsetName) throws UnsupportedEncodingException {
    if (charsetName == null) {
      throw new NullPointerException("charsetName");
    }
    try {
      return Charset.forName(charsetName);
    } catch (IllegalArgumentException e) {
      throw new UnsupportedEncodingException(charsetName);
    }
  }

  public static char[] decode(Charset cs, byte[] bytes, int offset, int length) {
    if (cs == ISO_8859_1.INSTANCE) {
      char[] out = new char[length];
      for (int i = 0; i < length; i++) {
        out[i] = (char) (bytes[offset + i] & 0xff);
      }
      return out;
    }
    boolean ascii = true;
    for (int i = 0; i < length; i++) {
      if (bytes[offset + i] < 0) {
        ascii = false;
        break;
      }
    }
    if (ascii && (cs == UTF_8.INSTANCE || cs == US_ASCII.INSTANCE)) {
      char[] out = new char[length];
      for (int i = 0; i < length; i++) {
        out[i] = (char) bytes[offset + i];
      }
      return out;
    }
    CharsetDecoder decoder = cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    int n = (int) (length * (double) decoder.maxCharsPerByte()) + 1;
    CharBuffer out = CharBuffer.allocate(n);
    ByteBuffer in = ByteBuffer.wrap(bytes, offset, length);
    CoderResult cr = decoder.decode(in, out, true);
    if (cr.isUnderflow()) {
      cr = decoder.flush(out);
    }
    if (!cr.isUnderflow()) {
      throw new IllegalArgumentException(cr.toString());
    }
    char[] result = new char[out.position()];
    out.flip();
    out.get(result);
    return result;
  }

  public static byte[] encode(Charset cs, char[] chars, int offset, int length) {
    if (cs == UTF_8.INSTANCE || cs == ISO_8859_1.INSTANCE || cs == US_ASCII.INSTANCE) {
      boolean ascii = true;
      for (int i = 0; i < length; i++) {
        if (chars[offset + i] >= 0x80) {
          ascii = false;
          break;
        }
      }
      if (ascii) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
          out[i] = (byte) chars[offset + i];
        }
        return out;
      }
    }
    CharsetEncoder encoder = cs.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    int n = (int) (length * (double) encoder.maxBytesPerChar()) + 8;
    ByteBuffer out = ByteBuffer.allocate(n);
    CharBuffer in = CharBuffer.wrap(chars, offset, length);
    CoderResult cr = encoder.encode(in, out, true);
    if (cr.isUnderflow()) {
      cr = encoder.flush(out);
    }
    if (!cr.isUnderflow()) {
      throw new IllegalArgumentException(cr.toString());
    }
    byte[] result = new byte[out.position()];
    out.flip();
    out.get(result);
    return result;
  }
}
