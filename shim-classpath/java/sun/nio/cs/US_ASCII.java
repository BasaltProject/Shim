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

public final class US_ASCII extends Charset implements HistoricallyNamedCharset {
  public static final US_ASCII INSTANCE = new US_ASCII();

  private US_ASCII() {
    super("US-ASCII", new String[] {"iso-ir-6", "ANSI_X3.4-1986", "ISO_646.irv:1991", "ASCII", "ISO646-US", "us", "IBM367",
        "cp367", "csASCII", "646", "iso_646.irv:1983", "ANSI_X3.4-1968", "ascii7"});
  }

  public String historicalName() {
    return "ASCII";
  }

  public boolean contains(Charset cs) {
    return cs instanceof US_ASCII;
  }

  public CharsetDecoder newDecoder() {
    return new CharsetDecoder(this, 1.0f, 1.0f) {
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        while (src.hasRemaining()) {
          byte b = src.get(src.position());
          if (b < 0) {
            return CoderResult.malformedForLength(1);
          }
          if (!dst.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          dst.put((char) b);
          src.position(src.position() + 1);
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }

  public CharsetEncoder newEncoder() {
    return new ISO_8859_1.Encoder(this, 0x80);
  }
}
