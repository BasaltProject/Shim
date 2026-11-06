/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/** CCITT Group 3 and Group 4 coding for TIFF; rows are packed MSB first with 1 meaning black. */
final class TiffFax {
  static final int kRle = 2;
  static final int kT4 = 3;
  static final int kT6 = 4;

  private static final int kMaxMakeup = 40;
  private static final Map<Integer, Integer> kWhiteRuns = decodeTable(TiffFaxTables.kTermWhite,
      TiffFaxTables.kMakeupWhite);
  private static final Map<Integer, Integer> kBlackRuns = decodeTable(TiffFaxTables.kTermBlack,
      TiffFaxTables.kMakeupBlack);

  private TiffFax() {}

  private static Map<Integer, Integer> decodeTable(int[] term, int[] makeup) {
    Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    for (int i = 0; i < term.length; i++) {
      map.put(term[i], i);
    }
    for (int i = 1; i < makeup.length; i++) {
      map.put(makeup[i], i < 28 ? i * 64 : 1792 + (i - 28) * 64);
    }
    return map;
  }

  private static final class Bits {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    int acc;
    int count;

    void put(int code, int length) {
      for (int i = length - 1; i >= 0; i--) {
        acc = (acc << 1) | ((code >> i) & 1);
        if (++count == 8) {
          out.write(acc);
          acc = 0;
          count = 0;
        }
      }
    }

    void alignByte() {
      while (count != 0) {
        put(0, 1);
      }
    }

    void putEntry(int entry) {
      put(entry & 0xffff, entry >>> 16);
    }
  }

  private static boolean pixel(byte[] row, int off, int x) {
    return x >= 0 && (row[off + (x >> 3)] & (0x80 >> (x & 7))) != 0;
  }

  private static int nextChange(byte[] row, int off, int after, int width, int toColor) {
    for (int x = after + 1; x < width; x++) {
      boolean p = pixel(row, off, x);
      if (p != pixel(row, off, x - 1) && p == (toColor == 1)) {
        return x;
      }
    }
    return width;
  }

  private static int nextAnyChange(byte[] row, int off, int after, int width) {
    for (int x = after + 1; x < width; x++) {
      if (pixel(row, off, x) != pixel(row, off, x - 1)) {
        return x;
      }
    }
    return width;
  }

  private static void putRun(Bits bits, int run, int color) {
    int[] makeup = color == 0 ? TiffFaxTables.kMakeupWhite : TiffFaxTables.kMakeupBlack;
    int[] term = color == 0 ? TiffFaxTables.kTermWhite : TiffFaxTables.kTermBlack;
    int sixtyFours = run >> 6;
    for (; sixtyFours > kMaxMakeup; sixtyFours -= kMaxMakeup) {
      bits.putEntry(makeup[kMaxMakeup]);
    }
    if (sixtyFours != 0) {
      bits.putEntry(makeup[sixtyFours]);
    }
    bits.putEntry(term[run & 63]);
  }

  private static void encode1d(Bits bits, byte[] rows, int off, int width) {
    int x = 0;
    int color = 0;
    if (pixel(rows, off, 0)) {
      putRun(bits, 0, 0);
      color = 1;
    }
    while (x < width) {
      int next = nextAnyChange(rows, off, x, width);
      putRun(bits, next - x, color);
      x = next;
      color ^= 1;
    }
  }

  private static void encode2d(Bits bits, byte[] rows, int off, byte[] ref, int refOff, int width) {
    int a0 = -1;
    int color = 0;
    while (a0 < width) {
      int a1 = nextChange(rows, off, a0, width, color ^ 1);
      int b1 = nextChange(ref, refOff, a0, width, color ^ 1);
      int b2 = nextAnyChange(ref, refOff, b1, width);
      if (b2 < a1) {
        bits.put(1, 4);
        a0 = b2;
      } else if (Math.abs(a1 - b1) <= 3) {
        int d = a1 - b1;
        switch (d) {
        case 0:
          bits.put(1, 1);
          break;
        case 1:
          bits.put(3, 3);
          break;
        case 2:
          bits.put(3, 6);
          break;
        case 3:
          bits.put(3, 7);
          break;
        case -1:
          bits.put(2, 3);
          break;
        case -2:
          bits.put(2, 6);
          break;
        default:
          bits.put(2, 7);
          break;
        }
        a0 = a1;
        color ^= 1;
      } else {
        int a2 = nextAnyChange(rows, off, a1, width);
        bits.put(1, 3);
        putRun(bits, a1 - Math.max(a0, 0), color);
        putRun(bits, a2 - a1, color ^ 1);
        a0 = a2;
      }
    }
  }

  static byte[] encode(byte[] rows, int stride, int width, int height, int kind, boolean twoDimensional) {
    Bits bits = new Bits();
    byte[] white = new byte[stride];
    for (int y = 0; y < height; y++) {
      int off = y * stride;
      switch (kind) {
      case kRle:
        encode1d(bits, rows, off, width);
        bits.alignByte();
        break;
      case kT4:
        if (!twoDimensional || (y & 1) == 0) {
          bits.put(twoDimensional ? 3 : 1, twoDimensional ? 13 : 12);
          encode1d(bits, rows, off, width);
        } else {
          bits.put(2, 13);
          encode2d(bits, rows, off, rows, off - stride, width);
        }
        break;
      default:
        if (y == 0) {
          encode2d(bits, rows, off, white, 0, width);
        } else {
          encode2d(bits, rows, off, rows, off - stride, width);
        }
        break;
      }
    }
    if (kind == kT4) {
      for (int i = 0; i < 6; i++) {
        bits.put(twoDimensional ? 3 : 1, twoDimensional ? 13 : 12);
      }
    } else if (kind == kT6) {
      bits.put(0x001001, 24);
    }
    bits.alignByte();
    return bits.out.toByteArray();
  }

