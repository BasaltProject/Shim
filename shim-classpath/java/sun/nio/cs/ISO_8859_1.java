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

public final class ISO_8859_1 extends Charset implements HistoricallyNamedCharset {
  public static final ISO_8859_1 INSTANCE = new ISO_8859_1();

  private ISO_8859_1() {
    super("ISO-8859-1", new String[] {"iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "cp819", "csISOLatin1",
        "819",
        "IBM-819", "ISO8859_1", "ISO_8859-1:1987", "ISO_8859_1", "8859_1", "ISO8859-1"});
  }

  public String historicalName() {
    return "ISO8859_1";
  }

  public boolean contains(Charset cs) {
    return cs.name().equals("US-ASCII") || cs instanceof ISO_8859_1 || cs instanceof US_ASCII;
  }

  public CharsetDecoder newDecoder() {
    return new CharsetDecoder(this, 1.0f, 1.0f) {
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        int n = Math.min(src.remaining(), dst.remaining());
        for (int i = 0; i < n; i++) {
          dst.put((char) (src.get() & 0xff));
        }
        return src.hasRemaining() ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
      }
    };
  }

  public CharsetEncoder newEncoder() {
    return new Encoder(this, 0x100);
  }

  static final class Encoder extends CharsetEncoder {
    private final int limit;
    private final Surrogate.Parser sgp = new Surrogate.Parser();

    Encoder(Charset cs, int limit) {
      super(cs, 1.0f, 1.0f);
      this.limit = limit;
    }

    public boolean canEncode(char c) {
      return c < limit;
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      int mark = src.position();
      try {
        while (src.hasRemaining()) {
          char c = src.get();
          if (c < limit) {
            if (!dst.hasRemaining()) {
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) c);
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
  }
}
