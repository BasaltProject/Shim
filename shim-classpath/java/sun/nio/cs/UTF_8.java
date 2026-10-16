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

public final class UTF_8 extends Charset implements HistoricallyNamedCharset {
  public static final UTF_8 INSTANCE = new UTF_8();

  private UTF_8() {
    super("UTF-8", new String[] {"UTF8", "unicode-1-1-utf-8"});
  }

  public String historicalName() {
    return "UTF8";
  }

  public boolean contains(Charset cs) {
    return true;
  }

  public CharsetDecoder newDecoder() {
    return new Decoder(this);
  }

  public CharsetEncoder newEncoder() {
    return new Encoder(this);
  }

  private static boolean notContinuation(int b) {
    return (b & 0xc0) != 0x80;
  }

  private static boolean malformed3(int b1, int b2, int b3) {
    return (b1 == 0xe0 && (b2 & 0xe0) == 0x80) || notContinuation(b2) || notContinuation(b3);
  }

  private static boolean malformed3Second(int b1, int b2) {
    return (b1 == 0xe0 && (b2 & 0xe0) == 0x80) || notContinuation(b2);
  }

  private static boolean malformed4Second(int b1, int b2) {
    return (b1 == 0xf0 && (b2 < 0x90 || b2 > 0xbf)) || (b1 == 0xf4 && (b2 & 0xf0) != 0x80) || notContinuation(b2);
  }

  static CoderResult decodeLoop(ByteBuffer src, CharBuffer dst, boolean cesu) {
    if (cesu) {
      return decodeCesu(src, dst);
    }
    int start = src.position();
    int end = src.limit();
    while (start < end) {
      int b1 = src.get(start) & 0xff;
      int remaining = end - start;
      if (b1 < 0x80) {
        if (!dst.hasRemaining()) {
          return finish(src, start, CoderResult.OVERFLOW);
        }
        dst.put((char) b1);
        start++;
      } else if (b1 >= 0xc2 && b1 <= 0xdf) {
        if (remaining < 2) {
          return finish(src, start, CoderResult.UNDERFLOW);
        }
        if (!dst.hasRemaining()) {
          return finish(src, start, CoderResult.OVERFLOW);
        }
        int b2 = src.get(start + 1) & 0xff;
        if (notContinuation(b2)) {
          return finish(src, start, CoderResult.malformedForLength(1));
        }
        dst.put((char) (((b1 & 0x1f) << 6) | (b2 & 0x3f)));
        start += 2;
      } else if (b1 >= 0xe0 && b1 <= 0xef) {
        if (remaining < 3 || !dst.hasRemaining()) {
          if (remaining > 1 && malformed3Second(b1, src.get(start + 1) & 0xff)) {
            return finish(src, start, CoderResult.malformedForLength(1));
          }
          return finish(src, start, remaining < 3 ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW);
        }
        int b2 = src.get(start + 1) & 0xff;
        int b3 = src.get(start + 2) & 0xff;
        if (malformed3(b1, b2, b3)) {
          int length = (b1 == 0xe0 && (b2 & 0xe0) == 0x80) || notContinuation(b2) ? 1 : 2;
          return finish(src, start, CoderResult.malformedForLength(length));
        }
        char c = (char) (((b1 & 0x0f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f));
        if (Character.isSurrogate(c)) {
          return finish(src, start, CoderResult.malformedForLength(3));
        }
        dst.put(c);
        start += 3;
      } else if (b1 >= 0xf0 && b1 <= 0xf7) {
        if (remaining < 4 || dst.remaining() < 2) {
          if (b1 > 0xf4 || (remaining > 1 && malformed4Second(b1, src.get(start + 1) & 0xff))) {
            return finish(src, start, CoderResult.malformedForLength(1));
          }
          if (remaining > 2 && notContinuation(src.get(start + 2) & 0xff)) {
            return finish(src, start, CoderResult.malformedForLength(2));
          }
          return finish(src, start, remaining < 4 ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW);
        }
        int b2 = src.get(start + 1) & 0xff;
        int b3 = src.get(start + 2) & 0xff;
        int b4 = src.get(start + 3) & 0xff;
        int uc = ((b1 & 0x07) << 18) | ((b2 & 0x3f) << 12) | ((b3 & 0x3f) << 6) | (b4 & 0x3f);
        if (notContinuation(b2) || notContinuation(b3) || notContinuation(b4)
            || !Character.isSupplementaryCodePoint(uc)) {
          int length;
          if (b1 > 0xf4 || (b1 == 0xf0 && (b2 < 0x90 || b2 > 0xbf)) || (b1 == 0xf4 && (b2 & 0xf0) != 0x80)
              || notContinuation(b2)) {
            length = 1;
          } else if (notContinuation(b3)) {
            length = 2;
          } else {
            length = 3;
          }
          return finish(src, start, CoderResult.malformedForLength(length));
        }
        dst.put(Character.highSurrogate(uc));
        dst.put(Character.lowSurrogate(uc));
        start += 4;
      } else {
        return finish(src, start, CoderResult.malformedForLength(1));
      }
    }
    return finish(src, start, CoderResult.UNDERFLOW);
  }

  private static CoderResult decodeCesu(ByteBuffer src, CharBuffer dst) {
    int start = src.position();
    int end = src.limit();
    while (start < end) {
      int b1 = src.get(start) & 0xff;
      if (b1 < 0x80) {
        if (!dst.hasRemaining()) {
          return finish(src, start, CoderResult.OVERFLOW);
        }
        dst.put((char) b1);
        start++;
        continue;
      }
      int need;
      if (b1 >= 0xc2 && b1 <= 0xdf) {
        need = 2;
      } else if (b1 >= 0xe0 && b1 <= 0xef) {
        need = 3;
      } else {
        return finish(src, start, CoderResult.malformedForLength(1));
      }
      int cp = need == 2 ? b1 & 0x1f : b1 & 0x0f;
      boolean underflow = false;
      for (int i = 1; i < need; i++) {
        if (start + i >= end) {
          underflow = true;
          break;
        }
        int b = src.get(start + i) & 0xff;
        int lo = 0x80;
        if (i == 1 && b1 == 0xe0) {
          lo = 0xa0;
        }
        if (b < lo || b > 0xbf) {
          return finish(src, start, CoderResult.malformedForLength(i));
        }
        cp = (cp << 6) | (b & 0x3f);
      }
      if (underflow) {
        return finish(src, start, CoderResult.UNDERFLOW);
      }
      if (!dst.hasRemaining()) {
        return finish(src, start, CoderResult.OVERFLOW);
      }
      dst.put((char) cp);
      start += need;
    }
    return finish(src, start, CoderResult.UNDERFLOW);
  }

  private static CoderResult finish(ByteBuffer src, int position, CoderResult result) {
    src.position(position);
    return result;
  }

  static final class Decoder extends CharsetDecoder {
    Decoder(Charset cs) {
      super(cs, 1.0f, 1.0f);
    }

    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
      return UTF_8.decodeLoop(src, dst, false);
    }
  }

