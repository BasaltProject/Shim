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

import java.nio.CharBuffer;

public abstract class Reader implements Closeable, Readable {
  public int read(CharBuffer buffer) throws IOException {
    int c = read(buffer.array(),
                 buffer.arrayOffset() + buffer.position(),
                 buffer.remaining());

    if (c > 0) {
      buffer.position(buffer.position() + c);
    }

    return c;
  }

  public int read() throws IOException {
    char[] buffer = new char[1];
    int c = read(buffer);
    if (c <= 0) {
      return -1;
    } else {
      return (int) buffer[0];
    }
  }

  public int read(char[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public abstract int read(char[] buffer, int offset, int length)
    throws IOException;

  public boolean markSupported() {
    return false;
  }
  
  public void mark(int readAheadLimit) throws IOException {
    throw new IOException("mark not supported");
  }
  
  public void reset() throws IOException {
    throw new IOException("reset not supported");
  }

  public abstract void close() throws IOException;
}
