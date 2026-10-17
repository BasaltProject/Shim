/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class ExemptionMechanismSpi {
  public ExemptionMechanismSpi() {
  }

  protected abstract int engineGetOutputSize(int inputLen);

  protected abstract void engineInit(Key key) throws InvalidKeyException, ExemptionMechanismException;

  protected abstract void engineInit(Key key, AlgorithmParameterSpec params)
      throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

  protected abstract void engineInit(Key key, AlgorithmParameters params)
      throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException;

  protected abstract byte[] engineGenExemptionBlob() throws ExemptionMechanismException;

  protected abstract int engineGenExemptionBlob(byte[] output, int outputOffset)
      throws ShortBufferException, ExemptionMechanismException;
}
