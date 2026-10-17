/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface RSAKey {
  BigInteger getModulus();

  default java.security.spec.AlgorithmParameterSpec getParams() {
    return null;
  }
}
