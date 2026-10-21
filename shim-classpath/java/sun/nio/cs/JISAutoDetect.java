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

// Picks ISO-2022-JP, EUC-JP or Shift_JIS from a trial decode of the buffered input at the first escape or 8-bit byte.
public final class JISAutoDetect extends Charset implements HistoricallyNamedCharset {
  public static final JISAutoDetect INSTANCE = new JISAutoDetect();

  private static final String kContains = ",EUC-JP,ISO-2022-JP,ISO-2022-JP-2,Shift_JIS,US-ASCII,x-windows-50220,"
      + "x-windows-50221,x-windows-iso2022jp,";

  private JISAutoDetect() {
    super("x-JISAutoDetect", new String[] {"JISAutoDetect"});
  }

  public String historicalName() {
    return "JISAutoDetect";
  }

  public boolean contains(Charset cs) {
    return cs == this || kContains.indexOf("," + cs.name() + ",") >= 0;
  }

  public boolean canEncode() {
    return false;
  }

  public CharsetEncoder newEncoder() {
    throw new UnsupportedOperationException();
  }

  public CharsetDecoder newDecoder() {
    return new Decoder(this);
  }

  private static final class Trial {
    boolean error;
    int consumed;
    int kana;
  }

  private static final class Decoder extends CharsetDecoder {
    private CharsetDecoder detected;

    Decoder(Charset cs) {
      super(cs, 0.5f, 1.0f);
    }

    protected void implReset() {
      detected = null;
    }

    private static Trial trial(String charset, ByteBuffer src) {
      ByteBuffer in = src.duplicate();
      CharBuffer sandbox = CharBuffer.allocate(in.remaining() * 2 + 16);
      CoderResult r = Charset.forName(charset).newDecoder().decode(in, sandbox, false);
      Trial t = new Trial();
      t.error = r.isError();
      t.consumed = in.position() - src.position();
      int hiragana = 0;
      int halfwidth = 0;
      for (int i = 0; i < sandbox.position(); i++) {
        char c = sandbox.get(i);
        if (c >= 0x3040 && c <= 0x309f) {
          hiragana++;
        } else if (c >= 0xff65 && c <= 0xff9f) {
          halfwidth++;
        }
      }
      t.kana = Math.max(hiragana, halfwidth);
      return t;
    }

    private void use(String charset) {
      detected = Charset.forName(charset).newDecoder();
    }

    private static String pickEightBit(ByteBuffer src) {
      Trial euc = trial("EUC-JP", src);
      if (euc.error) {
        return "Shift_JIS";
      }
      Trial sjis = trial("Shift_JIS", src);
      if (sjis.error || euc.consumed > sjis.consumed) {
        return "EUC-JP";
      }
      if (euc.consumed < sjis.consumed) {
        return "Shift_JIS";
      }
      if (euc.consumed == 0) {
        return null;
      }
      return euc.kana >= 2 ? "EUC-JP" : "Shift_JIS";
    }

    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
      while (detected == null) {
        if (!src.hasRemaining()) {
          return CoderResult.UNDERFLOW;
        }
        int b = src.get(src.position()) & 0xff;
        if (b == 0x1b && !trial("ISO-2022-JP", src).error) {
          use("ISO-2022-JP");
          break;
        }
        if (b == 0x1b || b >= 0x80) {
          String pick = pickEightBit(src);
          if (pick == null) {
            return CoderResult.UNDERFLOW;
          }
          use(pick);
          break;
        }
        if (!dst.hasRemaining()) {
          return CoderResult.OVERFLOW;
        }
        dst.put((char) b);
        src.position(src.position() + 1);
      }
      return detected.decode(src, dst, false);
    }
  }
}
