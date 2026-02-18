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

public final class ISO2022_KR extends Charset implements HistoricallyNamedCharset {
  public static final ISO2022_KR INSTANCE = new ISO2022_KR();

  private ISO2022_KR() {
    super("ISO-2022-KR", new String[] {"ISO2022KR", "csISO2022KR"});
  }

  @Override
  public String historicalName() {
    return "ISO2022KR";
  }

  @Override
  public boolean contains(Charset cs) {
    return cs.name().equals("US-ASCII") || cs.name().equals("EUC-KR") || cs == this;
  }

  @Override
  public CharsetDecoder newDecoder() {
    final CharsetDecoder euc = Charset.forName("EUC-KR").newDecoder();
    return new CharsetDecoder(this, 1.0f, 1.0f) {
      private boolean shifted;

      @Override
      protected void implReset() {
        shifted = false;
      }

      @Override
      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        int mark = src.position();
        while (src.hasRemaining()) {
          int b1 = src.get() & 0xff;
          if (b1 == 0x1b && src.remaining() >= 3 && src.get(src.position()) == '$' && src.get(src.position() + 1) == ')'
              && src.get(src.position() + 2) == 'C') {
            src.position(src.position() + 3);
            mark = src.position();
            continue;
          }
          if (b1 == 0x1b && src.remaining() < 3 && couldBeHeader(src)) {
            src.position(mark);
            return CoderResult.UNDERFLOW;
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
          if (!shifted) {
            if (!dst.hasRemaining()) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            dst.put((char) b1);
            mark = src.position();
            continue;
          }
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
          euc.reset();
          CharBuffer one = CharBuffer.allocate(2);
          CoderResult r = euc.decode(ByteBuffer.wrap(new byte[] {(byte) (b1 | 0x80), (byte) (b2 | 0x80)}), one, true);
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
        }
        return CoderResult.UNDERFLOW;
      }

      private boolean couldBeHeader(ByteBuffer src) {
        byte[] h = {'$', ')', 'C'};
        int n = src.remaining();
        for (int i = 0; i < n; i++) {
          if (src.get(src.position() + i) != h[i]) {
            return false;
          }
        }
        return true;
      }
    };
  }

  @Override
  public boolean canEncode() {
    return true;
  }

  @Override
  public CharsetEncoder newEncoder() {
    final CharsetEncoder euc = Charset.forName("EUC-KR").newEncoder();
    return new CharsetEncoder(this, 4.0f, 8.0f, new byte[] {0x3f}) {
      private boolean shifted;
      private boolean headerWritten;

      @Override
      protected void implReset() {
        shifted = false;
        headerWritten = false;
      }

      @Override
      public boolean isLegalReplacement(byte[] repl) {
        return true;
      }

      @Override
      public boolean canEncode(char c) {
        return c < 0x80 || (!Character.isSurrogate(c) && euc.canEncode(c));
      }

      @Override
      protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        int mark = src.position();
        while (src.hasRemaining()) {
          char c = src.get();
          if (c < 0x80) {
            int need = (shifted ? 1 : 0) + 1;
            if (dst.remaining() < need) {
              src.position(mark);
              return CoderResult.OVERFLOW;
            }
            if (shifted) {
              dst.put((byte) 0x0f);
              shifted = false;
            }
            dst.put((byte) c);
            if (c == '\n') {
              headerWritten = false;
            }
            mark = src.position();
            continue;
          }
          byte[] pair = null;
          if (!Character.isSurrogate(c)) {
            ByteBuffer two = ByteBuffer.allocate(4);
            euc.reset();
            CoderResult r = euc.encode(CharBuffer.wrap(new char[] {c}), two, true);
            if (!r.isError() && two.position() == 2 && (two.get(0) & 0xff) >= 0xa1) {
              pair = new byte[] {(byte) (two.get(0) & 0x7f), (byte) (two.get(1) & 0x7f)};
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
              if (dst.remaining() < 2) {
                src.position(mark);
                return CoderResult.OVERFLOW;
              }
              if (shifted) {
                dst.put((byte) 0x0f);
                shifted = false;
              }
              dst.put((byte) 0x3f);
              if (len == 2) {
                src.get();
              }
              mark = src.position();
              continue;
            }
            src.position(mark);
            return err;
          }
          int need = 2 + (shifted ? 0 : 1) + (headerWritten ? 0 : 4);
          if (dst.remaining() < need) {
            src.position(mark);
            return CoderResult.OVERFLOW;
          }
          if (!headerWritten) {
            dst.put((byte) 0x1b).put((byte) '$').put((byte) ')').put((byte) 'C');
            headerWritten = true;
          }
          if (!shifted) {
            dst.put((byte) 0x0e);
            shifted = true;
          }
          dst.put(pair[0]).put(pair[1]);
          mark = src.position();
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }
}
