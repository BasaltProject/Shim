/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.url.ldap;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

public class ldapURLContextFactory implements ObjectFactory {
  @Override
  public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx, Hashtable<?, ?> env)
      throws NamingException {
    if (urlInfo == null) {
      return new ldapURLContext(env);
    }
    if (urlInfo instanceof String) {
      return new ldapURLContext(env).lookup((String) urlInfo);
    }
    throw new IllegalArgumentException("ldapURLContextFactory.getObjectInstance: argument must be a LDAP URL String");
  }
}
