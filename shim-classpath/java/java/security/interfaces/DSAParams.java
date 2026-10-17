/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface DSAParams extends java.security.spec.AlgorithmParameterSpec {
  BigInteger getP();

  BigInteger getQ();

  BigInteger getG();
}
