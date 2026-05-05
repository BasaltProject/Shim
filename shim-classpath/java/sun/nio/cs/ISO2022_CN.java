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

// ISO-2022-CN encoders over GB 2312 or the CNS 11643 planes of EUC-TW; decoding shares the ISO-2022-CN graph.
public final class ISO2022_CN extends Charset implements HistoricallyNamedCharset {
  public static final ISO2022_CN CNS = new ISO2022_CN("x-ISO-2022-CN-CNS",
      new String[] {"ISO-2022-CN-CNS", "ISO2022CN_CNS"}, "ISO2022CN_CNS", true);
  public static final ISO2022_CN GB = new ISO2022_CN("x-ISO-2022-CN-GB",
      new String[] {"ISO-2022-CN-GB", "ISO2022CN_GB"}, "ISO2022CN_GB", false);

  private final String historical;
  private final boolean cns;

  private ISO2022_CN(String name, String[] aliases, String historical, boolean cns) {
    super(name, aliases);
    this.historical = historical;
    this.cns = cns;
  }

  public String historicalName() {
    return historical;
  }

  public boolean contains(Charset cs) {
    String n = cs.name();
    return cs == this || n.equals("US-ASCII") || n.equals(cns ? "x-EUC-TW" : "GB2312");
  }

  public CharsetDecoder newDecoder() {
    return new TableCharset.GraphDecoder(this, 1.0f, 1.0f, CodeTable.named("ISO-2022-CN"));
  }

  public CharsetEncoder newEncoder() {
    return new Encoder(this, Charset.forName(cns ? "x-EUC-TW" : "GB2312").newEncoder());
  }

  private static final class Encoder extends CharsetEncoder {
    private final CharsetEncoder table;
    private final boolean cns;
    private final ByteBuffer one = ByteBuffer.allocate(8);
    private final byte[] out = new byte[16];
    private boolean shifted;
    private boolean soDesignated;
    private boolean ss2Designated;
    private int ss3Plane;

    Encoder(ISO2022_CN cs, CharsetEncoder table) {
      super(cs, 4.0f, 8.0f, new byte[] {0x3f});
      this.table = table;
      this.cns = cs.cns;
    }

    public boolean isLegalReplacement(byte[] repl) {
      return true;
    }

    protected void implReset() {
      shifted = false;
      soDesignated = false;
      ss2Designated = false;
      ss3Plane = 0;
    }

    public boolean canEncode(char c) {
      return c < 0x80 || code(c) >= 0;
    }

    // Plane number in the high byte and the seven bit code in the low bytes, or -1.
    private int code(int cp) {
      if (Character.isSurrogate((char) cp) && cp <= 0xffff) {
        return -1;
      }
      one.clear();
      table.reset();
      CoderResult r = table.encode(CharBuffer.wrap(Character.toChars(cp)), one, true);
      if (r.isError()) {
        return -1;
      }
      if (one.position() == 2) {
        return (1 << 16) | ((one.get(0) & 0x7f) << 8) | (one.get(1) & 0x7f);
      }
      int plane = (one.get(1) & 0xff) - 0xa0;
      if (cns && cp <= 0xffff && one.position() == 4 && (one.get(0) & 0xff) == 0x8e && plane >= 2 && plane <= 3) {
        return (((one.get(1) & 0xff) - 0xa0) << 16) | ((one.get(2) & 0x7f) << 8) | (one.get(3) & 0x7f);
      }
      return -1;
    }

    private static int escape(byte[] out, int n, int c1, int c2, int c3) {
      out[n++] = 0x1b;
      out[n++] = (byte) c1;
      out[n++] = (byte) c2;
      out[n++] = (byte) c3;
      return n;
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      while (src.hasRemaining()) {
        int cp = TableCharset.readCodePoint(src);
        if (cp < 0) {
          return TableCharset.readError(cp);
        }
        int width = Character.charCount(cp);
        int n = 0;
        boolean reset = false;
        int plane = 0;
        if (cp < 0x80) {
          if (shifted) {
            out[n++] = 0x0f;
          }
          out[n++] = (byte) cp;
          reset = cp == '\n';
        } else {
          int code = code(cp);
          if (code < 0) {
            return CoderResult.unmappableForLength(width);
          }
          plane = code >>> 16;
          if (plane == 1) {
            if (!soDesignated) {
              n = escape(out, n, '$', ')', cns ? 'G' : 'A');
            }
            if (!shifted) {
              out[n++] = 0x0e;
            }
          } else if (plane == 2) {
            if (!ss2Designated) {
              n = escape(out, n, '$', '*', 'H');
            }
            out[n++] = 0x1b;
            out[n++] = 'N';
          } else {
            if (ss3Plane != plane) {
              n = escape(out, n, '$', '+', 'I' + plane - 3);
            }
            out[n++] = 0x1b;
            out[n++] = 'O';
          }
          out[n++] = (byte) (code >>> 8);
          out[n++] = (byte) code;
        }
        if (dst.remaining() < n) {
          return CoderResult.OVERFLOW;
        }
        dst.put(out, 0, n);
        if (cp < 0x80) {
          shifted = false;
          if (reset) {
            soDesignated = false;
            ss2Designated = false;
            ss3Plane = 0;
          }
        } else if (plane == 1) {
          soDesignated = true;
          shifted = true;
        } else if (plane == 2) {
          ss2Designated = true;
        } else {
          ss3Plane = plane;
        }
        src.position(src.position() + width);
      }
      return CoderResult.UNDERFLOW;
    }
  }
}
