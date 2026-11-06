/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;

public final class SunJGSSProvider extends Provider {
  private static final long serialVersionUID = 1L;

  public SunJGSSProvider() {
    super("SunJGSS", "25", "Sun (Kerberos v5, SPNEGO)");
  }
}
