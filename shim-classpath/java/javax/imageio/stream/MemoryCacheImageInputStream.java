/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MemoryCacheImageInputStream extends ImageInputStreamImpl {
  private InputStream stream;
  private byte[] cache = new byte[0];
  private int cached;
  private boolean eof;

  public MemoryCacheImageInputStream(InputStream stream) {
    if (stream == null) {
      throw new IllegalArgumentException("stream == null!");
    }
    this.stream = stream;
  }

  private boolean fill(long upTo) throws IOException {
    while (cached <= upTo && !eof) {
      if (cache.length == cached) {
        byte[] bigger = new byte[Math.max(4096, cache.length * 2)];
        System.arraycopy(cache, 0, bigger, 0, cached);
        cache = bigger;
      }
      int n = stream.read(cache, cached, cache.length - cached);
      if (n < 0) {
        eof = true;
      } else {
        cached += n;
      }
    }
    return upTo < cached;
  }

  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    if (!fill(streamPos)) {
      return -1;
    }
    return cache[(int) streamPos++] & 0xff;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    checkClosed();
    if (b == null) {
      throw new NullPointerException("b == null!");
    }
    if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
      throw new IndexOutOfBoundsException("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
    }
    bitOffset = 0;
    if (len == 0) {
      return 0;
    }
    if (!fill(streamPos)) {
      return -1;
    }
    fill(streamPos + len - 1);
    int n = (int) Math.min((long) len, cached - streamPos);
    System.arraycopy(cache, (int) streamPos, b, off, n);
    streamPos += n;
    return n;
  }

  public void flushBefore(long pos) throws IOException {
    super.flushBefore(pos);
  }

  public boolean isCached() {
    return true;
  }

  public boolean isCachedFile() {
    return false;
  }

  public boolean isCachedMemory() {
    return true;
  }

  public void close() throws IOException {
    super.close();
    cache = new byte[0];
    cached = 0;
    stream = null;
  }

  protected void finalize() throws Throwable {
  }
}
