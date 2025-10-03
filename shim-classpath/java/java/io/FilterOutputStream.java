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

public class FilterOutputStream extends OutputStream {
  protected OutputStream out;

  private volatile boolean closed;
  private final Object closeLock = new Object();

  public FilterOutputStream(OutputStream out) {
    this.out = out;
  }

  public void write(int b) throws IOException {
    out.write(b);
  }

  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    if ((off | len | (b.length - (len + off)) | (off + len)) < 0) {
      throw new IndexOutOfBoundsException();
    }
    for (int i = 0; i < len; i++) {
      write(b[off + i]);
    }
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    if (closed) {
      return;
    }
    synchronized (closeLock) {
      if (closed) {
        return;
      }
      closed = true;
    }
    Throwable flushException = null;
    try {
      flush();
    } catch (Throwable e) {
      flushException = e;
      throw e;
    } finally {
      if (flushException == null) {
        out.close();
      } else {
        try {
          out.close();
        } catch (Throwable closeException) {
          if (flushException != closeException) {
            closeException.addSuppressed(flushException);
          }
          throw closeException;
        }
      }
    }
  }
}
