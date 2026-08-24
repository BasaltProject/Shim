/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public class OpenMBeanOperationInfoSupport extends javax.management.MBeanOperationInfo implements javax.management.openmbean.OpenMBeanOperationInfo {
  private static final long serialVersionUID = 4996859732565369366L;


  public OpenMBeanOperationInfoSupport(java.lang.String string, java.lang.String string2, javax.management.openmbean.OpenMBeanParameterInfo[] openMBeanParameterInfos, javax.management.openmbean.OpenType<?> openType, int i) {
    super(string, (java.lang.reflect.Method) null);
  }
  public OpenMBeanOperationInfoSupport(java.lang.String string, java.lang.String string2, javax.management.openmbean.OpenMBeanParameterInfo[] openMBeanParameterInfos, javax.management.openmbean.OpenType<?> openType, int i, javax.management.Descriptor descriptor) {
    super(string, (java.lang.reflect.Method) null);
  }

  public boolean equals(java.lang.Object object) {
    throw new UnsupportedOperationException();
  }
  public javax.management.openmbean.OpenType<?> getReturnOpenType() {
    throw new UnsupportedOperationException();
  }
  public int hashCode() {
    throw new UnsupportedOperationException();
  }
  public java.lang.String toString() {
    throw new UnsupportedOperationException();
  }
}
