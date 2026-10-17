/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.ClassLoadingMXBean;
import javax.management.ObjectName;

public class ClassLoadingImpl implements ClassLoadingMXBean {
  private volatile boolean verbose;

  @Override
  public long getTotalLoadedClassCount() {
    return getLoadedClassCount();
  }

  @Override
  public int getLoadedClassCount() {
    return 1;
  }

  @Override
  public long getUnloadedClassCount() {
    return 0;
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public void setVerbose(boolean value) {
    verbose = value;
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=ClassLoading");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
