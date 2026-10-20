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
import java.nio.charset.CodingErrorAction;

public final class ISO2022_JP extends Charset implements HistoricallyNamedCharset {
  public static final ISO2022_JP INSTANCE = new ISO2022_JP();

  private static final int kAscii = 0;
  private static final int kRoman = 1;
  private static final int kKana = 2;
  private static final int kJis = 3;

  private ISO2022_JP() {
    super("ISO-2022-JP", new String[] {"iso2022jp", "jis", "csISO2022JP", "jis_encoding", "csjisencoding"});
  }

  @Override
  public String historicalName() {
    return "ISO2022JP";
  }

  @Override
  public boolean contains(Charset cs) {
    return cs.name().equals("US-ASCII") || cs.name().equals("x-JIS0208") || cs == this;
  }

  @Override
  public CharsetDecoder newDecoder() {
    final CharsetDecoder jis = Charset.forName("x-JIS0208").newDecoder();
    return new CharsetDecoder(this, 0.5f, 1.0f) {
      private int mode = kAscii;
      private boolean shifted;

      @Override
      protected void implReset() {
        mode = kAscii;
        shifted = false;
      }

      @Override
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        int mark = src.position();
        try {
          while (src.hasRemaining()) {
            int b1 = src.get() & 0xff;
            if (b1 == 0x1b) {
              if (src.remaining() < 2) {
                return CoderResult.UNDERFLOW;
              }
              int b2 = src.get() & 0xff;
              int b3 = src.get() & 0xff;
              int next = -1;
              if (b2 == '(' && b3 == 'B') {
                next = kAscii;
              } else if (b2 == '(' && b3 == 'J') {
                next = kRoman;
              } else if (b2 == '(' && b3 == 'I') {
                next = kKana;
              } else if (b2 == '$' && (b3 == '@' || b3 == 'B')) {
                next = kJis;
              }
              if (next < 0) {
                src.position(mark);
                return CoderResult.malformedForLength(b2 == '(' || b2 == '$' ? 3 : 1);
              }
              mode = next;
              mark = src.position();
              continue;
            }
            if (b1 == 0x0e) {
              shifted = true;
              mark = src.position();
              continue;
            }
            if (b1 == 0x0f) {
              shifted = false;
              mark = src.position();
              continue;
            }
            if (b1 >= 0x80) {
              src.position(mark);
              return CoderResult.malformedForLength(1);
            }
            if (b1 == '\n' || b1 == '\r') {
              if (!dst.hasRemaining()) {
                src.position(mark);
                return CoderResult.OVERFLOW;
              }
              dst.put((char) b1);
              mode = kAscii;
              mark = src.position();
              continue;
            }
            char out;
            if (mode == kJis) {
              if (b1 < 0x21 || b1 > 0x7e) {
                src.position(mark);
                return CoderResult.malformedForLength(1);
              }
              if (!src.hasRemaining()) {
                src.position(mark);
                return CoderResult.UNDERFLOW;
              }
              int b2 = src.get() & 0xff;
              if (b2 < 0x21 || b2 > 0x7e) {
                src.position(mark);
                return CoderResult.malformedForLength(1);
              }
              jis.reset();
              CharBuffer one = CharBuffer.allocate(2);
              CoderResult r = jis.decode(ByteBuffer.wrap(new byte[] {(byte) b1, (byte) b2}), one, true);
              if (r.isError() || one.position() == 0) {
                src.position(mark);
                return CoderResult.unmappableForLength(2);
              }
              if (!dst.hasRemaining()) {
                src.position(mark);
                return CoderResult.OVERFLOW;
              }
              dst.put(one.get(0));
              mark = src.position();
              continue;
            }
            if (mode == kKana || (shifted && b1 >= 0x21 && b1 <= 0x5f)) {
              if (b1 >= 0x21 && b1 <= 0x5f) {
                out = (char) (0xff61 + (b1 - 0x21));
              } else if (b1 < 0x21 || b1 == 0x7f) {
                out = (char) b1;
              } else {
                src.position(mark);
                return CoderResult.unmappableForLength(1);
              }
            } else if (mode == kRoman) {
              out = b1 == 0x5c ? '¥' : (b1 == 0x7e ? '‾' : (char) b1);
            } else {
              out = (char) b1;
            }
            if (!dst.hasRemaining()) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            dst.put(out);
            mark = src.position();
          }
          return CoderResult.UNDERFLOW;
        } finally {
          if (src.position() > mark && mark >= 0) {
          }
        }
      }
    };
  }

  @Override
  public boolean canEncode() {
    return true;
  }

  @Override
  public CharsetEncoder newEncoder() {
    final CharsetEncoder jis = Charset.forName("x-JIS0208").newEncoder();
    return new CharsetEncoder(this, 4.0f, 8.0f, new byte[] {0x21, 0x29}) {
      private int mode = kAscii;

      @Override
      protected void implReset() {
        mode = kAscii;
      }

      @Override
      public boolean isLegalReplacement(byte[] repl) {
        return true;
      }

      @Override
      protected CoderResult implFlush(ByteBuffer out) {
        if (mode != kAscii) {
          if (out.remaining() < 3) {
            return CoderResult.OVERFLOW;
          }
          out.put((byte) 0x1b).put((byte) '(').put((byte) 'B');
          mode = kAscii;
        }
        return CoderResult.UNDERFLOW;
      }

      private boolean switchTo(ByteBuffer dst, int target) {
        if (mode == target) {
          return true;
        }
        if (dst.remaining() < 3) {
          return false;
        }
        dst.put((byte) 0x1b);
        if (target == kJis) {
          dst.put((byte) '$').put((byte) 'B');
        } else {
          dst.put((byte) '(').put((byte) (target == kAscii ? 'B' : (target == kRoman ? 'J' : 'I')));
        }
        mode = target;
        return true;
      }

      @Override
      public boolean canEncode(char c) {
        if (c < 0x80 || c == '¥' || c == '‾' || (c >= 0xff61 && c <= 0xff9f)) {
          return true;
        }
        return jis.canEncode(c);
      }

      @Override
      protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        int mark = src.position();
        while (src.hasRemaining()) {
          char c = src.get();
          if (c < 0x80) {
            if (dst.remaining() < 4 && !(mode == kAscii && dst.remaining() >= 1)) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            if (!switchTo(dst, kAscii) || !dst.hasRemaining()) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) c);
          } else if (c == '¥' || c == '‾') {
            if (dst.remaining() < 4 || !switchTo(dst, kRoman)) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) (c == '¥' ? 0x5c : 0x7e));
          } else if (c >= 0xff61 && c <= 0xff9f) {
            if (dst.remaining() < 4 || !switchTo(dst, kKana)) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            dst.put((byte) (c - 0xff61 + 0x21));
          } else {
            byte[] pair = null;
            if (!Character.isSurrogate(c)) {
              ByteBuffer two = ByteBuffer.allocate(4);
              jis.reset();
              CoderResult r = jis.encode(CharBuffer.wrap(new char[] {c}), two, true);
              if (!r.isError() && two.position() == 2) {
                pair = new byte[] {two.get(0), two.get(1)};
              }
            }
            if (pair == null) {
              int len = 1;
              CoderResult err;
              if (Character.isHighSurrogate(c) && src.hasRemaining() && Character.isLowSurrogate(src.get(src.position()))) {
                len = 2;
                err = CoderResult.unmappableForLength(2);
              } else if (Character.isSurrogate(c)) {
                if (Character.isHighSurrogate(c) && !src.hasRemaining()) {
                  src.position(mark);
                  return CoderResult.UNDERFLOW;
                }
                err = CoderResult.malformedForLength(1);
              } else {
                err = CoderResult.unmappableForLength(1);
              }
              CodingErrorAction action = err.isMalformed() ? malformedInputAction() : unmappableCharacterAction();
              if (action == CodingErrorAction.REPLACE) {
                if (dst.remaining() < 5 || !switchTo(dst, kJis)) {
                  src.position(mark);
                  return CoderResult.OVERFLOW;
                }
                dst.put((byte) 0x21).put((byte) 0x29);
                if (len == 2) {
                  src.get();
                }
                mark = src.position();
                continue;
              }
              src.position(mark);
              return err;
            }
            if (dst.remaining() < 5 || !switchTo(dst, kJis)) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            dst.put(pair[0]).put(pair[1]);
          }
          mark = src.position();
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }
}
