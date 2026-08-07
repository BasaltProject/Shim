/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.event;

public interface IIOReadWarningListener extends java.util.EventListener {
  void warningOccurred(javax.imageio.ImageReader p0, java.lang.String p1);
}
