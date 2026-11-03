/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

public interface FlavorMap {
  java.util.Map<java.awt.datatransfer.DataFlavor, java.lang.String> getNativesForFlavors(java.awt.datatransfer.DataFlavor[] p0);
  java.util.Map<java.lang.String, java.awt.datatransfer.DataFlavor> getFlavorsForNatives(java.lang.String[] p0);
}
