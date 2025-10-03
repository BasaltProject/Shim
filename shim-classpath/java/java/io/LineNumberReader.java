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

public class LineNumberReader extends BufferedReader {
  private int lineNumber = 0;
  private int markedLineNumber;
  private boolean skipLF;
  private boolean markedSkipLF;

  public LineNumberReader(Reader in) {
    super(in);
  }

  public LineNumberReader(Reader in, int sz) {
    super(in, sz);
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int read() throws IOException {
    synchronized (lock) {
      int c = super.read();
      if (skipLF) {
        if (c == '\n') {
          c = super.read();
        }
        skipLF = false;
      }
      switch (c) {
      case '\r':
        skipLF = true;
        lineNumber++;
        return '\n';
      case '\n':
        lineNumber++;
        return '\n';
      }
      return c;
    }
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      int n = super.read(cbuf, off, len);
      if (n == -1) {
        return -1;
      }
      for (int i = off; i < off + n; i++) {
        int c = cbuf[i];
        if (skipLF) {
          skipLF = false;
          if (c == '\n') {
            continue;
          }
        }
        switch (c) {
        case '\r':
          skipLF = true;
          lineNumber++;
          break;
        case '\n':
          lineNumber++;
          break;
        }
      }
      return n;
    }
  }

  public String readLine() throws IOException {
    synchronized (lock) {
      boolean[] term = new boolean[1];
      String l = super.readLine(skipLF, term);
      skipLF = false;
      if (l != null) {
        lineNumber++;
      }
      return l;
    }
  }

  private static final int kMaxSkipBufferSize = 8192;
  private char[] skipBuffer = null;

  public long skip(long n) throws IOException {
    if (n < 0) {
      throw new IllegalArgumentException("skip() value is negative");
    }
    int nn = (int) Math.min(n, kMaxSkipBufferSize);
    synchronized (lock) {
      if (skipBuffer == null || skipBuffer.length < nn) {
        skipBuffer = new char[nn];
      }
      long r = n;
      while (r > 0) {
        int nc = read(skipBuffer, 0, (int) Math.min(r, nn));
        if (nc == -1) {
          break;
        }
        r -= nc;
      }
      return n - r;
    }
  }

  public void mark(int readAheadLimit) throws IOException {
    synchronized (lock) {
      super.mark(readAheadLimit);
      markedLineNumber = lineNumber;
      markedSkipLF = skipLF;
    }
  }

  public void reset() throws IOException {
    synchronized (lock) {
      super.reset();
      lineNumber = markedLineNumber;
      skipLF = markedSkipLF;
    }
  }
}
