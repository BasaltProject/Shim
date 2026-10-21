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

// ISO-2022-JP flavors; the encoder maps each character to the first sequence that decodes to it.
public final class ISO2022_JP extends Charset implements HistoricallyNamedCharset {
  private static final String kJisContains = "ISO-2022-JP,ISO-2022-JP-2,JIS_X0201,US-ASCII,x-JIS0208,x-windows-50220,"
      + "x-windows-50221,x-windows-iso2022jp";
  private static final String kCommonOverrides = "2225:B2142,ff0d:B215d,ffe0:B2171,ffe1:B2172,ffe2:B224c,";
  private static final String kKanaOverrides = "ff61:B2123,ff62:B2156,ff63:B2157,ff64:B2122,ff65:B2126,ff66:B2572,"
      + "ff67:B2521,ff68:B2523,ff69:B2525,"
      + "ff6a:B2527,ff6b:B2529,ff6c:B2563,ff6d:B2565,ff6e:B2567,ff6f:B2543,ff70:B213c,ff71:B2522,ff72:B2524,"
      + "ff73:B2526,ff74:B2528,ff75:B252a,ff76:B252b,ff77:B252d,ff78:B252f,ff79:B2531,ff7a:B2533,ff7b:B2535,"
      + "ff7c:B2537,ff7d:B2539,ff7e:B253b,ff7f:B253d,ff80:B253f,ff81:B2541,ff82:B2544,ff83:B2546,ff84:B2548,"
      + "ff85:B254a,ff86:B254b,ff87:B254c,ff88:B254d,ff89:B254e,ff8a:B254f,ff8b:B2552,ff8c:B2555,ff8d:B2558,"
      + "ff8e:B255b,ff8f:B255e,ff90:B255f,ff91:B2560,ff92:B2561,ff93:B2562,ff94:B2564,ff95:B2566,ff96:B2568,"
      + "ff97:B2569,ff98:B256a,ff99:B256b,ff9a:B256c,ff9b:B256d,ff9c:B256f,ff9d:B2573,ff9e:B212b,ff9f:B212c,";

  public static final ISO2022_JP INSTANCE = new ISO2022_JP("ISO-2022-JP",
      new String[] {"iso2022jp", "jis", "csISO2022JP", "jis_encoding", "csjisencoding"}, "ISO2022JP", "ISO-2022-JP",
      false, "");
  public static final ISO2022_JP INSTANCE2 = new ISO2022_JP("ISO-2022-JP-2",
      new String[] {"csISO2022JP2", "iso2022jp2"}, "ISO2022JP2", "ISO-2022-JP-2", true, "");
  public static final ISO2022_JP MS50220 = new ISO2022_JP("x-windows-50220", new String[] {"cp50220", "ms50220"},
      "MS50220", "x-windows-50220", true, kCommonOverrides + kKanaOverrides);
  public static final ISO2022_JP MS50221 = new ISO2022_JP("x-windows-50221", new String[] {"cp50221", "ms50221"},
      "MS50221", "x-windows-50220", true, kCommonOverrides);
  public static final ISO2022_JP WINDOWS = new ISO2022_JP("x-windows-iso2022jp", new String[] {"windows-iso2022jp"},
      "windows-iso2022jp", "x-windows-iso2022jp", false, "ab:B2263,af:B2131,b5:B264c,b7:B2126,b8:B2124,bb:B2264,"
      + "221a:B2d75,222b:B2d72,22a5:B2d76,3094:B2574,ffe2:B7c7b,");

  private static final int kAscii = 0;
  private static final int kRoman = 1;
  private static final int kKana = 2;
  private static final int kJis0208 = 3;
  private static final int kJis0212 = 4;
  private static final byte[][] kEscapes = {{0x1b, '(', 'B'}, {0x1b, '(', 'J'}, {0x1b, '(', 'I'}, {0x1b, '$', 'B'},
      {0x1b, '$', '(', 'D'}};

  private final String historical;
  private final String graph;
  private final boolean jis0212;
  private final String overrides;
  private int[] map;

  private ISO2022_JP(String name, String[] aliases, String historical, String graph, boolean jis0212,
      String overrides) {
    super(name, aliases);
    this.historical = historical;
    this.graph = graph;
    this.jis0212 = jis0212;
    this.overrides = overrides;
  }

  public String historicalName() {
    return historical;
  }

  public boolean contains(Charset cs) {
    String n = cs.name();
    return cs == this || (jis0212 && n.equals("JIS_X0212-1990"))
        || ("," + kJisContains + ",").indexOf("," + n + ",") >= 0;
  }

