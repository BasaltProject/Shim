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
import javax.crypto.spec.DHParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class DHParameters extends AlgorithmParametersSpi {
  private BigInteger p;
  private BigInteger g;
  private int l;

  public DHParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof DHParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    DHParameterSpec s = (DHParameterSpec) paramSpec;
    p = s.getP();
    g = s.getG();
    l = s.getL();
  }

  @Override
  protected void engineInit(byte[] params) throws IOException {
    DerInputStream in = new DerValue(params).data();
    p = in.getBigInteger();
    g = in.getBigInteger();
    l = in.available() > 0 ? in.getInteger() : 0;
    if (in.available() > 0) {
      throw new IOException("Extra data at the end");
    }
  }

  @Override
  protected void engineInit(byte[] params, String format) throws IOException {
    engineInit(params);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (paramSpec.isAssignableFrom(DHParameterSpec.class)) {
      return (T) new DHParameterSpec(p, g, l);
    }
    throw new InvalidParameterSpecException("Inappropriate parameter Specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream body = new DerOutputStream();
    body.putInteger(p);
    body.putInteger(g);
    if (l > 0) {
      body.putInteger(l);
    }
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String format) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    String s = "SunJCE Diffie-Hellman Parameters:\np:\n" + DHKeyCodec.hex(p) + "\ng:\n" + DHKeyCodec.hex(g);
    return l > 0 ? s + "\nl:\n    " + l : s;
  }
}
