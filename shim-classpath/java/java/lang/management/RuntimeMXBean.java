/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

public interface RuntimeMXBean extends PlatformManagedObject {
  default long getPid() {
    throw new UnsupportedOperationException();
  }

  String getName();

  String getVmName();

  String getVmVendor();

  String getVmVersion();

  String getSpecName();

  String getSpecVendor();

  String getSpecVersion();

  String getManagementSpecVersion();

  String getClassPath();

  String getLibraryPath();

  boolean isBootClassPathSupported();

  String getBootClassPath();

  java.util.List<String> getInputArguments();

  long getUptime();

  long getStartTime();

  java.util.Map<String, String> getSystemProperties();
}
