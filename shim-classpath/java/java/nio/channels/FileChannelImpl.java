/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.Set;

public class FileChannelImpl extends FileChannel {

  private final RandomAccessFile raf;
  private final boolean readable;
  private final boolean writable;
  private final boolean append;
  private boolean open = true;

  public FileChannelImpl(RandomAccessFile raf, boolean readable, boolean writable, boolean append) {
    this.raf = Objects.requireNonNull(raf, "raf");
    this.readable = readable;
    this.writable = writable;
    this.append = append;
  }

  public static FileChannel open(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException
  {
    Objects.requireNonNull(path, "path");
    boolean read = false;
    boolean write = false;
    boolean append = false;
    boolean create = false;
    boolean createNew = false;
    boolean truncateExisting = false;

    if (options != null) {
      for (OpenOption opt : options) {
        if (opt == StandardOpenOption.READ) {
          read = true;
        } else if (opt == StandardOpenOption.WRITE) {
          write = true;
        } else if (opt == StandardOpenOption.APPEND) {
          append = true;
        } else if (opt == StandardOpenOption.CREATE) {
          create = true;
        } else if (opt == StandardOpenOption.CREATE_NEW) {
          createNew = true;
        } else if (opt == StandardOpenOption.TRUNCATE_EXISTING) {
          truncateExisting = true;
        }
      }
    }

    if (!read && !write && !append) {
      read = true;
    }
    if (append) {
      write = true;
    }

    File file = path.toFile();
    if (createNew) {
      if (file.exists()) {
        throw new FileAlreadyExistsException(path.toString());
      }
      File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
        parent.mkdirs();
      }
      file.createNewFile();
    } else if (create && !file.exists()) {
      File parent = file.getParentFile();
      if (parent != null && !parent.exists()) {
        parent.mkdirs();
      }
      file.createNewFile();
    } else if (!file.exists()) {
      throw new NoSuchFileException(path.toString());
    }

    String mode = write ? "rw" : "r";
    RandomAccessFile raf = new RandomAccessFile(file, mode);
    if (truncateExisting && write) {
      raf.setLength(0);
    }
    if (append) {
      raf.seek(raf.length());
    }

    return new FileChannelImpl(raf, read, write, append);
  }

