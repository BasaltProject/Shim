/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public interface OpenMBeanOperationInfo {
  boolean equals(java.lang.Object object);
  java.lang.String getDescription();
  int getImpact();
  java.lang.String getName();
  javax.management.openmbean.OpenType<?> getReturnOpenType();
  java.lang.String getReturnType();
  javax.management.MBeanParameterInfo[] getSignature();
  int hashCode();
  java.lang.String toString();
}
