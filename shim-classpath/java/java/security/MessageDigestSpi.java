/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.nio.ByteBuffer;

public abstract class MessageDigestSpi {
  protected int engineGetDigestLength() {
    return 0;
  }

  protected abstract void engineUpdate(byte input);

  protected abstract void engineUpdate(byte[] input, int offset, int len);

  protected void engineUpdate(ByteBuffer input) {
    if (!input.hasRemaining()) return;
    if (input.hasArray()) {
      byte[] b = input.array();
      int ofs = input.arrayOffset();
      int pos = input.position();
      int lim = input.limit();
      engineUpdate(b, ofs + pos, lim - pos);
      input.position(lim);
    } else {
      int len = input.remaining();
      byte[] b = new byte[Math.min(len, 4096)];
      while (input.hasRemaining()) {
        int n = Math.min(input.remaining(), b.length);
        input.get(b, 0, n);
        engineUpdate(b, 0, n);
      }
    }
  }

  protected abstract byte[] engineDigest();

  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    byte[] digest = engineDigest();
    if (len < digest.length) {
      throw new DigestException("insufficient space in the output buffer");
    }
    if (buf.length - offset < digest.length) {
      throw new DigestException("insufficient space in the output buffer");
    }
    System.arraycopy(digest, 0, buf, offset, digest.length);
    return digest.length;
  }

  protected abstract void engineReset();

  public Object clone() throws CloneNotSupportedException {
    if (this instanceof Cloneable) {
      return super.clone();
    }
    throw new CloneNotSupportedException();
  }
}
