/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect;

public interface AttachingConnector extends com.sun.jdi.connect.Connector {
  com.sun.jdi.VirtualMachine attach(java.util.Map<java.lang.String, ? extends com.sun.jdi.connect.Connector.Argument> map) throws java.io.IOException, com.sun.jdi.connect.IllegalConnectorArgumentsException;
}
