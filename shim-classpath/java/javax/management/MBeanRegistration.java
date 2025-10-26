/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface MBeanRegistration {
  ObjectName preRegister(MBeanServer p0, ObjectName p1) throws Exception;

  void postRegister(Boolean p0);

  void preDeregister() throws Exception;

  void postDeregister();
}
