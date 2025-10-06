/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.x509;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.PSSParameterSpec;

final class RSAUtilAccess {
  private RSAUtilAccess() {
  }

  static PSSParameterSpec pssSpec(byte[] encoded) throws IOException, InvalidAlgorithmParameterException {
    try {
      AlgorithmParameters ap = AlgorithmParameters.getInstance("RSASSA-PSS");
      ap.init(encoded);
      return ap.getParameterSpec(PSSParameterSpec.class);
    } catch (GeneralSecurityException e) {
      throw new InvalidAlgorithmParameterException(e.getMessage(), e);
    }
  }
}
