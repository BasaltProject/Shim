/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans;

public interface AppletInitializer {
  void initialize(java.applet.Applet p0, java.beans.beancontext.BeanContext p1);
  void activate(java.applet.Applet p0);
}
