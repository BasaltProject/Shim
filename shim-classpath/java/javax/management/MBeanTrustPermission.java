/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.security.BasicPermission;

public class MBeanTrustPermission extends BasicPermission {
  private static final long serialVersionUID = -2952178077029018140L;

  public MBeanTrustPermission(String name) {
    this(name, null);
  }

  public MBeanTrustPermission(String name, String actions) {
    super(name, actions);
    if (name == null) {
      throw new NullPointerException("MBeanTrustPermission: target name can't be null");
    }
    if (actions != null && actions.length() > 0) {
      throw new IllegalArgumentException("MBeanTrustPermission actions must be null: " + actions);
    }
    if (!name.equals("register") && !name.equals("*")) {
      throw new IllegalArgumentException("MBeanTrustPermission: Unknown target name [" + name + "]");
    }
  }
}
