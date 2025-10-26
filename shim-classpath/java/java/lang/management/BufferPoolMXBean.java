/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface BufferPoolMXBean extends PlatformManagedObject {
  String getName();

  long getCount();

  long getTotalCapacity();

  long getMemoryUsed();
}
