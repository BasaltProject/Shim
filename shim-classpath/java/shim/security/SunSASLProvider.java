/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;

public final class SunSASLProvider extends Provider {
  private static final long serialVersionUID = 1L;

  public SunSASLProvider() {
    super("SunSASL", "25", "Sun SASL provider(implements client mechanisms for: DIGEST-MD5, EXTERNAL, PLAIN, CRAM-MD5, NTLM; server mechanisms for: DIGEST-MD5, CRAM-MD5, NTLM)");
  }
}
