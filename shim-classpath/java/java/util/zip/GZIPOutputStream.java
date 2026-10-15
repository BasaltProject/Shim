/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.io.IOException;
import java.io.OutputStream;

public class GZIPOutputStream extends DeflaterOutputStream {
  public static final int GZIP_MAGIC = 0x8b1f;

  protected CRC32 crc = new CRC32();
  private boolean closed;

  private static final int TRAILER_SIZE = 8;

  public GZIPOutputStream(OutputStream out, int size) throws IOException {
    this(out, size, false);
  }

  public GZIPOutputStream(OutputStream out, int size, boolean syncFlush) throws IOException {
    super(out, new Deflater(6, true), size);
    writeHeader();
    crc.reset();
  }

  public GZIPOutputStream(OutputStream out) throws IOException {
    this(out, 512, false);
  }

  public GZIPOutputStream(OutputStream out, boolean syncFlush) throws IOException {
    this(out, 512, syncFlush);
  }

  private void writeHeader() throws IOException {
    byte[] header = new byte[] {
      (byte) GZIP_MAGIC,
      (byte) (GZIP_MAGIC >> 8),
      (byte) 8, // Deflater.DEFLATED
      0, // flags
      0, 0, 0, 0, // mtime
      0, // xfl
      0  // os
    };
    out.write(header);
  }

  @Override
  public synchronized void write(byte[] buf, int off, int len) throws IOException {
    super.write(buf, off, len);
    crc.update(buf, off, len);
  }

  @Override
  public void finish() throws IOException {
    if (!deflater.finished()) {
      deflater.finish();
      while (!deflater.finished()) {
        deflate();
      }
      writeTrailer();
    }
  }

  private void writeTrailer() throws IOException {
    byte[] trailer = new byte[TRAILER_SIZE];
    writeInt((int) crc.getValue(), trailer, 0);
    writeInt((int) (deflater.getBytesRead() & 0xffffffffL), trailer, 4);
    out.write(trailer);
  }

  private static void writeInt(int v, byte[] b, int off) {
    b[off] = (byte) (v & 0xff);
    b[off + 1] = (byte) ((v >> 8) & 0xff);
    b[off + 2] = (byte) ((v >> 16) & 0xff);
    b[off + 3] = (byte) ((v >> 24) & 0xff);
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      finish();
      deflater.end();
      out.close();
      closed = true;
    }
  }
}
