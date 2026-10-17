/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import shim.FileNative;
import shim.NioNative;

public class FileChannelImpl extends FileChannel {
  private static final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
  private static final long kTransferSize = 8192;

  private final FileDescriptor fd;
  private final boolean writable;
  private final boolean readable;
  private final boolean append;
  private final Closeable parent;
  private final String path;
  private final Object positionLock = new Object();
  private volatile FileLockTable fileLockTable;

  private FileChannelImpl(FileDescriptor fd, String path, boolean readable, boolean writable, boolean append,
      Closeable parent) {
    this.fd = fd;
    this.path = path;
    this.readable = readable;
    this.writable = writable;
    this.append = append;
    this.parent = parent;
  }

  public static FileChannel open(FileDescriptor fd, String path, boolean readable, boolean writable, boolean append,
      Closeable parent) {
    return new FileChannelImpl(fd, path, readable, writable, append, parent);
  }

  private void ensureOpen() throws IOException {
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
  }

  private int fdval() {
    return fdAccess.get(fd);
  }

  @Override
  protected void implCloseChannel() throws IOException {
    FileLockTable table = fileLockTable;
    if (table != null) {
      table.removeAll();
    }
    if (parent != null) {
      parent.close();
    } else {
      fdAccess.close(fd);
    }
  }

  private static IOException failure(int r) {
    return new IOException(FileNative.message(r));
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    ensureOpen();
    if (!readable) {
      throw new NonReadableChannelException();
    }
    synchronized (positionLock) {
      return readInternal(dst, -1);
    }
  }

  private int readInternal(ByteBuffer dst, long position) throws IOException {
    int rem = dst.remaining();
    if (rem == 0) {
      return 0;
    }
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    boolean direct = !dst.hasArray();
    byte[] array = direct ? new byte[Math.min(rem, 1 << 16)] : dst.array();
    int offset = direct ? 0 : dst.arrayOffset() + dst.position();
    int len = direct ? array.length : rem;
    int r;
    if (position < 0) {
      r = FileNative.read(fdval(), array, offset, len);
    } else {
      r = FileNative.pread(fdval(), array, offset, len, position);
    }
    if (r < 0) {
      throw failure(r);
    }
    if (r == 0) {
      return -1;
    }
    if (direct) {
      dst.put(array, 0, r);
    } else {
      dst.position(dst.position() + r);
    }
    return r;
  }

