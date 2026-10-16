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

public class FileInputStream extends InputStream {
  //   static {
  //     System.loadLibrary("natives");
  //   }

  private int fd;
  private int remaining;
  private String path;

  public FileInputStream(FileDescriptor fd) {
    this.fd = fd.value;
  }

  public FileInputStream(String path) throws FileNotFoundException {
    this.path = path;
    try {
      fd = open(path);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      FileNotFoundException notFound = new FileNotFoundException(e.getMessage());
      notFound.initCause(e);
      throw notFound;
    }
    remaining = (int) new File(path).length();
  }

  public FileInputStream(File file) throws FileNotFoundException {
    this(file.getPath());
  }

  public FileDescriptor getFD() throws IOException {
    if (fd == -1) throw new IOException("closed");
    return new FileDescriptor(fd);
  }

  public java.nio.channels.FileChannel getChannel() {
    try {
      RandomAccessFile raf = path != null
          ? new RandomAccessFile(new File(path), "r")
          : new RandomAccessFile(File.createTempFile("shim-fis", ".tmp"), "r");
      return new java.nio.channels.FileChannelImpl(raf, true, false, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int available() throws IOException {
    return remaining;
  }

  private static native int open(String path) throws IOException;

  private static native int read(int fd) throws IOException;

  private static native int read(int fd, byte[] b, int offset, int length)
    throws IOException;

  public static native void close(int fd) throws IOException;

  public int read() throws IOException {
    int c = read(fd);
    if (c >= 0 && remaining > 0) {
      -- remaining;
    }
    return c;
  }

  public int read(byte[] b, int offset, int length) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }

    if (offset < 0 || offset + length > b.length) {
      throw new ArrayIndexOutOfBoundsException();
    }

    int c = read(fd, b, offset, length);
    if (c > 0 && remaining > 0) {
      remaining -= c;
    }
    return c;
  }

  public void close() throws IOException {
    if (fd != -1) {
      close(fd);
      fd = -1;
    }
  }
}
