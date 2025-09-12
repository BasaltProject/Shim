/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class CheckedOutputStream extends FilterOutputStream {
  private final Checksum cksum;

  public CheckedOutputStream(OutputStream out, Checksum cksum) {
    super(out);
    this.cksum = Objects.requireNonNull(cksum, "cksum");
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    cksum.update(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    cksum.update(b, off, len);
  }

  public Checksum getChecksum() {
    return cksum;
  }
}
