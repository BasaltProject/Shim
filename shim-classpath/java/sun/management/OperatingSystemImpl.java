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
import com.sun.management.UnixOperatingSystemMXBean;
import java.io.File;
import javax.management.ObjectName;

public class OperatingSystemImpl implements UnixOperatingSystemMXBean {
  private long lastTotal = -1;
  private long lastIdle = -1;
  private long lastProcessTicks = -1;
  private long lastProcessWall = -1;

  private static long meminfo(String key) {
    try (BufferedReader in = new BufferedReader(new FileReader("/proc/meminfo"))) {
      String line;
      while ((line = in.readLine()) != null) {
        if (line.startsWith(key + ":")) {
          return Long.parseLong(line.substring(key.length() + 1).trim().split(" ")[0]) * 1024;
        }
      }
    } catch (Exception e) {
      return -1;
    }
    return -1;
  }

  private static long processTicks() {
    try (BufferedReader in = new BufferedReader(new FileReader("/proc/self/stat"))) {
      String line = in.readLine();
      String[] f = line.substring(line.lastIndexOf(')') + 2).split(" ");
      return Long.parseLong(f[11]) + Long.parseLong(f[12]);
    } catch (Exception e) {
      return -1;
    }
  }

  @Override
  public long getCommittedVirtualMemorySize() {
    try (BufferedReader in = new BufferedReader(new FileReader("/proc/self/statm"))) {
      return Long.parseLong(in.readLine().split(" ")[0]) * 4096;
    } catch (Exception e) {
      return -1;
    }
  }

  @Override
  public long getTotalSwapSpaceSize() {
    return meminfo("SwapTotal");
  }

  @Override
  public long getFreeSwapSpaceSize() {
    return meminfo("SwapFree");
  }

  @Override
  public long getProcessCpuTime() {
    long ticks = processTicks();
    return ticks < 0 ? -1 : ticks * 10000000L;
  }

  @Override
  public long getFreeMemorySize() {
    return meminfo("MemFree");
  }

  @Override
  public long getTotalMemorySize() {
    return meminfo("MemTotal");
  }

  @Override
  public synchronized double getCpuLoad() {
    try (BufferedReader in = new BufferedReader(new FileReader("/proc/stat"))) {
      String[] f = in.readLine().trim().split("\\s+");
      long total = 0;
      for (int i = 1; i < f.length; i++) {
        total += Long.parseLong(f[i]);
      }
      long idle = Long.parseLong(f[4]) + Long.parseLong(f[5]);
      double load = -1;
      if (lastTotal >= 0 && total > lastTotal) {
        load = 1.0 - (double) (idle - lastIdle) / (double) (total - lastTotal);
      }
      lastTotal = total;
      lastIdle = idle;
      return load;
    } catch (Exception e) {
      return -1;
    }
  }

  @Override
  public synchronized double getProcessCpuLoad() {
    long ticks = processTicks();
    long wall = System.nanoTime();
    double load = -1;
    if (ticks >= 0 && lastProcessTicks >= 0 && wall > lastProcessWall) {
      double cpu = (ticks - lastProcessTicks) * 1.0e7;
      load = Math.min(1.0, cpu / ((wall - lastProcessWall) * (double) getAvailableProcessors()));
    }
    lastProcessTicks = ticks;
    lastProcessWall = wall;
    return load;
  }

  @Override
  public long getOpenFileDescriptorCount() {
    String[] names = new File("/proc/self/fd").list();
    return names == null ? -1 : names.length;
  }

  @Override
  public long getMaxFileDescriptorCount() {
    try (BufferedReader in = new BufferedReader(new FileReader("/proc/self/limits"))) {
      String line;
      while ((line = in.readLine()) != null) {
        if (line.startsWith("Max open files")) {
          String v = line.substring("Max open files".length()).trim().split("\\s+")[0];
          return v.equals("unlimited") ? Long.MAX_VALUE : Long.parseLong(v);
        }
      }
    } catch (Exception e) {
      return -1;
    }
    return -1;
  }

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
