/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.sasl.gsskerb;

import com.sun.security.sasl.SaslUtil;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

public final class FactoryImpl implements SaslClientFactory, SaslServerFactory {
  private static final int kPolicy = SaslUtil.NOPLAINTEXT | SaslUtil.NOACTIVE | SaslUtil.NOANONYMOUS
      | SaslUtil.PASS_CREDENTIALS;

  public FactoryImpl() {
  }

  public SaslClient createSaslClient(String[] mechs, String authorizationId, String protocol, String serverName,
      Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    for (String m : mechs) {
      if (m.equals("GSSAPI") && SaslUtil.policyMatches(kPolicy, props)) {
        return new GssKrb5Client(authorizationId, protocol, serverName, props, cbh);
      }
    }
    return null;
  }

  public SaslServer createSaslServer(String mech, String protocol, String serverName, Map<String, ?> props,
      CallbackHandler cbh) throws SaslException {
    if (mech.equals("GSSAPI") && SaslUtil.policyMatches(kPolicy, props)) {
      if (cbh == null) {
        throw new SaslException("Callback handler with support for AuthorizeCallback required");
      }
      return new GssKrb5Server(protocol, serverName, props, cbh);
    }
    return null;
  }

  public String[] getMechanismNames(Map<String, ?> props) {
    return SaslUtil.policyMatches(kPolicy, props) ? new String[] {"GSSAPI"} : new String[0];
  }
}