  public CharsetDecoder newDecoder() {
    return new TableCharset.GraphDecoder(this, 0.5f, 1.0f, CodeTable.named(graph));
  }

  public CharsetEncoder newEncoder() {
    return new Encoder(this, encodeMap());
  }

  private synchronized int[] encodeMap() {
    if (map != null) {
      return map;
    }
    int[] m = new int[0x10000];
    CharsetDecoder d = newDecoder();
    CharBuffer out = CharBuffer.allocate(4);
    for (int mode = kRoman; mode <= kJis0212; mode++) {
      byte[] esc = kEscapes[mode];
      boolean wide = mode >= kJis0208;
      byte[] seq = new byte[esc.length + (wide ? 2 : 1)];
      System.arraycopy(esc, 0, seq, 0, esc.length);
      for (int code = wide ? 0x2121 : 0x21; code <= (wide ? 0x7e7e : 0x7e); code++) {
        if (wide && ((code & 0xff) < 0x21 || (code & 0xff) > 0x7e)) {
          continue;
        }
        seq[seq.length - 1] = (byte) code;
        if (wide) {
          seq[seq.length - 2] = (byte) (code >> 8);
        }
        d.reset();
        out.clear();
        ByteBuffer in = ByteBuffer.wrap(seq);
        CoderResult r = d.decode(in, out, true);
        if (r.isError() || in.hasRemaining() || out.position() != 1) {
          continue;
        }
        char c = out.get(0);
        if (c >= 0x80 && !Character.isSurrogate(c) && m[c] == 0) {
          m[c] = (mode << 16) | code;
        }
      }
    }
    int at = 0;
    while (at < overrides.length()) {
      int colon = overrides.indexOf(':', at);
      int comma = overrides.indexOf(',', colon);
      int cp = Integer.parseInt(overrides.substring(at, colon), 16);
      String v = overrides.substring(colon + 1, comma);
      m[cp] = v.equals("-") ? 0 : ("JIBD".indexOf(v.charAt(0)) + kRoman) << 16 | Integer.parseInt(v.substring(1), 16);
      at = comma + 1;
    }
    map = m;
    return m;
  }

  private static final class Encoder extends CharsetEncoder {
    private final int[] map;
    private int mode = kAscii;

    Encoder(ISO2022_JP cs, int[] map) {
      super(cs, 4.0f, cs.jis0212 ? 9.0f : 8.0f, new byte[] {0x21, 0x29});
      this.map = map;
    }

    public boolean isLegalReplacement(byte[] repl) {
      return true;
    }

    protected void implReset() {
      mode = kAscii;
    }

    public boolean canEncode(char c) {
      return !Character.isSurrogate(c) && (c < 0x80 || map[c] != 0);
    }

    protected CoderResult implFlush(ByteBuffer out) {
      if (mode != kAscii) {
        if (out.remaining() < 3) {
          return CoderResult.OVERFLOW;
        }
        out.put(kEscapes[kAscii]);
        mode = kAscii;
      }
      return CoderResult.UNDERFLOW;
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      while (src.hasRemaining()) {
        int cp = TableCharset.readCodePoint(src);
        if (cp < 0) {
          return TableCharset.readError(cp);
        }
        int width = Character.charCount(cp);
        int target;
        int code;
        byte[] replacement = null;
        if (cp < 0x80) {
          target = kAscii;
          code = cp;
        } else {
          int v = cp > 0xffff ? 0 : map[cp];
          if (v == 0) {
            if (unmappableCharacterAction() != CodingErrorAction.REPLACE) {
              return CoderResult.unmappableForLength(width);
            }
            replacement = replacement();
            v = kJis0208 << 16;
          }
          target = v >>> 16;
          code = v & 0xffff;
        }
        if (target != mode) {
          byte[] esc = kEscapes[target];
          if (dst.remaining() < esc.length) {
            return CoderResult.OVERFLOW;
          }
          dst.put(esc);
          mode = target;
        }
        if (replacement != null) {
          if (dst.remaining() < replacement.length) {
            return CoderResult.OVERFLOW;
          }
          dst.put(replacement);
        } else if (target >= kJis0208) {
          if (dst.remaining() < 2) {
            return CoderResult.OVERFLOW;
          }
          dst.put((byte) (code >> 8));
          dst.put((byte) code);
        } else {
          if (!dst.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          dst.put((byte) code);
        }
        src.position(src.position() + width);
      }
      return CoderResult.UNDERFLOW;
    }
  }
}
