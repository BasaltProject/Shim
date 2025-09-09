/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public final class StandardCharsets {
  private StandardCharsets() {}

  public static final Charset US_ASCII = new SimpleCharset("US-ASCII", new String[] {"iso-ir-6", "ANSI_X3.4-1968", "ascii"});
  public static final Charset ISO_8859_1 = new SimpleCharset("ISO-8859-1", new String[] {"iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "cp819"});
  public static final Charset UTF_8 = new SimpleCharset("UTF-8", new String[] {"UTF8", "unicode-1-1-utf-8"});
  public static final Charset UTF_16BE = new SimpleCharset("UTF-16BE", new String[] {"UTF_16BE", "ISO-10646-UCS-2", "X-UTF-16BE", "UnicodeBigUnmarked"});
  public static final Charset UTF_16LE = new SimpleCharset("UTF-16LE", new String[] {"UTF_16LE", "X-UTF-16LE", "UnicodeLittleUnmarked"});
  public static final Charset UTF_16 = new SimpleCharset("UTF-16", new String[] {"UTF_16", "utf16", "unicode", "UnicodeBig"});

  private static final class SimpleCharset extends Charset {
    SimpleCharset(String name, String[] aliases) {
      super(name, aliases);
    }

    @Override
    public CharsetDecoder newDecoder() {
      if ("UTF-8".equals(name())) {
        return new SimpleDecoder(this, 1.0f, 1.0f);
      }
      if ("UTF-16".equals(name()) || "UTF-16BE".equals(name()) || "UTF-16LE".equals(name())) {
        return new SimpleDecoder(this, 0.5f, 1.0f);
      }
      return new SimpleDecoder(this, 1.0f, 1.0f);
    }

    @Override
    public CharsetEncoder newEncoder() {
      if ("UTF-8".equals(name())) {
        return new SimpleEncoder(this, 1.1f, 4.0f);
      }
      if ("UTF-16".equals(name()) || "UTF-16BE".equals(name()) || "UTF-16LE".equals(name())) {
        return new SimpleEncoder(this, 2.0f, 4.0f);
      }
      return new SimpleEncoder(this, 1.0f, 1.0f);
    }
  }

  private static final class SimpleDecoder extends CharsetDecoder {
    private final String charsetName;

    SimpleDecoder(Charset cs, float avg, float max) {
      super(cs, avg, max);
      this.charsetName = cs.name();
    }

    @Override
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
      if ("ISO-8859-1".equals(charsetName)) {
        while (in.hasRemaining()) {
          if (!out.hasRemaining()) return CoderResult.OVERFLOW;
          byte b = in.get();
          out.put((char) (b & 0xFF));
        }
        return CoderResult.UNDERFLOW;
      } else if ("US-ASCII".equals(charsetName)) {
        while (in.hasRemaining()) {
          byte b = in.get();
          if (b < 0) {
            in.position(in.position() - 1);
            return CoderResult.malformedForLength(1);
          }
          if (!out.hasRemaining()) {
            in.position(in.position() - 1);
            return CoderResult.OVERFLOW;
          }
          out.put((char) b);
        }
        return CoderResult.UNDERFLOW;
      } else if ("UTF-16BE".equals(charsetName) || "UTF-16".equals(charsetName)) {
        while (in.remaining() >= 2) {
          if (!out.hasRemaining()) return CoderResult.OVERFLOW;
          byte b1 = in.get();
          byte b2 = in.get();
          char c = (char) (((b1 & 0xFF) << 8) | (b2 & 0xFF));
          out.put(c);
        }
        return CoderResult.UNDERFLOW;
      } else if ("UTF-16LE".equals(charsetName)) {
        while (in.remaining() >= 2) {
          if (!out.hasRemaining()) return CoderResult.OVERFLOW;
          byte b1 = in.get();
          byte b2 = in.get();
          char c = (char) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
          out.put(c);
        }
        return CoderResult.UNDERFLOW;
      } else {
        // UTF-8
        while (in.hasRemaining()) {
          int p = in.position();
          byte b = in.get();
          if ((b & 0x80) == 0) {
            if (!out.hasRemaining()) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put((char) b);
          } else if ((b & 0xE0) == 0xC0) {
            if (!in.hasRemaining()) {
              in.position(p);
              return CoderResult.UNDERFLOW;
            }
            byte b2 = in.get();
            if ((b2 & 0xC0) != 0x80) {
              in.position(p);
              return CoderResult.malformedForLength(1);
            }
            int cp = ((b & 0x1F) << 6) | (b2 & 0x3F);
            if (!out.hasRemaining()) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put((char) cp);
          } else if ((b & 0xF0) == 0xE0) {
            if (in.remaining() < 2) {
              in.position(p);
              return CoderResult.UNDERFLOW;
            }
            byte b2 = in.get();
            byte b3 = in.get();
            if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
              in.position(p);
              return CoderResult.malformedForLength(1);
            }
            int cp = ((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F);
            if (!out.hasRemaining()) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put((char) cp);
          } else if ((b & 0xF8) == 0xF0) {
            if (in.remaining() < 3) {
              in.position(p);
              return CoderResult.UNDERFLOW;
            }
            byte b2 = in.get();
            byte b3 = in.get();
            byte b4 = in.get();
            if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80 || (b4 & 0xC0) != 0x80) {
              in.position(p);
              return CoderResult.malformedForLength(1);
            }
            int cp = ((b & 0x07) << 18) | ((b2 & 0x3F) << 12) | ((b3 & 0x3F) << 6) | (b4 & 0x3F);
            if (out.remaining() < 2) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put(Character.highSurrogate(cp));
            out.put(Character.lowSurrogate(cp));
          } else {
            in.position(p);
            return CoderResult.malformedForLength(1);
          }
        }
        return CoderResult.UNDERFLOW;
      }
    }
  }

  private static final class SimpleEncoder extends CharsetEncoder {
    private final String charsetName;

    SimpleEncoder(Charset cs, float avg, float max) {
      super(cs, avg, max);
      this.charsetName = cs.name();
    }

    @Override
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
      if ("ISO-8859-1".equals(charsetName)) {
        while (in.hasRemaining()) {
          char c = in.get();
          if (c > 255) {
            in.position(in.position() - 1);
            return CoderResult.unmappableForLength(1);
          }
          if (!out.hasRemaining()) {
            in.position(in.position() - 1);
            return CoderResult.OVERFLOW;
          }
          out.put((byte) c);
        }
        return CoderResult.UNDERFLOW;
      } else if ("US-ASCII".equals(charsetName)) {
        while (in.hasRemaining()) {
          char c = in.get();
          if (c > 127) {
            in.position(in.position() - 1);
            return CoderResult.unmappableForLength(1);
          }
          if (!out.hasRemaining()) {
            in.position(in.position() - 1);
            return CoderResult.OVERFLOW;
          }
          out.put((byte) c);
        }
        return CoderResult.UNDERFLOW;
      } else if ("UTF-16BE".equals(charsetName) || "UTF-16".equals(charsetName)) {
        while (in.hasRemaining()) {
          if (out.remaining() < 2) return CoderResult.OVERFLOW;
          char c = in.get();
          out.put((byte) (c >> 8));
          out.put((byte) c);
        }
        return CoderResult.UNDERFLOW;
      } else if ("UTF-16LE".equals(charsetName)) {
        while (in.hasRemaining()) {
          if (out.remaining() < 2) return CoderResult.OVERFLOW;
          char c = in.get();
          out.put((byte) c);
          out.put((byte) (c >> 8));
        }
        return CoderResult.UNDERFLOW;
      } else {
        // UTF-8
        while (in.hasRemaining()) {
          int p = in.position();
          char c = in.get();
          if (c < 0x80) {
            if (!out.hasRemaining()) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put((byte) c);
          } else if (c < 0x800) {
            if (out.remaining() < 2) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put((byte) (0xC0 | (c >> 6)));
            out.put((byte) (0x80 | (c & 0x3F)));
          } else if (Character.isSurrogate(c)) {
            if (Character.isHighSurrogate(c)) {
              if (!in.hasRemaining()) {
                in.position(p);
                return CoderResult.UNDERFLOW;
              }
              char low = in.get();
              if (!Character.isLowSurrogate(low)) {
                in.position(p);
                return CoderResult.malformedForLength(1);
              }
              if (out.remaining() < 4) {
                in.position(p);
                return CoderResult.OVERFLOW;
              }
              int cp = Character.toCodePoint(c, low);
              out.put((byte) (0xF0 | (cp >> 18)));
              out.put((byte) (0x80 | ((cp >> 12) & 0x3F)));
              out.put((byte) (0x80 | ((cp >> 6) & 0x3F)));
              out.put((byte) (0x80 | (cp & 0x3F)));
            } else {
              in.position(p);
              return CoderResult.malformedForLength(1);
            }
          } else {
            if (out.remaining() < 3) {
              in.position(p);
              return CoderResult.OVERFLOW;
            }
            out.put((byte) (0xE0 | (c >> 12)));
            out.put((byte) (0x80 | ((c >> 6) & 0x3F)));
            out.put((byte) (0x80 | (c & 0x3F)));
          }
        }
        return CoderResult.UNDERFLOW;
      }
    }
  }
}
