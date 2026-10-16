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

public class PushbackReader extends FilterReader {
  private char[] buf;
  private int pos;

  public PushbackReader(Reader in, int size) {
    super(in);
    if (size <= 0) {
      throw new IllegalArgumentException("size <= 0");
    }
    this.buf = new char[size];
    this.pos = size;
  }

  public PushbackReader(Reader in) {
    this(in, 1);
  }

  private void ensureOpen() throws IOException {
    if (buf == null) {
      throw new IOException("Stream closed");
    }
  }

  public int read() throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (pos < buf.length) {
        return buf[pos++];
      } else {
        return super.read();
      }
    }
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      Objects.checkFromIndexSize(off, len, cbuf.length);
      if (len == 0) {
        return 0;
      }
      int avail = buf.length - pos;
      if (avail > 0) {
        if (len < avail) {
          avail = len;
        }
        System.arraycopy(buf, pos, cbuf, off, avail);
        pos += avail;
        off += avail;
        len -= avail;
      }
      if (len > 0) {
        len = super.read(cbuf, off, len);
        if (len == -1) {
          return (avail == 0) ? -1 : avail;
        }
        return avail + len;
      }
      return avail;
    }
  }

  public void unread(int c) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (pos == 0) {
        throw new IOException("Pushback buffer overflow");
      }
      buf[--pos] = (char) c;
    }
  }

  public void unread(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (len > pos) {
        throw new IOException("Pushback buffer overflow");
      }
      pos -= len;
      System.arraycopy(cbuf, off, buf, pos, len);
    }
  }

  public void unread(char[] cbuf) throws IOException {
    unread(cbuf, 0, cbuf.length);
  }

  public boolean ready() throws IOException {
    synchronized (lock) {
      ensureOpen();
      return (pos < buf.length) || super.ready();
    }
  }

  public void mark(int readAheadLimit) throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public boolean markSupported() {
    return false;
  }

  public void close() throws IOException {
    super.close();
    buf = null;
  }

  public long skip(long n) throws IOException {
    if (n < 0L) {
      throw new IllegalArgumentException("skip value is negative");
    }
    synchronized (lock) {
      ensureOpen();
      int avail = buf.length - pos;
      if (avail > 0) {
        if (n <= avail) {
          pos += (int) n;
          return n;
        } else {
          pos = buf.length;
          n -= avail;
        }
      }
      return avail + super.skip(n);
    }
  }
}
