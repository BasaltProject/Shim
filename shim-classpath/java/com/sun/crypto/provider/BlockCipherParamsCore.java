/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.IvParameterSpec;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;

abstract class BlockCipherParamsCore extends AlgorithmParametersSpi {
  private final int blkSize;
  private final int[] moreSizes;
  private byte[] iv;

  BlockCipherParamsCore(int blkSize, int... moreSizes) {
    this.blkSize = blkSize;
    this.moreSizes = moreSizes;
  }

  private String sizeText() {
    return blkSize + java.util.Arrays.toString(moreSizes);
  }

  private boolean sizeOk(int len) {
    if (len == blkSize) {
      return true;
    }
    for (int s : moreSizes) {
      if (len == s) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof IvParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    byte[] tmpIv = ((IvParameterSpec) paramSpec).getIV();
    if (!sizeOk(tmpIv.length)) {
      throw new InvalidParameterSpecException("IV length not " + sizeText() + " bytes long");
    }
    this.iv = tmpIv;
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue val = new DerValue(encoded);
    byte[] tmp = val.getOctetString();
    if (val.getTag() != DerValue.tag_OctetString) {
      throw new IOException("IV not an octet string");
    }
    if (!sizeOk(tmp.length)) {
      throw new IOException("IV not " + sizeText() + " bytes long");
    }
    this.iv = tmp;
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (paramSpec.isAssignableFrom(IvParameterSpec.class)) {
      return paramSpec.cast(new IvParameterSpec(this.iv));
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream out = new DerOutputStream();
    out.putOctetString(this.iv);
    return out.toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    return HexDumpEncoder.describe("iv", iv);
  }
}
