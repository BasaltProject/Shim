/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;

public final class KerberosPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = -7374788026156829911L;

  public static final int KRB_NT_UNKNOWN = 0;
  public static final int KRB_NT_PRINCIPAL = 1;
  public static final int KRB_NT_SRV_INST = 2;
  public static final int KRB_NT_SRV_HST = 3;
  public static final int KRB_NT_SRV_XHST = 4;
  public static final int KRB_NT_UID = 5;
  public static final int KRB_NT_ENTERPRISE = 10;

  private transient String fullName;
  private transient String realm;
  private transient int nameType;

  public KerberosPrincipal(String name) {
    this(name, KRB_NT_PRINCIPAL);
  }

  public KerberosPrincipal(String name, int nameType) {
    if (name == null) {
      throw new IllegalArgumentException("Name must not be null");
    }
    if (nameType < 0 || (nameType > 5 && nameType != 10)) {
      throw new IllegalArgumentException("Illegal name type");
    }
    int at = -1;
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == '\\') {
        i++;
      } else if (c == '@') {
        if (at < 0) {
          at = i;
        }
      }
    }
    String r;
    String n;
    if (at >= 0) {
      n = name.substring(0, at);
      r = name.substring(at + 1);
      if (r.length() == 0) {
        throw new IllegalArgumentException("empty realm part not allowed");
      }
    } else {
      n = name;
      try {
        r = sun.security.krb5.Config.getInstance().defaultRealm();
      } catch (sun.security.krb5.KrbException e) {
        throw new IllegalArgumentException("Cannot find default realm");
      }
    }
    this.realm = r;
    this.fullName = n + "@" + r;
    this.nameType = nameType;
  }

  public String getRealm() {
    return realm;
  }

  public int hashCode() {
    return getName().hashCode();
  }

  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof KerberosPrincipal)) {
      return false;
    }
    String myFullName = getName();
    String otherFullName = ((KerberosPrincipal) other).getName();
    return myFullName.equals(otherFullName);
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.writeObject(fullName);
    oos.writeObject(realm);
    oos.writeInt(nameType);
  }

  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    fullName = (String) ois.readObject();
    realm = (String) ois.readObject();
    nameType = ois.readInt();
  }

  public String getName() {
    return fullName;
  }

  public int getNameType() {
    return nameType;
  }

  public String toString() {
    return getName();
  }
}
