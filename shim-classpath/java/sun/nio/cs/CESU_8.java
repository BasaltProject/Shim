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

public final class CESU_8 extends Charset implements HistoricallyNamedCharset {
  public static final CESU_8 INSTANCE = new CESU_8();

  private CESU_8() {
    super("CESU-8", new String[] {"CESU8", "csCESU-8"});
  }

  public String historicalName() {
    return "CESU8";
  }

  public boolean contains(Charset cs) {
    return true;
  }

  public CharsetDecoder newDecoder() {
    return new CharsetDecoder(this, 1.0f, 1.0f) {
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        return UTF_8.decodeLoop(src, dst, true);
      }
    };
  }

  public CharsetEncoder newEncoder() {
    return new CharsetEncoder(this, 1.1f, 3.0f) {
      private final Surrogate.Parser sgp = new Surrogate.Parser();

      public boolean canEncode(char c) {
        return !Character.isSurrogate(c);
      }

      private void putThree(ByteBuffer dst, char c) {
        dst.put((byte) (0xe0 | (c >> 12)));
        dst.put((byte) (0x80 | ((c >> 6) & 0x3f)));
        dst.put((byte) (0x80 | (c & 0x3f)));
      }

      protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        int mark = src.position();
        try {
          while (src.hasRemaining()) {
            char c = src.get();
            if (c < 0x80) {
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              dst.put((byte) c);
            } else if (c < 0x800) {
              if (dst.remaining() < 2) {
                return CoderResult.OVERFLOW;
              }
              dst.put((byte) (0xc0 | (c >> 6)));
              dst.put((byte) (0x80 | (c & 0x3f)));
            } else if (Character.isSurrogate(c)) {
              int cp = sgp.parse(c, src);
              if (cp < 0) {
                return sgp.error();
              }
              if (dst.remaining() < 6) {
                return CoderResult.OVERFLOW;
              }
              putThree(dst, Character.highSurrogate(cp));
              putThree(dst, Character.lowSurrogate(cp));
              mark++;
            } else {
              if (dst.remaining() < 3) {
                return CoderResult.OVERFLOW;
              }
              putThree(dst, c);
            }
            mark++;
          }
          return CoderResult.UNDERFLOW;
        } finally {
          src.position(mark);
        }
      }
    };
  }
}
