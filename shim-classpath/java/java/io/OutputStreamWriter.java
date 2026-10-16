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
import java.nio.charset.CharsetEncoder;
import sun.nio.cs.StreamEncoder;

public class OutputStreamWriter extends Writer {
  private final StreamEncoder se;

  public OutputStreamWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
    super(out);
    if (charsetName == null) {
      throw new NullPointerException("charsetName");
    }
    se = StreamEncoder.forOutputStreamWriter(out, this, charsetName);
  }

  public OutputStreamWriter(OutputStream out) {
    super(out);
    se = StreamEncoder.forOutputStreamWriter(out, this, Charset.defaultCharset());
  }

  public OutputStreamWriter(OutputStream out, Charset cs) {
    super(out);
    if (cs == null) {
      throw new NullPointerException("charset");
    }
    se = StreamEncoder.forOutputStreamWriter(out, this, cs);
  }

  public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
    super(out);
    if (enc == null) {
      throw new NullPointerException("charset encoder");
    }
    se = StreamEncoder.forOutputStreamWriter(out, this, enc);
  }

  public String getEncoding() {
    return se.getEncoding();
  }

  void flushBuffer() throws IOException {
    se.flushBuffer();
  }

  public void write(int c) throws IOException {
    se.write(c);
  }

  public void write(char[] cbuf, int off, int len) throws IOException {
    se.write(cbuf, off, len);
  }

  public void write(String str, int off, int len) throws IOException {
    se.write(str, off, len);
  }

  public Writer append(CharSequence csq, int start, int end) throws IOException {
    se.append(csq, start, end);
    return this;
  }

  public Writer append(CharSequence csq) throws IOException {
    se.append(csq);
    return this;
  }

  public void flush() throws IOException {
    se.flush();
  }

  public void close() throws IOException {
    se.close();
  }
}
