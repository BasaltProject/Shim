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

package java.util.zip;

import java.nio.ByteBuffer;

public class CRC32 implements Checksum {
  private static final int InitialRemainder = 0xFFFFFFFF;
  private static final int[] table = new int[256];

  static {
    for (int dividend = 0; dividend < 256; ++dividend) {
      int crc = dividend;
      for (int bit = 0; bit < 8; ++bit) {
        crc = (crc >>> 1) ^ ((crc & 1) != 0 ? 0xEDB88320 : 0);
      }
      table[dividend] = crc;
    }
  }

  private int remainder = InitialRemainder;

  public void reset() {
    remainder = InitialRemainder;
  }

  public void update(int b) {
    remainder = (remainder >>> 8) ^ table[(remainder ^ b) & 0xFF];
  }

  public void update(byte[] array, int offset, int length) {
    int size = array.length;
    if (offset < 0 || length < 0 || offset > size - length) {
      throw new ArrayIndexOutOfBoundsException("Range [" + offset + ", " + offset + " + " + length
          + ") out of bounds for length " + size);
    }
    if (length != 0) remainder = updateBytes(remainder, array, offset, length);
  }

  public void update(byte[] array) {
    update(array, 0, array.length);
  }

  public void update(ByteBuffer buffer) {
    Checksum.super.update(buffer);
  }

  public long getValue() {
    return (remainder ^ 0xFFFFFFFFL) & 0xFFFFFFFFL;
  }

  private static native int updateBytes(int remainder, byte[] array, int offset, int length);
}
