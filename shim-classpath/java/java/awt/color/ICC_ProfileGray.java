/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.color;

public final class ICC_ProfileGray extends ICC_Profile {
  ICC_ProfileGray(byte[] data, int kind) {
    super(data, kind);
  }

  public float[] getMediaWhitePoint() {
    return new float[] {0.9642f, 1.0f, 0.8249f};
  }

  public float getGamma() {
    return 1.0f;
  }

  public short[] getTRC() {
    return new short[] {(short) 0, (short) 0xffff};
  }
}
