/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.BufferPoolMXBean;
import javax.management.ObjectName;

public class BufferPoolImpl implements BufferPoolMXBean {
  private final String name;

  BufferPoolImpl(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getCount() {
    return 0;
  }

  @Override
  public long getTotalCapacity() {
    return 0;
  }

  @Override
  public long getMemoryUsed() {
    return 0;
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.nio:type=BufferPool,name=" + name);
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
