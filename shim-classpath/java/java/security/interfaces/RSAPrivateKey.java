/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface RSAPrivateKey extends java.security.PrivateKey, RSAKey {
  @Deprecated
  long serialVersionUID = 5187144804936595022L;

  BigInteger getPrivateExponent();

  @Override
  default java.security.spec.AlgorithmParameterSpec getParams() {
    return null;
  }
}
