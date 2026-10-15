/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

import java.util.Objects;

public class SwitchPoint {
  private volatile boolean invalidated = false;

  public SwitchPoint() {
  }

  public boolean hasBeenInvalidated() {
    return invalidated;
  }

  public MethodHandle guardWithTest(MethodHandle target, MethodHandle fallback) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(fallback, "fallback");
    if (!target.type().equals(fallback.type())) {
      throw new IllegalArgumentException("target and fallback types must match: "
          + target.type() + " vs " + fallback.type());
    }
    return new MethodHandle.SwitchPointMethodHandle(this, target, fallback);
  }

  public static void invalidateAll(SwitchPoint[] switchPoints) {
    Objects.requireNonNull(switchPoints, "switchPoints");
    for (SwitchPoint sp : switchPoints) {
      if (sp != null) {
        sp.invalidated = true;
      }
    }
  }
}
