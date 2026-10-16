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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class ByteArrayOutputStream extends OutputStream {
  protected byte[] buf;
  protected int count;

  public ByteArrayOutputStream() {
    this(32);
  }

  public ByteArrayOutputStream(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Negative initial size: " + size);
    }
    buf = new byte[size];
  }

  private void ensureCapacity(int minCapacity) {
    int oldCapacity = buf.length;
    int minGrowth = minCapacity - oldCapacity;
    if (minGrowth > 0) {
      int newLength = Math.max(minCapacity, oldCapacity * 2);
      if (newLength < 0) {
        if (minCapacity < 0) {
          throw new OutOfMemoryError("Required length exceeds implementation limit");
        }
        newLength = Integer.MAX_VALUE - 8;
      }
      buf = Arrays.copyOf(buf, newLength);
    }
  }

  public synchronized void write(int b) {
    ensureCapacity(count + 1);
    buf[count] = (byte) b;
    count += 1;
  }

  public synchronized void write(byte[] b, int off, int len) {
    Objects.checkFromIndexSize(off, len, b.length);
    ensureCapacity(count + len);
    System.arraycopy(b, off, buf, count, len);
    count += len;
  }

  public void writeBytes(byte[] b) {
    write(b, 0, b.length);
  }

  public synchronized void writeTo(OutputStream out) throws IOException {
    out.write(buf, 0, count);
  }

  public synchronized void reset() {
    count = 0;
  }

  public synchronized byte[] toByteArray() {
    return Arrays.copyOf(buf, count);
  }

  public synchronized int size() {
    return count;
  }

  public synchronized String toString() {
    return new String(buf, 0, count);
  }

  public synchronized String toString(String charsetName) throws UnsupportedEncodingException {
    return new String(buf, 0, count, charsetName);
  }

  public synchronized String toString(Charset charset) {
    return new String(buf, 0, count, charset);
  }

  @Deprecated
  public synchronized String toString(int hibyte) {
    return new String(buf, hibyte, 0, count);
  }

  public void close() throws IOException { }
}
