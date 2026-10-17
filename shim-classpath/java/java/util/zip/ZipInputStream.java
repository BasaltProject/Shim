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
import java.nio.charset.Charset;
import java.util.Objects;

public class ZipInputStream extends InflaterInputStream {
  private static final long LOCSIG = 0x04034b50L;
  private static final long EXTSIG = 0x08074b50L;
  private static final long CENSIG = 0x02014b50L;
  private static final long ENDSIG = 0x06054b50L;

  private final Charset charset;
  private final CRC32 crc = new CRC32();
  private ZipEntry currentEntry;
  private int entryFlag;
  private boolean entryEof;
  private long entryBytesRead;
  private boolean closed;

  public ZipInputStream(InputStream in) {
    this(in, null);
  }

  public ZipInputStream(InputStream in, Charset charset) {
    super(in, new Inflater(true), 512);
    this.charset = charset;
  }

  protected ZipEntry createZipEntry(String name) {
    return new ZipEntry(name);
  }

  private static long get32(byte[] b, int off) {
    return ((long) (b[off] & 0xff))
        | (((long) (b[off + 1] & 0xff)) << 8)
        | (((long) (b[off + 2] & 0xff)) << 16)
        | (((long) (b[off + 3] & 0xff)) << 24);
  }

  private static int get16(byte[] b, int off) {
    return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
  }

  private int readBytes(byte[] b, int off, int len) throws IOException {
    int drained = inf.drainRemaining(b, off, len);
    if (drained == len) {
      return drained;
    }
    int read = in.read(b, off + drained, len - drained);
    if (read < 0) {
      return drained > 0 ? drained : -1;
    }
    return drained + read;
  }

  private void readFully(byte[] b, int off, int len) throws IOException {
    while (len > 0) {
      int n = readBytes(b, off, len);
      if (n < 0) {
        throw new EOFException("Unexpected end of ZLIB input stream");
      }
      off += n;
      len -= n;
    }
  }

  public ZipEntry getNextEntry() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
    if (currentEntry != null) {
      closeEntry();
    }

    byte[] sigBuf = new byte[4];
    int n = readBytes(sigBuf, 0, 4);
    if (n < 4) {
      return null;
    }
    long sig = get32(sigBuf, 0);
    if (sig == CENSIG || sig == ENDSIG) {
      return null;
    }
    if (sig != LOCSIG) {
      throw new ZipException("invalid LOC header (bad signature: 0x" + Long.toHexString(sig) + ")");
    }

    byte[] header = new byte[26];
    readFully(header, 0, 26);
    int version = get16(header, 0);
    int flag = get16(header, 2);
    int method = get16(header, 4);
    long time = get32(header, 6);
    long crcVal = get32(header, 10);
    long csize = get32(header, 14);
    long size = get32(header, 18);
    int fnLen = get16(header, 22);
    int extraLen = get16(header, 24);

    byte[] fnBytes = new byte[fnLen];
    readFully(fnBytes, 0, fnLen);
    String name = (charset != null) ? new String(fnBytes, charset) : new String(fnBytes, "UTF-8");

    ZipEntry entry = createZipEntry(name);
    entry.method = method;
    entry.flag = flag;
    entry.xdostime = time;
    if (extraLen > 0) {
      byte[] extra = new byte[extraLen];
      readFully(extra, 0, extraLen);
      entry.setExtra0(extra, false);
    }
    if ((flag & 8) == 0) {
      entry.setCrc(crcVal);
      entry.setCompressedSize(csize);
      entry.setSize(size);
    }

    currentEntry = entry;
    entryFlag = flag;
    entryEof = false;
    entryBytesRead = 0;
    crc.reset();
    byte[] rem = null;
    int remLen = inf.getRemaining();
    if (remLen > 0) {
      rem = new byte[remLen];
      inf.drainRemaining(rem, 0, remLen);
    }
    inf.reset();
    if (rem != null) {
      inf.setInput(rem, 0, rem.length);
    }
    return entry;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
    if (currentEntry == null || entryEof) {
      return -1;
    }
    if (len == 0) {
      return 0;
    }

    int method = currentEntry.getMethod();
    if (method == ZipEntry.DEFLATED) {
      int n = super.read(b, off, len);
      if (n > 0) {
        crc.update(b, off, n);
        entryBytesRead += n;
        return n;
      }

      entryEof = true;
      if ((entryFlag & 8) != 0) {
        byte[] desc = new byte[16];
        readFully(desc, 0, 4);
        long sigOrCrc = get32(desc, 0);
        long entryCrc;
        long entryCsize;
        long entrySize;
        if (sigOrCrc == EXTSIG) {
          readFully(desc, 4, 12);
          entryCrc = get32(desc, 4);
          entryCsize = get32(desc, 8);
          entrySize = get32(desc, 12);
        } else {
          entryCrc = sigOrCrc;
          readFully(desc, 4, 8);
          entryCsize = get32(desc, 4);
          entrySize = get32(desc, 8);
        }
        currentEntry.setCrc(entryCrc);
        currentEntry.setCompressedSize(entryCsize);
        currentEntry.setSize(entrySize);
        if (currentEntry.getCrc() != -1 && currentEntry.getCrc() != crc.getValue()) {
          throw new ZipException("corrupt zip entry (CRC mismatch)");
        }
        if (currentEntry.getSize() != -1 && currentEntry.getSize() != entryBytesRead) {
          throw new ZipException("corrupt zip entry (size mismatch)");
        }
      }
      return -1;
    } else if (method == ZipEntry.STORED) {
      long remaining = currentEntry.getSize() - entryBytesRead;
      if (remaining <= 0) {
        entryEof = true;
        return -1;
      }
      int toRead = (int) Math.min(len, remaining);
      int n = readBytes(b, off, toRead);
      if (n < 0) {
        throw new EOFException("Unexpected end of ZLIB input stream");
      }
      crc.update(b, off, n);
      entryBytesRead += n;
      if (entryBytesRead == currentEntry.getSize()) {
        entryEof = true;
        if (currentEntry.getCrc() != -1 && currentEntry.getCrc() != crc.getValue()) {
          throw new ZipException("corrupt zip entry (CRC mismatch)");
        }
      }
      return n;
    } else {
      throw new ZipException("unsupported compression method: " + method);
    }
  }

  public void closeEntry() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
    if (currentEntry != null) {
      byte[] tmp = new byte[512];
      while (read(tmp, 0, tmp.length) != -1) {
        // drain
      }
      currentEntry = null;
      entryEof = true;
    }
  }

  @Override
  public int available() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
    return (currentEntry != null && !entryEof) ? 1 : 0;
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      super.close();
      closed = true;
      currentEntry = null;
    }
  }
}
