/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface DSAPrivateKey extends DSAKey, java.security.PrivateKey {
  @Deprecated
  long serialVersionUID = 7776497482533790279L;

  BigInteger getX();

  @Override
  default DSAParams getParams() {
    return null;
  }
}
