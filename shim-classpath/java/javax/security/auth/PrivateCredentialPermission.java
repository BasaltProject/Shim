/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.StringTokenizer;

public final class PrivateCredentialPermission extends Permission {
  private static final long serialVersionUID = 5284372143517237068L;

  private static final class CredOwner implements java.io.Serializable {
    private static final long serialVersionUID = -5607449830436408266L;

    final String principalClass;
    final String principalName;

    CredOwner(String principalClass, String principalName) {
      this.principalClass = principalClass;
      this.principalName = principalName;
    }

    boolean implies(Object obj) {
      if (!(obj instanceof CredOwner)) {
        return false;
      }
      CredOwner that = (CredOwner) obj;
      if (principalClass.equals("*") || principalClass.equals(that.principalClass)) {
        return principalName.equals("*") || principalName.equals(that.principalName);
      }
      return false;
    }

    @Override
    public String toString() {
      return "CredOwner:\n\tPrincipal Class = " + principalClass + "\n\tPrincipal Name = " + principalName;
    }
  }

  private String credentialClass;
  private CredOwner[] credOwners;

  public PrivateCredentialPermission(String name, String actions) {
    super(name);
    if (!"read".equalsIgnoreCase(actions)) {
      throw new IllegalArgumentException("actions can only be 'read'");
    }
    init(name);
  }

  public String getCredentialClass() {
    return credentialClass;
  }

  public String[][] getPrincipals() {
    String[][] result = new String[credOwners.length][2];
    for (int i = 0; i < credOwners.length; i++) {
      result[i][0] = credOwners[i].principalClass;
      result[i][1] = credOwners[i].principalName;
    }
    return result;
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof PrivateCredentialPermission)) {
      return false;
    }
    PrivateCredentialPermission that = (PrivateCredentialPermission) p;
    if (!impliesCredentialClass(credentialClass, that.credentialClass)) {
      return false;
    }
    return impliesPrincipalSet(credOwners, that.credOwners);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof PrivateCredentialPermission)) {
      return false;
    }
    PrivateCredentialPermission that = (PrivateCredentialPermission) obj;
    return implies(that) && that.implies(this);
  }

  @Override
  public int hashCode() {
    return credentialClass.hashCode();
  }

  @Override
  public String getActions() {
    return "read";
  }

  @Override
  public PermissionCollection newPermissionCollection() {
    return null;
  }

  private void init(String name) {
    if (name == null || name.trim().length() == 0) {
      throw new IllegalArgumentException("invalid empty name");
    }
    ArrayList<CredOwner> owners = new ArrayList<CredOwner>();
    StringTokenizer tokenizer = new StringTokenizer(name, " ", true);
    String principalClass;
    String principalName;
    credentialClass = tokenizer.nextToken();
    if (!tokenizer.hasMoreTokens()) {
      throw syntax(name, "Credential Class not followed by a Principal Class and Name");
    }
    tokenizer.nextToken();
    while (tokenizer.hasMoreTokens()) {
      principalClass = tokenizer.nextToken();
      if (!tokenizer.hasMoreTokens()) {
        throw syntax(name, "Principal Class not followed by a Principal Name");
      }
      tokenizer.nextToken();
      principalName = tokenizer.nextToken();
      if (!principalName.startsWith("\"")) {
        throw syntax(name, "Principal Name must be surrounded by quotes");
      }
      if (!principalName.endsWith("\"")) {
        StringBuilder sb = new StringBuilder(principalName);
        while (tokenizer.hasMoreTokens()) {
          sb.append(tokenizer.nextToken());
          if (sb.toString().endsWith("\"")) {
            break;
          }
        }
        principalName = sb.toString();
        if (!principalName.endsWith("\"")) {
          throw syntax(name, "Principal Name missing end quote");
        }
      }
      if (principalClass.equals("*") && !principalName.equals("\"*\"")) {
        throw new IllegalArgumentException("PrivateCredentialPermission Principal Class can not be a wildcard (*) "
            + "value if Principal Name is not a wildcard (*) value");
      }
      if (tokenizer.hasMoreTokens()) {
        tokenizer.nextToken();
      }
      owners.add(new CredOwner(principalClass, principalName.substring(1, principalName.length() - 1)));
    }
    credOwners = owners.toArray(new CredOwner[0]);
  }

  private static IllegalArgumentException syntax(String name, String detail) {
    return new IllegalArgumentException("permission name [" + name + "] syntax invalid: " + detail);
  }

  private boolean impliesCredentialClass(String thisC, String thatC) {
    if (thisC == null || thatC == null) {
      return false;
    }
    return thisC.equals("*") || thisC.equals(thatC);
  }

  private boolean impliesPrincipalSet(CredOwner[] thisP, CredOwner[] thatP) {
    if (thisP == null || thatP == null) {
      return false;
    }
    if (thatP.length == 0) {
      return true;
    }
    if (thisP.length == 0) {
      return false;
    }
    for (CredOwner mine : thisP) {
      boolean found = false;
      for (CredOwner theirs : thatP) {
        if (mine.implies(theirs)) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }
}
