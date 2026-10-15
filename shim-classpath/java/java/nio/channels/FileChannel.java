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

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class FileChannel
    implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel, Channel
{
  protected FileChannel() {}

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

  public static FileChannel open(Path path, OpenOption... options) throws IOException {
    Set<OpenOption> set = new HashSet<OpenOption>();
    if (options != null) {
      for (OpenOption opt : options) {
        set.add(opt);
      }
    }
    return open(path, set);
  }

  public static FileChannel open(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException
  {
    return FileChannelImpl.open(path, options, attrs);
  }

  @Override
  public abstract int read(ByteBuffer dst) throws IOException;

  public abstract int read(ByteBuffer dst, long position) throws IOException;

  @Override
  public abstract long read(ByteBuffer[] dsts, int offset, int length) throws IOException;

  @Override
  public final long read(ByteBuffer[] dsts) throws IOException {
    return read(dsts, 0, dsts.length);
  }

  @Override
  public abstract int write(ByteBuffer src) throws IOException;

  public abstract int write(ByteBuffer src, long position) throws IOException;

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

  public abstract MappedByteBuffer map(MapMode mode, long position, long size) throws IOException;

  public abstract FileLock lock(long position, long size, boolean shared) throws IOException;

  public final FileLock lock() throws IOException {
    return lock(0L, Long.MAX_VALUE, false);
  }

  public abstract FileLock tryLock(long position, long size, boolean shared) throws IOException;

  public final FileLock tryLock() throws IOException {
    return tryLock(0L, Long.MAX_VALUE, false);
  }
}
