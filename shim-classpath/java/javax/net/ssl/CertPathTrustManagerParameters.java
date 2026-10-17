/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.cert.CertPathParameters;

public class CertPathTrustManagerParameters implements ManagerFactoryParameters {
  private final CertPathParameters parameters;

  public CertPathTrustManagerParameters(CertPathParameters parameters) {
    this.parameters = (CertPathParameters) parameters.clone();
  }

  public CertPathParameters getParameters() {
    return (CertPathParameters) parameters.clone();
  }
}
