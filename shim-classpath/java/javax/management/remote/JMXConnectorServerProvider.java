/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

public interface JMXConnectorServerProvider {
  javax.management.remote.JMXConnectorServer newJMXConnectorServer(javax.management.remote.JMXServiceURL jMXServiceURL, java.util.Map<java.lang.String, ?> map, javax.management.MBeanServer mBeanServer) throws java.io.IOException;
}
