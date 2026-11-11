/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.url.dns;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

public final class dnsURLContextFactory implements ObjectFactory {
  @Override
  public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx, Hashtable<?, ?> env)
      throws NamingException {
    if (urlInfo == null) {
      return new dnsURLContext(env);
    }
    if (urlInfo instanceof String) {
      return new dnsURLContext(env).lookup((String) urlInfo);
    }
    throw new IllegalArgumentException("dnsURLContextFactory.getObjectInstance: argument must be a DNS URL String");
  }
}
