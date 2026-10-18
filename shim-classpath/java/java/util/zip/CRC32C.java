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

public final class CRC32C implements Checksum {
  private static final int POLY = 0x82F63B78;
  private static final int[] TABLE = new int[256];

  static {
    for (int i = 0; i < 256; i++) {
      int c = i;
      for (int bit = 0; bit < 8; bit++) {
        if ((c & 1) != 0) {
          c = (c >>> 1) ^ POLY;
        } else {
          c = c >>> 1;
        }
      }
      TABLE[i] = c;
    }
  }

  private int crc = -1;

  public CRC32C() {}

  @Override
  public void update(int b) {
    crc = TABLE[(crc ^ (b & 0xFF)) & 0xFF] ^ (crc >>> 8);
  }

  @Override
  public void update(byte[] b, int off, int len) {
    Objects.requireNonNull(b, "b");
    if (off < 0 || len < 0 || off > b.length - len) {
      throw new ArrayIndexOutOfBoundsException("Range [" + off + ", " + off + " + " + len
          + ") out of bounds for length " + b.length);
    }
    int local = crc;
    int end = off + len;
    for (int i = off; i < end; i++) {
      local = TABLE[(local ^ (b[i] & 0xFF)) & 0xFF] ^ (local >>> 8);
    }
    crc = local;
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
    crc = -1;
  }

  @Override
  public long getValue() {
    return (crc ^ -1) & 0xFFFFFFFFL;
  }
}
