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


package java.nio;


class FixedArrayByteBuffer extends DirectByteBuffer {
  private final byte[] array;

  private FixedArrayByteBuffer(long address, byte[] array, int mark, int pos, int lim, int cap, boolean readOnly) {
    super(address, mark, pos, lim, cap, readOnly);
    this.array = array;
  }

  public static FixedArrayByteBuffer make(int capacity) {
    long[] address = new long[1];
    byte[] array = allocateFixed(capacity, address);
    return new FixedArrayByteBuffer(address[0], array, -1, 0, capacity, capacity, false);
  }

  private static native byte[] allocateFixed(int capacity, long[] address);

  @Override
  DirectByteBuffer make(long address, int mark, int pos, int lim, int cap, boolean readOnly) {
    return new FixedArrayByteBuffer(address, array, mark, pos, lim, cap, readOnly);
  }
}
