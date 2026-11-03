/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

public interface FlavorTable extends java.awt.datatransfer.FlavorMap {
  java.util.List<java.lang.String> getNativesForFlavor(java.awt.datatransfer.DataFlavor p0);
  java.util.List<java.awt.datatransfer.DataFlavor> getFlavorsForNative(java.lang.String p0);
}
