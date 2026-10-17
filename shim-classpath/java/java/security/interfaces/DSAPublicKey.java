/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

import java.math.BigInteger;

public interface DSAPublicKey extends DSAKey, java.security.PublicKey {
  @Deprecated
  long serialVersionUID = 1234526332779022332L;

  BigInteger getY();

  @Override
  default DSAParams getParams() {
    return null;
  }
}
