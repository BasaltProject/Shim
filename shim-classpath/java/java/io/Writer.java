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

public abstract class Writer implements Closeable, Flushable, Appendable {
  public void write(int c) throws IOException {
    char[] buffer = new char[] { (char) c };
    write(buffer);
  }

  public void write(char[] buffer) throws IOException {
    write(buffer, 0, buffer.length);
  }

  public void write(String s) throws IOException {
    write(s.toCharArray());
  }

  public void write(String s, int offset, int length) throws IOException {
    char[] b = new char[length];
    s.getChars(offset, offset + length, b, 0);
    write(b);
  }

  public abstract void write(char[] buffer, int offset, int length)
    throws IOException;

  public Writer append(final char c) throws IOException {
    write((int)c);
    return this;
  }

  public Writer append(final CharSequence sequence) throws IOException {
    return append(sequence, 0, sequence == null ? 4 : sequence.length());
  }

  public Writer append(CharSequence sequence, int start, int end) 
      throws IOException {
    if (sequence == null) {
      sequence = "null";
    }
    final int length = end - start;
    if (sequence instanceof String) {
      write((String)sequence, start, length);
    } else {
      final char[] charArray = new char[length];
      for (int i = start; i < end; i++) { 
        charArray[i - start] = sequence.charAt(i);
      }
      write(charArray, 0, length);
    }
    return this;
  }

  public abstract void flush() throws IOException;

  public abstract void close() throws IOException;

  public static Writer nullWriter() {
    return new Writer() {
      private volatile boolean closed;

      private void ensureOpen() throws IOException {
        if (closed) {
          throw new IOException("Stream closed");
        }
      }

      @Override
      public void write(int c) throws IOException {
        ensureOpen();
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        java.util.Objects.checkFromIndexSize(off, len, cbuf.length);
        ensureOpen();
      }

      @Override
      public void write(String str, int off, int len) throws IOException {
        java.util.Objects.checkFromIndexSize(off, len, str.length());
        ensureOpen();
      }

      @Override
      public Writer append(CharSequence csq) throws IOException {
        ensureOpen();
        return this;
      }

      @Override
      public Writer append(CharSequence csq, int start, int end) throws IOException {
        ensureOpen();
        return this;
      }

      @Override
      public Writer append(char c) throws IOException {
        ensureOpen();
        return this;
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
}
