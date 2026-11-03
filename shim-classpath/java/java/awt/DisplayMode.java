/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public final class DisplayMode {
  private Dimension size;
  private int bitDepth;
  private int refreshRate;

  public static final int BIT_DEPTH_MULTI = -1;
  public static final int REFRESH_RATE_UNKNOWN = 0;

  public DisplayMode(int width, int height, int bitDepth, int refreshRate) {
    this.size = new Dimension(width, height);
    this.bitDepth = bitDepth;
    this.refreshRate = refreshRate;
  }

  public int getHeight() { return size.height; }

  public int getWidth() { return size.width; }

  public int getBitDepth() { return bitDepth; }

  public int getRefreshRate() { return refreshRate; }

  public boolean equals(DisplayMode dm) {
    if (dm == null) {
      return false;
    }
    return (getHeight() == dm.getHeight() && getWidth() == dm.getWidth() && getBitDepth() == dm.getBitDepth()
        && getRefreshRate() == dm.getRefreshRate());
  }

  public boolean equals(Object dm) {
    if (dm instanceof DisplayMode) {
      return equals((DisplayMode) dm);
    } else {
      return false;
    }
  }

  public int hashCode() { return getWidth() + getHeight() + getBitDepth() * 7 + getRefreshRate() * 13; }

  public String toString() {
    return getWidth() + "x" + getHeight() + "x" + (getBitDepth() == BIT_DEPTH_MULTI ? "[Multi depth]"
        : getBitDepth() + "bpp") + "@" + (getRefreshRate() == REFRESH_RATE_UNKNOWN ? "[Unknown refresh rate]"
        : getRefreshRate() + "Hz");
  }
}
