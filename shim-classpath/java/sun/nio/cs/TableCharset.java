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

// Charset driven by a decode graph and an encode map, with stateless, EBCDIC shift and composing encoders.
public final class TableCharset extends Charset implements HistoricallyNamedCharset {
  static final int kEbcdic = 'E';
  static final int kComposing = 'C';
  static final int kDecodeOnly = 'N';

  private final String historical;
  private final String[] contained;
  private final int family;
  private final float averageBytes;
  private final float maxBytes;
  private final byte[] replacement;
  private final float averageChars;
  private final float maxChars;
  private final String[] blob;
  private CodeTable table;

  TableCharset(String[] f, String[] blob) {
    super(f[0], f[1].isEmpty() ? new String[0] : f[1].split(","));
    historical = f[2].isEmpty() ? f[0] : f[2];
    family = f[3].charAt(0);
    averageBytes = Float.parseFloat(f[4]);
    maxBytes = Float.parseFloat(f[5]);
    replacement = CodeTable.hexBytes(f[6]);
    averageChars = Float.parseFloat(f[7]);
    maxChars = Float.parseFloat(f[8]);
    contained = f[9].isEmpty() ? new String[0] : f[9].split(",");
    this.blob = blob;
  }


  public String historicalName() {
    return historical;
  }

  public boolean contains(Charset cs) {
    if (cs == this) {
      return true;
    }
    for (String n : contained) {
      if (n.equals(cs.name())) {
        return true;
      }
    }
    return false;
  }

  public boolean canEncode() {
    return family != kDecodeOnly;
  }

  synchronized CodeTable table() {
    if (table == null) {
      table = CodeTable.load(blob);
    }
    return table;
  }

  public CharsetDecoder newDecoder() {
    return new GraphDecoder(this, averageChars, maxChars, table());
  }

  public CharsetEncoder newEncoder() {
    if (family == kDecodeOnly) {
      throw new UnsupportedOperationException();
    }
    final CodeTable t = table();
    if (family == kEbcdic) {
      return new EbcdicEncoder(this, t);
    }
    if (family == kComposing) {
      return new ComposingEncoder(this, t);
    }
    return new MapEncoder(this, averageBytes, maxBytes, replacement.clone(), t);
  }

  static class GraphDecoder extends CharsetDecoder {
    private final CodeTable t;
    private int state;

    GraphDecoder(Charset cs, float average, float max, CodeTable t) {
      super(cs, average, max);
      this.t = t;
    }

    protected void implReset() {
      state = 0;
    }

    protected CoderResult implFlush(CharBuffer out) {
      String f = t.flush[state];
      if (f.length() > out.remaining()) {
        return CoderResult.OVERFLOW;
      }
      out.put(f);
      return CoderResult.UNDERFLOW;
    }

    protected CoderResult decodeLoop(ByteBuffer src, CharBuffer dst) {
      int limit = src.limit();
      while (src.hasRemaining()) {
        int pos = src.position();
        int nodeIndex = t.roots[state];
        int[] node = t.nodes[nodeIndex];
        int depth = 0;
        int e = 0;
        while (true) {
          if (pos + depth >= limit) {
            boolean full = (e & CodeTable.kFullFirst) != 0 && !dst.hasRemaining();
            return full ? CoderResult.OVERFLOW : CoderResult.UNDERFLOW;
          }
          e = node[src.get(pos + depth) & 0xff];
          depth++;
          int kind = e >>> 28;
          int payload = e & 0x07ffffff;
          if (kind == CodeTable.kChild) {
            nodeIndex = payload;
            node = t.nodes[payload];
            continue;
          }
          if ((e & CodeTable.kFullFirst) != 0 && !dst.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          if (kind == CodeTable.kMalformed) {
            return CoderResult.malformedForLength(payload);
          }
          if (kind == CodeTable.kUnmappable) {
            return CoderResult.unmappableForLength(payload);
          }
          if (kind == CodeTable.kChar) {
            if (payload > 0xffff) {
              if (dst.remaining() < 2) {
                return CoderResult.OVERFLOW;
              }
              dst.put(Character.highSurrogate(payload));
              dst.put(Character.lowSurrogate(payload));
            } else {
              if (!dst.hasRemaining()) {
                return CoderResult.OVERFLOW;
              }
              dst.put((char) payload);
            }
            if (t.nodeNext[nodeIndex] >= 0) {
              state = t.nodeNext[nodeIndex];
            }
            src.position(pos + depth);
            break;
          }
          if (t.specialKind[payload] != CodeTable.kChar) {
            state = t.specialNext[payload];
            int length = t.specialConsumed[payload];
            return t.specialKind[payload] == CodeTable.kMalformed ? CoderResult.malformedForLength(length)
                : CoderResult.unmappableForLength(length);
          }
          String out = t.specialOut[payload];
          if (dst.remaining() < out.length()) {
            return CoderResult.OVERFLOW;
          }
          dst.put(out);
          state = t.specialNext[payload];
          src.position(pos + t.specialConsumed[payload]);
          break;
        }
      }
      return CoderResult.UNDERFLOW;
    }
  }

