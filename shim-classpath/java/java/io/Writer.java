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

public abstract class Writer implements Appendable, Closeable, Flushable {
  private char[] writeBuffer;
  private static final int kWriteBufferSize = 1024;

  protected Object lock;

  public static Writer nullWriter() {
    return new Writer() {
      private volatile boolean closed;

      private void ensureOpen() throws IOException {
        if (closed) {
          throw new IOException("Stream closed");
        }
      }

      @Override
      public Writer append(char c) throws IOException {
        ensureOpen();
        return this;
      }

      @Override
      public Writer append(CharSequence csq) throws IOException {
        ensureOpen();
        return this;
      }

      @Override
      public Writer append(CharSequence csq, int start, int end) throws IOException {
        ensureOpen();
        if (csq != null) {
          Objects.checkFromToIndex(start, end, csq.length());
        }
        return this;
      }

      @Override
      public void write(int c) throws IOException {
        ensureOpen();
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, cbuf.length);
        ensureOpen();
      }

      @Override
      public void write(String str) throws IOException {
        Objects.requireNonNull(str);
        ensureOpen();
      }

      @Override
      public void write(String str, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, str.length());
        ensureOpen();
      }

      @Override
      public void flush() throws IOException {
        ensureOpen();
      }

      @Override
      public void close() {
        closed = true;
      }
    };
  }

  protected Writer() {
    this.lock = this;
  }

  protected Writer(Object lock) {
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock = lock;
  }

  public void write(int c) throws IOException {
    synchronized (lock) {
      if (writeBuffer == null) {
        writeBuffer = new char[kWriteBufferSize];
      }
      writeBuffer[0] = (char) c;
      write(writeBuffer, 0, 1);
    }
  }

  public void write(char[] cbuf) throws IOException {
    write(cbuf, 0, cbuf.length);
  }

  public abstract void write(char[] cbuf, int off, int len) throws IOException;

  public void write(String str) throws IOException {
    write(str, 0, str.length());
  }

  public void write(String str, int off, int len) throws IOException {
    synchronized (lock) {
      char[] cbuf;
      if (len <= kWriteBufferSize) {
        if (writeBuffer == null) {
          writeBuffer = new char[kWriteBufferSize];
        }
        cbuf = writeBuffer;
      } else {
        cbuf = new char[len];
      }
      str.getChars(off, off + len, cbuf, 0);
      write(cbuf, 0, len);
    }
  }

  public Writer append(CharSequence csq) throws IOException {
    write(String.valueOf(csq));
    return this;
  }

  public Writer append(CharSequence csq, int start, int end) throws IOException {
    if (csq == null) {
      csq = "null";
    }
    return append(csq.subSequence(start, end));
  }

  public Writer append(char c) throws IOException {
    write(c);
    return this;
  }

  public abstract void flush() throws IOException;

  public abstract void close() throws IOException;
}
