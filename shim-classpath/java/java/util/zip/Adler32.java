/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Adler32 implements Checksum {
  private static final int BASE = 65521;
  private int s1 = 1;
  private int s2 = 0;

  public Adler32() {}

  @Override
  public void update(int b) {
    s1 = (s1 + (b & 0xFF)) % BASE;
    s2 = (s2 + s1) % BASE;
  }

  @Override
  public void update(byte[] b, int off, int len) {
    Objects.requireNonNull(b, "b");
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException("Range [" + off + ", " + off + " + " + len
          + ") out of bounds for length " + b.length);
    }
    int localS1 = s1;
    int localS2 = s2;
    int end = off + len;
    for (int i = off; i < end; i++) {
      localS1 = (localS1 + (b[i] & 0xFF)) % BASE;
      localS2 = (localS2 + localS1) % BASE;
    }
    s1 = localS1;
    s2 = localS2;
  }

  @Override
  public void update(byte[] b) {
    Objects.requireNonNull(b, "b");
    update(b, 0, b.length);
  }

  @Override
  public void update(ByteBuffer buffer) {
    Checksum.super.update(buffer);
  }

  @Override
  public void reset() {
    s1 = 1;
    s2 = 0;
  }

  @Override
  public long getValue() {
    return (((long) s2 << 16) | s1) & 0xFFFFFFFFL;
  }
}
