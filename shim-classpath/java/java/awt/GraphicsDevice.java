/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class GraphicsDevice {
  public static final int TYPE_RASTER_SCREEN = 0;
  public static final int TYPE_PRINTER = 1;
  public static final int TYPE_IMAGE_BUFFER = 2;
  protected GraphicsDevice() {
  }
  public abstract int getType();
  public java.awt.GraphicsConfiguration getBestConfiguration(java.awt.GraphicsConfigTemplate gct) {
    java.awt.GraphicsConfiguration[] configs = getConfigurations();
    return gct.getBestConfiguration(configs);
  }
  public abstract java.awt.GraphicsConfiguration[] getConfigurations();
  private java.awt.Window fullScreenWindow;
  public void setFullScreenWindow(java.awt.Window w) {
    if (w != null) {
      w.setBounds(getDefaultConfiguration().getBounds());
    }
    fullScreenWindow = w;
  }
  public java.awt.DisplayMode getDisplayMode() {
    java.awt.GraphicsConfiguration gc = getDefaultConfiguration();
    java.awt.Rectangle r = gc.getBounds();
    java.awt.image.ColorModel cm = gc.getColorModel();
    return new java.awt.DisplayMode(r.width, r.height, cm.getPixelSize(), 0);
  }
  public boolean isFullScreenSupported() { return false; }
  public boolean isDisplayChangeSupported() { return false; }
  public void setDisplayMode(java.awt.DisplayMode dm) {
    throw new UnsupportedOperationException("Cannot change display mode");
  }
  public java.awt.DisplayMode[] getDisplayModes() { return new java.awt.DisplayMode[] {getDisplayMode()}; }
  public int getAvailableAcceleratedMemory() { return -1; }
  public abstract java.lang.String getIDstring();
  public abstract java.awt.GraphicsConfiguration getDefaultConfiguration();
  public java.awt.Window getFullScreenWindow() { return fullScreenWindow; }
  public boolean isWindowTranslucencySupported(java.awt.GraphicsDevice.WindowTranslucency translucencyKind) {
    return false;
  }

  public enum WindowTranslucency {
    PERPIXEL_TRANSPARENT, TRANSLUCENT, PERPIXEL_TRANSLUCENT;
  }
}
