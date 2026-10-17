/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ChunkedOutputStream extends PrintStream {
  private static final int kDefaultChunk = 4096;

  private final PrintStream out;
  private final byte[] buf;
  private int count;
  private final int preferredChunkDataSize;
  private boolean closed;

  public ChunkedOutputStream(PrintStream o) {
    this(o, kDefaultChunk);
  }

  public ChunkedOutputStream(PrintStream o, int size) {
    super(o);
    out = o;
    preferredChunkDataSize = size <= 0 ? kDefaultChunk : size;
    buf = new byte[preferredChunkDataSize];
  }

  private void flushBuffer() {
    if (count == 0) {
      return;
    }
    byte[] hex = Integer.toHexString(count).getBytes(StandardCharsets.ISO_8859_1);
    out.write(hex, 0, hex.length);
    out.write('\r');
    out.write('\n');
    out.write(buf, 0, count);
    out.write('\r');
    out.write('\n');
    count = 0;
  }

  @Override
  public synchronized void write(int b) {
    if (closed) {
      return;
    }
    buf[count++] = (byte) b;
    if (count == preferredChunkDataSize) {
      flushBuffer();
    }
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) {
    if (closed) {
      return;
    }
    while (len > 0) {
      int n = Math.min(len, preferredChunkDataSize - count);
      System.arraycopy(b, off, buf, count, n);
      count += n;
      off += n;
      len -= n;
      if (count == preferredChunkDataSize) {
        flushBuffer();
      }
    }
  }

  @Override
  public synchronized void flush() {
    if (closed) {
      return;
    }
    flushBuffer();
    out.flush();
  }

  @Override
  public synchronized void close() {
    if (closed) {
      return;
    }
    flushBuffer();
    out.write('0');
    out.write('\r');
    out.write('\n');
    out.write('\r');
    out.write('\n');
    out.flush();
    closed = true;
  }

  @Override
  public boolean checkError() {
    return out.checkError();
  }
}
