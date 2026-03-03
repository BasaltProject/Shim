/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public enum NestingKind {
  TOP_LEVEL, MEMBER, LOCAL, ANONYMOUS;

  public boolean isNested() {
    return this != TOP_LEVEL;
  }
}
