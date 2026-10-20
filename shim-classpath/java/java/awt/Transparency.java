/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

public interface Transparency {
  int OPAQUE = 1;
  int BITMASK = 2;
  int TRANSLUCENT = 3;

  int getTransparency();
}
