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

public final class ChaCha20Poly1305Parameters extends AlgorithmParametersSpi {
  private byte[] nonce;

  public ChaCha20Poly1305Parameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof IvParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    byte[] iv = ((IvParameterSpec) paramSpec).getIV();
    if (iv.length != 12) {
      throw new InvalidParameterSpecException("ChaCha20-Poly1305 nonce must be 12 bytes in length");
    }
    nonce = iv;
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue val = new DerValue(encoded);
    byte[] n = val.getOctetString();
    if (n.length != 12) {
      throw new IOException("ChaCha20-Poly1305 nonce must be 12 bytes in length");
    }
    nonce = n;
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (IvParameterSpec.class.isAssignableFrom(paramSpec)) {
      return paramSpec.cast(new IvParameterSpec(nonce));
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream out = new DerOutputStream();
    out.putOctetString(nonce);
    return out.toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    return HexDumpEncoder.describe("nonce", nonce);
  }
}
