/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

@Deprecated
public class StringBufferInputStream extends InputStream {
  protected String buffer;
  protected int pos;
  protected int count;

  public StringBufferInputStream(String s) {
    this.buffer = s;
    count = s.length();
  }

  public synchronized int read() {
    return (pos < count) ? (buffer.charAt(pos++) & 0xFF) : -1;
  }

  public synchronized int read(byte[] b, int off, int len) {
    if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    }
    if (pos >= count) {
      return -1;
    }
    if (pos + len > count) {
      len = count - pos;
    }
    if (len <= 0) {
      return 0;
    }
    String s = buffer;
    int cnt = len;
    while (--cnt >= 0) {
      b[off++] = (byte) s.charAt(pos++);
    }
    return len;
  }

  public synchronized long skip(long n) {
    if (n < 0) {
      return 0;
    }
    if (n > count - pos) {
      n = count - pos;
    }
    pos += (int) n;
    return n;
  }

  public synchronized int available() {
    return count - pos;
  }

  public synchronized void reset() {
    pos = 0;
  }
}
