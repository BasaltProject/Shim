/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

public interface ECPublicKey extends java.security.PublicKey, ECKey {
  @Deprecated
  long serialVersionUID = -3314988629879632826L;

  java.security.spec.ECPoint getW();

  @Override
  default java.security.spec.ECParameterSpec getParams() {
    return null;
  }
}
