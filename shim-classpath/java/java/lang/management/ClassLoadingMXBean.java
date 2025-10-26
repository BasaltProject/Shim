/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface ClassLoadingMXBean extends PlatformManagedObject {
  long getTotalLoadedClassCount();

  int getLoadedClassCount();

  long getUnloadedClassCount();

  boolean isVerbose();

  void setVerbose(boolean p0);
}
