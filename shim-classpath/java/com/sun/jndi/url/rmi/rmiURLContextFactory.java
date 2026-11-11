/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.url.rmi;

import com.sun.jndi.rmi.registry.RegistryContextFactory;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

public final class rmiURLContextFactory implements ObjectFactory {
  @Override
  public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx, Hashtable<?, ?> env)
      throws NamingException {
    if (urlInfo == null) {
      return new rmiURLContext(env);
    }
    if (urlInfo instanceof String) {
      return new rmiURLContext(env).lookup((String) urlInfo);
    }
    if (urlInfo instanceof String[]) {
      String[] urls = (String[]) urlInfo;
      NamingException last = null;
      for (String u : urls) {
        try {
          return new rmiURLContext(env).lookup(u);
        } catch (NamingException e) {
          last = e;
        }
      }
      throw last == null ? new javax.naming.ConfigurationException("empty URL array") : last;
    }
    throw new IllegalArgumentException("rmiURLContextFactory.getObjectInstance: argument must be an RMI URL String or an array of them");
  }

  static Class<?> factoryClass() {
    return RegistryContextFactory.class;
  }
}
