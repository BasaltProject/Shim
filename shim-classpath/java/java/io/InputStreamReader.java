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
import java.nio.charset.CharsetDecoder;
import sun.nio.cs.StreamDecoder;

public class InputStreamReader extends Reader {
  private final StreamDecoder sd;

  public InputStreamReader(InputStream in) {
    super(in);
    sd = StreamDecoder.forInputStreamReader(in, this, Charset.defaultCharset());
  }

  public InputStreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
    super(in);
    if (charsetName == null) {
      throw new NullPointerException("charsetName");
    }
    sd = StreamDecoder.forInputStreamReader(in, this, charsetName);
  }

  public InputStreamReader(InputStream in, Charset cs) {
    super(in);
    if (cs == null) {
      throw new NullPointerException("charset");
    }
    sd = StreamDecoder.forInputStreamReader(in, this, cs);
  }

  public InputStreamReader(InputStream in, CharsetDecoder dec) {
    super(in);
    if (dec == null) {
      throw new NullPointerException("charset decoder");
    }
    sd = StreamDecoder.forInputStreamReader(in, this, dec);
  }

  public String getEncoding() {
    return sd.getEncoding();
  }

  public int read() throws IOException {
    return sd.read();
  }

  public int read(char[] cbuf, int offset, int length) throws IOException {
    return sd.read(cbuf, offset, length);
  }

  public boolean ready() throws IOException {
    return sd.ready();
  }

  public void close() throws IOException {
    sd.close();
  }
}
