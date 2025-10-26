/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import javax.management.ObjectName;

public class MemoryPoolImpl implements MemoryPoolMXBean {
  @Override
  public String getName() {
    return "Shim Heap";
  }

  @Override
  public MemoryType getType() {
    return MemoryType.HEAP;
  }

  @Override
  public MemoryUsage getUsage() {
    return new MemoryImpl().getHeapMemoryUsage();
  }

  @Override
  public MemoryUsage getPeakUsage() {
    return getUsage();
  }

  @Override
  public void resetPeakUsage() {
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public String[] getMemoryManagerNames() {
    return new String[] {"Shim Collector"};
  }

  @Override
  public long getUsageThreshold() {
    throw new UnsupportedOperationException("Usage threshold is not supported");
  }

  @Override
  public void setUsageThreshold(long threshold) {
    throw new UnsupportedOperationException("Usage threshold is not supported");
  }

  @Override
  public boolean isUsageThresholdExceeded() {
    throw new UnsupportedOperationException("Usage threshold is not supported");
  }

  @Override
  public long getUsageThresholdCount() {
    throw new UnsupportedOperationException("Usage threshold is not supported");
  }

  @Override
  public boolean isUsageThresholdSupported() {
    return false;
  }

  @Override
  public long getCollectionUsageThreshold() {
    throw new UnsupportedOperationException("CollectionUsage threshold is not supported");
  }

  @Override
  public void setCollectionUsageThreshold(long threshold) {
    throw new UnsupportedOperationException("CollectionUsage threshold is not supported");
  }

  @Override
  public boolean isCollectionUsageThresholdExceeded() {
    throw new UnsupportedOperationException("CollectionUsage threshold is not supported");
  }

  @Override
  public long getCollectionUsageThresholdCount() {
    throw new UnsupportedOperationException("CollectionUsage threshold is not supported");
  }

  @Override
  public MemoryUsage getCollectionUsage() {
    return null;
  }

  @Override
  public boolean isCollectionUsageThresholdSupported() {
    return false;
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=MemoryPool,name=Shim Heap");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
