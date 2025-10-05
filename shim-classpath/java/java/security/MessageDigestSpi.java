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
  public MessageDigestSpi() {
  }

  protected int engineGetDigestLength() {
    return 0;
  }

  protected abstract void engineUpdate(byte input);

  protected abstract void engineUpdate(byte[] input, int offset, int len);

  protected void engineUpdate(ByteBuffer input) {
    if (!input.hasRemaining()) {
      return;
    }
    byte[] tmp;
    if (input.hasArray()) {
      byte[] b = input.array();
      int ofs = input.arrayOffset();
      int pos = input.position();
      int lim = input.limit();
      engineUpdate(b, ofs + pos, lim - pos);
      input.position(lim);
    } else {
      int len = input.remaining();
      int n = len > 4096 ? 4096 : len;
      tmp = new byte[n];
      while (len > 0) {
        int chunk = Math.min(len, tmp.length);
        input.get(tmp, 0, chunk);
        engineUpdate(tmp, 0, chunk);
        len -= chunk;
      }
    }
  }

  protected abstract byte[] engineDigest();

  protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
    byte[] digest = engineDigest();
    if (len < digest.length) {
      throw new DigestException("partial digests not returned");
    }
    if (buf.length - offset < digest.length) {
      throw new DigestException("insufficient space in the output buffer to store the digest");
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
