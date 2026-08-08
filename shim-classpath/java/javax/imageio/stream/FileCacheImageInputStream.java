/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileCacheImageInputStream extends ImageInputStreamImpl {
  private InputStream stream;
  private File cacheFile;
  private RandomAccessFile cache;
  private byte[] buf = new byte[1024];
  private long length = 0L;
  private boolean foundEOF = false;

  public FileCacheImageInputStream(InputStream stream, File cacheDir) throws IOException {
    if (stream == null) {
      throw new IllegalArgumentException("stream == null!");
    }
    if ((cacheDir != null) && !(cacheDir.isDirectory())) {
      throw new IllegalArgumentException("Not a directory!");
    }
    this.stream = stream;
    if (cacheDir == null) {
      this.cacheFile = File.createTempFile("imageio", ".tmp");
    } else {
      this.cacheFile = File.createTempFile("imageio", ".tmp", cacheDir);
    }
    this.cache = new RandomAccessFile(cacheFile, "rw");
    cacheFile.deleteOnExit();
  }

  private long readUntil(long pos) throws IOException {
    if (pos < length) {
      return pos;
    }
    if (foundEOF) {
      return length;
    }
    long len = pos - length;
    cache.seek(length);
    while (len > 0) {
      int nbytes = stream.read(buf, 0, (int) Math.min(len, (long) buf.length));
      if (nbytes == -1) {
        foundEOF = true;
        return length;
      }
      cache.write(buf, 0, nbytes);
      len -= nbytes;
      length += nbytes;
    }
    return pos;
  }

  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    long next = streamPos + 1;
    long pos = readUntil(next);
    if (pos >= next) {
      cache.seek(streamPos++);
      return cache.read();
    } else {
      return -1;
    }
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
    long pos = readUntil(streamPos + len);
    len = (int) Math.min((long) len, pos - streamPos);
    if (len > 0) {
      cache.seek(streamPos);
      cache.readFully(b, off, len);
      streamPos += len;
      return len;
    } else {
      return -1;
    }
  }

  public boolean isCached() { return true; }

  public boolean isCachedFile() { return true; }

  public boolean isCachedMemory() { return false; }

  public void close() throws IOException {
    super.close();
    cache.close();
    cacheFile.delete();
    stream = null;
    cache = null;
    cacheFile = null;
  }

  protected void finalize() throws Throwable {
  }
}
