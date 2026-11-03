/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public class ImageCapabilities implements Cloneable {
  private boolean accelerated = false;

  public ImageCapabilities(boolean accelerated) { this.accelerated = accelerated; }

  public boolean isAccelerated() { return accelerated; }

  public boolean isTrueVolatile() { return false; }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
