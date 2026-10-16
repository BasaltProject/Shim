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

import java.util.Objects;

public class StringWriter extends Writer {
  private StringBuffer buf;

  public StringWriter() {
    buf = new StringBuffer();
    lock = buf;
  }

  public StringWriter(int initialSize) {
    if (initialSize < 0) {
      throw new IllegalArgumentException("Negative buffer size");
    }
    buf = new StringBuffer(initialSize);
    lock = buf;
  }

  public void write(int c) {
    buf.append((char) c);
  }

  public void write(char[] cbuf, int off, int len) {
    Objects.checkFromIndexSize(off, len, cbuf.length);
    if (len == 0) {
      return;
    }
    buf.append(cbuf, off, len);
  }

  public void write(String str) {
    buf.append(str);
  }

  public void write(String str, int off, int len) {
    buf.append(str, off, off + len);
  }

  public StringWriter append(CharSequence csq) {
    write(String.valueOf(csq));
    return this;
  }

  public StringWriter append(CharSequence csq, int start, int end) {
    if (csq == null) {
      csq = "null";
    }
    return append(csq.subSequence(start, end));
  }

  public StringWriter append(char c) {
    write(c);
    return this;
  }

  public String toString() {
    return buf.toString();
  }

  public StringBuffer getBuffer() {
    return buf;
  }

  public void flush() { }

  public void close() throws IOException { }
}
