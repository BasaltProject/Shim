/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface VirtualMachineManager {
  java.util.List<com.sun.jdi.connect.Connector> allConnectors();
  java.util.List<com.sun.jdi.connect.AttachingConnector> attachingConnectors();
  java.util.List<com.sun.jdi.VirtualMachine> connectedVirtualMachines();
  com.sun.jdi.VirtualMachine createVirtualMachine(com.sun.jdi.connect.spi.Connection connection, java.lang.Process process) throws java.io.IOException;
  com.sun.jdi.VirtualMachine createVirtualMachine(com.sun.jdi.connect.spi.Connection connection) throws java.io.IOException;
  com.sun.jdi.connect.LaunchingConnector defaultConnector();
  java.util.List<com.sun.jdi.connect.LaunchingConnector> launchingConnectors();
  java.util.List<com.sun.jdi.connect.ListeningConnector> listeningConnectors();
  int majorInterfaceVersion();
  int minorInterfaceVersion();
}
