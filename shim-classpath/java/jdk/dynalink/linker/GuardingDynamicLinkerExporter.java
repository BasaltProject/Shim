/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

public abstract class GuardingDynamicLinkerExporter implements java.util.function.Supplier<java.util.List<jdk.dynalink.linker.GuardingDynamicLinker>> {
  public static final java.lang.String AUTOLOAD_PERMISSION_NAME = "dynalink.exportLinkersAutomatically";

  protected GuardingDynamicLinkerExporter() {
  }

}
