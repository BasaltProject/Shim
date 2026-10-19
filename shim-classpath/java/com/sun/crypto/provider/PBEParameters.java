/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.PBEParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;

public final class PBEParameters extends AlgorithmParametersSpi {
  private byte[] salt;
  private int iterationCount;

  public PBEParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof PBEParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    PBEParameterSpec spec = (PBEParameterSpec) paramSpec;
    this.salt = spec.getSalt().clone();
    this.iterationCount = spec.getIterationCount();
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue sequence = new DerValue(encoded);
    if (sequence.getTag() != DerValue.tag_Sequence) {
      throw new IOException("PBE parameter parsing error: not an ASN.1 SEQUENCE tag");
    }
    DerInputStream in = sequence.data();
    this.salt = in.getDerValue().getOctetString();
    this.iterationCount = in.getDerValue().getInteger();
    if (in.available() != 0) {
      throw new IOException("PBE parameter parsing error: extra data");
    }
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (PBEParameterSpec.class.isAssignableFrom(paramSpec)) {
      return (T) new PBEParameterSpec(salt, iterationCount);
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream bytes = new DerOutputStream();
    bytes.putOctetString(salt);
    bytes.putInteger(iterationCount);
    return new DerValue(DerValue.tag_Sequence, bytes.toByteArray()).toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    String separator = System.lineSeparator();
    StringBuilder sb = new StringBuilder(separator + "    salt:" + separator + "[");
    sb.append(new HexDumpEncoder().encodeBuffer(salt));
    sb.append("]");
    String hex = new BigInteger(Integer.toString(iterationCount)).toString(16).toUpperCase();
    if ((hex.length() & 1) != 0) {
      hex = "0" + hex;
    }
    sb.append(separator + "    iterationCount:");
    sb.append(separator + "    " + hex + separator);
    return sb.toString();
  }
}
