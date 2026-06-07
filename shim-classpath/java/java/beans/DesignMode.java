/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

public interface DesignMode {
  java.lang.String PROPERTYNAME = "designTime";
  void setDesignTime(boolean p0);
  boolean isDesignTime();
}
