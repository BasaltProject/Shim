/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public interface OpenMBeanAttributeInfo extends javax.management.openmbean.OpenMBeanParameterInfo {
  boolean equals(java.lang.Object object);
  int hashCode();
  boolean isIs();
  boolean isReadable();
  boolean isWritable();
  java.lang.String toString();
}
