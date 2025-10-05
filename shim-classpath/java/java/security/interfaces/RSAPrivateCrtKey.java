/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface RSAPrivateCrtKey extends RSAPrivateKey {
  @Deprecated
  long serialVersionUID = -5682214253527700368L;

  BigInteger getPublicExponent();

  BigInteger getPrimeP();

  BigInteger getPrimeQ();

  BigInteger getPrimeExponentP();

  BigInteger getPrimeExponentQ();

  BigInteger getCrtCoefficient();
}
