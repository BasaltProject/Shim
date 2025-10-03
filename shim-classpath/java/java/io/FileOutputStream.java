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

public class FileOutputStream extends OutputStream {
  private final FileDescriptor fd;
  private final boolean append;
  private final String path;
  private volatile boolean closed;
  private FileChannel channel;

  public FileOutputStream(String name) throws FileNotFoundException {
    this(name != null ? new File(name) : null, false);
  }

  public FileOutputStream(String name, boolean append) throws FileNotFoundException {
    this(name != null ? new File(name) : null, append);
  }

  public FileOutputStream(File file) throws FileNotFoundException {
    this(file, false);
  }

  public FileOutputStream(File file, boolean append) throws FileNotFoundException {
    String name = (file != null ? file.getPath() : null);
    if (name == null) {
      throw new NullPointerException();
    }
    if (file.isInvalid()) {
      throw new FileNotFoundException("Invalid file path");
    }
    this.fd = new FileDescriptor();
    this.append = append;
    this.path = name;
    int flags = FileNative.WRITE | FileNative.CREATE | (append ? FileNative.APPEND : FileNative.TRUNCATE);
    int r = FileNative.open(name, flags, 0666);
    if (r < 0) {
      throw new FileNotFoundException(name + " (" + FileNative.message(r) + ")");
    }
    fd.fd = r;
  }

  public FileOutputStream(FileDescriptor fdObj) {
    if (fdObj == null) {
      throw new NullPointerException();
    }
    this.fd = fdObj;
    this.append = false;
    this.path = null;
  }

  private void ensureOpen() throws IOException {
    if (closed || fd.fd == -1) {
      throw new IOException("Stream Closed");
    }
  }

  public void write(int b) throws IOException {
    ensureOpen();
    int r = FileNative.writeByte(fd.fd, b);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
  }

  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    ensureOpen();
    int r = FileNative.write(fd.fd, b, off, len);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
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
        channel = new FileChannelImpl(new RandomAccessFile(fd, path, false, true), false, true, append);
      }
      return channel;
    }
  }
}
