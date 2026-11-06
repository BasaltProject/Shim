/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

final class TiffCodec {
  private TiffCodec() { }

  static byte[] lzwEncode(byte[] data, int off, int len) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(len / 2 + 16);
    int[] bitBuf = {0, 0};
    int numBits = 9;
    int limit = (1 << numBits) - 2;
    int[] table = new int[4096 * 2];
    int[] hash = new int[9973];
    int[] numStrings = {0};
    clearTable(hash, table, numStrings);
    emit(out, bitBuf, 256, numBits);
    int prefix = -1;
    for (int i = off; i < off + len; i++) {
      int c = data[i] & 0xff;
      int found = find(hash, table, numStrings[0], prefix, c);
      if (found != -1) {
        prefix = found;
        continue;
      }
      emit(out, bitBuf, prefix, numBits);
      int idx = add(hash, table, numStrings, prefix, c);
      if (idx > limit) {
        if (numBits == 12) {
          emit(out, bitBuf, 256, numBits);
          clearTable(hash, table, numStrings);
          numBits = 9;
        } else {
          numBits++;
        }
        limit = (1 << numBits) - 2;
      }
      prefix = c;
    }
    if (prefix != -1) {
      emit(out, bitBuf, prefix, numBits);
    }
    emit(out, bitBuf, 257, numBits);
    if (bitBuf[1] > 0) {
      out.write((bitBuf[0] << (8 - bitBuf[1])) & 0xff);
    }
    return out.toByteArray();
  }

  private static void emit(ByteArrayOutputStream out, int[] state, int code, int bits) {
    int acc = state[0];
    int n = state[1];
    acc = (acc << bits) | (code & ((1 << bits) - 1));
    n += bits;
    while (n >= 8) {
      out.write((acc >> (n - 8)) & 0xff);
      n -= 8;
    }
    state[0] = acc & ((1 << n) - 1);
    state[1] = n;
  }

  private static int hashOf(int prefix, int c) {
    return ((((short) ((byte) c << 8)) ^ (short) prefix) & 0xffff) % 9973;
  }

  private static void clearTable(int[] hash, int[] table, int[] numStrings) {
    numStrings[0] = 0;
    Arrays.fill(hash, -1);
    for (int i = 0; i < 258; i++) {
      add(hash, table, numStrings, -1, (byte) i);
    }
  }

  private static int add(int[] hash, int[] table, int[] numStrings, int prefix, int c) {
    int n = numStrings[0];
    if (n >= 4096) {
      return 65535;
    }
    int h = hashOf(prefix, (byte) c);
    while (hash[h] != -1) {
      h = (h + 2039) % 9973;
    }
    hash[h] = n;
    table[n * 2] = prefix;
    table[n * 2 + 1] = (byte) c;
    numStrings[0] = n + 1;
    return n;
  }

  private static int find(int[] hash, int[] table, int n, int prefix, int c) {
    int h = hashOf(prefix, (byte) c);
    while (hash[h] != -1) {
      int code = hash[h];
      if (table[code * 2] == prefix && table[code * 2 + 1] == (byte) c) {
        return code;
      }
      h = (h + 2039) % 9973;
    }
    return -1;
  }

  static byte[] lzwDecode(byte[] in, int expected) throws IOException {
    byte[] out = new byte[expected];
    int[] prefix = new int[4096];
    byte[] suffix = new byte[4096];
    int[] length = new int[4096];
    for (int i = 0; i < 256; i++) {
      suffix[i] = (byte) i;
      length[i] = 1;
      prefix[i] = -1;
    }
    int pos = 0;
    int bitPos = 0;
    int bits = 9;
    int next = 258;
    int old = -1;
    int outPos = 0;
    int totalBits = in.length * 8;
    while (outPos < expected) {
      if (bitPos + bits > totalBits) {
        break;
      }
      int code = 0;
      for (int k = 0; k < bits; k++) {
        code = (code << 1) | ((in[(bitPos + k) >> 3] >> (7 - ((bitPos + k) & 7))) & 1);
      }
      bitPos += bits;
      if (code == 257) {
        break;
      }
      if (code == 256) {
        bits = 9;
        next = 258;
        old = -1;
        continue;
      }
      if (old == -1) {
        if (code >= 256) {
          throw new IOException("Bad LZW stream");
        }
        out[outPos++] = (byte) code;
        old = code;
        continue;
      }
      int entry;
      int first;
      if (code < next) {
        entry = code;
        first = firstChar(prefix, suffix, code);
      } else {
        entry = old;
        first = firstChar(prefix, suffix, old);
      }
      if (next < 4096) {
        prefix[next] = old;
        suffix[next] = (byte) first;
        length[next] = length[old] + 1;
        next++;
      }
      int target = code < next ? code : next - 1;
      int len = length[target];
      int p = outPos + len;
      int c = target;
      while (c != -1 && p > outPos) {
        p--;
        if (p < expected) {
          out[p] = suffix[c];
        }
        c = prefix[c];
      }
      outPos += len;
      old = code;
      if (next >= (1 << bits) - 1 && bits < 12) {
        bits++;
      }
    }
    return out;
  }

  private static int firstChar(int[] prefix, byte[] suffix, int code) {
    while (prefix[code] != -1) {
      code = prefix[code];
    }
    return suffix[code] & 0xff;
  }

  static byte[] deflate(byte[] data, int off, int len, int level) {
    Deflater d = new Deflater(level);
    d.setInput(data, off, len);
    d.finish();
    ByteArrayOutputStream out = new ByteArrayOutputStream(len / 2 + 16);
    byte[] buf = new byte[8192];
    while (!d.finished()) {
      int n = d.deflate(buf);
      out.write(buf, 0, n);
    }
    d.end();
    return out.toByteArray();
  }

  static byte[] inflate(byte[] in, int expected) throws IOException {
    Inflater inf = new Inflater();
    inf.setInput(in);
    byte[] out = new byte[expected];
    try {
      int n = 0;
      while (n < expected && !inf.finished()) {
        int r = inf.inflate(out, n, expected - n);
        if (r == 0 && (inf.needsInput() || inf.needsDictionary())) {
          break;
        }
        n += r;
      }
    } catch (DataFormatException e) {
      throw new IOException("Bad deflate stream");
    } finally {
      inf.end();
    }
    return out;
  }

  static byte[] packBitsEncode(byte[] input, int offset, int count) {
    byte[] o = new byte[count * 2 + 4];
    int op = 0;
    int max = offset + count - 1;
    int i = offset;
    while (i <= max) {
      int run = 1;
      byte replicate = input[i];
      while (run < 127 && i < max && input[i] == input[i + 1]) {
        run++;
        i++;
      }
      if (run > 1) {
        i++;
        o[op++] = (byte) -(run - 1);
        o[op++] = replicate;
      }
      run = 0;
      int save = op;
      while (run < 128 && ((i < max && input[i] != input[i + 1]) || (i < max - 1 && input[i] != input[i + 2]))) {
        run++;
        o[++op] = input[i++];
      }
      if (run > 0) {
        o[save] = (byte) (run - 1);
        op++;
      }
      if (i == max) {
        if (run > 0 && run < 128) {
          o[save]++;
          o[op++] = input[i++];
        } else {
          o[op++] = 0;
          o[op++] = input[i++];
        }
      }
    }
    return Arrays.copyOf(o, op);
  }

  static byte[] packBitsDecode(byte[] in, int expected) {
    byte[] out = new byte[expected];
    int ip = 0;
    int op = 0;
    while (op < expected && ip < in.length) {
      int n = in[ip++];
      if (n >= 0) {
        int c = Math.min(n + 1, Math.min(in.length - ip, expected - op));
        System.arraycopy(in, ip, out, op, c);
        ip += n + 1;
        op += c;
      } else if (n != -128) {
        int c = Math.min(1 - n, expected - op);
        if (ip < in.length) {
          Arrays.fill(out, op, op + c, in[ip++]);
        }
        op += c;
      }
    }
    return out;
  }
}
