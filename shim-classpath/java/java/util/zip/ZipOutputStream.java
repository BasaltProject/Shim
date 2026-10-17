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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ZipOutputStream extends DeflaterOutputStream {
  public static final int STORED = ZipEntry.STORED;
  public static final int DEFLATED = ZipEntry.DEFLATED;

  private static final int USE_UTF8 = 0x800;
  private static final int LOCSIG = 0x04034b50;
  private static final int EXTSIG = 0x08074b50;
  private static final int CENSIG = 0x02014b50;
  private static final int ENDSIG = 0x06054b50;

  private static final class XEntry {
    final ZipEntry entry;
    final long offset;

    XEntry(ZipEntry entry, long offset) {
      this.entry = entry;
      this.offset = offset;
    }
  }

  private XEntry current;
  private final List<XEntry> xentries = new ArrayList<XEntry>();
  private final HashSet<String> names = new HashSet<String>();
  private final CRC32 crc = new CRC32();
  private long written;
  private long locoff;
  private byte[] comment;
  private int method = DEFLATED;
  private boolean finished;
  private boolean closed;
  private final Charset charset;

  public ZipOutputStream(OutputStream out) {
    this(out, StandardCharsets.UTF_8);
  }

  public ZipOutputStream(OutputStream out, Charset charset) {
    super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true));
    this.charset = Objects.requireNonNull(charset, "charset");
    usesDefaultDeflater = true;
  }

  public void setComment(String comment) {
    byte[] bytes = null;
    if (comment != null) {
      bytes = comment.getBytes(charset);
      if (bytes.length > 0xffff) {
        throw new IllegalArgumentException("ZIP file comment too long");
      }
    }
    this.comment = bytes;
  }

  public void setMethod(int method) {
    if (method != DEFLATED && method != STORED) {
      throw new IllegalArgumentException("invalid compression method");
    }
    this.method = method;
  }

  public void setLevel(int level) {
    def.setLevel(level);
  }

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  public void putNextEntry(ZipEntry e) throws IOException {
    ensureOpen();
    if (current != null) {
      closeEntry();
    }
    if (e.xdostime == -1) {
      e.setTime(System.currentTimeMillis());
    }
    if (e.method == -1) {
      e.method = method;
    }
    e.flag = 0;
    switch (e.method) {
    case DEFLATED:
      if (e.size == -1 || e.csize == -1 || e.crc == -1) {
        e.flag = 8;
      }
      break;
    case STORED:
      if (e.size == -1) {
        e.size = e.csize;
      } else if (e.csize == -1) {
        e.csize = e.size;
      } else if (e.size != e.csize) {
        throw new ZipException("STORED entry where compressed != uncompressed size");
      }
      if (e.size == -1 || e.crc == -1) {
        throw new ZipException("STORED entry missing size, compressed size, or crc-32");
      }
      break;
    default:
      throw new ZipException("unsupported compression method");
    }
    if (!names.add(e.name)) {
      throw new ZipException("duplicate entry: " + e.name);
    }
    if (charset == StandardCharsets.UTF_8) {
      e.flag |= USE_UTF8;
    }
    current = new XEntry(e, written);
    xentries.add(current);
    writeLOC(current);
  }

  public void closeEntry() throws IOException {
    ensureOpen();
    if (current == null) {
      return;
    }
    ZipEntry e = current.entry;
    switch (e.method) {
    case DEFLATED:
      def.finish();
      while (!def.finished()) {
        deflate();
      }
      if ((e.flag & 8) == 0) {
        if (e.size != def.getBytesRead()) {
          throw new ZipException("invalid entry size (expected " + e.size + " but got " + def.getBytesRead()
              + " bytes)");
        }
        if (e.csize != def.getBytesWritten()) {
          throw new ZipException("invalid entry compressed size (expected " + e.csize + " but got "
              + def.getBytesWritten() + " bytes)");
        }
        if (e.crc != crc.getValue()) {
          throw new ZipException("invalid entry CRC-32 (expected 0x" + Long.toHexString(e.crc) + " but got 0x"
              + Long.toHexString(crc.getValue()) + ")");
        }
      } else {
        e.size = def.getBytesRead();
        e.csize = def.getBytesWritten();
        e.crc = crc.getValue();
        writeEXT(e);
      }
      def.reset();
      written += e.csize;
      break;
    case STORED:
      if (e.size != written - locoff) {
        throw new ZipException("invalid entry size (expected " + e.size + " but got " + (written - locoff)
            + " bytes)");
      }
      if (e.crc != crc.getValue()) {
        throw new ZipException("invalid entry crc-32 (expected 0x" + Long.toHexString(e.crc) + " but got 0x"
            + Long.toHexString(crc.getValue()) + ")");
      }
      break;
    default:
      throw new ZipException("invalid compression method");
    }
    crc.reset();
    current = null;
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) throws IOException {
    ensureOpen();
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    if (current == null) {
      throw new ZipException("no current ZIP entry");
    }
    ZipEntry entry = current.entry;
    switch (entry.method) {
    case DEFLATED:
      super.write(b, off, len);
      break;
    case STORED:
      written += len;
      if (written - locoff > entry.size) {
        throw new ZipException("attempt to write past end of STORED entry");
      }
      out.write(b, off, len);
      break;
    default:
      throw new ZipException("invalid compression method");
    }
    crc.update(b, off, len);
  }

  @Override
  public void finish() throws IOException {
    ensureOpen();
    if (finished) {
      return;
    }
    if (current != null) {
      closeEntry();
    }
    long off = written;
    for (XEntry xentry : xentries) {
      writeCEN(xentry);
    }
    writeEND(off, written - off);
    finished = true;
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      super.close();
      closed = true;
    }
  }

  private int version(ZipEntry e) throws ZipException {
    switch (e.method) {
    case DEFLATED:
      return 20;
    case STORED:
      return 10;
    default:
      throw new ZipException("unsupported compression method");
    }
  }

  private byte[] timestampExtra(ZipEntry e, boolean cen) {
    int flags = 0;
    int n = 0;
    long um = -1;
    long ua = -1;
    long uc = -1;
    if (e.mtime != null) {
      flags |= 1;
      n++;
      um = e.mtime.to(TimeUnit.SECONDS);
    }
    if (e.atime != null) {
      flags |= 2;
      ua = e.atime.to(TimeUnit.SECONDS);
      if (!cen) {
        n++;
      }
    }
    if (e.ctime != null) {
      flags |= 4;
      uc = e.ctime.to(TimeUnit.SECONDS);
      if (!cen) {
        n++;
      }
    }
    if (flags == 0) {
      return new byte[0];
    }
    boolean ntfs = um > ZipEntry.UPPER_UNIXTIME_BOUND || ua > ZipEntry.UPPER_UNIXTIME_BOUND
        || uc > ZipEntry.UPPER_UNIXTIME_BOUND;
    byte[] out;
    if (ntfs) {
      out = new byte[36];
      put16(out, 0, 0x000a);
      put16(out, 2, 32);
      put16(out, 8, 1);
      put16(out, 10, 24);
      put64(out, 12, fileTimeToWin(e.mtime));
      put64(out, 20, fileTimeToWin(e.atime));
      put64(out, 28, fileTimeToWin(e.ctime));
      return out;
    }
    out = new byte[n * 4 + 5];
    put16(out, 0, 0x5455);
    put16(out, 2, n * 4 + 1);
    out[4] = (byte) flags;
    int pos = 5;
    if ((flags & 1) != 0) {
      put32(out, pos, um);
      pos += 4;
    }
    if (!cen && (flags & 2) != 0) {
      put32(out, pos, ua);
      pos += 4;
    }
    if (!cen && (flags & 4) != 0) {
      put32(out, pos, uc);
    }
    return out;
  }

  private static long fileTimeToWin(FileTime t) {
    return t == null ? Long.MIN_VALUE : t.to(TimeUnit.MICROSECONDS) * 10 + 116444736000000000L;
  }

  private byte[] filteredExtra(byte[] extra) {
    if (extra == null) {
      return new byte[0];
    }
    java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
    int off = 0;
    int len = extra.length;
    while (off + 4 <= len) {
      int tag = ZipEntry.get16(extra, off);
      int sz = ZipEntry.get16(extra, off + 2);
      if (off + 4 + sz > len) {
        break;
      }
      if (tag != 0x5455 && tag != 0x0001) {
        bo.write(extra, off, 4 + sz);
      }
      off += 4 + sz;
    }
    if (off < len) {
      bo.write(extra, off, len - off);
    }
    return bo.toByteArray();
  }

  private byte[] extraBlock(ZipEntry e, boolean cen) {
    byte[] ts = timestampExtra(e, cen);
    byte[] ex = filteredExtra(e.extra);
    byte[] all = new byte[ts.length + ex.length];
    System.arraycopy(ts, 0, all, 0, ts.length);
    System.arraycopy(ex, 0, all, ts.length, ex.length);
    return all;
  }

  private void writeLOC(XEntry xentry) throws IOException {
    ZipEntry e = xentry.entry;
    int flag = e.flag;
    byte[] nameBytes = e.name.getBytes(charset);
    byte[] extra = extraBlock(e, false);
    byte[] h = new byte[30];
    put32(h, 0, LOCSIG & 0xffffffffL);
    put16(h, 4, version(e));
    put16(h, 6, flag);
    put16(h, 8, e.method);
    put32(h, 10, e.xdostime);
    if ((flag & 8) == 0) {
      put32(h, 14, e.crc);
      put32(h, 18, e.csize);
      put32(h, 22, e.size);
    }
    put16(h, 26, nameBytes.length);
    put16(h, 28, extra.length);
    out.write(h);
    out.write(nameBytes);
    out.write(extra);
    written += 30 + nameBytes.length + extra.length;
    locoff = written;
  }

  private void writeEXT(ZipEntry e) throws IOException {
    byte[] h = new byte[16];
    put32(h, 0, EXTSIG & 0xffffffffL);
    put32(h, 4, e.crc);
    put32(h, 8, e.csize);
    put32(h, 12, e.size);
    out.write(h);
    written += 16;
  }

  private void writeCEN(XEntry xentry) throws IOException {
    ZipEntry e = xentry.entry;
    byte[] nameBytes = e.name.getBytes(charset);
    byte[] extra = extraBlock(e, true);
    byte[] commentBytes = e.comment != null ? e.comment.getBytes(charset) : new byte[0];
    byte[] h = new byte[46];
    put32(h, 0, CENSIG & 0xffffffffL);
    put16(h, 4, version(e));
    put16(h, 6, version(e));
    put16(h, 8, e.flag);
    put16(h, 10, e.method);
    put32(h, 12, e.xdostime);
    put32(h, 16, e.crc);
    put32(h, 20, e.csize);
    put32(h, 24, e.size);
    put16(h, 28, nameBytes.length);
    put16(h, 30, extra.length);
    put16(h, 32, commentBytes.length);
    put32(h, 42, xentry.offset);
    out.write(h);
    out.write(nameBytes);
    out.write(extra);
    out.write(commentBytes);
    written += 46 + nameBytes.length + extra.length + commentBytes.length;
  }

  private void writeEND(long off, long len) throws IOException {
    byte[] h = new byte[22];
    put32(h, 0, ENDSIG & 0xffffffffL);
    put16(h, 8, xentries.size());
    put16(h, 10, xentries.size());
    put32(h, 12, len);
    put32(h, 16, off);
    put16(h, 20, comment != null ? comment.length : 0);
    out.write(h);
    written += 22;
    if (comment != null) {
      out.write(comment);
      written += comment.length;
    }
  }

  private static void put16(byte[] b, int off, int v) {
    b[off] = (byte) v;
    b[off + 1] = (byte) (v >> 8);
  }

  private static void put32(byte[] b, int off, long v) {
    put16(b, off, (int) v);
    put16(b, off + 2, (int) (v >> 16));
  }

  private static void put64(byte[] b, int off, long v) {
    put32(b, off, v);
    put32(b, off + 4, v >> 32);
  }
}
