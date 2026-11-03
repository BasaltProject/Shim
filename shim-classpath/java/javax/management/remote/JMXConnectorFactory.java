/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import javax.management.remote.rmi.RMIConnector;

public class JMXConnectorFactory {
  public static final String DEFAULT_CLASS_LOADER = "jmx.remote.default.class.loader";
  public static final String PROTOCOL_PROVIDER_PACKAGES = "jmx.remote.protocol.provider.pkgs";
  public static final String PROTOCOL_PROVIDER_CLASS_LOADER = "jmx.remote.protocol.provider.class.loader";

  private static final String PROTOCOL_PROVIDER_DEFAULT_PACKAGE = "com.sun.jmx.remote.protocol";

  private JMXConnectorFactory() {
  }

  public static JMXConnector connect(JMXServiceURL serviceURL) throws IOException {
    return connect(serviceURL, null);
  }

  public static JMXConnector connect(JMXServiceURL serviceURL, Map<String, ?> environment) throws IOException {
    if (serviceURL == null) {
      throw new NullPointerException("Null JMXServiceURL");
    }
    if (environment == null) {
      environment = new HashMap<String, Object>();
    }
    JMXConnector conn = newJMXConnector(serviceURL, environment);
    conn.connect(environment);
    return conn;
  }

  public static JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map<String, ?> environment)
      throws IOException {
    Map<String, Object> envcopy;
    if (environment == null) {
      envcopy = new HashMap<String, Object>();
    } else {
      envcopy = new HashMap<String, Object>(environment);
    }
    if (serviceURL == null) {
      throw new NullPointerException("Null JMXServiceURL");
    }
    String protocol = serviceURL.getProtocol();
    String providerClassName = "ClientProvider";
    JMXServiceURL providerURL = serviceURL;
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    for (JMXConnectorProvider p : ServiceLoader.load(JMXConnectorProvider.class, cl)) {
      JMXConnector c = p.newJMXConnector(serviceURL, envcopy);
      if (c != null) {
        return c;
      }
    }
    String pkgs = (String) envcopy.get(PROTOCOL_PROVIDER_PACKAGES);
    if (pkgs != null) {
      for (String pkg : pkgs.split("\\|")) {
        pkg = pkg.trim();
        if (pkg.length() == 0) {
          continue;
        }
        String cn = pkg + "." + protocol.replace('-', '_') + "." + providerClassName;
        try {
          Class<?> c = Class.forName(cn, true, cl);
          JMXConnectorProvider p = (JMXConnectorProvider) c.getDeclaredConstructor().newInstance();
          JMXConnector conn = p.newJMXConnector(serviceURL, envcopy);
          if (conn != null) {
            return conn;
          }
        } catch (ReflectiveOperationException e) {
        }
      }
    }
    if (protocol.equals("rmi")) {
      return new RMIConnector(serviceURL, envcopy);
    }
    throw new MalformedURLException("Unsupported protocol: " + protocol);
  }
}
