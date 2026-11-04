/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.tools.attach.spi;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.AttachPermission;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public abstract class AttachProvider {
  private static final Object lock = new Object();
  private static List<AttachProvider> providers = null;

  protected AttachProvider() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(new AttachPermission("createAttachProvider"));
    }
  }

  public abstract String name();

  public abstract String type();

  public abstract VirtualMachine attachVirtualMachine(String id) throws AttachNotSupportedException, IOException;

  public VirtualMachine attachVirtualMachine(VirtualMachineDescriptor vmd) throws AttachNotSupportedException,
      IOException {
    if (vmd.provider() != this) {
      throw new AttachNotSupportedException("provider mismatch");
    }
    return attachVirtualMachine(vmd.id());
  }

  public abstract List<VirtualMachineDescriptor> listVirtualMachines();

  public static List<AttachProvider> providers() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(new AttachPermission("getAttachProviders"));
    }
    synchronized (lock) {
      if (providers == null) {
        providers = new ArrayList<AttachProvider>();
        Iterator<AttachProvider> i = ServiceLoader.load(AttachProvider.class, ClassLoader.getSystemClassLoader())
            .iterator();
        while (true) {
          try {
            if (!i.hasNext()) {
              break;
            }
            providers.add(i.next());
          } catch (ServiceConfigurationError e) {
            Throwable cause = e.getCause();
            if (!(cause instanceof SecurityException)) {
              throw e;
            }
          }
        }
        boolean haveDefault = false;
        for (AttachProvider p : providers) {
          if (p.getClass().getName().equals("sun.tools.attach.AttachProviderImpl")) {
            haveDefault = true;
          }
        }
        if (!haveDefault) {
          providers.add(new sun.tools.attach.AttachProviderImpl());
        }
      }
      return java.util.Collections.unmodifiableList(providers);
    }
  }
}
