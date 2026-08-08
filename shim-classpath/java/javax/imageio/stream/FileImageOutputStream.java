/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileImageOutputStream extends ImageOutputStreamImpl {
  private RandomAccessFile raf;

  public FileImageOutputStream(File f) throws FileNotFoundException, IOException {
    this(f == null ? null : new RandomAccessFile(f, "rw"));
  }

  public FileImageOutputStream(RandomAccessFile raf) {
    if (raf == null) {
      throw new IllegalArgumentException("raf == null!");
    }
    this.raf = raf;
  }

  public int read() throws IOException {
    checkClosed();
    bitOffset = 0;
    int val = raf.read();
    if (val != -1) {
      ++streamPos;
    }
    return val;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    checkClosed();
    bitOffset = 0;
    int nbytes = raf.read(b, off, len);
    if (nbytes != -1) {
      streamPos += nbytes;
    }
    return nbytes;
  }

  public void write(int b) throws IOException {
    flushBits();
    raf.write(b);
    ++streamPos;
  }

  public void write(byte[] b, int off, int len) throws IOException {
    flushBits();
    raf.write(b, off, len);
    streamPos += len;
  }

  public long length() {
    try {
      checkClosed();
      return raf.length();
    } catch (IOException e) {
      return -1L;
    }
  }

  public void seek(long pos) throws IOException {
    checkClosed();
    if (pos < flushedPos) {
      throw new IndexOutOfBoundsException("pos < flushedPos!");
    }
    bitOffset = 0;
    raf.seek(pos);
    streamPos = raf.getFilePointer();
  }

  public void close() throws IOException {
    super.close();
    raf.close();
  }

  protected void finalize() throws Throwable {
  }
}
