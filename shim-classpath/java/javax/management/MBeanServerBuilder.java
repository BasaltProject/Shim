/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import shim.management.MBeanServerImpl;

public class MBeanServerBuilder {
  public MBeanServerBuilder() {
  }

  public MBeanServerDelegate newMBeanServerDelegate() {
    return new MBeanServerDelegate();
  }

  public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate) {
    return new MBeanServerImpl(defaultDomain, outer == null ? null : outer, delegate);
  }
}