  private void ensureOpen() throws ClosedChannelException {
    if (!open) {
      throw new ClosedChannelException();
    }
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void close() throws IOException {
    if (open) {
      open = false;
      raf.close();
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    ensureOpen();
    if (!readable) {
      throw new NonReadableChannelException();
    }
    Objects.requireNonNull(dst, "dst");
    if (dst.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int rem = dst.remaining();
    if (rem == 0) {
      return 0;
    }

    synchronized (raf) {
      long curPos = raf.getFilePointer();
      long len = raf.length();
      if (curPos >= len) {
        return -1;
      }
      int toRead = (int) Math.min((long) rem, len - curPos);
      if (toRead <= 0) {
        return -1;
      }

      byte[] buf = new byte[Math.min(toRead, 8192)];
      int totalRead = 0;
      while (totalRead < toRead) {
        int chunk = Math.min(buf.length, toRead - totalRead);
        int read = raf.read(buf, 0, chunk);
        if (read < 0) {
          break;
        }
        dst.put(buf, 0, read);
        totalRead += read;
      }
      return totalRead > 0 ? totalRead : -1;
    }
  }

  @Override
  public int read(ByteBuffer dst, long position) throws IOException {
    ensureOpen();
    if (!readable) {
      throw new NonReadableChannelException();
    }
    if (position < 0) {
      throw new IllegalArgumentException("Negative position: " + position);
    }
    Objects.requireNonNull(dst, "dst");
    if (dst.isReadOnly()) {
      throw new ReadOnlyBufferException();
    }
    int rem = dst.remaining();
    if (rem == 0) {
      return 0;
    }

    synchronized (raf) {
      long saved = raf.getFilePointer();
      try {
        long len = raf.length();
        if (position >= len) {
          return -1;
        }
        raf.seek(position);
        int toRead = (int) Math.min((long) rem, len - position);
        if (toRead <= 0) {
          return -1;
        }
        byte[] buf = new byte[Math.min(toRead, 8192)];
        int totalRead = 0;
        while (totalRead < toRead) {
          int chunk = Math.min(buf.length, toRead - totalRead);
          int read = raf.read(buf, 0, chunk);
          if (read < 0) {
            break;
          }
          dst.put(buf, 0, read);
          totalRead += read;
        }
        return totalRead > 0 ? totalRead : -1;
      } finally {
        raf.seek(saved);
      }
    }
  }

  @Override
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
    Objects.checkFromIndexSize(offset, length, dsts.length);
    ensureOpen();
    if (!readable) {
      throw new NonReadableChannelException();
    }
    long totalRead = 0;
    for (int i = 0; i < length; i++) {
      ByteBuffer dst = dsts[offset + i];
      if (dst.hasRemaining()) {
        int n = read(dst);
        if (n < 0) {
          return totalRead > 0 ? totalRead : -1;
        }
        totalRead += n;
      }
    }
    return totalRead;
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    Objects.requireNonNull(src, "src");
    int rem = src.remaining();
    if (rem == 0) {
      return 0;
    }

    synchronized (raf) {
      if (append) {
        raf.seek(raf.length());
      }
      byte[] buf = new byte[Math.min(rem, 8192)];
      int totalWritten = 0;
      while (src.hasRemaining()) {
        int chunk = Math.min(buf.length, src.remaining());
        src.get(buf, 0, chunk);
        raf.write(buf, 0, chunk);
        totalWritten += chunk;
      }
      return totalWritten;
    }
  }

  @Override
  public int write(ByteBuffer src, long position) throws IOException {
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    if (position < 0) {
      throw new IllegalArgumentException("Negative position: " + position);
    }
    Objects.requireNonNull(src, "src");
    int rem = src.remaining();
    if (rem == 0) {
      return 0;
    }

    synchronized (raf) {
      long saved = raf.getFilePointer();
      try {
        raf.seek(position);
        byte[] buf = new byte[Math.min(rem, 8192)];
        int totalWritten = 0;
        while (src.hasRemaining()) {
          int chunk = Math.min(buf.length, src.remaining());
          src.get(buf, 0, chunk);
          raf.write(buf, 0, chunk);
          totalWritten += chunk;
        }
        return totalWritten;
      } finally {
        raf.seek(saved);
      }
    }
  }

  @Override
  public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
    Objects.checkFromIndexSize(offset, length, srcs.length);
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    long totalWritten = 0;
    for (int i = 0; i < length; i++) {
      ByteBuffer src = srcs[offset + i];
      if (src.hasRemaining()) {
        totalWritten += write(src);
      }
    }
    return totalWritten;
  }

  @Override
  public long position() throws IOException {
    ensureOpen();
    synchronized (raf) {
      return raf.getFilePointer();
    }
  }

  @Override
  public FileChannel position(long newPosition) throws IOException {
    ensureOpen();
    if (newPosition < 0) {
      throw new IllegalArgumentException("Negative position: " + newPosition);
    }
    synchronized (raf) {
      raf.seek(newPosition);
    }
    return this;
  }

  @Override
  public long size() throws IOException {
    ensureOpen();
    synchronized (raf) {
      return raf.length();
    }
  }

  @Override
  public FileChannel truncate(long size) throws IOException {
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    if (size < 0) {
      throw new IllegalArgumentException("Negative size: " + size);
    }
    synchronized (raf) {
      if (size < raf.length()) {
        raf.setLength(size);
      }
      if (raf.getFilePointer() > size) {
        raf.seek(size);
      }
    }
    return this;
  }

  @Override
  public void force(boolean metaData) throws IOException {
    ensureOpen();
    synchronized (raf) {
      raf.getFD().sync();
    }
  }

  @Override
  public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
    ensureOpen();
    if (position < 0 || count < 0) {
      throw new IllegalArgumentException();
    }
    Objects.requireNonNull(target, "target");
    long total = 0;
    ByteBuffer buf = ByteBuffer.allocate(8192);
    while (total < count) {
      buf.clear();
      int toRead = (int) Math.min((long) buf.capacity(), count - total);
      buf.limit(toRead);
      int read = read(buf, position + total);
      if (read < 0) {
        break;
      }
      buf.flip();
      while (buf.hasRemaining()) {
        target.write(buf);
      }
      total += read;
    }
    return total;
  }

  @Override
  public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
    ensureOpen();
    if (!writable) {
      throw new NonWritableChannelException();
    }
    if (position < 0 || count < 0) {
      throw new IllegalArgumentException();
    }
    Objects.requireNonNull(src, "src");
    long total = 0;
    ByteBuffer buf = ByteBuffer.allocate(8192);
    while (total < count) {
      buf.clear();
      int toRead = (int) Math.min((long) buf.capacity(), count - total);
      buf.limit(toRead);
      int read = src.read(buf);
      if (read < 0) {
        break;
      }
      buf.flip();
      int written = write(buf, position + total);
      total += written;
    }
    return total;
  }

  @Override
  public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
    ensureOpen();
    if (position < 0 || size < 0 || size > Integer.MAX_VALUE) {
      throw new IllegalArgumentException();
    }
    if (mode == MapMode.READ_WRITE && !writable) {
      throw new NonWritableChannelException();
    }
    if (mode == MapMode.READ_ONLY && !readable) {
      throw new NonReadableChannelException();
    }

    int cap = (int) size;
    ByteBuffer buf = ByteBuffer.allocateDirect(cap);
    if (cap > 0) {
      read(buf, position);
      buf.position(0);
    }
    return (MappedByteBuffer) buf;
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
    return new FileLockImpl(this, position, size, shared);
  }

  @Override
  public FileLock tryLock(long position, long size, boolean shared) throws IOException {
    return lock(position, size, shared);
  }

  private static class FileLockImpl extends FileLock {
    private boolean valid = true;

    FileLockImpl(FileChannel channel, long position, long size, boolean shared) {
      super(channel, position, size, shared);
    }

    @Override
    public boolean isValid() {
      return valid && channel().isOpen();
    }

    @Override
    public synchronized void release() throws IOException {
      if (valid) {
        if (!channel().isOpen()) {
          throw new ClosedChannelException();
        }
        valid = false;
      }
    }
  }
}
