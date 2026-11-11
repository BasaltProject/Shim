/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.rmi.registry;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public final class RegistryContextFactory implements InitialContextFactory {
  public static final String ADDRESS_TYPE = "URL";

  @Override
  public Context getInitialContext(Hashtable<?, ?> env) throws NamingException {
    if (env != null) {
      env = (Hashtable<?, ?>) env.clone();
    }
    return URLToContext.getContext(env == null ? null : (String) env.get(Context.PROVIDER_URL), env);
  }
}
