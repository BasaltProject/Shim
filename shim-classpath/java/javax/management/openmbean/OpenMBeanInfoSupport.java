/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public class OpenMBeanInfoSupport extends javax.management.MBeanInfo implements javax.management.openmbean.OpenMBeanInfo {
  private static final long serialVersionUID = 4349395935420511492L;


  public OpenMBeanInfoSupport(java.lang.String string, java.lang.String string2, javax.management.openmbean.OpenMBeanAttributeInfo[] openMBeanAttributeInfos, javax.management.openmbean.OpenMBeanConstructorInfo[] openMBeanConstructorInfos, javax.management.openmbean.OpenMBeanOperationInfo[] openMBeanOperationInfos, javax.management.MBeanNotificationInfo[] mBeanNotificationInfos) {
    super(string, string2, (javax.management.MBeanAttributeInfo[]) null, (javax.management.MBeanConstructorInfo[]) null, (javax.management.MBeanOperationInfo[]) null, mBeanNotificationInfos);
  }
  public OpenMBeanInfoSupport(java.lang.String string, java.lang.String string2, javax.management.openmbean.OpenMBeanAttributeInfo[] openMBeanAttributeInfos, javax.management.openmbean.OpenMBeanConstructorInfo[] openMBeanConstructorInfos, javax.management.openmbean.OpenMBeanOperationInfo[] openMBeanOperationInfos, javax.management.MBeanNotificationInfo[] mBeanNotificationInfos, javax.management.Descriptor descriptor) {
    super(string, string2, (javax.management.MBeanAttributeInfo[]) null, (javax.management.MBeanConstructorInfo[]) null, (javax.management.MBeanOperationInfo[]) null, mBeanNotificationInfos);
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
