/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.DigestException;
import java.security.MessageDigestSpi;
import java.security.ProviderException;
import java.util.Arrays;

abstract class DigestBase extends MessageDigestSpi implements Cloneable {
  private byte[] oneByte;
  private final String algorithm;
  private final int digestLength;
  private final int blockSize;
  byte[] buffer;
  int bufOfs;
  long bytesProcessed;

  DigestBase(String algorithm, int digestLength, int blockSize) {
    this.algorithm = algorithm;
    this.digestLength = digestLength;
    this.blockSize = blockSize;
    buffer = new byte[blockSize];
  }

  abstract void implCompress(byte[] b, int ofs);

  // Providers may process a contiguous sequence of complete blocks at once.
  // The default retains the existing per-block behavior for other algorithms.
  void implCompressBlocks(byte[] b, int ofs, int length) {
    while (length != 0) {
      implCompress(b, ofs);
      ofs += blockSize;
      length -= blockSize;
    }
  }

  abstract void implDigest(byte[] out, int ofs);

  abstract void implReset();

  @Override
  protected final int engineGetDigestLength() {
    return digestLength;
  }

  @Override
  protected final void engineUpdate(byte b) {
    if (oneByte == null) {
      oneByte = new byte[1];
    }
    oneByte[0] = b;
    engineUpdate(oneByte, 0, 1);
  }

  @Override
  protected final void engineUpdate(byte[] b, int ofs, int len) {
    if (len == 0) {
      return;
    }
    if (ofs < 0 || len < 0 || ofs > b.length - len) {
      throw new ArrayIndexOutOfBoundsException("Range [" + ofs + ", " + ofs + " + " + len
          + ") out of bounds for length " + b.length);
    }
    if (bytesProcessed < 0) {
      engineReset();
    }
    bytesProcessed += len;
    if (bufOfs != 0) {
      int n = Math.min(len, blockSize - bufOfs);
      System.arraycopy(b, ofs, buffer, bufOfs, n);
      bufOfs += n;
      ofs += n;
      len -= n;
      if (bufOfs >= blockSize) {
        implCompress(buffer, 0);
        bufOfs = 0;
      }
    }
    int complete = len - len % blockSize;
    if (complete != 0) {
      implCompressBlocks(b, ofs, complete);
      len -= complete;
      ofs += complete;
    }
    if (len > 0) {
      System.arraycopy(b, ofs, buffer, 0, len);
      bufOfs = len;
    }
  }

  @Override
  protected final void engineReset() {
    if (bytesProcessed == 0) {
      return;
    }
    implReset();
    bufOfs = 0;
    bytesProcessed = 0;
    Arrays.fill(buffer, (byte) 0x00);
  }

  @Override
  protected final byte[] engineDigest() {
    byte[] b = new byte[digestLength];
    try {
      engineDigest(b, 0, b.length);
    } catch (DigestException e) {
      throw new ProviderException("Internal error", e);
    }
    return b;
  }

  @Override
  protected final int engineDigest(byte[] out, int ofs, int len) throws DigestException {
    if (len < digestLength) {
      throw new DigestException("Length must be at least " + digestLength + " for " + algorithm + "digests");
    }
    if (ofs < 0 || len < 0 || ofs > out.length - len) {
      throw new DigestException("Buffer too short to store digest");
    }
    if (bytesProcessed < 0) {
      engineReset();
    }
    implDigest(out, ofs);
    bytesProcessed = -1;
    return digestLength;
  }

  void padBlock(long lengthBits, boolean bigEndianLength, int lengthBytes) {
    int blockSize = this.blockSize;
    buffer[bufOfs++] = (byte) 0x80;
    if (bufOfs > blockSize - lengthBytes) {
      Arrays.fill(buffer, bufOfs, blockSize, (byte) 0);
      implCompress(buffer, 0);
      bufOfs = 0;
    }
    Arrays.fill(buffer, bufOfs, blockSize, (byte) 0);
    for (int i = 0; i < 8; i++) {
      int shift = 8 * i;
      int pos = bigEndianLength ? blockSize - 1 - i : blockSize - lengthBytes + i;
      buffer[pos] = (byte) (lengthBits >>> shift);
    }
    implCompress(buffer, 0);
    bufOfs = 0;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    DigestBase copy = (DigestBase) super.clone();
    copy.buffer = buffer.clone();
    copy.oneByte = null;
    return copy;
  }
}
