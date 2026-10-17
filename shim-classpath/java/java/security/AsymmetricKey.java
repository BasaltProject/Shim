/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.security.spec.AlgorithmParameterSpec;

public interface AsymmetricKey extends Key, DEREncodable {
  default AlgorithmParameterSpec getParams() {
    return null;
  }
}
