/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.spec;

import java.security.spec.PSSParameterSpec;

public final class RSAPSSParameterSpec implements SignatureMethodParameterSpec {
  private final PSSParameterSpec spec;

  public RSAPSSParameterSpec(PSSParameterSpec spec) {
    this.spec = spec;
  }

  public PSSParameterSpec getPSSParameterSpec() {
    return spec;
  }
}
