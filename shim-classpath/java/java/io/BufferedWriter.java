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

public class BufferedWriter extends Writer {
  private static final int kDefaultCharBufferSize = 8192;

  private Writer out;
  private char[] cb;
  private int nChars;
  private int nextChar;

  public BufferedWriter(Writer out) {
    this(out, kDefaultCharBufferSize);
  }

  public BufferedWriter(Writer out, int sz) {
    super(out);
    if (sz <= 0) {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    this.out = out;
    cb = new char[sz];
    nChars = sz;
    nextChar = 0;
  }

  private void ensureOpen() throws IOException {
    if (out == null) {
      throw new IOException("Stream closed");
    }
  }

  void flushBuffer() throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (nextChar == 0) {
        return;
      }
      out.write(cb, 0, nextChar);
      nextChar = 0;
    }
  }

  public void write(int c) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (nextChar >= nChars) {
        flushBuffer();
      }
      cb[nextChar++] = (char) c;
    }
  }

  public void write(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      Objects.checkFromIndexSize(off, len, cbuf.length);
      if (len == 0) {
        return;
      }
      if (len >= nChars) {
        flushBuffer();
        out.write(cbuf, off, len);
        return;
      }
      int b = off;
      int t = off + len;
      while (b < t) {
        int d = Math.min(nChars - nextChar, t - b);
        System.arraycopy(cbuf, b, cb, nextChar, d);
        b += d;
        nextChar += d;
        if (nextChar >= nChars) {
          flushBuffer();
        }
      }
    }
  }

  public void write(String s, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      int b = off;
      int t = off + len;
      while (b < t) {
        int d = Math.min(nChars - nextChar, t - b);
        s.getChars(b, b + d, cb, nextChar);
        b += d;
        nextChar += d;
        if (nextChar >= nChars) {
          flushBuffer();
        }
      }
    }
  }

  public void newLine() throws IOException {
    write(System.lineSeparator());
  }

  public void flush() throws IOException {
    synchronized (lock) {
      flushBuffer();
      out.flush();
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      if (out == null) {
        return;
      }
      try {
        flushBuffer();
      } finally {
        try {
          out.close();
        } finally {
          out = null;
          cb = null;
        }
      }
    }
  }
}
