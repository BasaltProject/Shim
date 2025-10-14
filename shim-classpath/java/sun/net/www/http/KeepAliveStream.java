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

public class KeepAliveStream extends InputStream {
  private InputStream in;
  private final HttpClient hc;
  private final long expected;
  private long count;
  private boolean closed;

  public KeepAliveStream(InputStream in, long expected, HttpClient hc) {
    this.in = in;
    this.expected = expected;
    this.hc = hc;
    if (expected == 0) {
      release();
    }
  }

  private void release() {
    if (in != null) {
      in = null;
      hc.finished();
    }
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
    if (in == null || count >= expected) {
      return -1;
    }
    int n = in.read(b, off, (int) Math.min(len, expected - count));
    if (n < 0) {
      hc.closeServer();
      in = null;
      throw new IOException("Premature EOF");
    }
    count += n;
    if (count >= expected) {
      release();
    }
    return n;
  }

  @Override
  public int available() throws IOException {
    if (closed) {
      throw new IOException("stream is closed");
    }
    if (in == null) {
      return 0;
    }
    return (int) Math.min(in.available(), expected - count);
  }

  @Override
  public long skip(long n) throws IOException {
    if (closed) {
      throw new IOException("stream is closed");
    }
    long total = 0;
    byte[] tmp = new byte[Math.max(1, (int) Math.min(n, 8192))];
    while (total < n) {
      int r = read(tmp, 0, (int) Math.min(tmp.length, n - total));
      if (r < 0) {
        break;
      }
      total += r;
    }
    return total;
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    if (in != null) {
      long left = expected - count;
      if (left <= in.available()) {
        byte[] tmp = new byte[(int) Math.min(Math.max(left, 1), 8192)];
        while (left > 0) {
          int r = in.read(tmp, 0, (int) Math.min(tmp.length, left));
          if (r < 0) {
            break;
          }
          left -= r;
        }
        if (left == 0) {
          release();
          return;
        }
      }
      hc.closeServer();
      in = null;
    }
  }
}
