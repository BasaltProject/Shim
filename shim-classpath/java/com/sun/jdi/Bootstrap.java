/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public class Bootstrap {
  public Bootstrap() {
  }

  private static com.sun.jdi.VirtualMachineManager manager;

  public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager() {
    if (manager == null) {
      manager = new shim.jdi.Connectors.Manager();
    }
    return manager;
  }
}
