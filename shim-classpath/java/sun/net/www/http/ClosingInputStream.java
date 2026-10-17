/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

class ClosingInputStream extends InputStream {
  private InputStream in;
  private final HttpClient hc;
  private boolean closed;

  ClosingInputStream(InputStream in, HttpClient hc) {
    this.in = in;
    this.hc = hc;
  }

  @Override
  public int read() throws IOException {
    byte[] one = new byte[1];
    int n = read(one, 0, 1);
    return n < 0 ? -1 : one[0] & 0xff;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("stream is closed");
    }
    Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return 0;
    }
    if (in == null) {
      return -1;
    }
    int n = in.read(b, off, len);
    if (n < 0) {
      hc.closeServer();
      in = null;
    }
    return n;
  }

  @Override
  public int available() throws IOException {
    if (closed) {
      throw new IOException("stream is closed");
    }
    return in == null ? 0 : in.available();
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    hc.closeServer();
    in = null;
  }
}
