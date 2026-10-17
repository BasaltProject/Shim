/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.management;

import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;

public class RuntimeImpl implements RuntimeMXBean {
  private static final long kStart = System.currentTimeMillis();

  @Override
  public String getName() {
    String host;
    try {
      host = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {
      host = "localhost";
    }
    return ProcessHandle.current().pid() + "@" + host;
  }

  @Override
  public String getVmName() {
    return System.getProperty("java.vm.name");
  }

  @Override
  public String getVmVendor() {
    return System.getProperty("java.vm.vendor");
  }

  @Override
  public String getVmVersion() {
    return System.getProperty("java.vm.version");
  }

  @Override
  public String getSpecName() {
    return System.getProperty("java.vm.specification.name");
  }

  @Override
  public String getSpecVendor() {
    return System.getProperty("java.vm.specification.vendor");
  }

  @Override
  public String getSpecVersion() {
    return System.getProperty("java.vm.specification.version");
  }

  @Override
  public String getManagementSpecVersion() {
    return "4.0";
  }

  @Override
  public String getClassPath() {
    return System.getProperty("java.class.path");
  }

  @Override
  public String getLibraryPath() {
    return System.getProperty("java.library.path");
  }

  @Override
  public boolean isBootClassPathSupported() {
    return false;
  }

  @Override
  public String getBootClassPath() {
    throw new UnsupportedOperationException("Boot class path mechanism is not supported");
  }

  @Override
  public List<String> getInputArguments() {
    return Collections.unmodifiableList(new ArrayList<String>());
  }

  @Override
  public long getUptime() {
    return Math.max(0, System.currentTimeMillis() - kStart);
  }

  @Override
  public long getStartTime() {
    return kStart;
  }

  @Override
  public Map<String, String> getSystemProperties() {
    Map<String, String> map = new HashMap<String, String>();
    java.util.Properties props = System.getProperties();
    for (String key : props.stringPropertyNames()) {
      map.put(key, props.getProperty(key));
    }
    return Collections.unmodifiableMap(map);
  }

  @Override
  public long getPid() {
    return ProcessHandle.current().pid();
  }

  @Override
  public ObjectName getObjectName() {
    try {
      return new ObjectName("java.lang:type=Runtime");
    } catch (javax.management.MalformedObjectNameException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
