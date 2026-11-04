/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

public class GarbageCollectionNotificationInfo implements javax.management.openmbean.CompositeDataView {
  public static final java.lang.String GARBAGE_COLLECTION_NOTIFICATION = "com.sun.management.gc.notification";

  public GarbageCollectionNotificationInfo(java.lang.String string, java.lang.String string2, java.lang.String string3, com.sun.management.GcInfo gcInfo) {
  }

  public static com.sun.management.GarbageCollectionNotificationInfo from(javax.management.openmbean.CompositeData compositeData) {
    throw new UnsupportedOperationException();
  }
  public java.lang.String getGcAction() {
    throw new UnsupportedOperationException();
  }
  public java.lang.String getGcCause() {
    throw new UnsupportedOperationException();
  }
  public com.sun.management.GcInfo getGcInfo() {
    throw new UnsupportedOperationException();
  }
  public java.lang.String getGcName() {
    throw new UnsupportedOperationException();
  }
  public javax.management.openmbean.CompositeData toCompositeData(javax.management.openmbean.CompositeType compositeType) {
    throw new UnsupportedOperationException();
  }
}
