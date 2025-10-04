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

public final class UnicodeCharset extends Charset implements HistoricallyNamedCharset {
  private static final int kNone = 0;
  private static final int kBig = 1;
  private static final int kLittle = 2;

  public static final UnicodeCharset UTF_16 = new UnicodeCharset("UTF-16", new String[] {"UTF_16", "utf16",
      "unicode", "UnicodeBig"}, "UTF-16", 2, kNone, kBig, true);
  public static final UnicodeCharset UTF_16BE = new UnicodeCharset("UTF-16BE", new String[] {"UTF_16BE",
      "ISO-10646-UCS-2", "X-UTF-16BE", "UnicodeBigUnmarked"}, "UnicodeBigUnmarked", 2, kBig, kBig, false);
  public static final UnicodeCharset UTF_16LE = new UnicodeCharset("UTF-16LE", new String[] {"UTF_16LE",
      "X-UTF-16LE", "UnicodeLittleUnmarked"}, "UnicodeLittleUnmarked", 2, kLittle, kLittle, false);
  public static final UnicodeCharset UTF_16LE_BOM = new UnicodeCharset("x-UTF-16LE-BOM",
      new String[] {"UnicodeLittle"}, "UnicodeLittle", 2, kNone, kLittle, true);
  public static final UnicodeCharset UTF_32 = new UnicodeCharset("UTF-32", new String[] {"UTF_32", "UTF32"},
      "UTF-32", 4, kNone, kBig, false);
  public static final UnicodeCharset UTF_32BE = new UnicodeCharset("UTF-32BE", new String[] {"UTF_32BE",
      "X-UTF-32BE"}, "UTF-32BE", 4, kBig, kBig, false);
  public static final UnicodeCharset UTF_32LE = new UnicodeCharset("UTF-32LE", new String[] {"UTF_32LE",
      "X-UTF-32LE"}, "UTF-32LE", 4, kLittle, kLittle, false);
  public static final UnicodeCharset UTF_32BE_BOM = new UnicodeCharset("X-UTF-32BE-BOM",
      new String[] {"UTF_32BE_BOM", "UTF-32BE-BOM"}, "X-UTF-32BE-BOM", 4, kBig, kBig, true);
  public static final UnicodeCharset UTF_32LE_BOM = new UnicodeCharset("X-UTF-32LE-BOM",
      new String[] {"UTF_32LE_BOM", "UTF-32LE-BOM"}, "X-UTF-32LE-BOM", 4, kLittle, kLittle, true);

  private final String historical;
  private final int width;
  private final int byteOrder;
  private final int defaultOrder;
  private final boolean bom;

  private UnicodeCharset(String name, String[] aliases, String historical, int width, int byteOrder,
      int defaultOrder, boolean bom) {
    super(name, aliases);
    this.historical = historical;
    this.width = width;
    this.byteOrder = byteOrder;
    this.defaultOrder = defaultOrder;
    this.bom = bom;
  }

  public String historicalName() {
    return historical;
  }

  public boolean contains(Charset cs) {
    return true;
  }

  public CharsetDecoder newDecoder() {
    return width == 2 ? new Decoder16(this) : new Decoder32(this);
  }

  public CharsetEncoder newEncoder() {
    return width == 2 ? new Encoder16(this) : new Encoder32(this);
  }

  private static byte[] replacementBytes(int width, int order) {
    if (width == 2) {
      return order == kLittle ? new byte[] {(byte) 0xfd, (byte) 0xff} : new byte[] {(byte) 0xff, (byte) 0xfd};
    }
    return order == kLittle ? new byte[] {(byte) 0xfd, (byte) 0xff, 0, 0}
        : new byte[] {0, 0, (byte) 0xff, (byte) 0xfd};
  }

  private static final class Decoder16 extends CharsetDecoder {
    private final UnicodeCharset cs;
    private int current;

    Decoder16(UnicodeCharset cs) {
      super(cs, 0.5f, 1.0f);
      this.cs = cs;
      this.current = cs.byteOrder;
    }

    protected void implReset() {
      current = cs.byteOrder;
    }

    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
      int mark = src.position();
      try {
        while (src.remaining() > 1) {
          int b1 = src.get() & 0xff;
          int b2 = src.get() & 0xff;
          if (current == kNone) {
            char c = (char) ((b1 << 8) | b2);
            if (c == 0xfeff) {
              current = kBig;
              mark += 2;
              continue;
            } else if (c == 0xfffe) {
              current = kLittle;
              mark += 2;
              continue;
            }
            current = cs.defaultOrder;
          }
          char c = current == kBig ? (char) ((b1 << 8) | b2) : (char) ((b2 << 8) | b1);
          if (Character.isSurrogate(c)) {
            if (Character.isHighSurrogate(c)) {
              if (src.remaining() < 2) {
                return CoderResult.UNDERFLOW;
              }
              int b3 = src.get() & 0xff;
              int b4 = src.get() & 0xff;
              char c2 = current == kBig ? (char) ((b3 << 8) | b4) : (char) ((b4 << 8) | b3);
              if (!Character.isLowSurrogate(c2)) {
                return CoderResult.malformedForLength(4);
              }
              if (dst.remaining() < 2) {
                return CoderResult.OVERFLOW;
              }
              mark += 4;
              dst.put(c);
              dst.put(c2);
              continue;
            }
            return CoderResult.malformedForLength(2);
          }
          if (!dst.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          mark += 2;
          dst.put(c);
        }
        return CoderResult.UNDERFLOW;
      } finally {
        src.position(mark);
      }
    }
  }

