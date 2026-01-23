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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ZipFile implements Closeable {
  public static final int OPEN_READ = 0x1;
  public static final int OPEN_DELETE = 0x4;

  private static final int kLocSig = 0x04034b50;
  private static final int kCenSig = 0x02014b50;
  private static final int kEndSig = 0x06054b50;

  private static final class Cen {
    final ZipEntry entry;
    final long locoff;

    Cen(ZipEntry entry, long locoff) {
      this.entry = entry;
      this.locoff = locoff;
    }
  }

  private final String name;
  private final File zipFile;
  private final int mode;
  private final Charset charset;
  private RandomAccessFile raf;
  private volatile boolean closed;
  private final List<Cen> entryList = new ArrayList<Cen>();
  private final Map<String, Cen> index = new HashMap<String, Cen>();
  private String comment;

  public ZipFile(String name) throws IOException {
    this(new File(name), OPEN_READ);
  }

  public ZipFile(File file, int mode) throws IOException {
    this(file, mode, StandardCharsets.UTF_8);
  }

  public ZipFile(File file) throws ZipException, IOException {
    this(file, OPEN_READ);
  }

  public ZipFile(File file, int mode, Charset charset) throws IOException {
    if (((mode & OPEN_READ) == 0) || ((mode & ~(OPEN_READ | OPEN_DELETE)) != 0)) {
      throw new IllegalArgumentException("Illegal mode: 0x" + Integer.toHexString(mode));
    }
    Objects.requireNonNull(charset, "charset");
    this.zipFile = file;
    this.name = file.getPath();
    this.mode = mode;
    this.charset = charset;
    if (!file.exists()) {
      throw new NoSuchFileException(name);
    }
    this.raf = new RandomAccessFile(file, "r");
    boolean ok = false;
    try {
      readDirectory();
      ok = true;
    } finally {
      if (!ok) {
        raf.close();
      }
    }
  }

  public ZipFile(String name, Charset charset) throws IOException {
    this(new File(name), OPEN_READ, charset);
  }

  public ZipFile(File file, Charset charset) throws IOException {
    this(file, OPEN_READ, charset);
  }

  private static int u16(byte[] b, int off) {
    return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
  }

  private static long u32(byte[] b, int off) {
    return (u16(b, off) | ((long) u16(b, off + 2) << 16)) & 0xffffffffL;
  }

  private static long u64(byte[] b, int off) {
    return u32(b, off) | (u32(b, off + 4) << 32);
  }

  private void readDirectory() throws IOException {
    long len = raf.length();
    if (len == 0) {
      throw new ZipException("zip file is empty");
    }
    int tail = (int) Math.min(len, 22 + 0xffff + 20);
    byte[] buf = new byte[tail];
    raf.seek(len - tail);
    raf.readFully(buf);
    int endPos = -1;
    for (int i = tail - 22; i >= 0; i--) {
      if (u32(buf, i) == (kEndSig & 0xffffffffL) && i + 22 + u16(buf, i + 20) == tail) {
        endPos = i;
        break;
      }
    }
    if (endPos < 0) {
      for (int i = tail - 22; i >= 0; i--) {
        if (u32(buf, i) == (kEndSig & 0xffffffffL)) {
          endPos = i;
          break;
        }
      }
    }
    if (endPos < 0) {
      throw new ZipException("zip END header not found");
    }
    long total = u16(buf, endPos + 10);
    long cenSize = u32(buf, endPos + 12);
    long cenOff = u32(buf, endPos + 16);
    int clen = u16(buf, endPos + 20);
    if (clen > 0) {
      comment = new String(buf, endPos + 22, Math.min(clen, tail - endPos - 22), charset);
    }
    long endAbs = len - tail + endPos;
    if (total == 0xffff || cenSize == 0xffffffffL || cenOff == 0xffffffffL) {
      long locAbs = endAbs - 20;
      if (locAbs >= 0) {
        byte[] loc = new byte[20];
        raf.seek(locAbs);
        raf.readFully(loc);
        if (u32(loc, 0) == 0x07064b50L) {
          byte[] z64 = new byte[56];
          raf.seek(u64(loc, 8));
          raf.readFully(z64);
          if (u32(z64, 0) == 0x06064b50L) {
            total = u64(z64, 32);
            cenSize = u64(z64, 40);
            cenOff = u64(z64, 48);
          }
        }
      }
    }
    if (cenSize > len || cenOff > len || cenSize < 0 || cenOff < 0) {
      throw new ZipException("invalid END header (bad central directory offset)");
    }
    byte[] cen = new byte[(int) cenSize];
    raf.seek(cenOff);
    raf.readFully(cen);
    int pos = 0;
    for (long i = 0; i < total; i++) {
      if (pos + 46 > cen.length || u32(cen, pos) != (kCenSig & 0xffffffffL)) {
        throw new ZipException("invalid CEN header (bad signature)");
      }
      int flag = u16(cen, pos + 8);
      int method = u16(cen, pos + 10);
      long xdostime = u32(cen, pos + 12);
      long crc = u32(cen, pos + 16);
      long csize = u32(cen, pos + 20);
      long size = u32(cen, pos + 24);
      int nlen = u16(cen, pos + 28);
      int elen = u16(cen, pos + 30);
      int clen2 = u16(cen, pos + 32);
      long locoff = u32(cen, pos + 42);
      if (pos + 46 + nlen + elen + clen2 > cen.length) {
        throw new ZipException("invalid CEN header (bad header size)");
      }
      Charset cs = (flag & 0x800) != 0 ? StandardCharsets.UTF_8 : charset;
      String ename = new String(cen, pos + 46, nlen, cs);
      ZipEntry e = new ZipEntry(ename, true);
      e.flag = flag;
      e.method = method;
      e.xdostime = xdostime;
      e.crc = crc;
      e.csize = csize;
      e.size = size;
      if (elen > 0) {
        byte[] extra = new byte[elen];
        System.arraycopy(cen, pos + 46 + nlen, extra, 0, elen);
        long[] wide = new long[] {-1};
        applyZip64(e, extra, size == 0xffffffffL, csize == 0xffffffffL, locoff == 0xffffffffL, wide);
        if (wide[0] != -1) {
          locoff = wide[0];
        }
        e.setExtra0(extra, false);
      }
      if (clen2 > 0) {
        e.comment = new String(cen, pos + 46 + nlen + elen, clen2, cs);
      }
      Cen c = new Cen(e, locoff);
      entryList.add(c);
      index.put(ename, c);
      pos += 46 + nlen + elen + clen2;
    }
  }

  private void applyZip64(ZipEntry e, byte[] extra, boolean needSize, boolean needCsize, boolean needOff,
      long[] locoff) {
    int off = 0;
    while (off + 4 <= extra.length) {
      int tag = u16(extra, off);
      int sz = u16(extra, off + 2);
      off += 4;
      if (tag == 0x0001) {
        int p = off;
        if (needSize && p + 8 <= off + sz) {
          e.size = u64(extra, p);
          p += 8;
        }
        if (needCsize && p + 8 <= off + sz) {
          e.csize = u64(extra, p);
          p += 8;
        }
        if (needOff && p + 8 <= off + sz) {
          locoff[0] = u64(extra, p);
        }
        return;
      }
      off += sz;
    }
  }

  private void ensureOpen() {
    if (closed) {
      throw new IllegalStateException("zip file closed");
    }
  }

  public String getComment() {
    synchronized (this) {
      ensureOpen();
      return comment;
    }
  }

  public ZipEntry getEntry(String name) {
    Objects.requireNonNull(name, "name");
    synchronized (this) {
      ensureOpen();
      Cen c = index.get(name);
      if (c == null && !name.endsWith("/")) {
        c = index.get(name + "/");
      }
      return c == null ? null : copy(c.entry);
    }
  }

  private static ZipEntry copy(ZipEntry e) {
    ZipEntry r = new ZipEntry(e.name, true);
    r.xdostime = e.xdostime;
    r.mtime = e.mtime;
    r.atime = e.atime;
    r.ctime = e.ctime;
    r.crc = e.crc;
    r.size = e.size;
    r.csize = e.csize;
    r.method = e.method;
    r.flag = e.flag;
    r.extra = e.extra == null ? null : e.extra.clone();
    r.comment = e.comment;
    return r;
  }

  private long dataStart(Cen c) throws IOException {
    byte[] loc = new byte[30];
    synchronized (this) {
      raf.seek(c.locoff);
      raf.readFully(loc);
    }
    if (u32(loc, 0) != (kLocSig & 0xffffffffL)) {
      throw new ZipException("ZipFile invalid LOC header (bad signature)");
    }
    return c.locoff + 30 + u16(loc, 26) + u16(loc, 28);
  }

  private final class Region extends InputStream {
    private long pos;
    private final long end;
    private boolean eof;

    Region(long start, long length) {
      this.pos = start;
      this.end = start + length;
    }

    @Override
    public int read() throws IOException {
      byte[] one = new byte[1];
      int n = read(one, 0, 1);
      return n < 0 ? -1 : (one[0] & 0xff);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      java.util.Objects.checkFromIndexSize(off, len, b.length);
      synchronized (ZipFile.this) {
        if (closed) {
          throw new IOException("Stream closed");
        }
        if (len == 0) {
          return 0;
        }
        long avail = end - pos;
        if (avail <= 0) {
          return -1;
        }
        int n = (int) Math.min(len, avail);
        raf.seek(pos);
        int r = raf.read(b, off, n);
        if (r > 0) {
          pos += r;
        }
        return r;
      }
    }

    @Override
    public int available() {
      long avail = end - pos;
      return avail > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) avail;
    }

    @Override
    public long skip(long n) {
      long s = Math.max(0, Math.min(n, end - pos));
      pos += s;
      return s;
    }
  }

  private final class InflaterStream extends InflaterInputStream {
    private long remaining;
    private boolean closedStream;

    InflaterStream(InputStream in, long size) {
      super(in, new Inflater(true), 8192);
      this.remaining = size;
    }

    @Override
    public int read() throws IOException {
      byte[] buffer = new byte[1];
      int c = read(buffer, 0, 1);
      return c < 0 ? c : (buffer[0] & 0xff);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
      int c = super.read(buffer, offset, length);
      if (c > 0) {
        remaining -= c;
      }
      return c;
    }

    @Override
    public int available() throws IOException {
      if (closedStream) {
        return 0;
      }
      long avail = remaining;
      return avail > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(avail, 0);
    }

    @Override
    public void close() throws IOException {
      if (!closedStream) {
        closedStream = true;
        inf.end();
        super.close();
      }
    }
  }

  public InputStream getInputStream(ZipEntry entry) throws IOException {
    Objects.requireNonNull(entry, "entry");
    Cen c;
    synchronized (this) {
      ensureOpen();
      c = index.get(entry.name);
    }
    if (c == null) {
      return null;
    }
    long start = dataStart(c);
    ZipEntry e = c.entry;
    switch ((int) e.method) {
    case ZipEntry.STORED:
      return new Region(start, e.csize);
    case ZipEntry.DEFLATED:
      return new InflaterStream(new Region(start, e.csize), e.size);
    default:
      throw new ZipException("invalid compression method");
    }
  }

  public String getName() {
    return name;
  }

  public Enumeration<? extends ZipEntry> entries() {
    synchronized (this) {
      ensureOpen();
    }
    return new Enumeration<ZipEntry>() {
      private int i;

      @Override
      public boolean hasMoreElements() {
        synchronized (ZipFile.this) {
          return i < entryList.size();
        }
      }

      @Override
      public ZipEntry nextElement() {
        synchronized (ZipFile.this) {
          ensureOpen();
          if (i >= entryList.size()) {
            throw new NoSuchElementException();
          }
          return copy(entryList.get(i++).entry);
        }
      }
    };
  }

  public Stream<? extends ZipEntry> stream() {
    synchronized (this) {
      ensureOpen();
    }
    final Enumeration<? extends ZipEntry> en = entries();
    return StreamSupport.stream(
        Spliterators.spliterator(new java.util.Iterator<ZipEntry>() {
          @Override
          public boolean hasNext() {
            return en.hasMoreElements();
          }

          @Override
          public ZipEntry next() {
            return en.nextElement();
          }
        }, size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL),
        false);
  }

  public int size() {
    synchronized (this) {
      ensureOpen();
      return entryList.size();
    }
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    synchronized (this) {
      if (closed) {
        return;
      }
      closed = true;
      raf.close();
    }
    if ((mode & OPEN_DELETE) != 0) {
      zipFile.delete();
    }
  }
}
