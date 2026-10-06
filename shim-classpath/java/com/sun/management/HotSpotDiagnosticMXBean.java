/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

import java.io.IOException;
import java.lang.management.PlatformManagedObject;
import java.util.List;

public interface HotSpotDiagnosticMXBean extends PlatformManagedObject {
  void dumpHeap(String outputFile, boolean live) throws IOException;

  List<VMOption> getDiagnosticOptions();

  VMOption getVMOption(String name);

  void setVMOption(String name, String value);

  enum ThreadDumpFormat {
    TEXT_PLAIN,
    JSON,
  }

  default void dumpThreads(String outputFile, ThreadDumpFormat format) throws IOException {
    throw new UnsupportedOperationException();
  }
}
