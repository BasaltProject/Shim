/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface ECPrivateKey extends java.security.PrivateKey, ECKey {
  @Deprecated
  long serialVersionUID = -7896394956925609184L;

  BigInteger getS();

  @Override
  default java.security.spec.ECParameterSpec getParams() {
    return null;
  }
}
