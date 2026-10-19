/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public abstract class Image {
  protected float accelerationPriority = .5f;

  public void flush() { }

  public void setAccelerationPriority(float priority) {
    if (priority < 0 || priority > 1) {
      throw new IllegalArgumentException("Priority must be a value between 0 and 1, inclusive");
    }
    accelerationPriority = priority;
  }

  public float getAccelerationPriority() {
    return accelerationPriority;
  }
}
