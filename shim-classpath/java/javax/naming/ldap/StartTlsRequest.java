/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.util.ServiceLoader;
import javax.naming.ConfigurationException;
import javax.naming.NamingException;

public class StartTlsRequest implements ExtendedRequest {
  private static final long serialVersionUID = 4441679576360753397L;

  public static final String OID = "1.3.6.1.4.1.1466.20037";

  public StartTlsRequest() {
  }

  public String getID() {
    return OID;
  }

  public byte[] getEncodedValue() {
    return null;
  }

  public ExtendedResponse createExtendedResponse(String id, byte[] berValue, int offset, int length)
      throws NamingException {
    if (id != null && !id.equals(OID)) {
      throw new ConfigurationException("Start TLS received the following response instead of " + OID + ": " + id);
    }
    for (StartTlsResponse resp : ServiceLoader.load(StartTlsResponse.class)) {
      if (resp != null) {
        return resp;
      }
    }
    throw new ConfigurationException("Provider for javax.naming.ldap.StartTlsResponse cannot be found");
  }
}
