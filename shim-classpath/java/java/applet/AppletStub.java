/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.applet;

public interface AppletStub {
  boolean isActive();
  java.lang.String getParameter(java.lang.String p0);
  java.net.URL getDocumentBase();
  java.net.URL getCodeBase();
  java.applet.AppletContext getAppletContext();
  void appletResize(int p0, int p1);
}
