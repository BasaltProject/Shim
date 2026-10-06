/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

public final class LdapPrincipal implements Principal, java.io.Serializable {
  private static final long serialVersionUID = 6820120005580754861L;

  private final String nameString;
  private final LdapName name;

  public LdapPrincipal(String name) throws InvalidNameException {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = new LdapName(name);
    nameString = name;
  }

  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object instanceof LdapPrincipal) {
      return name.equals(((LdapPrincipal) object).name);
    }
    return false;
  }

  public int hashCode() {
    return name.hashCode();
  }

  public String getName() {
    return nameString;
  }

  public String toString() {
    return name.toString();
  }
}
