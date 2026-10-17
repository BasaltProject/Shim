/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

public final class PSSParameters extends AlgorithmParametersSpi {
  private PSSParameterSpec spec;

  public PSSParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof PSSParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    PSSParameterSpec s = (PSSParameterSpec) paramSpec;
    String mgfName = s.getMGFAlgorithm();
    if (!mgfName.equalsIgnoreCase("MGF1")) {
      throw new InvalidParameterSpecException("Unsupported mgf " + mgfName + "; MGF1 only");
    }
    AlgorithmParameterSpec mgfSpec = s.getMGFParameters();
    if (!(mgfSpec instanceof MGF1ParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate mgf parameters; non-null MGF1ParameterSpec only");
    }
    if (RSAUtil.digestOid(s.getDigestAlgorithm()) == null) {
      throw new InvalidParameterSpecException("Unsupported hash algorithm: " + s.getDigestAlgorithm());
    }
    if (s.getTrailerField() != 1) {
      throw new InvalidParameterSpecException("Unsupported trailerField value " + s.getTrailerField());
    }
    this.spec = s;
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    this.spec = RSAUtil.decodePssParams(encoded);
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (spec != null && paramSpec.isAssignableFrom(PSSParameterSpec.class)) {
      return paramSpec.cast(spec);
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    return RSAUtil.encodePssParams(spec);
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    return spec.toString();
  }
}
