/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.color;

public final class ICC_ProfileRGB extends ICC_Profile {
  public static final int REDCOMPONENT = 0;
  public static final int GREENCOMPONENT = 1;
  public static final int BLUECOMPONENT = 2;

  ICC_ProfileRGB(byte[] data, int kind) {
    super(data, kind);
  }

  public float[] getMediaWhitePoint() {
    return new float[] {0.9642f, 1.0f, 0.8249f};
  }

  public float[][] getMatrix() {
    return new float[][] {{0.43607f, 0.38515f, 0.14307f}, {0.22249f, 0.71687f, 0.06061f}, {0.01392f, 0.09708f, 0.71410f}};
  }

  public float getGamma(int component) {
    if (component < 0 || component > 2) {
      throw new IllegalArgumentException("Must be RED, GREEN, or BLUE");
    }
    return kind == kLinearRgb ? 1.0f : 2.2f;
  }

  public short[] getTRC(int component) {
    if (component < 0 || component > 2) {
      throw new IllegalArgumentException("Must be RED, GREEN, or BLUE");
    }
    if (kind == kLinearRgb) {
      return new short[] {(short) 0, (short) 0xffff};
    }
    short[] curve = new short[256];
    for (int i = 0; i < 256; i++) {
      double v = i / 255.0;
      double lin = v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
      curve[i] = (short) Math.round(lin * 65535);
    }
    return curve;
  }
}
