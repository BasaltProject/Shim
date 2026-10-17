/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.MemoryManagerMXBean;
import javax.management.ObjectName;

public class MemoryManagerImpl implements MemoryManagerMXBean {
  private final String name;

  MemoryManagerImpl(String name) {
    this.name = name;
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
    return new String[] {"Shim Heap"};
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=MemoryManager,name=" + name);
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
