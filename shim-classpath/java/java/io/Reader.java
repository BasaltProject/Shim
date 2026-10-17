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
import java.nio.ReadOnlyBufferException;
import java.util.Objects;

public abstract class Reader implements Readable, Closeable {
  private static final int kMaxSkipBufferSize = 8192;
  private static final int kTransferBufferSize = 8192;

  protected Object lock;
  private char[] skipBuffer = null;

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
      public int read(CharBuffer target) throws IOException {
        Objects.requireNonNull(target);
        ensureOpen();
        if (target.hasRemaining()) {
          return -1;
        }
        return 0;
      }

      @Override
      public boolean ready() throws IOException {
        ensureOpen();
        return false;
      }

      @Override
      public long skip(long n) throws IOException {
        if (n < 0L) {
          throw new IllegalArgumentException("skip value is negative");
        }
        ensureOpen();
        return 0L;
      }

      @Override
      public long transferTo(Writer out) throws IOException {
        Objects.requireNonNull(out);
        ensureOpen();
        return 0L;
      }

      @Override
      public void close() {
        closed = true;
      }
    };
  }

  public static Reader of(final CharSequence cs) {
    Objects.requireNonNull(cs);
    return new Reader() {
      private boolean closed;
      private int next;
      private int mark;

      private void ensureOpen() throws IOException {
        if (closed) throw new IOException("Stream closed");
      }

      public int read() throws IOException {
        synchronized (this) {
          ensureOpen();
          return next < cs.length() ? cs.charAt(next++) : -1;
        }
      }

      public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (this) {
          ensureOpen();
          Objects.checkFromIndexSize(off, len, cbuf.length);
          if (len == 0) return 0;
          if (next >= cs.length()) return -1;
          int n = Math.min(cs.length() - next, len);
          for (int i = 0; i < n; i++) cbuf[off + i] = cs.charAt(next + i);
          next += n;
          return n;
        }
      }

      public long skip(long n) throws IOException {
        synchronized (this) {
          ensureOpen();
          if (next >= cs.length()) return 0;
          long r = Math.min(cs.length() - next, n);
          r = Math.max(-next, r);
          next += (int) r;
          return r;
        }
      }

      public boolean ready() throws IOException {
        synchronized (this) {
          ensureOpen();
          return true;
        }
      }

      public boolean markSupported() {
        return true;
      }

      public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) throw new IllegalArgumentException("Read-ahead limit < 0");
        synchronized (this) {
          ensureOpen();
          mark = next;
        }
      }

      public void reset() throws IOException {
        synchronized (this) {
          ensureOpen();
          next = mark;
        }
      }

      public void close() {
        synchronized (this) {
          closed = true;
        }
      }
    };
  }

  public String readAllAsString() throws IOException {
    StringBuilder result = new StringBuilder();
    char[] cbuf = new char[kTransferBufferSize];
    int nread;
    while ((nread = read(cbuf, 0, cbuf.length)) != -1) {
      result.append(cbuf, 0, nread);
    }
    return result.toString();
  }

  public java.util.List<String> readAllLines() throws IOException {
    java.util.List<String> lines = new java.util.ArrayList<String>();
    BufferedReader br = this instanceof BufferedReader ? (BufferedReader) this : new BufferedReader(this);
    String line;
    while ((line = br.readLine()) != null) {
      lines.add(line);
    }
    return lines;
  }

  protected Reader() {
    this.lock = this;
  }

  protected Reader(Object lock) {
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock = lock;
  }

  public int read(CharBuffer target) throws IOException {
    if (target.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int nread;
    if (target.hasArray()) {
      char[] cbuf = target.array();
      int pos = target.position();
      int rem = Math.max(target.limit() - pos, 0);
      int off = target.arrayOffset() + pos;
      nread = this.read(cbuf, off, rem);
      if (nread > 0) {
        target.position(pos + nread);
      }
    } else {
      int len = target.remaining();
      char[] cbuf = new char[len];
      nread = read(cbuf, 0, len);
      if (nread > 0) {
        target.put(cbuf, 0, nread);
      }
    }
    return nread;
  }

  public int read() throws IOException {
    char[] cb = new char[1];
    if (read(cb, 0, 1) == -1) {
      return -1;
    } else {
      return cb[0];
    }
  }

  public int read(char[] cbuf) throws IOException {
    return read(cbuf, 0, cbuf.length);
  }

  public abstract int read(char[] cbuf, int off, int len) throws IOException;

  public long skip(long n) throws IOException {
    if (n < 0L) {
      throw new IllegalArgumentException("skip value is negative");
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

  public boolean ready() throws IOException {
    return false;
  }

  public boolean markSupported() {
    return false;
  }

  public void mark(int readAheadLimit) throws IOException {
    throw new IOException("mark() not supported");
  }

  public void reset() throws IOException {
    throw new IOException("reset() not supported");
  }

  public abstract void close() throws IOException;

  public long transferTo(Writer out) throws IOException {
    Objects.requireNonNull(out, "out");
    long transferred = 0;
    char[] buffer = new char[kTransferBufferSize];
    int nRead;
    while ((nRead = read(buffer, 0, kTransferBufferSize)) >= 0) {
      out.write(buffer, 0, nRead);
      transferred += nRead;
    }
    return transferred;
  }
}
