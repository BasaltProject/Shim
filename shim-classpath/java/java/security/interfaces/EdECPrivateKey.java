/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

public interface EdECPrivateKey extends EdECKey, java.security.PrivateKey {
  java.util.Optional<byte[]> getBytes();

  @Override
  default java.security.spec.NamedParameterSpec getParams() {
    return null;
  }
}
