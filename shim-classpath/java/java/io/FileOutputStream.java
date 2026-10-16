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

public class FileOutputStream extends OutputStream {
  //   static {
  //     System.loadLibrary("natives");
  //   }

  private int fd;
  private String path;
  private boolean append;

  public FileOutputStream(FileDescriptor fd) {
    this.fd = fd.value;
  }

  public FileOutputStream(String path) throws FileNotFoundException {
    this(path, false);
  }

  public FileOutputStream(String path, boolean append) throws FileNotFoundException {
    this.path = path;
    this.append = append;
    try {
      fd = open(path, append);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      FileNotFoundException notFound = new FileNotFoundException(e.getMessage());
      notFound.initCause(e);
      throw notFound;
    }
  }

  public FileOutputStream(File file) throws FileNotFoundException {
    this(file.getPath());
  }

  public FileOutputStream(File file, boolean append) throws FileNotFoundException {
    this(file.getPath(), append);
  }

  public FileDescriptor getFD() throws IOException {
    if (fd == -1) throw new IOException("closed");
    return new FileDescriptor(fd);
  }

  public java.nio.channels.FileChannel getChannel() {
    try {
      RandomAccessFile raf = path != null
          ? new RandomAccessFile(new File(path), "rw")
          : new RandomAccessFile(File.createTempFile("shim-fos", ".tmp"), "rw");
      return new java.nio.channels.FileChannelImpl(raf, false, true, append);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static native int open(String path, boolean append) throws IOException;

  private static native void write(int fd, int c) throws IOException;

  private static native void write(int fd, byte[] b, int offset, int length)
    throws IOException;

  private static native void close(int fd) throws IOException;

  public void write(int c) throws IOException {
    write(fd, c);
  }

  public void write(byte[] b, int offset, int length) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }

    if (offset < 0 || length < 0 || length > b.length || offset > b.length - length) {
      throw new ArrayIndexOutOfBoundsException();
    }

    write(fd, b, offset, length);
  }

  public void close() throws IOException {
    if (fd != -1) {
      close(fd);
      fd = -1;
    }
  }
}
