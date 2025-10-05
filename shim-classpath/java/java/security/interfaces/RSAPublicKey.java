/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface RSAPublicKey extends java.security.PublicKey, RSAKey {
  @Deprecated
  long serialVersionUID = -8727434096241101194L;

  BigInteger getPublicExponent();

  @Override
  default java.security.spec.AlgorithmParameterSpec getParams() {
    return null;
  }
}
