/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface RSAMultiPrimePrivateCrtKey extends RSAPrivateKey {
  @Deprecated
  long serialVersionUID = 618058533534628008L;

  BigInteger getPublicExponent();

  BigInteger getPrimeP();

  BigInteger getPrimeQ();

  BigInteger getPrimeExponentP();

  BigInteger getPrimeExponentQ();

  BigInteger getCrtCoefficient();

  java.security.spec.RSAOtherPrimeInfo[] getOtherPrimeInfo();
}
