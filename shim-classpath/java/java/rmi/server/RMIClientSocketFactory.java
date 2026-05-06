/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public interface RMIClientSocketFactory {
  java.net.Socket createSocket(java.lang.String string, int i) throws java.io.IOException;
}
