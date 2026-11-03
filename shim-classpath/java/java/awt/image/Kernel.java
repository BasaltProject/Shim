/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.image;

public class Kernel implements Cloneable {
  private int width;
  private int height;
  private int xOrigin;
  private int yOrigin;
  private float[] data;

  public Kernel(int width, int height, float[] data) {
    this.width = width;
    this.height = height;
    this.xOrigin = (width - 1) >> 1;
    this.yOrigin = (height - 1) >> 1;
    int len = width * height;
    if (data.length < len) {
      throw new IllegalArgumentException("Data array too small (is " + data.length + " and should be " + len);
    }
    this.data = new float[len];
    System.arraycopy(data, 0, this.data, 0, len);
  }

  public final int getXOrigin() { return xOrigin; }

  public final int getYOrigin() { return yOrigin; }

  public final int getWidth() { return width; }

  public final int getHeight() { return height; }

  public final float[] getKernelData(float[] kernelData) {
    if (kernelData == null) {
      kernelData = new float[data.length];
    } else if (kernelData.length < data.length) {
      throw new IllegalArgumentException("Data array too small (should be " + data.length + " but is " + kernelData.length + ")");
    }
    System.arraycopy(data, 0, kernelData, 0, data.length);
    return kernelData;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
}
