/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.GarbageCollectorMXBean;
import javax.management.ObjectName;

public class GarbageCollectorImpl implements com.sun.management.GarbageCollectorMXBean {
  @Override
  public com.sun.management.GcInfo getLastGcInfo() {
    return null;
  }

  @Override
  public long getCollectionCount() {
    return -1;
  }

  @Override
  public long getCollectionTime() {
    return -1;
  }

  @Override
  public String getName() {
    return "Shim Collector";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public String[] getMemoryPoolNames() {
    return new String[] {"Shim Heap"};
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=GarbageCollector,name=Shim Collector");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
