/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;

public class HotSpotDiagnosticImpl implements HotSpotDiagnosticMXBean {
  @Override
  public void dumpHeap(String outputFile, boolean live) throws IOException {
    if (outputFile == null) {
      throw new NullPointerException("outputFile cannot be null");
    }
    if (!outputFile.endsWith(".hprof")) {
      throw new IllegalArgumentException("heap dump file must have .hprof extension");
    }
    throw new UnsupportedOperationException("heap dumps are not supported");
  }

  @Override
  public List<VMOption> getDiagnosticOptions() {
    return new ArrayList<VMOption>();
  }

  @Override
  public VMOption getVMOption(String name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (name.equals("MaxHeapSize")) {
      return new VMOption(name, Long.toString(Runtime.getRuntime().maxMemory()), false, VMOption.Origin.DEFAULT);
    }
    throw new IllegalArgumentException("VM option \"" + name + "\" does not exist");
  }

  @Override
  public void setVMOption(String name, String value) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    if (value == null) {
      throw new NullPointerException("value cannot be null");
    }
    throw new IllegalArgumentException("VM option \"" + name + "\" does not exist or is not writeable");
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("com.sun.management:type=HotSpotDiagnostic");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
