/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.CompilationMXBean;
import javax.management.ObjectName;

public class CompilationImpl implements CompilationMXBean {
  @Override
  public String getName() {
    return "Shim JIT Compiler";
  }

  @Override
  public boolean isCompilationTimeMonitoringSupported() {
    return true;
  }

  @Override
  public long getTotalCompilationTime() {
    return 0;
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=Compilation");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
