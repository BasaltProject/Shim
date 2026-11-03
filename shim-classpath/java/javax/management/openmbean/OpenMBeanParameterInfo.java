/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public interface OpenMBeanParameterInfo {
  boolean equals(java.lang.Object object);
  java.lang.Object getDefaultValue();
  java.lang.String getDescription();
  java.util.Set<?> getLegalValues();
  java.lang.Comparable<?> getMaxValue();
  java.lang.Comparable<?> getMinValue();
  java.lang.String getName();
  javax.management.openmbean.OpenType<?> getOpenType();
  boolean hasDefaultValue();
  boolean hasLegalValues();
  boolean hasMaxValue();
  boolean hasMinValue();
  int hashCode();
  boolean isValue(java.lang.Object object);
  java.lang.String toString();
}
