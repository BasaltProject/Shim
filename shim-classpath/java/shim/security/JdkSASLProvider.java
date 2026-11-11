/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;

public final class JdkSASLProvider extends Provider {
  private static final long serialVersionUID = 1L;

  public JdkSASLProvider() {
    super("JdkSASL", "25", "JDK SASL provider(implements client and server mechanisms for GSSAPI)");
    put("SaslClientFactory.GSSAPI", "com.sun.security.sasl.gsskerb.FactoryImpl");
    put("SaslServerFactory.GSSAPI", "com.sun.security.sasl.gsskerb.FactoryImpl");
  }
}
