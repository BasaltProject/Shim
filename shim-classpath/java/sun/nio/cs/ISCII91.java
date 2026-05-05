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

// ISCII-91 Devanagari: decoding holds one character back once a nukta-capable character starts a run.
public final class ISCII91 extends Charset implements HistoricallyNamedCharset {
  public static final ISCII91 INSTANCE = new ISCII91();

  private static final String kHigh = "\uffff\u0901\u0902\u0903\u0905\u0906\u0907\u0908\u0909\u090a\u090b\u090e"
      + "\u090f\u0910\u090d\u0912\u0913\u0914\u0911\u0915\u0916\u0917\u0918\u0919\u091a\u091b\u091c\u091d\u091e"
      + "\u091f\u0920\u0921\u0922\u0923\u0924\u0925\u0926\u0927\u0928\u0929\u092a\u092b\u092c\u092d\u092e\u092f"
      + "\u095f\u0930\u0931\u0932\u0933\u0934\u0935\u0936\u0937\u0938\u0939\u200d\u093e\u093f\u0940\u0941\u0942"
      + "\u0943\u0946\u0947\u0948\u0945\u094a\u094b\u094c\u0949\u094d\u093c\u0964\uffff\uffff\uffff\uffff\ufffd"
      + "\ufffd\u0966\u0967\u0968\u0969\u096a\u096b\u096c\u096d\u096e\u096f\uffff\uffff\uffff\uffff\uffff";
  private static final String kNuktaBase = "\u0901\u0907\u0908\u090b\u093f\u0940\u0943\u0964";
  private static final String kNuktaForm = "\u0950\u090c\u0961\u0960\u0962\u0963\u0944\u093d";
  private static final String kEncode = "901:a1,902:a2,903:a3,905:a4,906:a5,907:a6,908:a7,909:a8,90a:a9,90b:aa,"
      + "90c:a6e9,90d:ae,90e:ab,90f:ac,910:ad,911:b2,912:af,913:b0,914:b1,915:b3,916:b4,917:b5,918:b6,919:b7,91a:b8,"
      + "91b:b9,91c:ba,91d:bb,91e:bc,91f:bd,920:be,921:bf,922:c0,923:c1,924:c2,925:c3,926:c4,927:c5,928:c6,929:c7,"
      + "92a:c8,92b:c9,92c:ca,92d:cb,92e:cc,92f:cd,930:cf,931:d0,932:d1,933:d2,934:d3,935:d4,936:d5,937:d6,938:d7,"
      + "939:d8,93c:e9,93d:eae9,93e:da,93f:db,940:dc,941:dd,942:de,943:df,944:dfe9,945:e3,946:e0,947:e1,948:e2,"
      + "949:e7,94a:e4,94b:e5,94c:e6,94d:e8,950:a1e9,951:f0b5,952:f0b8,953:fe,954:fe,958:b3e9,959:b4e9,95a:b5e9,"
      + "95b:bae9,95c:bfe9,95d:c0e9,95e:c9e9,95f:ce,960:aae9,961:a7e9,962:dbe9,963:dce9,964:ea,965:eaea,966:f1,"
      + "967:f2,968:f3,969:f4,96a:f5,96b:f6,96c:f7,96d:f8,96e:f9,96f:fa,970:f0bf,200c:e8,200d:e9,";

  private static final int kNone = -1;
  private static int[] encodeMap;

  private ISCII91() {
    super("x-ISCII91", new String[] {"iscii", "ST_SEV_358-88", "iso-ir-153", "csISO153GOST1976874", "ISCII91"});
  }

  public String historicalName() {
    return "ISCII91";
  }

  public boolean contains(Charset cs) {
    return cs == this || cs.name().equals("US-ASCII");
  }

  static char single(int b) {
    if (b < 0x80) {
      return (char) b;
    }
    return b == 0x80 ? '\u007f' : b < 0xa0 ? '\uffff' : kHigh.charAt(b - 0xa0);
  }

  public CharsetDecoder newDecoder() {
    return new CharsetDecoder(this, 1.0f, 1.0f) {
      private int pending = kNone;
      private boolean afterHalant;
      private boolean afterExtension;

      // parity: reset keeps the decoding context; only flush clears it.
      protected CoderResult implFlush(CharBuffer out) {
        if (pending != kNone) {
          if (!out.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          out.put((char) pending);
          pending = kNone;
        }
        afterHalant = false;
        afterExtension = false;
        return CoderResult.UNDERFLOW;
      }

      protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
        while (src.hasRemaining()) {
          int b = src.get(src.position()) & 0xff;
          char c = single(b);
          int out = kNone;
          if (pending != kNone) {
            int nukta = b == 0xe9 && !afterExtension ? kNuktaBase.indexOf(pending) : -1;
            if (afterExtension) {
              out = pending;
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              pending = kNone;
              afterExtension = false;
            } else if (nukta >= 0) {
              out = kNuktaForm.charAt(nukta);
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              pending = kNone;
            } else {
              out = pending;
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              pending = c;
              afterExtension = b == 0xef || b == 0xf0;
            }
          } else if (afterExtension) {
            out = 0xfffd;
            if (!dst.hasRemaining()) {
              return CoderResult.OVERFLOW;
            }
            afterExtension = false;
          } else if (afterHalant && (b == 0xe8 || b == 0xe9)) {
            out = b == 0xe8 ? 0x200c : 0x200d;
            if (!dst.hasRemaining()) {
              return CoderResult.OVERFLOW;
            }
            afterHalant = b == 0xe8;
          } else {
            if (c == '\uffff') {
              return CoderResult.unmappableForLength(1);
            }
            if (kNuktaBase.indexOf(c) >= 0) {
              afterHalant = false;
              pending = c;
            } else {
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              out = c;
              afterHalant = b == 0xe8;
              afterExtension = b == 0xef || b == 0xf0;
            }
          }
          if (out != kNone) {
            dst.put((char) out);
          }
          src.position(src.position() + 1);
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }

  private static synchronized int[] encodeMap() {
    if (encodeMap == null) {
      int[] m = new int[0x10000];
      int at = 0;
      while (at < kEncode.length()) {
        int colon = kEncode.indexOf(':', at);
        int comma = kEncode.indexOf(',', colon);
        String bytes = kEncode.substring(colon + 1, comma);
        int cp = Integer.parseInt(kEncode.substring(at, colon), 16);
        m[cp] = (bytes.length() / 2) << 16 | Integer.parseInt(bytes, 16);
        at = comma + 1;
      }
      encodeMap = m;
    }
    return encodeMap;
  }

  public CharsetEncoder newEncoder() {
    final int[] map = encodeMap();
    return new CharsetEncoder(this, 2.0f, 2.0f, new byte[] {0x3f}) {
      public boolean canEncode(char c) {
        return c < 0x80 || map[c] != 0;
      }

      protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        while (src.hasRemaining()) {
          int cp = TableCharset.readCodePoint(src);
          if (cp < 0) {
            return TableCharset.readError(cp);
          }
          int v = cp < 0x80 ? 1 << 16 | cp : cp > 0xffff ? 0 : map[cp];
          if (v == 0) {
            return CoderResult.unmappableForLength(Character.charCount(cp));
          }
          int len = v >>> 16;
          if (dst.remaining() < len) {
            return CoderResult.OVERFLOW;
          }
          if (len == 2) {
            dst.put((byte) (v >> 8));
          }
          dst.put((byte) v);
          src.position(src.position() + 1);
        }
        return CoderResult.UNDERFLOW;
      }
    };
  }
}
