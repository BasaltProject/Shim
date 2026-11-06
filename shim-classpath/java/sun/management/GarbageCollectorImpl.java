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
  private final String name;
  private final int kind;

  public GarbageCollectorImpl() {
    this("G1 Young Generation", 0);
  }

  public GarbageCollectorImpl(String name, int kind) {
    this.name = name;
    this.kind = kind;
  }

  private static native long stat(int index);

  @Override
  public com.sun.management.GcInfo getLastGcInfo() {
    return null;
  }

  @Override
  public long getCollectionCount() {
    return stat(kind * 2);
  }

  @Override
  public long getCollectionTime() {
    return stat(kind * 2 + 1) / 1000000L;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public String[] getMemoryPoolNames() {
    return kind == 0 ? new String[] {"G1 Eden Space", "G1 Survivor Space", "G1 Old Gen"} : new String[] {"G1 Eden Space", "G1 Survivor Space", "G1 Old Gen", "Metaspace"};
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=GarbageCollector,name=" + name);
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
