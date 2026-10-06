/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

public final class ServerFactoryImpl implements SaslServerFactory {
  private static final String[] myMechs = {"CRAM-MD5"};
  private static final int[] mechPolicies = {SaslUtil.NOPLAINTEXT | SaslUtil.NOANONYMOUS};

  public ServerFactoryImpl() {
  }

  public SaslServer createSaslServer(String mech, String protocol, String serverName, Map<String, ?> props,
      CallbackHandler cbh) throws SaslException {
    if (mech.equals(myMechs[0]) && SaslUtil.policyMatches(mechPolicies[0], props)) {
      if (cbh == null) {
        throw new SaslException("Callback handler with support for AuthorizeCallback required");
      }
      return new CramMD5Server(protocol, serverName, props, cbh);
    }
    return null;
  }

  public String[] getMechanismNames(Map<String, ?> props) {
    java.util.List<String> out = new java.util.ArrayList<String>();
    for (int i = 0; i < myMechs.length; i++) {
      if (SaslUtil.policyMatches(mechPolicies[i], props)) {
        out.add(myMechs[i]);
      }
    }
    return out.toArray(new String[0]);
  }
}
