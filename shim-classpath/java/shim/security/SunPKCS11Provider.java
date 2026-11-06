/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;

public final class SunPKCS11Provider extends Provider {
  private static final long serialVersionUID = 1L;

  public SunPKCS11Provider() {
    super("SunPKCS11", "25", "Unconfigured and unusable PKCS11 provider");
  }
}
