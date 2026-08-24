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
import javax.management.MBeanServer;
import javax.management.remote.rmi.RMIConnectorServer;

public class JMXConnectorServerFactory {
  public static final String DEFAULT_CLASS_LOADER = "jmx.remote.default.class.loader";
  public static final String DEFAULT_CLASS_LOADER_NAME = "jmx.remote.default.class.loader.name";
  public static final String PROTOCOL_PROVIDER_PACKAGES = "jmx.remote.protocol.provider.pkgs";
  public static final String PROTOCOL_PROVIDER_CLASS_LOADER = "jmx.remote.protocol.provider.class.loader";

  private JMXConnectorServerFactory() {
  }

  public static JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL, Map<String, ?> environment,
      MBeanServer mbeanServer) throws IOException {
    Map<String, Object> envcopy;
    if (environment == null) {
      envcopy = new HashMap<String, Object>();
    } else {
      envcopy = new HashMap<String, Object>(environment);
    }
    if (serviceURL == null) {
      throw new NullPointerException("Null JMXServiceURL");
    }
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    for (JMXConnectorServerProvider p : ServiceLoader.load(JMXConnectorServerProvider.class, cl)) {
      JMXConnectorServer s = p.newJMXConnectorServer(serviceURL, envcopy, mbeanServer);
      if (s != null) {
        return s;
      }
    }
    String protocol = serviceURL.getProtocol();
    if (protocol.equals("rmi")) {
      return new RMIConnectorServer(serviceURL, envcopy, mbeanServer);
    }
    throw new MalformedURLException("Unsupported protocol: " + protocol);
  }
}
