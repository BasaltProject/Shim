/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.util.Locale;

public final class HeadlessGraphicsEnvironment extends GraphicsEnvironment {
  public GraphicsDevice[] getScreenDevices() throws HeadlessException {
    throw new HeadlessException();
  }

  public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
    throw new HeadlessException();
  }

  public Graphics2D createGraphics(BufferedImage img) {
    if (img == null) {
      throw new NullPointerException("BufferedImage cannot be null");
    }
    return new SoftwareGraphics2D(img);
  }

  public Font[] getAllFonts() {
    String[] families = getAvailableFontFamilyNames();
    Font[] out = new Font[families.length * 4];
    int k = 0;
    for (String f : families) {
      for (int style = 0; style < 4; style++) {
        out[k++] = new Font(f, style, 1);
      }
    }
    return out;
  }

  public String[] getAvailableFontFamilyNames() {
    return Fonts.families();
  }

  public String[] getAvailableFontFamilyNames(Locale l) {
    return Fonts.families();
  }

  public boolean registerFont(Font font) {
    if (font == null) {
      throw new NullPointerException("font cannot be null.");
    }
    return Fonts.isKnown(font.getName());
  }
}
