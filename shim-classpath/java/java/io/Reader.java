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

import java.nio.CharBuffer;
import java.util.List;
import java.util.Objects;

public abstract class Reader implements Closeable, Readable {
  protected Object lock;

  protected Reader() {
    this.lock = this;
  }

  protected Reader(Object lock) {
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock = lock;
  }

  public int read(CharBuffer buffer) throws IOException {
    int c = read(buffer.array(),
                 buffer.arrayOffset() + buffer.position(),
                 buffer.remaining());

    if (c > 0) {
      buffer.position(buffer.position() + c);
    }

    return c;
  }

  public int read() throws IOException {
    char[] buffer = new char[1];
    int c = read(buffer);
    if (c <= 0) {
      return -1;
    } else {
      return (int) buffer[0];
    }
  }

  public int read(char[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public abstract int read(char[] buffer, int offset, int length)
    throws IOException;

  public boolean markSupported() {
    return false;
  }
  
  public void mark(int readAheadLimit) throws IOException {
    throw new IOException("mark not supported");
  }
  
  public void reset() throws IOException {
    throw new IOException("reset not supported");
  }

  public long skip(long n) throws IOException {
    if (n < 0L) {
      throw new IllegalArgumentException("skip value is negative");
    }
    int nn = (int) Math.min(n, 8192L);
    char[] skipBuffer = new char[nn];
    long r = n;
    while (r > 0) {
      int nc = read(skipBuffer, 0, (int) Math.min(r, nn));
      if (nc == -1) break;
      r -= nc;
    }
    return n - r;
  }

  public boolean ready() throws IOException {
    return false;
  }

  public String readAllAsString() throws IOException {
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[8192];
    int n;
    while ((n = read(buf, 0, buf.length)) != -1) {
      sb.append(buf, 0, n);
    }
    return sb.toString();
  }

  public List<String> readAllLines() throws IOException {
    return readAllAsString().lines().toList();
  }

  public abstract void close() throws IOException;

  public long transferTo(Writer out) throws IOException {
    Objects.requireNonNull(out, "out");
    long transferred = 0;
    char[] buffer = new char[8192];
    int count;
    while ((count = this.read(buffer, 0, buffer.length)) >= 0) {
      out.write(buffer, 0, count);
      transferred += count;
    }
    return transferred;
  }

  public static Reader nullReader() {
    return new Reader() {
      private volatile boolean closed;

      private void ensureOpen() throws IOException {
        if (closed) {
          throw new IOException("Stream closed");
        }
      }

      @Override
      public int read() throws IOException {
        ensureOpen();
        return -1;
      }

      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, cbuf.length);
        ensureOpen();
        if (len == 0) {
          return 0;
        }
        return -1;
      }

      @Override
      public void close() {
        closed = true;
      }
    };
  }

  public static Reader of(CharSequence cs) {
    Objects.requireNonNull(cs, "cs");
    return new Reader() {
      private int next = 0;
      private int mark = 0;
      private boolean closed = false;

      private void ensureOpen() throws IOException {
        if (closed) {
          throw new IOException("Stream closed");
        }
      }

      @Override
      public int read() throws IOException {
        ensureOpen();
        if (next >= cs.length()) {
          return -1;
        }
        return (int) cs.charAt(next++);
      }

      @Override
      public int read(char[] cbuf, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, cbuf.length);
        ensureOpen();
        if (len == 0) {
          return 0;
        }
        int lenTotal = cs.length();
        if (next >= lenTotal) {
          return -1;
        }
        int count = Math.min(lenTotal - next, len);
        for (int i = 0; i < count; i++) {
          cbuf[off + i] = cs.charAt(next++);
        }
        return count;
      }

      @Override
      public String readAllAsString() throws IOException {
        ensureOpen();
        String res = cs.subSequence(next, cs.length()).toString();
        next += res.length();
        return res;
      }

      @Override
      public List<String> readAllLines() throws IOException {
        return readAllAsString().lines().toList();
      }

      @Override
      public long skip(long n) throws IOException {
        ensureOpen();
        if (next >= cs.length()) {
          return 0L;
        }
        long r = Math.min((long) (cs.length() - next), n);
        r = Math.max((long) -next, r);
        next += (int) r;
        return r;
      }

      @Override
      public boolean ready() throws IOException {
        ensureOpen();
        return true;
      }

      @Override
      public boolean markSupported() {
        return true;
      }

      @Override
      public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
          throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        ensureOpen();
        mark = next;
      }

      @Override
      public void reset() throws IOException {
        ensureOpen();
        next = mark;
      }

      @Override
      public void close() {
        closed = true;
      }
    };
  }
}
