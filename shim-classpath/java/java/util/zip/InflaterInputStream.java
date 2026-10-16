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

package java.util.zip;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InflaterInputStream extends FilterInputStream {
  protected Inflater inf;
  protected byte[] buf;
  protected int len;

  private boolean closed;
  private boolean reachEOF;
  boolean usesDefaultInflater;
  private final byte[] singleByteBuf = new byte[1];
  private final byte[] skipBuf = new byte[512];

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  public InflaterInputStream(InputStream in, Inflater inf, int size) {
    super(in);
    if (in == null || inf == null) {
      throw new NullPointerException();
    } else if (size <= 0) {
      throw new IllegalArgumentException("buffer size <= 0");
    }
    this.inf = inf;
    buf = new byte[size];
  }

  public InflaterInputStream(InputStream in, Inflater inf) {
    this(in, inf, 512);
  }

  public InflaterInputStream(InputStream in) {
    this(in, new Inflater());
    usesDefaultInflater = true;
  }

  public int read() throws IOException {
    ensureOpen();
    return read(singleByteBuf, 0, 1) == -1 ? -1 : singleByteBuf[0] & 0xff;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    ensureOpen();
    if (b == null) {
      throw new NullPointerException();
    }
    java.util.Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return 0;
    }
    try {
      int n;
      while ((n = inf.inflate(b, off, len)) == 0) {
        if (inf.finished() || inf.needsDictionary()) {
          reachEOF = true;
          return -1;
        }
        if (inf.needsInput()) {
          fill();
        }
      }
      return n;
    } catch (DataFormatException e) {
      String s = e.getMessage();
      throw new ZipException(s != null ? s : "Invalid ZLIB data format");
    }
  }

  public int available() throws IOException {
    ensureOpen();
    if (reachEOF) {
      return 0;
    } else if (inf.finished()) {
      reachEOF = true;
      return 0;
    }
    return 1;
  }

  public long skip(long n) throws IOException {
    if (n < 0) {
      throw new IllegalArgumentException("negative skip length");
    }
    ensureOpen();
    int max = (int) Math.min(n, Integer.MAX_VALUE);
    int total = 0;
    while (total < max) {
      int len = Math.min(max - total, skipBuf.length);
      len = read(skipBuf, 0, len);
      if (len == -1) {
        reachEOF = true;
        break;
      }
      total += len;
    }
    return total;
  }

  public void close() throws IOException {
    if (!closed) {
      if (usesDefaultInflater) {
        inf.end();
      }
      in.close();
      closed = true;
    }
  }

  protected void fill() throws IOException {
    ensureOpen();
    len = in.read(buf, 0, buf.length);
    if (len == -1) {
      throw new EOFException("Unexpected end of ZLIB input stream");
    }
    inf.setInput(buf, 0, len);
  }

  public boolean markSupported() {
    return false;
  }

  public synchronized void mark(int readlimit) { }

  public synchronized void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }
}
