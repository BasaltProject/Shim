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

public class StringReader extends Reader {
  private String str;
  private int length;
  private int next = 0;
  private int mark = 0;

  public StringReader(String s) {
    this.str = s;
    this.length = s.length();
  }

  private void ensureOpen() throws IOException {
    if (str == null) {
      throw new IOException("Stream closed");
    }
  }

  public int read() throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (next >= length) {
        return -1;
      }
      return str.charAt(next++);
    }
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      Objects.checkFromIndexSize(off, len, cbuf.length);
      if (len == 0) {
        return 0;
      }
      if (next >= length) {
        return -1;
      }
      int n = Math.min(length - next, len);
      str.getChars(next, next + n, cbuf, off);
      next += n;
      return n;
    }
  }

  public long skip(long n) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (next >= length) {
        return 0;
      }
      long r = Math.min(length - next, n);
      r = Math.max(-next, r);
      next += (int) r;
      return r;
    }
  }

  public boolean ready() throws IOException {
    synchronized (lock) {
      ensureOpen();
      return true;
    }
  }

  public boolean markSupported() {
    return true;
  }

  public void mark(int readAheadLimit) throws IOException {
    if (readAheadLimit < 0) {
      throw new IllegalArgumentException("Read-ahead limit < 0");
    }
    synchronized (lock) {
      ensureOpen();
      mark = next;
    }
  }

  public void reset() throws IOException {
    synchronized (lock) {
      ensureOpen();
      next = mark;
    }
  }

  public void close() {
    str = null;
  }
}
