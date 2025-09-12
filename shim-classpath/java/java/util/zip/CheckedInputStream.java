/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CheckedInputStream extends FilterInputStream {
  private final Checksum cksum;

  public CheckedInputStream(InputStream in, Checksum cksum) {
    super(in);
    this.cksum = Objects.requireNonNull(cksum, "cksum");
  }

  @Override
  public int read() throws IOException {
    int b = in.read();
    if (b != -1) {
      cksum.update(b);
    }
    return b;
  }

  @Override
  public int read(byte[] buf) throws IOException {
    return read(buf, 0, buf.length);
  }

  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    int n = in.read(buf, off, len);
    if (n > 0) {
      cksum.update(buf, off, n);
    }
    return n;
  }

  @Override
  public long skip(long n) throws IOException {
    byte[] buf = new byte[512];
    long total = 0;
    while (total < n) {
      long rem = n - total;
      int toRead = (int) Math.min(rem, buf.length);
      int r = read(buf, 0, toRead);
      if (r < 0) {
        break;
      }
      total += r;
    }
    return total;
  }

  public Checksum getChecksum() {
    return cksum;
  }
}
