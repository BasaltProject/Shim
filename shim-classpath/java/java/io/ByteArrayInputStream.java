/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import java.util.Arrays;
import java.util.Objects;

public class ByteArrayInputStream extends InputStream {
  protected byte[] buf;
  protected int pos;
  protected int mark = 0;
  protected int count;

  public ByteArrayInputStream(byte[] buf) {
    this.buf = buf;
    this.pos = 0;
    this.count = buf.length;
  }

  public ByteArrayInputStream(byte[] buf, int offset, int length) {
    this.buf = buf;
    this.pos = offset;
    this.count = Math.min(offset + length, buf.length);
    this.mark = offset;
  }

  public synchronized int read() {
    return (pos < count) ? (buf[pos++] & 0xff) : -1;
  }

  public synchronized int read(byte[] b, int off, int len) {
    Objects.checkFromIndexSize(off, len, b.length);
    if (pos >= count) {
      return -1;
    }
    int avail = count - pos;
    if (len > avail) {
      len = avail;
    }
    if (len <= 0) {
      return 0;
    }
    System.arraycopy(buf, pos, b, off, len);
    pos += len;
    return len;
  }

  public synchronized byte[] readAllBytes() {
    byte[] result = Arrays.copyOfRange(buf, pos, count);
    pos = count;
    return result;
  }

  public int readNBytes(byte[] b, int off, int len) {
    int n = read(b, off, len);
    return n == -1 ? 0 : n;
  }

  public synchronized long transferTo(OutputStream out) throws IOException {
    int len = count - pos;
    out.write(buf, pos, len);
    pos = count;
    return len;
  }

  public synchronized long skip(long n) {
    long k = count - pos;
    if (n < k) {
      k = n < 0 ? 0 : n;
    }
    pos += (int) k;
    return k;
  }

  public synchronized int available() {
    return count - pos;
  }

  public boolean markSupported() {
    return true;
  }

  public void mark(int readAheadLimit) {
    mark = pos;
  }

  public synchronized void reset() {
    pos = mark;
  }

  public void close() throws IOException { }
}