  private static final class Encoder16 extends CharsetEncoder {
    private final UnicodeCharset cs;
    private final int order;
    private boolean needsMark;
    private final Surrogate.Parser sgp = new Surrogate.Parser();

    Encoder16(UnicodeCharset cs) {
      super(cs, 2.0f, cs.bom ? 4.0f : 2.0f, replacementBytes(2, cs.defaultOrder));
      this.cs = cs;
      this.order = cs.byteOrder == kNone ? cs.defaultOrder : cs.byteOrder;
      this.needsMark = cs.bom;
    }

    protected void implReset() {
      needsMark = cs.bom;
    }

    public boolean canEncode(char c) {
      return !Character.isSurrogate(c);
    }

    private void put(ByteBuffer dst, char c) {
      if (order == kBig) {
        dst.put((byte) (c >> 8));
        dst.put((byte) c);
      } else {
        dst.put((byte) c);
        dst.put((byte) (c >> 8));
      }
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      int mark = src.position();
      if (needsMark && src.hasRemaining()) {
        if (dst.remaining() < 2) {
          return CoderResult.OVERFLOW;
        }
        put(dst, (char) 0xfeff);
        needsMark = false;
      }
      try {
        while (src.hasRemaining()) {
          char c = src.get();
          if (!Character.isSurrogate(c)) {
            if (dst.remaining() < 2) {
              return CoderResult.OVERFLOW;
            }
            mark++;
            put(dst, c);
            continue;
          }
          int d = sgp.parse(c, src);
          if (d < 0) {
            return sgp.error();
          }
          if (dst.remaining() < 4) {
            return CoderResult.OVERFLOW;
          }
          mark += 2;
          put(dst, Character.highSurrogate(d));
          put(dst, Character.lowSurrogate(d));
        }
        return CoderResult.UNDERFLOW;
      } finally {
        src.position(mark);
      }
    }
  }

  private static final class Decoder32 extends CharsetDecoder {
    private final UnicodeCharset cs;
    private int current = kNone;

    Decoder32(UnicodeCharset cs) {
      super(cs, 0.25f, 1.0f);
      this.cs = cs;
    }

    protected void implReset() {
      current = kNone;
    }

    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
      int mark = src.position();
      try {
        while (src.remaining() >= 4) {
          int b1 = src.get() & 0xff;
          int b2 = src.get() & 0xff;
          int b3 = src.get() & 0xff;
          int b4 = src.get() & 0xff;
          int big = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
          int little = (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
          if (current == kNone) {
            if (big == 0xfeff && cs.byteOrder != kLittle) {
              current = kBig;
              mark += 4;
              continue;
            } else if (big == 0xfffe0000 && cs.byteOrder != kBig) {
              current = kLittle;
              mark += 4;
              continue;
            }
            current = cs.byteOrder == kNone ? kBig : cs.byteOrder;
          }
          int cp = current == kBig ? big : little;
          if (cp >= 0 && cp < 0x10000) {
            if (!dst.hasRemaining()) {
              return CoderResult.OVERFLOW;
            }
            mark += 4;
            dst.put((char) cp);
          } else if (Character.isValidCodePoint(cp)) {
            if (dst.remaining() < 2) {
              return CoderResult.OVERFLOW;
            }
            mark += 4;
            dst.put(Character.highSurrogate(cp));
            dst.put(Character.lowSurrogate(cp));
          } else {
            return CoderResult.malformedForLength(4);
          }
        }
        return CoderResult.UNDERFLOW;
      } finally {
        src.position(mark);
      }
    }
  }

  private static final class Encoder32 extends CharsetEncoder {
    private final UnicodeCharset cs;
    private final int order;
    private boolean needsMark;
    private final Surrogate.Parser sgp = new Surrogate.Parser();

    Encoder32(UnicodeCharset cs) {
      super(cs, 4.0f, cs.bom ? 8.0f : 4.0f, replacementBytes(4, cs.defaultOrder));
      this.cs = cs;
      this.order = cs.byteOrder == kNone ? cs.defaultOrder : cs.byteOrder;
      this.needsMark = cs.bom;
    }

    protected void implReset() {
      needsMark = cs.bom;
    }

    public boolean canEncode(char c) {
      return !Character.isSurrogate(c);
    }

    private void put(ByteBuffer dst, int cp) {
      if (order == kBig) {
        dst.put((byte) (cp >> 24));
        dst.put((byte) (cp >> 16));
        dst.put((byte) (cp >> 8));
        dst.put((byte) cp);
      } else {
        dst.put((byte) cp);
        dst.put((byte) (cp >> 8));
        dst.put((byte) (cp >> 16));
        dst.put((byte) (cp >> 24));
      }
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      int mark = src.position();
      if (needsMark && src.hasRemaining()) {
        if (dst.remaining() < 4) {
          return CoderResult.OVERFLOW;
        }
        put(dst, 0xfeff);
        needsMark = false;
      }
      try {
        while (src.hasRemaining()) {
          char c = src.get();
          int cp = c;
          if (Character.isSurrogate(c)) {
            cp = sgp.parse(c, src);
            if (cp < 0) {
              return sgp.error();
            }
          }
          if (dst.remaining() < 4) {
            return CoderResult.OVERFLOW;
          }
          mark += Character.charCount(cp);
          put(dst, cp);
        }
        return CoderResult.UNDERFLOW;
      } finally {
        src.position(mark);
      }
    }
  }
}
