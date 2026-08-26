/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.ietf.jgss;

public interface GSSName {
  org.ietf.jgss.Oid NT_HOSTBASED_SERVICE = Oid.getInstance("1.2.840.113554.1.2.1.4");
  org.ietf.jgss.Oid NT_USER_NAME = Oid.getInstance("1.2.840.113554.1.2.1.1");
  org.ietf.jgss.Oid NT_MACHINE_UID_NAME = Oid.getInstance("1.2.840.113554.1.2.1.2");
  org.ietf.jgss.Oid NT_STRING_UID_NAME = Oid.getInstance("1.2.840.113554.1.2.1.3");
  org.ietf.jgss.Oid NT_ANONYMOUS = Oid.getInstance("1.3.6.1.5.6.3");
  org.ietf.jgss.Oid NT_EXPORT_NAME = Oid.getInstance("1.3.6.1.5.6.4");

  org.ietf.jgss.GSSName canonicalize(org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  boolean equals(java.lang.Object object);
  boolean equals(org.ietf.jgss.GSSName gSSName) throws org.ietf.jgss.GSSException;
  byte[] export() throws org.ietf.jgss.GSSException;
  org.ietf.jgss.Oid getStringNameType() throws org.ietf.jgss.GSSException;
  int hashCode();
  boolean isAnonymous();
  boolean isMN();
  java.lang.String toString();
}
