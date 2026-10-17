/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public abstract class KDFSpi {
  protected KDFSpi(KDFParameters kdfParameters) throws InvalidAlgorithmParameterException {
  }

  protected abstract KDFParameters engineGetParameters();

  protected abstract SecretKey engineDeriveKey(String alg, AlgorithmParameterSpec derivationSpec)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException;

  protected abstract byte[] engineDeriveData(AlgorithmParameterSpec derivationSpec)
      throws InvalidAlgorithmParameterException;
}
