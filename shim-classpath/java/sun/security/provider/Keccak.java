/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.util.Arrays;

/** Incremental Keccak sponge used by the post-quantum algorithms. */
public final class Keccak {
  private final long[] state = new long[25];
  private final int rate;
  private final byte pad;
  private final byte[] buf;
  private int pos;
  private boolean squeezing;

  private Keccak(int rate, int pad) {
    this.rate = rate;
    this.pad = (byte) pad;
    this.buf = new byte[rate];
  }

  public static Keccak shake128() {
    return new Keccak(168, 0x1f);
  }

  public static Keccak shake256() {
    return new Keccak(136, 0x1f);
  }

  public static Keccak sha3_256() {
    return new Keccak(136, 0x06);
  }

  public static Keccak sha3_512() {
    return new Keccak(72, 0x06);
  }

  public Keccak absorb(byte[] data) {
    return absorb(data, 0, data.length);
  }

  public Keccak absorb(byte[] data, int off, int len) {
    for (int i = 0; i < len; i++) {
      state[pos >> 3] ^= (data[off + i] & 0xffL) << ((pos & 7) * 8);
      if (++pos == rate) {
        SHA3.keccak(state);
        pos = 0;
      }
    }
    return this;
  }

  private void finish() {
    state[pos >> 3] ^= (pad & 0xffL) << ((pos & 7) * 8);
    state[(rate - 1) >> 3] ^= 0x80L << (((rate - 1) & 7) * 8);
    SHA3.keccak(state);
    pos = 0;
    squeezing = true;
  }

  public void squeeze(byte[] out, int off, int len) {
    if (!squeezing) {
      finish();
    }
    for (int i = 0; i < len; i++) {
      if (pos == rate) {
        SHA3.keccak(state);
        pos = 0;
      }
      out[off + i] = (byte) (state[pos >> 3] >>> ((pos & 7) * 8));
      pos++;
    }
  }

  public byte[] squeeze(int len) {
    byte[] out = new byte[len];
    squeeze(out, 0, len);
    return out;
  }

  public void clear() {
    Arrays.fill(state, 0L);
    Arrays.fill(buf, (byte) 0);
  }
}
