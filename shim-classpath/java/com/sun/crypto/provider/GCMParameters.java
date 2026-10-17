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
import javax.crypto.spec.GCMParameterSpec;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;

public final class GCMParameters extends AlgorithmParametersSpi {
  private byte[] iv;
  private int tLen;

  public GCMParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof GCMParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    GCMParameterSpec gps = (GCMParameterSpec) paramSpec;
    this.tLen = gps.getTLen() / 8;
    this.iv = gps.getIV();
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue val = new DerValue(encoded);
    if (val.getTag() != DerValue.tag_Sequence) {
      throw new IOException("GCM parameter parsing error: no SEQ tag");
    }
    java.io.ByteArrayInputStream unused = null;
    sun.security.util.DerInputStream in = val.data();
    DerValue ivVal = in.getDerValue();
    this.iv = ivVal.getOctetString();
    if (in.available() != 0) {
      DerValue tagVal = in.getDerValue();
      this.tLen = tagVal.getInteger();
    } else {
      this.tLen = 12;
    }
    if (in.available() != 0) {
      throw new IOException("GCM parameter parsing error: extra data");
    }
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (GCMParameterSpec.class.isAssignableFrom(paramSpec)) {
      return paramSpec.cast(new GCMParameterSpec(tLen * 8, iv));
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream inner = new DerOutputStream();
    inner.putOctetString(iv);
    inner.putInteger(tLen);
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, inner);
    return out.toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    return HexDumpEncoder.describe("iv", iv) + "tLen(bits):\n" + (tLen * 8) + "\n";
  }
}
