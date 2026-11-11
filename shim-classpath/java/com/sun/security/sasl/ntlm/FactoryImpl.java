/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl.ntlm;

import com.sun.security.sasl.SaslUtil;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

public final class FactoryImpl implements SaslClientFactory, SaslServerFactory {
  private static final int kPolicy = SaslUtil.NOPLAINTEXT | SaslUtil.NOANONYMOUS;

  public FactoryImpl() {
  }

  @Override
  public SaslClient createSaslClient(String[] mechs, String authorizationId, String protocol, String serverName,
      Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    for (String m : mechs) {
      if (m.equals("NTLM") && SaslUtil.policyMatches(kPolicy, props)) {
        if (cbh == null) {
          throw new SaslException("Callback handler with support for RealmCallback, NameCallback, and PasswordCallback required");
        }
        return new NTLMClient(serverName, props, cbh);
      }
    }
    return null;
  }

  @Override
  public SaslServer createSaslServer(String mech, String protocol, String serverName, Map<String, ?> props,
      CallbackHandler cbh) throws SaslException {
    if (!mech.equals("NTLM") || !SaslUtil.policyMatches(kPolicy, props)) {
      return null;
    }
    if (cbh == null) {
      throw new SaslException("Callback handler with support for RealmCallback, NameCallback, and PasswordCallback required");
    }
    return new NTLMServer(serverName, props, cbh);
  }

  @Override
  public String[] getMechanismNames(Map<String, ?> props) {
    return SaslUtil.policyMatches(kPolicy, props) ? new String[] {"NTLM"} : new String[0];
  }
}
