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
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;
import javax.security.sasl.SaslException;

public final class ClientFactoryImpl implements SaslClientFactory {
  private static final String[] myMechs = {"EXTERNAL", "CRAM-MD5", "PLAIN"};
  private static final int[] mechPolicies = {
      SaslUtil.NOPLAINTEXT | SaslUtil.NOACTIVE | SaslUtil.NODICTIONARY | SaslUtil.NOANONYMOUS,
      SaslUtil.NOPLAINTEXT | SaslUtil.NOANONYMOUS,
      SaslUtil.NOANONYMOUS};

  public ClientFactoryImpl() {
  }

  public SaslClient createSaslClient(String[] mechs, String authorizationId, String protocol, String serverName,
      Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    for (int i = 0; i < mechs.length; i++) {
      if (mechs[i].equals(myMechs[0]) && SaslUtil.policyMatches(mechPolicies[0], props)) {
        return new ExternalClient(authorizationId);
      } else if (mechs[i].equals(myMechs[2]) && SaslUtil.policyMatches(mechPolicies[2], props)) {
        return new PlainClient(authorizationId, cbh);
      } else if (mechs[i].equals(myMechs[1]) && SaslUtil.policyMatches(mechPolicies[1], props)) {
        return new CramMD5Client(cbh);
      }
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
