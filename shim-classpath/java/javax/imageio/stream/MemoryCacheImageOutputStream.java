/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class MemoryCacheImageOutputStream extends ImageOutputStreamImpl {
  private OutputStream stream;
  private byte[] buf = new byte[1024];
  private int size;
  private long base;

  public MemoryCacheImageOutputStream(OutputStream stream) {
    if (stream == null) {
      throw new IllegalArgumentException("stream == null!");
    }
    this.stream = stream;
  }

  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    int rel = (int) (streamPos - base);
    if (rel >= size) {
      return -1;
    }
    streamPos++;
    return buf[rel] & 0xff;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    checkClosed();
    bitOffset = 0;
    if (len == 0) {
      return 0;
    }
    int rel = (int) (streamPos - base);
    if (rel >= size) {
      return -1;
    }
    int n = Math.min(len, size - rel);
    System.arraycopy(buf, rel, b, off, n);
    streamPos += n;
    return n;
  }

  public void write(int b) throws IOException {
    flushBits();
    int rel = (int) (streamPos - base);
    ensure(rel + 1);
    buf[rel] = (byte) b;
    size = Math.max(size, rel + 1);
    streamPos++;
  }

  public void write(byte[] b, int off, int len) throws IOException {
    flushBits();
    if (off < 0 || len < 0 || off + len > b.length) {
      throw new IndexOutOfBoundsException();
    }
    int rel = (int) (streamPos - base);
    ensure(rel + len);
    System.arraycopy(b, off, buf, rel, len);
    size = Math.max(size, rel + len);
    streamPos += len;
  }

  private void ensure(int n) {
    if (n > buf.length) {
      buf = Arrays.copyOf(buf, Math.max(n, buf.length * 2));
    }
  }

  public long length() {
    return base + size;
  }

  public boolean isCached() {
    return true;
  }

  public boolean isCachedMemory() {
    return true;
  }

  public boolean isCachedFile() {
    return false;
  }

  public void close() throws IOException {
    long length = flushedPos + 0;
    super.close();
    flushBefore(length() > flushedPos ? Math.min(length(), getFlushedOrEnd()) : flushedPos);
    stream.flush();
    buf = new byte[0];
  }

  private long getFlushedOrEnd() {
    return base + size;
  }

  public void flushBefore(long pos) throws IOException {
    long oFlushedPos = flushedPos;
    if (pos < oFlushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    if (pos > streamPos && pos > base + size) {
      throw new IndexOutOfBoundsException("pos > getStreamPosition()!");
    }
    int n = (int) (pos - base);
    if (n > 0) {
      stream.write(buf, 0, Math.min(n, size));
      System.arraycopy(buf, n, buf, 0, size - n);
      size -= n;
      base = pos;
    }
    flushedPos = pos;
  }

  public void flush() throws IOException {
    flushBefore(Math.min(streamPos, base + size));
    stream.flush();
  }

  public void reset() throws IOException {
    super.reset();
  }

  protected void finalize() throws Throwable {
  }
}
