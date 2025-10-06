/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.interfaces;

import java.math.BigInteger;

public interface DHPrivateKey extends DHKey, java.security.PrivateKey {
  @Deprecated
  long serialVersionUID = 2211791113380396553L;

  BigInteger getX();

  @Override
  default javax.crypto.spec.DHParameterSpec getParams() {
    return null;
  }
}
