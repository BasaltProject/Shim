/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.Set;

public interface AlgorithmConstraints {
  boolean permits(Set<CryptoPrimitive> primitives, String algorithm, AlgorithmParameters parameters);

  boolean permits(Set<CryptoPrimitive> primitives, Key key);

  boolean permits(Set<CryptoPrimitive> primitives, String algorithm, Key key, AlgorithmParameters parameters);
}
