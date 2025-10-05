/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.interfaces;

public interface DSAKeyPairGenerator {
  void initialize(DSAParams params, java.security.SecureRandom random) throws java.security.InvalidParameterException;

  void initialize(int modlen, boolean genParams, java.security.SecureRandom random)
      throws java.security.InvalidParameterException;
}
