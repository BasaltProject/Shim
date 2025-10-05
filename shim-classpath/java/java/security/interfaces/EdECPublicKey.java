/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

public interface EdECPublicKey extends EdECKey, java.security.PublicKey {
  java.security.spec.EdECPoint getPoint();

  @Override
  default java.security.spec.NamedParameterSpec getParams() {
    return null;
  }
}
