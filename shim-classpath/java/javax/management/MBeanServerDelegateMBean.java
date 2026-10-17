/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface MBeanServerDelegateMBean {
  String getMBeanServerId();

  String getSpecificationName();

  String getSpecificationVersion();

  String getSpecificationVendor();

  String getImplementationName();

  String getImplementationVersion();

  String getImplementationVendor();
}
