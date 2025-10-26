/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface PersistentMBean {
  void load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;

  void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;
}
