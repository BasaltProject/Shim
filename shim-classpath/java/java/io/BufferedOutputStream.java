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

import java.util.Objects;

public class BufferedOutputStream extends FilterOutputStream {
  protected byte[] buf;
  protected int count;

  public BufferedOutputStream(OutputStream out) {
    this(out, 8192);
  }

  public BufferedOutputStream(OutputStream out, int size) {
    super(out);
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    buf = new byte[size];
  }

  private void flushBuffer() throws IOException {
    if (count > 0) {
      out.write(buf, 0, count);
      count = 0;
    }
  }

  public synchronized void write(int b) throws IOException {
    if (count >= buf.length) {
      flushBuffer();
    }
    buf[count++] = (byte) b;
  }

  public synchronized void write(byte[] b, int off, int len) throws IOException {
    if (len >= buf.length) {
      flushBuffer();
      out.write(b, off, len);
      return;
    }
    if (len > buf.length - count) {
      flushBuffer();
    }
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  public synchronized void flush() throws IOException {
    flushBuffer();
    out.flush();
  }
}