  @Override
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
    java.util.Objects.checkFromIndexSize(offset, length, dsts.length);
    ensureOpen();
    if (!readable) {
      throw new NonReadableChannelException();
    }
    synchronized (positionLock) {
      long total = 0;
      for (int i = offset; i < offset + length; i++) {
        ByteBuffer dst = dsts[i];
        if (!dst.hasRemaining()) {
          continue;
        }
        int want = dst.remaining();
        int n = readInternal(dst, -1);
        if (n < 0) {
          return total > 0 ? total : -1;
        }
        total += n;
        if (n < want) {
          break;
        }
      }
      return total;
    }
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    synchronized (positionLock) {
      return writeInternal(src, -1);
    }
  }

  private int writeInternal(ByteBuffer src, long position) throws IOException {
    int rem = src.remaining();
    if (rem == 0) {
      return 0;
    }
    byte[] array;
    int offset;
    if (src.hasArray()) {
      array = src.array();
      offset = src.arrayOffset() + src.position();
    } else {
      array = new byte[rem];
      src.duplicate().get(array);
      offset = 0;
    }
    int r;
    if (position < 0) {
      r = FileNative.write(fdval(), array, offset, rem);
    } else {
      r = FileNative.pwrite(fdval(), array, offset, rem, position);
    }
    if (r < 0) {
      throw failure(r);
    }
    src.position(src.position() + r);
    return r;
  }

  @Override
  public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
    java.util.Objects.checkFromIndexSize(offset, length, srcs.length);
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    synchronized (positionLock) {
      long total = 0;
      for (int i = offset; i < offset + length; i++) {
        ByteBuffer src = srcs[i];
        if (!src.hasRemaining()) {
          continue;
        }
        int want = src.remaining();
        int n = writeInternal(src, -1);
        total += n;
        if (n < want) {
          break;
        }
      }
      return total;
    }
  }

  @Override
  public long position() throws IOException {
    ensureOpen();
    synchronized (positionLock) {
      long p = (append) ? FileNative.size(fdval()) : FileNative.seek(fdval(), 0, 1);
      if (p < 0) {
        throw failure((int) p);
      }
      return p;
    }
  }

  @Override
  public FileChannel position(long newPosition) throws IOException {
    ensureOpen();
    if (newPosition < 0) {
      throw new IllegalArgumentException();
    }
    synchronized (positionLock) {
      long p = FileNative.seek(fdval(), newPosition, 0);
      if (p < 0) {
        throw failure((int) p);
      }
      return this;
    }
  }

  @Override
  public long size() throws IOException {
    ensureOpen();
    long s = FileNative.size(fdval());
    if (s < 0) {
      throw failure((int) s);
    }
    return s;
  }

  @Override
  public FileChannel truncate(long newSize) throws IOException {
    ensureOpen();
    if (newSize < 0) {
      throw new IllegalArgumentException("Negative size");
    }
    if (!writable) {
      throw new NonWritableChannelException();
    }
    synchronized (positionLock) {
      long p = position();
      long size = size();
      if (newSize < size) {
        int r = FileNative.truncate(fdval(), newSize);
        if (r < 0) {
          throw failure(r);
        }
      }
      if (p > newSize) {
        p = newSize;
      }
      position(p);
      return this;
    }
  }

  @Override
  public void force(boolean metaData) throws IOException {
    ensureOpen();
    int r = FileNative.sync(fdval(), metaData);
    if (r < 0) {
      throw failure(r);
    }
  }

  @Override
  public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
    ensureOpen();
    if (!target.isOpen()) {
      throw new ClosedChannelException();
    }
    if (!readable) {
      throw new NonReadableChannelException();
    }
    if (target instanceof FileChannelImpl && !((FileChannelImpl) target).writable) {
      throw new NonWritableChannelException();
    }
    if ((position < 0) || (count < 0)) {
      throw new IllegalArgumentException();
    }
    long sz = size();
    if (position > sz) {
      return 0;
    }
    int icount = (int) Math.min(count, Integer.MAX_VALUE);
    if ((sz - position) < icount) {
      icount = (int) (sz - position);
    }
    int c = (int) Math.min(icount, kTransferSize);
    ByteBuffer bb = ByteBuffer.allocate(c);
    long tw = 0;
    long pos = position;
    try {
      while (tw < icount) {
        bb.limit(Math.min((int) (icount - tw), (int) kTransferSize));
        int nr = read(bb, pos);
        if (nr <= 0) {
          break;
        }
        bb.flip();
        int nw = target.write(bb);
        tw += nw;
        if (nw != nr) {
          break;
        }
        pos += nw;
        bb.clear();
      }
      return tw;
    } catch (IOException x) {
      if (tw > 0) {
        return tw;
      }
      throw x;
    }
  }

  @Override
  public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
    ensureOpen();
    if (!src.isOpen()) {
      throw new ClosedChannelException();
    }
    if (src instanceof FileChannelImpl && !((FileChannelImpl) src).readable) {
      throw new NonReadableChannelException();
    }
    if (!writable) {
      throw new NonWritableChannelException();
    }
    if ((position < 0) || (count < 0)) {
      throw new IllegalArgumentException();
    }
    if (position > size()) {
      return 0;
    }
    int c = (int) Math.min(count, kTransferSize);
    ByteBuffer bb = ByteBuffer.allocate(c);
    long tw = 0;
    long p = position;
    try {
      while (tw < count) {
        bb.limit((int) Math.min((count - tw), kTransferSize));
        int nr = src.read(bb);
        if (nr <= 0) {
          break;
        }
        bb.flip();
        int nw = write(bb, p);
        tw += nw;
        if (nw != nr) {
          break;
        }
        p += nw;
        bb.clear();
      }
      return tw;
    } catch (IOException x) {
      if (tw > 0) {
        return tw;
      }
      throw x;
    }
  }

  @Override
  public int read(ByteBuffer dst, long position) throws IOException {
    if (dst == null) {
      throw new NullPointerException();
    }
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (!readable) {
      throw new NonReadableChannelException();
    }
    ensureOpen();
    return readInternal(dst, position);
  }

  @Override
  public int write(ByteBuffer src, long position) throws IOException {
    if (src == null) {
      throw new NullPointerException();
    }
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (!writable) {
      throw new NonWritableChannelException();
    }
    ensureOpen();
    return writeInternal(src, position);
  }

  @Override
  public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
    ensureOpen();
    if (mode == null) {
      throw new NullPointerException("Mode is null");
    }
    if (position < 0L) {
      throw new IllegalArgumentException("Negative position");
    }
    if (size < 0L) {
      throw new IllegalArgumentException("Negative size");
    }
    if (position + size < 0) {
      throw new IllegalArgumentException("Position + size overflow");
    }
    if (size > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Size exceeds Integer.MAX_VALUE");
    }
    boolean readOnly = mode == MapMode.READ_ONLY;
    boolean priv = mode == MapMode.PRIVATE;
    if (!readOnly && !writable) {
      throw new NonWritableChannelException();
    }
    if (!readable) {
      throw new NonReadableChannelException();
    }
    long filesize = FileNative.size(fdval());
    if (filesize < position + size) {
      if (!writable) {
        throw new IOException("Channel not open for writing - cannot extend file to required size");
      }
      if (priv) {
        // private mappings do not grow the file in the reference behaviour either
      }
      int r = truncateGrow(position + size);
      if (r < 0) {
        throw failure(r);
      }
    }
    if (size == 0) {
      return new java.nio.DirectByteBuffer(0, 0, readOnly);
    }
    int pagePosition = (int) (position % 4096);
    long mapPosition = position - pagePosition;
    long mapSize = size + pagePosition;
    long addr = NioNative.mmap(fdval(), readOnly ? 1 : 3, priv ? 1 : 0, mapPosition, mapSize);
    if (addr < 0) {
      throw failure((int) addr);
    }
    java.nio.DirectByteBuffer bb = new java.nio.DirectByteBuffer(addr + pagePosition, (int) size, readOnly);
    bb.mapped(addr, mapSize);
    return bb;
  }

  private int truncateGrow(long newSize) {
    return FileNative.truncate(fdval(), newSize);
  }

  private FileLockTable fileLockTable() throws IOException {
    if (fileLockTable == null) {
      synchronized (this) {
        if (fileLockTable == null) {
          fileLockTable = new FileLockTable(this, fdval());
        }
      }
    }
    return fileLockTable;
  }

  @Override
  public FileLock lock(long position, long size, boolean shared) throws IOException {
    ensureOpen();
    if (shared && !readable) {
      throw new NonReadableChannelException();
    }
    if (!shared && !writable) {
      throw new NonWritableChannelException();
    }
    if (size == 0) {
      size = Long.MAX_VALUE - Math.max(0, position);
    }
    FileLockImpl fli = new FileLockImpl(this, position, size, shared);
    FileLockTable flt = fileLockTable();
    flt.add(fli);
    boolean completed = false;
    try {
      int n = NioNative.lock(fdval(), position, size, shared, true);
      if (n < 0) {
        throw failure(n);
      }
      completed = isOpen();
    } finally {
      if (!completed) {
        flt.remove(fli);
      }
    }
    return completed ? fli : null;
  }

  @Override
  public FileLock tryLock(long position, long size, boolean shared) throws IOException {
    ensureOpen();
    if (shared && !readable) {
      throw new NonReadableChannelException();
    }
    if (!shared && !writable) {
      throw new NonWritableChannelException();
    }
    if (size == 0) {
      size = Long.MAX_VALUE - Math.max(0, position);
    }
    FileLockImpl fli = new FileLockImpl(this, position, size, shared);
    FileLockTable flt = fileLockTable();
    flt.add(fli);
    int result = NioNative.lock(fdval(), position, size, shared, false);
    if (result == 0) {
      return fli;
    }
    flt.remove(fli);
    if (result < 0) {
      throw failure(result);
    }
    return null;
  }

  void release(FileLockImpl fli) throws IOException {
    ensureOpen();
    int r = NioNative.unlock(fdval(), fli.position(), fli.size());
    if (r < 0) {
      throw failure(r);
    }
    fileLockTable().remove(fli);
  }

  private static final class FileLockTable {
    private static final Map<Object, List<FileLockImpl>> kTable = new HashMap<Object, List<FileLockImpl>>();

    private final Object key;
    private final FileChannelImpl channel;
    private final List<FileLockImpl> mine = new ArrayList<FileLockImpl>();

    FileLockTable(FileChannelImpl channel, int fd) throws IOException {
      this.channel = channel;
      long[] st = new long[16];
      int r = NioNative.fstatx(fd, st);
      if (r < 0) {
        throw failure(r);
      }
      this.key = st[NioNative.kDev] + ":" + st[NioNative.kIno];
    }

    void add(FileLockImpl fli) throws OverlappingFileLockException {
      synchronized (kTable) {
        List<FileLockImpl> list = kTable.get(key);
        if (list == null) {
          list = new ArrayList<FileLockImpl>();
          kTable.put(key, list);
        }
        for (FileLockImpl existing : list) {
          if (existing.overlaps(fli.position(), fli.size())) {
            throw new OverlappingFileLockException();
          }
        }
        list.add(fli);
        mine.add(fli);
      }
    }

    void remove(FileLockImpl fli) {
      synchronized (kTable) {
        List<FileLockImpl> list = kTable.get(key);
        if (list != null) {
          list.remove(fli);
          if (list.isEmpty()) {
            kTable.remove(key);
          }
        }
        mine.remove(fli);
      }
    }

    void removeAll() {
      synchronized (kTable) {
        for (FileLockImpl fli : new ArrayList<FileLockImpl>(mine)) {
          fli.invalidate();
          List<FileLockImpl> list = kTable.get(key);
          if (list != null) {
            list.remove(fli);
            if (list.isEmpty()) {
              kTable.remove(key);
            }
          }
        }
        mine.clear();
      }
    }
  }
}
