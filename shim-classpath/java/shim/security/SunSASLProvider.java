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
    put("SaslClientFactory.DIGEST-MD5", "com.sun.security.sasl.digest.FactoryImpl");
    put("SaslClientFactory.NTLM", "com.sun.security.sasl.ntlm.FactoryImpl");
    put("SaslClientFactory.CRAM-MD5", "com.sun.security.sasl.ClientFactoryImpl");
    put("SaslClientFactory.PLAIN", "com.sun.security.sasl.ClientFactoryImpl");
    put("SaslClientFactory.EXTERNAL", "com.sun.security.sasl.ClientFactoryImpl");
    put("SaslServerFactory.DIGEST-MD5", "com.sun.security.sasl.digest.FactoryImpl");
    put("SaslServerFactory.NTLM", "com.sun.security.sasl.ntlm.FactoryImpl");
    put("SaslServerFactory.CRAM-MD5", "com.sun.security.sasl.ServerFactoryImpl");
  }
}
