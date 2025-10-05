/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

public interface XECPrivateKey extends XECKey, java.security.PrivateKey {
  java.util.Optional<byte[]> getScalar();

  @Override
  default java.security.spec.AlgorithmParameterSpec getParams() {
    return null;
  }
}
