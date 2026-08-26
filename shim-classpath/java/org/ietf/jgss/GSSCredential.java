/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.ietf.jgss;

public interface GSSCredential extends java.lang.Cloneable {
  int INITIATE_AND_ACCEPT = 0;
  int INITIATE_ONLY = 1;
  int ACCEPT_ONLY = 2;
  int DEFAULT_LIFETIME = 0;
  int INDEFINITE_LIFETIME = 2147483647;

  void add(org.ietf.jgss.GSSName gSSName, int i, int i2, org.ietf.jgss.Oid oid, int i3) throws org.ietf.jgss.GSSException;
  void dispose() throws org.ietf.jgss.GSSException;
  boolean equals(java.lang.Object object);
  org.ietf.jgss.Oid[] getMechs() throws org.ietf.jgss.GSSException;
  org.ietf.jgss.GSSName getName() throws org.ietf.jgss.GSSException;
  org.ietf.jgss.GSSName getName(org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  int getRemainingAcceptLifetime(org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  int getRemainingInitLifetime(org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  int getRemainingLifetime() throws org.ietf.jgss.GSSException;
  int getUsage() throws org.ietf.jgss.GSSException;
  int getUsage(org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  int hashCode();
}
