/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface Accessible {
  boolean isPackagePrivate();
  boolean isPrivate();
  boolean isProtected();
  boolean isPublic();
  int modifiers();
}
