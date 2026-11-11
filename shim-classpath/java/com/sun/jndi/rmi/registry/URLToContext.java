/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.rmi.registry;

import java.rmi.registry.Registry;
import java.util.Hashtable;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;

final class URLToContext {
  private URLToContext() {
  }

  static Context getContext(String url, Hashtable<?, ?> env) throws NamingException {
    String host = null;
    int port = -1;
    if (url != null) {
      String rest = url;
      int scheme = rest.indexOf("://");
      if (scheme >= 0) {
        if (!rest.substring(0, scheme).equalsIgnoreCase("rmi")) {
          throw new ConfigurationException("Invalid URL scheme: " + url);
        }
        rest = rest.substring(scheme + 3);
      }
      int slash = rest.indexOf('/');
      if (slash >= 0) {
        rest = rest.substring(0, slash);
      }
      int colon = rest.lastIndexOf(':');
      if (colon >= 0 && rest.indexOf(']') < colon) {
        try {
          port = Integer.parseInt(rest.substring(colon + 1));
        } catch (NumberFormatException e) {
          throw new ConfigurationException("Invalid port in URL: " + url);
        }
        rest = rest.substring(0, colon);
      }
      host = rest.isEmpty() ? null : rest;
    }
    if (port < 0) {
      port = Registry.REGISTRY_PORT;
    }
    return new RegistryContext(host, port, env);
  }
}
