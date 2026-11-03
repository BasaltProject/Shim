/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public class OpenMBeanConstructorInfoSupport extends javax.management.MBeanConstructorInfo implements javax.management.openmbean.OpenMBeanConstructorInfo {
  private static final long serialVersionUID = -4400441579007477003L;


  public OpenMBeanConstructorInfoSupport(java.lang.String string, java.lang.String string2, javax.management.openmbean.OpenMBeanParameterInfo[] openMBeanParameterInfos) {
    super(string, (java.lang.reflect.Constructor) null);
  }
  public OpenMBeanConstructorInfoSupport(java.lang.String string, java.lang.String string2, javax.management.openmbean.OpenMBeanParameterInfo[] openMBeanParameterInfos, javax.management.Descriptor descriptor) {
    super(string, (java.lang.reflect.Constructor) null);
  }

  public boolean equals(java.lang.Object object) {
    throw new UnsupportedOperationException();
  }
  public int hashCode() {
    throw new UnsupportedOperationException();
  }
  public java.lang.String toString() {
    throw new UnsupportedOperationException();
  }
}