  private static final class Source {
    final byte[] data;
    final boolean lsbFirst;
    int pos;

    Source(byte[] data, boolean lsbFirst) {
      this.data = data;
      this.lsbFirst = lsbFirst;
    }

    boolean more() {
      return pos < data.length * 8;
    }

    int bit() {
      if (pos >= data.length * 8) {
        pos++;
        return 0;
      }
      int b = data[pos >> 3] & 0xff;
      int v = lsbFirst ? (b >> (pos & 7)) & 1 : (b >> (7 - (pos & 7))) & 1;
      pos++;
      return v;
    }

    int peek(int n) {
      int save = pos;
      int v = 0;
      for (int i = 0; i < n; i++) {
        v = (v << 1) | bit();
      }
      pos = save;
      return v;
    }
  }

  private static int readRun(Source src, int color) {
    Map<Integer, Integer> table = color == 0 ? kWhiteRuns : kBlackRuns;
    int total = 0;
    while (true) {
      int code = 0;
      Integer run = null;
      for (int len = 1; len <= 13; len++) {
        code = (code << 1) | src.bit();
        run = table.get((len << 16) | code);
        if (run != null) {
          break;
        }
      }
      if (run == null) {
        return -1;
      }
      total += run;
      if (run < 64) {
        return total;
      }
    }
  }

  private static void paint(byte[] out, int off, int from, int to) {
    for (int x = from; x < to; x++) {
      out[off + (x >> 3)] |= (byte) (0x80 >> (x & 7));
    }
  }

  private static boolean decode1d(Source src, byte[] out, int off, int width) {
    int x = 0;
    int color = 0;
    while (x < width) {
      int run = readRun(src, color);
      if (run < 0) {
        return false;
      }
      run = Math.min(run, width - x);
      if (color == 1) {
        paint(out, off, x, x + run);
      }
      x += run;
      color ^= 1;
    }
    return true;
  }

  private static boolean decode2d(Source src, byte[] out, int off, byte[] ref, int refOff, int width) {
    int a0 = -1;
    int color = 0;
    while (a0 < width) {
      int b1 = nextChange(ref, refOff, a0, width, color ^ 1);
      int b2 = nextAnyChange(ref, refOff, b1, width);
      int a1;
      if (src.bit() == 1) {
        a1 = b1;
      } else if (src.peek(2) == 3) {
        src.pos += 2;
        a1 = b1 + 1;
      } else if (src.peek(2) == 2) {
        src.pos += 2;
        a1 = b1 - 1;
      } else if (src.peek(2) == 1) {
        src.pos += 2;
        int begin = Math.max(a0, 0);
        int r1 = readRun(src, color);
        int r2 = readRun(src, color ^ 1);
        if (r1 < 0 || r2 < 0) {
          return false;
        }
        int end1 = Math.min(width, begin + r1);
        int end2 = Math.min(width, end1 + r2);
        if (color == 0) {
          paint(out, off, end1, end2);
        } else {
          paint(out, off, begin, end1);
        }
        a0 = end2;
        continue;
      } else if (src.peek(3) == 1) {
        src.pos += 3;
        int begin = Math.max(a0, 0);
        if (color == 1) {
          paint(out, off, begin, Math.min(b2, width));
        }
        a0 = b2;
        continue;
      } else if (src.peek(5) == 3) {
        src.pos += 5;
        a1 = b1 + 2;
      } else if (src.peek(5) == 2) {
        src.pos += 5;
        a1 = b1 - 2;
      } else if (src.peek(6) == 3) {
        src.pos += 6;
        a1 = b1 + 3;
      } else if (src.peek(6) == 2) {
        src.pos += 6;
        a1 = b1 - 3;
      } else {
        return false;
      }
      a1 = Math.max(Math.min(a1, width), Math.max(a0, 0));
      if (color == 1) {
        paint(out, off, Math.max(a0, 0), a1);
      }
      a0 = a1;
      color ^= 1;
      if (a0 >= width) {
        break;
      }
    }
    return true;
  }

  private static boolean skipEol(Source src) {
    int zeros = 0;
    while (src.more()) {
      int b = src.bit();
      if (b == 0) {
        zeros++;
      } else if (zeros >= 11) {
        return true;
      } else {
        src.pos--;
        return false;
      }
    }
    return false;
  }

  static byte[] decode(byte[] data, int stride, int width, int height, int kind, boolean lsbFirst,
      boolean twoDimensional) {
    byte[] out = new byte[stride * height];
    Source src = new Source(data, lsbFirst);
    byte[] white = new byte[stride];
    for (int y = 0; y < height; y++) {
      int off = y * stride;
      if (kind == kRle) {
        decode1d(src, out, off, width);
        src.pos = (src.pos + 7) & ~7;
      } else if (kind == kT4) {
        boolean eol = skipEol(src);
        boolean twoD = false;
        if (twoDimensional && eol && src.more()) {
          twoD = src.bit() == 0;
        }
        if (!src.more()) {
          break;
        }
        if (twoD) {
          decode2d(src, out, off, y == 0 ? white : out, y == 0 ? 0 : off - stride, width);
        } else {
          decode1d(src, out, off, width);
        }
      } else {
        decode2d(src, out, off, y == 0 ? white : out, y == 0 ? 0 : off - stride, width);
      }
    }
    return out;
  }
}