  static final class Encoder extends CharsetEncoder {
    private final Surrogate.Parser sgp = new Surrogate.Parser();

    Encoder(Charset cs) {
      super(cs, 1.1f, 3.0f);
    }

    public boolean canEncode(char c) {
      return !Character.isSurrogate(c);
    }

    public boolean isLegalReplacement(byte[] repl) {
      return repl.length == 1 && repl[0] >= 0 || super.isLegalReplacement(repl);
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
            mark++;
          } else if (c < 0x800) {
            if (dst.remaining() < 2) {
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) (0xc0 | (c >> 6)));
            dst.put((byte) (0x80 | (c & 0x3f)));
            mark++;
          } else if (Character.isSurrogate(c)) {
            int cp = sgp.parse(c, src);
            if (cp < 0) {
              return sgp.error();
            }
            if (dst.remaining() < 4) {
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) (0xf0 | (cp >> 18)));
            dst.put((byte) (0x80 | ((cp >> 12) & 0x3f)));
            dst.put((byte) (0x80 | ((cp >> 6) & 0x3f)));
            dst.put((byte) (0x80 | (cp & 0x3f)));
            mark += 2;
          } else {
            if (dst.remaining() < 3) {
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) (0xe0 | (c >> 12)));
            dst.put((byte) (0x80 | ((c >> 6) & 0x3f)));
            dst.put((byte) (0x80 | (c & 0x3f)));
            mark++;
          }
        }
        return CoderResult.UNDERFLOW;
      } finally {
        src.position(mark);
      }
    }
  }
}
