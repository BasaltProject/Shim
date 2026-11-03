/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public final class DSAParameters extends AlgorithmParametersSpi {
  private BigInteger p;
  private BigInteger q;
  private BigInteger g;

  public DSAParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof DSAParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    DSAParameterSpec s = (DSAParameterSpec) paramSpec;
    p = s.getP();
    q = s.getQ();
    g = s.getG();
  }

  @Override
  protected void engineInit(byte[] params) throws IOException {
    DerInputStream in = new DerValue(params).data();
    p = in.getDerValue().getBigInteger();
    q = in.getDerValue().getBigInteger();
    g = in.getDerValue().getBigInteger();
  }

  @Override
  protected void engineInit(byte[] params, String format) throws IOException {
    engineInit(params);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (paramSpec.isAssignableFrom(DSAParameterSpec.class)) {
      return (T) new DSAParameterSpec(p, q, g);
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream body = new DerOutputStream();
    body.putInteger(p);
    body.putInteger(q);
    body.putInteger(g);
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
    return "\n\tp: " + p.toString(16) + "\n\tq: " + q.toString(16) + "\n\tg: " + g.toString(16) + "\n";
  }
}
