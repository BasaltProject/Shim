/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.interfaces;

import java.math.BigInteger;

public interface DHPublicKey extends DHKey, java.security.PublicKey {
  @Deprecated
  long serialVersionUID = -6628103563352519193L;

  BigInteger getY();

  @Override
  default javax.crypto.spec.DHParameterSpec getParams() {
    return null;
  }
}
