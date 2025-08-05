/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class GZIPInputStream extends InflaterInputStream {
  public static final int GZIP_MAGIC = 0x8b1f;

  protected CRC32 crc = new CRC32();
  protected boolean eos;
  private boolean closed;

  private static final int FTEXT = 1;
  private static final int FHCRC = 2;
  private static final int FEXTRA = 4;
  private static final int FNAME = 8;
  private static final int FCOMMENT = 16;

  public GZIPInputStream(InputStream in, int size) throws IOException {
    super(in, new Inflater(true), size);
    readHeader();
    crc.reset();
  }

  public GZIPInputStream(InputStream in) throws IOException {
    this(in, 512);
  }

  private void readHeader() throws IOException {
    byte[] header = new byte[10];
    readFully(header, 0, 10);

    int magic = (header[0] & 0xff) | ((header[1] & 0xff) << 8);
    if (magic != GZIP_MAGIC) {
      throw new ZipException("Not in GZIP format");
    }

    int method = header[2] & 0xff;
    if (method != 8) { // Deflater.DEFLATED
      throw new ZipException("Unsupported compression method: " + method);
    }

    int flg = header[3] & 0xff;

    // FEXTRA
    if ((flg & FEXTRA) != 0) {
      byte[] xlenBuf = new byte[2];
      readFully(xlenBuf, 0, 2);
      int xlen = (xlenBuf[0] & 0xff) | ((xlenBuf[1] & 0xff) << 8);
      skipBytes(xlen);
    }

    // FNAME
    if ((flg & FNAME) != 0) {
      skipZeroTerminated();
    }

    // FCOMMENT
    if ((flg & FCOMMENT) != 0) {
      skipZeroTerminated();
    }

    // FHCRC
    if ((flg & FHCRC) != 0) {
      byte[] crcBuf = new byte[2];
      readFully(crcBuf, 0, 2);
    }
  }

  private void skipZeroTerminated() throws IOException {
    while (true) {
      int b = readUByte();
      if (b == 0) break;
    }
  }

  private int readUByte() throws IOException {
    int b = in.read();
    if (b < 0) {
      throw new EOFException("Unexpected end of ZLIB input stream");
    }
    return b;
  }

  private void readFully(byte[] b, int off, int len) throws IOException {
    while (len > 0) {
      int n = in.read(b, off, len);
      if (n < 0) {
        throw new EOFException("Unexpected end of ZLIB input stream");
      }
      off += n;
      len -= n;
    }
  }

  private void skipBytes(int n) throws IOException {
    while (n > 0) {
      long skipped = in.skip(n);
      if (skipped <= 0) {
        if (in.read() < 0) {
          throw new EOFException();
        }
        n--;
      } else {
        n -= (int) skipped;
      }
    }
  }

  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
    if (eos) {
      return -1;
    }

    int n = super.read(buf, off, len);
    if (n > 0) {
      crc.update(buf, off, n);
      return n;
    }

    readTrailer();
    eos = true;
    return -1;
  }

  private void readTrailer() throws IOException {
    byte[] trailer = new byte[8];
    int drained = inf.drainRemaining(trailer, 0, 8);
    if (drained < 8) {
      readFully(trailer, drained, 8 - drained);
    }

    long trailerCrc = ((long) (trailer[0] & 0xff))
        | (((long) (trailer[1] & 0xff)) << 8)
        | (((long) (trailer[2] & 0xff)) << 16)
        | (((long) (trailer[3] & 0xff)) << 24);

    long isize = ((long) (trailer[4] & 0xff))
        | (((long) (trailer[5] & 0xff)) << 8)
        | (((long) (trailer[6] & 0xff)) << 16)
        | (((long) (trailer[7] & 0xff)) << 24);

    if (crc.getValue() != trailerCrc) {
      throw new ZipException("Corrupt GZIP trailer: CRC mismatch");
    }

    if ((inf.getBytesWritten() & 0xffffffffL) != isize) {
      throw new ZipException("Corrupt GZIP trailer: size mismatch");
    }
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      super.close();
      closed = true;
      eos = true;
    }
  }
}
