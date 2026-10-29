/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

public final class SHA extends DigestBase {
  private int[] state = new int[5];

  public SHA() {
    super("SHA-1", 20, 64);
    implReset();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    SHA copy = (SHA) super.clone();
    copy.state = state.clone();
    return copy;
  }

  @Override
  void implReset() {
    state[0] = 0x67452301;
    state[1] = 0xefcdab89;
    state[2] = 0x98badcfe;
    state[3] = 0x10325476;
    state[4] = 0xc3d2e1f0;
  }

  @Override
  void implDigest(byte[] out, int ofs) {
    padBlock(bytesProcessed << 3, true, 8);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 4; j++) {
        out[ofs + i * 4 + j] = (byte) (state[i] >>> (24 - 8 * j));
      }
    }
  }

  @Override
  void implCompress(byte[] buf, int ofs) {
    compressBlocks(state, buf, ofs, 64);
  }

  @Override
  void implCompressBlocks(byte[] buf, int ofs, int length) {
    // Bound each allocation-free native call, allowing the interpreter to
    // reach safepoints between chunks even for very large input arrays.
    while (length != 0) {
      int chunk = Math.min(length, 65536);
      compressBlocks(state, buf, ofs, chunk);
      ofs += chunk;
      length -= chunk;
    }
  }

  private static native void compressBlocks(int[] state, byte[] buf, int ofs, int length);
}
