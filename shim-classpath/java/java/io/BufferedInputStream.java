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

import java.util.Arrays;
import java.util.Objects;

public class BufferedInputStream extends FilterInputStream {
  private static final int kDefaultBufferSize = 8192;
  private static final int kMaxBufferSize = Integer.MAX_VALUE - 8;

  protected volatile byte[] buf;
  protected int count;
  protected int pos;
  protected int markpos = -1;
  protected int marklimit;

  private InputStream getInIfOpen() throws IOException {
    InputStream input = in;
    if (input == null) {
      throw new IOException("Stream closed");
    }
    return input;
  }

  private byte[] getBufIfOpen() throws IOException {
    byte[] buffer = buf;
    if (buffer == null) {
      throw new IOException("Stream closed");
    }
    return buffer;
  }

  public BufferedInputStream(InputStream in) {
    this(in, kDefaultBufferSize);
  }

  public BufferedInputStream(InputStream in, int size) {
    super(in);
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    buf = new byte[size];
  }

  private void fill() throws IOException {
    byte[] buffer = getBufIfOpen();
    if (markpos < 0) {
      pos = 0;
    } else if (pos >= buffer.length) {
      if (markpos > 0) {
        int sz = pos - markpos;
        System.arraycopy(buffer, markpos, buffer, 0, sz);
        pos = sz;
        markpos = 0;
      } else if (buffer.length >= marklimit) {
        markpos = -1;
        pos = 0;
      } else if (buffer.length >= kMaxBufferSize) {
        throw new OutOfMemoryError("Required array size too large");
      } else {
        int nsz = (pos <= kMaxBufferSize - pos) ? pos * 2 : kMaxBufferSize;
        if (nsz > marklimit) {
          nsz = marklimit;
        }
        byte[] nbuf = new byte[nsz];
        System.arraycopy(buffer, 0, nbuf, 0, pos);
        buf = nbuf;
        buffer = nbuf;
      }
    }
    count = pos;
    int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
    if (n > 0) {
      count = n + pos;
    }
  }

  public synchronized int read() throws IOException {
    if (pos >= count) {
      fill();
      if (pos >= count) {
        return -1;
      }
    }
    return getBufIfOpen()[pos++] & 0xff;
  }

  private int read1(byte[] b, int off, int len) throws IOException {
    int avail = count - pos;
    if (avail <= 0) {
      if (len >= getBufIfOpen().length && markpos < 0) {
        return getInIfOpen().read(b, off, len);
      }
      fill();
      avail = count - pos;
      if (avail <= 0) {
        return -1;
      }
    }
    int cnt = (avail < len) ? avail : len;
    System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
    pos += cnt;
    return cnt;
  }

  public synchronized int read(byte[] b, int off, int len) throws IOException {
    getBufIfOpen();
    Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return 0;
    }
    int n = 0;
    for (;;) {
      int nread = read1(b, off + n, len - n);
      if (nread <= 0) {
        return (n == 0) ? nread : n;
      }
      n += nread;
      if (n >= len) {
        return n;
      }
      InputStream input = in;
      if (input != null && input.available() <= 0) {
        return n;
      }
    }
  }

  public synchronized long skip(long n) throws IOException {
    getBufIfOpen();
    if (n <= 0) {
      return 0;
    }
    long avail = count - pos;
    if (avail <= 0) {
      if (markpos < 0) {
        return getInIfOpen().skip(n);
      }
      fill();
      avail = count - pos;
      if (avail <= 0) {
        return 0;
      }
    }
    long skipped = (avail < n) ? avail : n;
    pos += (int) skipped;
    return skipped;
  }

  public synchronized int available() throws IOException {
    int n = count - pos;
    int avail = getInIfOpen().available();
    return n > (Integer.MAX_VALUE - avail) ? Integer.MAX_VALUE : n + avail;
  }

  public synchronized void mark(int readlimit) {
    marklimit = readlimit;
    markpos = pos;
  }

  public synchronized void reset() throws IOException {
    getBufIfOpen();
    if (markpos < 0) {
      throw new IOException("Resetting to invalid mark");
    }
    pos = markpos;
  }

  public boolean markSupported() {
    return true;
  }

  public void close() throws IOException {
    byte[] buffer;
    while ((buffer = buf) != null) {
      buf = null;
      InputStream input = in;
      in = null;
      if (input != null) {
        input.close();
      }
      return;
    }
  }
}
