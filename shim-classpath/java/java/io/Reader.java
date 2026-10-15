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

public abstract class Reader implements Closeable, Readable {
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

  public abstract void close() throws IOException;

  public long transferTo(Writer out) throws IOException {
    java.util.Objects.requireNonNull(out, "out");
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
        java.util.Objects.checkFromIndexSize(off, len, cbuf.length);
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
}
