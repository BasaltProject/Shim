/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public interface CertPathChecker {
  void init(boolean forward) throws CertPathValidatorException;

  boolean isForwardCheckingSupported();

  void check(Certificate cert) throws CertPathValidatorException;
}
