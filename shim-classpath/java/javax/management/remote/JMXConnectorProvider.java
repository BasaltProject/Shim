/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

public interface JMXConnectorProvider {
  javax.management.remote.JMXConnector newJMXConnector(javax.management.remote.JMXServiceURL jMXServiceURL, java.util.Map<java.lang.String, ?> map) throws java.io.IOException;
}
