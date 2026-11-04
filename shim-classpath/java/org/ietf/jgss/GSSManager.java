/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.ietf.jgss;

public abstract class GSSManager {
  public GSSManager() {
  }

  public abstract void addProviderAtEnd(java.security.Provider provider, org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  public abstract void addProviderAtFront(java.security.Provider provider, org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSContext createContext(byte[] bs) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSContext createContext(org.ietf.jgss.GSSCredential gSSCredential) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSContext createContext(org.ietf.jgss.GSSName gSSName, org.ietf.jgss.Oid oid, org.ietf.jgss.GSSCredential gSSCredential, int i) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSCredential createCredential(int i) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSCredential createCredential(org.ietf.jgss.GSSName gSSName, int i, org.ietf.jgss.Oid[] oids, int i2) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSCredential createCredential(org.ietf.jgss.GSSName gSSName, int i, org.ietf.jgss.Oid oid, int i2) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSName createName(byte[] bs, org.ietf.jgss.Oid oid, org.ietf.jgss.Oid oid2) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSName createName(byte[] bs, org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSName createName(java.lang.String string, org.ietf.jgss.Oid oid, org.ietf.jgss.Oid oid2) throws org.ietf.jgss.GSSException;
  public abstract org.ietf.jgss.GSSName createName(java.lang.String string, org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
  public static org.ietf.jgss.GSSManager getInstance() {
    return new sun.security.jgss.GSSManagerImpl();
  }
  public abstract org.ietf.jgss.Oid[] getMechs();
  public abstract org.ietf.jgss.Oid[] getMechsForName(org.ietf.jgss.Oid oid);
  public abstract org.ietf.jgss.Oid[] getNamesForMech(org.ietf.jgss.Oid oid) throws org.ietf.jgss.GSSException;
}
