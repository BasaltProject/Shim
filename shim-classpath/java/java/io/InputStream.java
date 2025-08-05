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

public abstract class InputStream implements Closeable {
  public abstract int read() throws IOException;

  public int read(byte[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte[] buffer, int offset, int length) throws IOException {
    for (int i = 0; i < length; ++i) {
      int c = read();
      if (c == -1) {
        if (i == 0) {
          return -1;
        } else {
          return i;
        }
      } else {
        buffer[offset + i] = (byte) (c & 0xFF);
      }
    }
    return length;
  }

  public long skip(long count) throws IOException {
    final long Max = 8 * 1024;
    int size = (int) (count < Max ? count : Max);
    byte[] buffer = new byte[size];
    long remaining = count;
    int c;
    while ((c = read(buffer, 0, (int) (size < remaining ? size : remaining)))
           >= 0
           && remaining > 0) {
      remaining -= c;
    }
    return count - remaining;    
  }

  public int available() throws IOException {
    return 0;
  }

  public void mark(int limit) {
    // ignore
  }

  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public boolean markSupported() {
    return false;
  }

  public void close() throws IOException { }

  public byte[] readAllBytes() throws IOException {
    return readNBytes(Integer.MAX_VALUE);
  }

  public byte[] readNBytes(int len) throws IOException {
    if (len < 0) {
      throw new IllegalArgumentException("len < 0");
    }
    int initialCapacity = Math.min(len, 8192);
    byte[] buf = new byte[initialCapacity];
    int total = 0;
    while (total < len) {
      int remaining = len - total;
      if (total >= buf.length) {
        int newCap = (int) Math.min((long) buf.length * 2, (long) len);
        if (newCap <= buf.length) {
          newCap = len;
        }
        byte[] newBuf = new byte[newCap];
        System.arraycopy(buf, 0, newBuf, 0, total);
        buf = newBuf;
      }
      int toRead = Math.min(remaining, buf.length - total);
      int count = read(buf, total, toRead);
      if (count < 0) {
        break;
      }
      total += count;
    }
    if (total == buf.length) {
      return buf;
    }
    byte[] res = new byte[total];
    System.arraycopy(buf, 0, res, 0, total);
    return res;
  }

  public int readNBytes(byte[] buffer, int offset, int length) throws IOException {
    java.util.Objects.checkFromIndexSize(offset, length, buffer.length);
    int total = 0;
    while (total < length) {
      int count = read(buffer, offset + total, length - total);
      if (count < 0) {
        break;
      }
      total += count;
    }
    return total;
  }

  public long transferTo(OutputStream out) throws IOException {
    java.util.Objects.requireNonNull(out, "out");
    long transferred = 0;
    byte[] buffer = new byte[8192];
    int count;
    while ((count = this.read(buffer, 0, buffer.length)) >= 0) {
      out.write(buffer, 0, count);
      transferred += count;
    }
    return transferred;
  }

  public static InputStream nullInputStream() {
    return new InputStream() {
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
      public int read(byte[] b, int off, int len) throws IOException {
        java.util.Objects.checkFromIndexSize(off, len, b.length);
        ensureOpen();
        if (len == 0) {
          return 0;
        }
        return -1;
      }

      @Override
      public byte[] readAllBytes() throws IOException {
        ensureOpen();
        return new byte[0];
      }

      @Override
      public byte[] readNBytes(int len) throws IOException {
        if (len < 0) {
          throw new IllegalArgumentException("len < 0");
        }
        ensureOpen();
        return new byte[0];
      }

      @Override
      public int readNBytes(byte[] b, int off, int len) throws IOException {
        java.util.Objects.checkFromIndexSize(off, len, b.length);
        ensureOpen();
        return 0;
      }

      @Override
      public long skip(long n) throws IOException {
        ensureOpen();
        return 0;
      }

      @Override
      public long transferTo(OutputStream out) throws IOException {
        java.util.Objects.requireNonNull(out, "out");
        ensureOpen();
        return 0;
      }

      @Override
      public void close() {
        closed = true;
      }
    };
  }
}
