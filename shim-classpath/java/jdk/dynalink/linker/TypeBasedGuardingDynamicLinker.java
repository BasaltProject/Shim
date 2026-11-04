/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

public interface TypeBasedGuardingDynamicLinker extends jdk.dynalink.linker.GuardingDynamicLinker {
  boolean canLinkType(java.lang.Class<?> p0);
}
