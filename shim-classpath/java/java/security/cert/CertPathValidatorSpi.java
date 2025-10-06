/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.InvalidAlgorithmParameterException;

public abstract class CertPathValidatorSpi {
  public CertPathValidatorSpi() {
  }

  public abstract CertPathValidatorResult engineValidate(CertPath certPath, CertPathParameters params)
      throws CertPathValidatorException, InvalidAlgorithmParameterException;

  public CertPathChecker engineGetRevocationChecker() {
    throw new UnsupportedOperationException();
  }
}
