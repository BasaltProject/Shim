/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.util.ArrayList;
import javax.management.loading.ClassLoaderRepository;

public class MBeanServerFactory {
  private static final ArrayList<MBeanServer> mBeanServerList = new ArrayList<MBeanServer>();

  private MBeanServerFactory() {
  }

  public static void releaseMBeanServer(MBeanServer mbeanServer) {
    synchronized (mBeanServerList) {
      if (!mBeanServerList.remove(mbeanServer)) {
        throw new IllegalArgumentException("MBeanServer was not in list!");
      }
    }
  }

  public static MBeanServer createMBeanServer() {
    return createMBeanServer(null);
  }

  public static MBeanServer createMBeanServer(String domain) {
    MBeanServer mbeanServer = newMBeanServer(domain);
    synchronized (mBeanServerList) {
      mBeanServerList.add(mbeanServer);
    }
    return mbeanServer;
  }

  public static MBeanServer newMBeanServer() {
    return newMBeanServer(null);
  }

  public static MBeanServer newMBeanServer(String domain) {
    MBeanServerBuilder builder = new MBeanServerBuilder();
    MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
    MBeanServer server = builder.newMBeanServer(domain, null, delegate);
    if (server == null) {
      throw new JMRuntimeException("MBeanServerBuilder.newMBeanServer() returned null");
    }
    return server;
  }

  public static synchronized ArrayList<MBeanServer> findMBeanServer(String agentId) {
    ArrayList<MBeanServer> result = new ArrayList<MBeanServer>();
    synchronized (mBeanServerList) {
      for (MBeanServer server : mBeanServerList) {
        if (agentId == null) {
          result.add(server);
          continue;
        }
        try {
          String id = (String) server.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId");
          if (agentId.equals(id)) {
            result.add(server);
          }
        } catch (Exception e) {
          continue;
        }
      }
    }
    return result;
  }

  public static ClassLoaderRepository getClassLoaderRepository(MBeanServer server) {
    return server.getClassLoaderRepository();
  }
}
