/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.applet;

@Deprecated(since = "9", forRemoval = true)
public class Applet extends java.awt.Panel {
  public Applet() throws java.awt.HeadlessException {
    throw new java.awt.HeadlessException();
  }
  public void init() { throw new UnsupportedOperationException(); }
  public void start() { throw new UnsupportedOperationException(); }
  public void stop() { throw new UnsupportedOperationException(); }
  public void destroy() { throw new UnsupportedOperationException(); }
  public boolean isActive() { throw new UnsupportedOperationException(); }
  public void resize(java.awt.Dimension p0) { throw new UnsupportedOperationException(); }
  public void resize(int p0, int p1) { throw new UnsupportedOperationException(); }
  public java.util.Locale getLocale() { throw new UnsupportedOperationException(); }
  public java.lang.String getParameter(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public java.net.URL getDocumentBase() { throw new UnsupportedOperationException(); }
  public java.net.URL getCodeBase() { throw new UnsupportedOperationException(); }
  public java.applet.AppletContext getAppletContext() { throw new UnsupportedOperationException(); }
  public void showStatus(java.lang.String p0) { throw new UnsupportedOperationException(); }
  public java.awt.Image getImage(java.net.URL p0, java.lang.String p1) { throw new UnsupportedOperationException(); }
  public java.awt.Image getImage(java.net.URL p0) { throw new UnsupportedOperationException(); }
  public java.applet.AudioClip getAudioClip(java.net.URL p0) { throw new UnsupportedOperationException(); }
  public java.applet.AudioClip getAudioClip(java.net.URL p0, java.lang.String p1) { throw new UnsupportedOperationException(); }
  public void play(java.net.URL p0, java.lang.String p1) { throw new UnsupportedOperationException(); }
  public void play(java.net.URL p0) { throw new UnsupportedOperationException(); }
  public final void setStub(java.applet.AppletStub p0) { throw new UnsupportedOperationException(); }
  public boolean isValidateRoot() { throw new UnsupportedOperationException(); }
  public static final java.applet.AudioClip newAudioClip(java.net.URL p0) { throw new UnsupportedOperationException(); }
  public java.lang.String getAppletInfo() { throw new UnsupportedOperationException(); }
  public java.lang.String[][] getParameterInfo() { throw new UnsupportedOperationException(); }
  public javax.accessibility.AccessibleContext getAccessibleContext() { throw new UnsupportedOperationException(); }
}
