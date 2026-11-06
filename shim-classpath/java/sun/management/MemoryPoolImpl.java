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
  private final String name;
  private final MemoryType type;
  private final String[] managers;

  public MemoryPoolImpl() {
    this("G1 Eden Space", MemoryType.HEAP, "G1 Young Generation", "G1 Old Generation");
  }

  public MemoryPoolImpl(String name, MemoryType type, String... managers) {
    this.name = name;
    this.type = type;
    this.managers = managers;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public MemoryType getType() {
    return type;
  }

  @Override
  public MemoryUsage getUsage() {
    return type == MemoryType.HEAP ? new MemoryImpl().getHeapMemoryUsage() : new MemoryImpl().getNonHeapMemoryUsage();
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
    return managers.clone();
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
      return new ObjectName("java.lang:type=MemoryPool,name=" + ObjectName.quote(name).replace("\"", ""));
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
