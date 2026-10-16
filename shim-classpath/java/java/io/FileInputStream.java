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

package java.io;

import java.nio.channels.FileChannel;
import java.nio.channels.FileChannelImpl;
import java.util.Objects;
import shim.FileNative;

public class FileInputStream extends InputStream {
  private final FileDescriptor fd;
  private final String path;
  private volatile boolean closed;
  private FileChannel channel;

  public FileInputStream(String name) throws FileNotFoundException {
    this(name != null ? new File(name) : null);
  }

  public FileInputStream(File file) throws FileNotFoundException {
    String name = (file != null ? file.getPath() : null);
    if (name == null) {
      throw new NullPointerException();
    }
    if (file.isInvalid()) {
      throw new FileNotFoundException("Invalid file path");
    }
    fd = new FileDescriptor();
    path = name;
    int r = FileNative.open(name, FileNative.READ, 0);
    if (r < 0) {
      throw new FileNotFoundException(name + " (" + FileNative.message(r) + ")");
    }
    fd.fd = r;
  }

  public FileInputStream(FileDescriptor fdObj) {
    if (fdObj == null) {
      throw new NullPointerException();
    }
    fd = fdObj;
    path = null;
  }

  private void ensureOpen() throws IOException {
    if (closed || fd.fd == -1) {
      throw new IOException("Stream Closed");
    }
  }

  public int read() throws IOException {
    ensureOpen();
    int r = FileNative.readByte(fd.fd);
    if (r < -1) {
      throw new IOException(FileNative.strerror(-2 - r));
    }
    return r;
  }

  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    ensureOpen();
    if (len == 0) {
      return 0;
    }
    int r = FileNative.read(fd.fd, b, off, len);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
    return r == 0 ? -1 : r;
  }

  public byte[] readAllBytes() throws IOException {
    ensureOpen();
    long length = FileNative.size(fd.fd);
    long position = FileNative.seek(fd.fd, 0, 1);
    if (length > 0 && position >= 0 && length > position) {
      long size = length - position;
      if (size > Integer.MAX_VALUE - 8) {
        throw new OutOfMemoryError("Required array size too large");
      }
      byte[] buf = new byte[(int) size];
      int n = 0;
      while (n < buf.length) {
        int r = read(buf, n, buf.length - n);
        if (r < 0) {
          break;
        }
        n += r;
      }
      if (n < buf.length) {
        return java.util.Arrays.copyOf(buf, n);
      }
      byte[] rest = super.readAllBytes();
      if (rest.length == 0) {
        return buf;
      }
      byte[] all = java.util.Arrays.copyOf(buf, buf.length + rest.length);
      System.arraycopy(rest, 0, all, buf.length, rest.length);
      return all;
    }
    return super.readAllBytes();
  }

  public long skip(long n) throws IOException {
    ensureOpen();
    long cur = FileNative.seek(fd.fd, 0, 1);
    if (cur < 0) {
      return super.skip(n);
    }
    long end = FileNative.seek(fd.fd, n, 1);
    if (end < 0) {
      throw new IOException(FileNative.message((int) end));
    }
    return end - cur;
  }

  public int available() throws IOException {
    ensureOpen();
    long r = FileNative.available(fd.fd);
    if (r < 0) {
      throw new IOException(FileNative.message((int) r));
    }
    return r > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) r;
  }

  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    if (channel != null) {
      channel.close();
    }
    fd.closeAll();
  }

  public final FileDescriptor getFD() throws IOException {
    if (fd != null) {
      return fd;
    }
    throw new IOException();
  }

  public FileChannel getChannel() {
    synchronized (this) {
      if (channel == null) {
        channel = new FileChannelImpl(new RandomAccessFile(fd, path, true, false), true, false, false);
      }
      return channel;
    }
  }
}