  // Returns the code point at the buffer position, -1 when more input is needed, or a negative error length.
  static int readCodePoint(CharBuffer src) {
    int pos = src.position();
    char c = src.get(pos);
    if (Character.isHighSurrogate(c)) {
      if (pos + 1 >= src.limit()) {
        return -1;
      }
      char d = src.get(pos + 1);
      if (Character.isLowSurrogate(d)) {
        return Character.toCodePoint(c, d);
      }
      return -2;
    }
    if (Character.isLowSurrogate(c)) {
      return -2;
    }
    return c;
  }

  static CoderResult readError(int cp) {
    return cp == -1 ? CoderResult.UNDERFLOW : CoderResult.malformedForLength(1);
  }

  static class MapEncoder extends CharsetEncoder {
    final CodeTable t;

    MapEncoder(Charset cs, float average, float max, byte[] replacement, CodeTable t) {
      super(cs, average, max, replacement);
      this.t = t;
    }

    public boolean isLegalReplacement(byte[] repl) {
      return true;
    }

    public boolean canEncode(char c) {
      return t.canEncode(c);
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      while (src.hasRemaining()) {
        int cp = readCodePoint(src);
        if (cp < 0) {
          return readError(cp);
        }
        int width = Character.charCount(cp);
        int len = t.length(cp);
        if (len == 0) {
          return CoderResult.unmappableForLength(width);
        }
        if (dst.remaining() < len) {
          return CoderResult.OVERFLOW;
        }
        t.put(cp, dst);
        src.position(src.position() + width);
      }
      return CoderResult.UNDERFLOW;
    }
  }

  static final class EbcdicEncoder extends MapEncoder {
    private boolean dbcs;

    EbcdicEncoder(TableCharset cs, CodeTable t) {
      super(cs, cs.averageBytes, cs.maxBytes, cs.replacement.clone(), t);
    }

    protected void implReset() {
      dbcs = false;
    }

    protected CoderResult implFlush(ByteBuffer out) {
      if (dbcs) {
        if (!out.hasRemaining()) {
          return CoderResult.OVERFLOW;
        }
        out.put((byte) 0x0f);
        dbcs = false;
      }
      return CoderResult.UNDERFLOW;
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      while (src.hasRemaining()) {
        int cp = readCodePoint(src);
        if (cp < 0) {
          return readError(cp);
        }
        int width = Character.charCount(cp);
        int len = t.length(cp);
        if (len == 0) {
          return CoderResult.unmappableForLength(width);
        }
        boolean wide = len == 2;
        if (wide != dbcs) {
          if (!dst.hasRemaining()) {
            return CoderResult.OVERFLOW;
          }
          dst.put((byte) (wide ? 0x0e : 0x0f));
          dbcs = wide;
        }
        if (dst.remaining() < len) {
          return CoderResult.OVERFLOW;
        }
        t.put(cp, dst);
        src.position(src.position() + width);
      }
      return CoderResult.UNDERFLOW;
    }
  }

  static final class ComposingEncoder extends MapEncoder {
    private int pending = -1;

    ComposingEncoder(TableCharset cs, CodeTable t) {
      super(cs, cs.averageBytes, cs.maxBytes, cs.replacement.clone(), t);
    }

    protected void implReset() {
      pending = -1;
    }

    private boolean drain(ByteBuffer dst) {
      if (pending < 0) {
        return true;
      }
      if (dst.remaining() < t.length(pending)) {
        return false;
      }
      t.put(pending, dst);
      pending = -1;
      return true;
    }

    protected CoderResult implFlush(ByteBuffer out) {
      return drain(out) ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW;
    }

    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
      while (src.hasRemaining()) {
        int cp = readCodePoint(src);
        if (pending >= 0 && cp >= 0) {
          byte[] pair = t.composite(pending, cp);
          if (pair != null) {
            if (dst.remaining() < pair.length) {
              return CoderResult.OVERFLOW;
            }
            dst.put(pair);
            pending = -1;
            src.position(src.position() + 1);
            continue;
          }
        }
        if (!drain(dst)) {
          return CoderResult.OVERFLOW;
        }
        if (cp < 0) {
          return readError(cp);
        }
        int width = Character.charCount(cp);
        int len = t.length(cp);
        if (len == 0) {
          return CoderResult.unmappableForLength(width);
        }
        if (t.isComposingBase(cp)) {
          pending = cp;
          src.position(src.position() + width);
          continue;
        }
        if (dst.remaining() < len) {
          return CoderResult.OVERFLOW;
        }
        t.put(cp, dst);
        src.position(src.position() + width);
      }
      return CoderResult.UNDERFLOW;
    }
  }
}
