/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.management.OperatingSystemMXBean;
import javax.management.ObjectName;

public class OperatingSystemImpl implements OperatingSystemMXBean {
  @Override
  public String getName() {
    return System.getProperty("os.name");
  }

  @Override
  public String getArch() {
    return System.getProperty("os.arch");
  }

  @Override
  public String getVersion() {
    return System.getProperty("os.version");
  }

  @Override
  public int getAvailableProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }

  @Override
  public double getSystemLoadAverage() {
    try (BufferedReader in = new BufferedReader(new FileReader("/proc/loadavg"))) {
      String line = in.readLine();
      if (line != null) {
        return Double.parseDouble(line.split(" ")[0]);
      }
    } catch (Exception e) {
      return -1;
    }
    return -1;
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=OperatingSystem");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
