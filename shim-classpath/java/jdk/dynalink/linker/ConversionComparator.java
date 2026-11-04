/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

public interface ConversionComparator {
  jdk.dynalink.linker.ConversionComparator.Comparison compareConversion(java.lang.Class<?> p0, java.lang.Class<?> p1, java.lang.Class<?> p2);

  public enum Comparison {
    INDETERMINATE, TYPE_1_BETTER, TYPE_2_BETTER;


  }
}
