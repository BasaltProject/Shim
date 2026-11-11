/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.applet;

@Deprecated(since = "9", forRemoval = true)
public interface AppletContext {
  void showStatus(java.lang.String p0);
  java.awt.Image getImage(java.net.URL p0);
  java.applet.AudioClip getAudioClip(java.net.URL p0);
  java.applet.Applet getApplet(java.lang.String p0);
  java.util.Enumeration<java.applet.Applet> getApplets();
  void showDocument(java.net.URL p0, java.lang.String p1);
  void showDocument(java.net.URL p0);
  void setStream(java.lang.String p0, java.io.InputStream p1) throws java.io.IOException;
  java.io.InputStream getStream(java.lang.String p0);
  java.util.Iterator<java.lang.String> getStreamKeys();
}
