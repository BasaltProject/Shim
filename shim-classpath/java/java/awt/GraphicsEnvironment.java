/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.image.BufferedImage;
import java.util.Locale;

public abstract class GraphicsEnvironment {
  private static GraphicsEnvironment localEnv;
  private static Boolean headless;

  protected GraphicsEnvironment() { }

  public static synchronized GraphicsEnvironment getLocalGraphicsEnvironment() {
    if (localEnv == null) {
      localEnv = new shim.awt.HeadlessGraphicsEnvironment();
    }
    return localEnv;
  }

  public static boolean isHeadless() {
    return true;
  }

  static void checkHeadless() throws HeadlessException {
    throw new HeadlessException();
  }

  public boolean isHeadlessInstance() {
    return true;
  }

  public abstract GraphicsDevice[] getScreenDevices() throws HeadlessException;

  public abstract GraphicsDevice getDefaultScreenDevice() throws HeadlessException;

  public abstract Graphics2D createGraphics(BufferedImage img);

  public abstract Font[] getAllFonts();

  public abstract String[] getAvailableFontFamilyNames();

  public abstract String[] getAvailableFontFamilyNames(Locale l);

  public boolean registerFont(Font font) {
    if (font == null) {
      throw new NullPointerException("font cannot be null.");
    }
    return false;
  }

  public void preferLocaleFonts() { }

  public void preferProportionalFonts() { }

  public Point getCenterPoint() throws HeadlessException {
    throw new HeadlessException();
  }

  public Rectangle getMaximumWindowBounds() throws HeadlessException {
    throw new HeadlessException();
  }
}
