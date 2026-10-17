/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.FileSystem;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class FileChannel extends AbstractInterruptibleChannel
    implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel {
  protected FileChannel() { }

  public static FileChannel open(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    FileSystemProvider provider = path.getFileSystem().provider();
    return provider.newFileChannel(path, options, attrs);
  }

  private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

  public static FileChannel open(Path path, OpenOption... options) throws IOException {
    Set<OpenOption> set;
    if (options.length == 0) {
      set = Collections.emptySet();
    } else {
      set = new HashSet<OpenOption>();
      Collections.addAll(set, options);
    }
    return open(path, set, NO_ATTRIBUTES);
  }

  @Override
  public abstract int read(ByteBuffer dst) throws IOException;

  @Override
  public abstract long read(ByteBuffer[] dsts, int offset, int length) throws IOException;

  @Override
  public final long read(ByteBuffer[] dsts) throws IOException {
    return read(dsts, 0, dsts.length);
  }

  @Override
  public abstract int write(ByteBuffer src) throws IOException;

  @Override
  public abstract long write(ByteBuffer[] srcs, int offset, int length) throws IOException;

  @Override
  public final long write(ByteBuffer[] srcs) throws IOException {
    return write(srcs, 0, srcs.length);
  }

  @Override
  public abstract long position() throws IOException;

  @Override
  public abstract FileChannel position(long newPosition) throws IOException;

  @Override
  public abstract long size() throws IOException;

  @Override
  public abstract FileChannel truncate(long size) throws IOException;

  public abstract void force(boolean metaData) throws IOException;

  public abstract long transferTo(long position, long count, WritableByteChannel target) throws IOException;

  public abstract long transferFrom(ReadableByteChannel src, long position, long count) throws IOException;

  public abstract int read(ByteBuffer dst, long position) throws IOException;

  public abstract int write(ByteBuffer src, long position) throws IOException;

  public static class MapMode {
    public static final MapMode READ_ONLY = new MapMode("READ_ONLY");
    public static final MapMode READ_WRITE = new MapMode("READ_WRITE");
    public static final MapMode PRIVATE = new MapMode("PRIVATE");

    private final String name;

    private MapMode(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public abstract MappedByteBuffer map(MapMode mode, long position, long size) throws IOException;

  public MemorySegment map(MapMode mode, long offset, long size, Arena arena) throws IOException {
    throw new UnsupportedOperationException();
  }

  public abstract FileLock lock(long position, long size, boolean shared) throws IOException;

  public final FileLock lock() throws IOException {
    return lock(0L, Long.MAX_VALUE, false);
  }

  public abstract FileLock tryLock(long position, long size, boolean shared) throws IOException;

  public final FileLock tryLock() throws IOException {
    return tryLock(0L, Long.MAX_VALUE, false);
  }
}
