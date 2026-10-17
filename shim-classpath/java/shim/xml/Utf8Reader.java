/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

final class Utf8Reader extends Reader {
  private final InputStream in;
  private final byte[] buf = new byte[4096];
  private int pos;
  private int limit;
  private boolean eof;
  private int pendingLow = -1;
  private IOException pending;

  Utf8Reader(InputStream in) {
    this.in = in;
  }

  private int next() throws IOException {
    if (pos >= limit) {
      if (eof) {
        return -1;
      }
      limit = in.read(buf, 0, buf.length);
      pos = 0;
      if (limit <= 0) {
        eof = true;
        limit = 0;
        return -1;
      }
    }
    return buf[pos++] & 0xff;
  }

  private int peekByte() throws IOException {
    int b = next();
    if (b >= 0) {
      pos--;
    }
    return b;
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    if (len == 0) {
      return 0;
    }
    if (pending != null) {
      IOException e = pending;
      pending = null;
      throw e;
    }
    int n = 0;
    while (n < len) {
      if (pendingLow >= 0) {
        cbuf[off + n++] = (char) pendingLow;
        pendingLow = -1;
        continue;
      }
      if (n > 0 && pos >= limit && !eof) {
        break;
      }
      int b = next();
      if (b < 0) {
        break;
      }
      int cp;
      try {
        cp = decode(b);
      } catch (IOException e) {
        if (n > 0) {
          pending = e;
          return n;
        }
        throw e;
      }
      if (cp >= 0x10000) {
        cbuf[off + n++] = Character.highSurrogate(cp);
        char low = Character.lowSurrogate(cp);
        if (n < len) {
          cbuf[off + n++] = low;
        } else {
          pendingLow = low;
        }
      } else {
        cbuf[off + n++] = (char) cp;
      }
    }
    return n == 0 ? -1 : n;
  }

  private int cont(int index, int total) throws IOException {
    int b = peekByte();
    if (b < 0) {
      throw new XmlMalformedException("Expected byte " + index + " of " + total + "-byte UTF-8 sequence.");
    }
    if ((b & 0xC0) != 0x80) {
      throw new XmlMalformedException("Invalid byte " + index + " of " + total + "-byte UTF-8 sequence.");
    }
    next();
    return b & 0x3F;
  }

  private int decode(int b) throws IOException {
    if (b < 0x80) {
      return b;
    }
    if (b >= 0xC2 && b <= 0xDF) {
      return ((b & 0x1F) << 6) | cont(2, 2);
    }
    if (b >= 0xE0 && b <= 0xEF) {
      int b2 = peekByte();
      if (b2 < 0) {
        throw new XmlMalformedException("Expected byte 2 of 3-byte UTF-8 sequence.");
      }
      if ((b2 & 0xC0) != 0x80 || (b == 0xE0 && b2 < 0xA0) || (b == 0xED && b2 >= 0xA0)) {
        throw new XmlMalformedException("Invalid byte 2 of 3-byte UTF-8 sequence.");
      }
      next();
      int c3 = cont(3, 3);
      return ((b & 0x0F) << 12) | ((b2 & 0x3F) << 6) | c3;
    }
    if (b >= 0xF0 && b <= 0xF4) {
      int b2 = peekByte();
      if (b2 < 0) {
        throw new XmlMalformedException("Expected byte 2 of 4-byte UTF-8 sequence.");
      }
      if ((b2 & 0xC0) != 0x80 || (b == 0xF0 && b2 < 0x90)) {
        throw new XmlMalformedException("Invalid byte 2 of 4-byte UTF-8 sequence.");
      }
      if (b == 0xF4 && b2 >= 0x90) {
        throw new XmlMalformedException("High surrogate bits in UTF-8 sequence must not exceed 0x10 but found 0x"
            + Integer.toHexString(((b << 2) & 0x07) | ((b2 >> 4) & 0x03)) + ".");
      }
      next();
      int c3 = cont(3, 4);
      int c4 = cont(4, 4);
      return ((b & 0x07) << 18) | ((b2 & 0x3F) << 12) | (c3 << 6) | c4;
    }
    throw new XmlMalformedException("Invalid byte 1 of 1-byte UTF-8 sequence.");
  }

  public void close() throws IOException {
    in.close();
  }
}
