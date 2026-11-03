/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.jgss;

import java.io.ByteArrayOutputStream;
import java.security.Provider;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

public final class GSSManagerImpl extends GSSManager {
  static final Oid KRB5 = oid("1.2.840.113554.1.2.2");
  static final Oid SPNEGO = oid("1.3.6.1.5.5.2");
  static final Oid KRB5_NT_PRINCIPAL_NAME = oid("1.2.840.113554.1.2.2.1");
  static final Oid KRB5_NT_ENTERPRISE_NAME = oid("1.2.840.113554.1.2.2.6");

  private static Oid oid(String s) {
    try {
      return new Oid(s);
    } catch (GSSException e) {
      throw new IllegalStateException(e);
    }
  }

  public GSSManagerImpl() {
  }

  public Oid[] getMechs() {
    return new Oid[] {KRB5, SPNEGO};
  }

  public Oid[] getNamesForMech(Oid mech) throws GSSException {
    if (mech.equals(KRB5) || mech.equals(SPNEGO)) {
      return new Oid[] {GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_USER_NAME, KRB5_NT_PRINCIPAL_NAME,
          GSSName.NT_EXPORT_NAME};
    }
    throw new GSSException(GSSException.BAD_MECH);
  }

  public Oid[] getMechsForName(Oid nameType) {
    if (nameType.equals(GSSName.NT_HOSTBASED_SERVICE) || nameType.equals(GSSName.NT_USER_NAME)
        || nameType.equals(KRB5_NT_PRINCIPAL_NAME) || nameType.equals(GSSName.NT_EXPORT_NAME)) {
      return new Oid[] {KRB5, SPNEGO};
    }
    return new Oid[0];
  }

  public GSSName createName(String nameStr, Oid nameType) throws GSSException {
    return new NameImpl(nameStr, nameType, null);
  }

  public GSSName createName(byte[] name, Oid nameType) throws GSSException {
    if (nameType != null && nameType.equals(GSSName.NT_EXPORT_NAME)) {
      if (name.length < 6 || name[0] != 0x04 || name[1] != 0x01) {
        throw new GSSException(GSSException.BAD_NAME);
      }
      int oidLen = ((name[2] & 0xff) << 8) | (name[3] & 0xff);
      int pos = 4 + oidLen;
      if (pos + 4 > name.length) {
        throw new GSSException(GSSException.BAD_NAME);
      }
      int nameLen = ((name[pos] & 0xff) << 24) | ((name[pos + 1] & 0xff) << 16) | ((name[pos + 2] & 0xff) << 8)
          | (name[pos + 3] & 0xff);
      byte[] oidDer = new byte[oidLen];
      System.arraycopy(name, 4, oidDer, 0, oidLen);
      return new NameImpl(new String(name, pos + 4, nameLen), null, new Oid(oidDer));
    }
    return new NameImpl(new String(name), nameType, null);
  }

  public GSSName createName(String nameStr, Oid nameType, Oid mech) throws GSSException {
    checkMech(mech);
    return new NameImpl(nameStr, nameType, mech);
  }

  public GSSName createName(byte[] name, Oid nameType, Oid mech) throws GSSException {
    checkMech(mech);
    return createName(name, nameType);
  }

  private void checkMech(Oid mech) throws GSSException {
    if (mech != null && !mech.equals(KRB5) && !mech.equals(SPNEGO)) {
      throw new GSSException(GSSException.BAD_MECH);
    }
  }

  public GSSCredential createCredential(int usage) throws GSSException {
    throw new GSSException(GSSException.NO_CRED, -1, "No Kerberos credentials found");
  }

  public GSSCredential createCredential(GSSName aName, int lifetime, Oid mech, int usage) throws GSSException {
    throw new GSSException(GSSException.NO_CRED, -1, "No Kerberos credentials found");
  }

  public GSSCredential createCredential(GSSName aName, int lifetime, Oid[] mechs, int usage) throws GSSException {
    throw new GSSException(GSSException.NO_CRED, -1, "No Kerberos credentials found");
  }

  public GSSContext createContext(GSSName peer, Oid mech, GSSCredential myCred, int lifetime) throws GSSException {
    if (peer == null) {
      throw new GSSException(GSSException.BAD_NAME);
    }
    return new ContextImpl(peer, mech == null ? KRB5 : mech, true);
  }

  public GSSContext createContext(GSSCredential myCred) throws GSSException {
    return new ContextImpl(null, KRB5, false);
  }

  public GSSContext createContext(byte[] interProcessToken) throws GSSException {
    throw new GSSException(GSSException.NO_CONTEXT);
  }

  public void addProviderAtFront(Provider p, Oid mech) throws GSSException {
  }

  public void addProviderAtEnd(Provider p, Oid mech) throws GSSException {
  }

  private static final class NameImpl implements GSSName {
    private final String name;
    private final Oid type;
    private final Oid mech;

    NameImpl(String name, Oid type, Oid mech) throws GSSException {
      if (name == null) {
        throw new GSSException(GSSException.BAD_NAME, -1, "Name cannot be null");
      }
      this.name = name;
      this.type = type;
      this.mech = mech;
    }

    public boolean equals(GSSName other) throws GSSException {
      return equals((Object) other);
    }

    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof NameImpl)) {
        return false;
      }
      NameImpl n = (NameImpl) other;
      return name.equals(n.name) && (type == null ? n.type == null : type.equals(n.type));
    }

    public int hashCode() {
      return name.hashCode();
    }

    public GSSName canonicalize(Oid mech) throws GSSException {
      return new NameImpl(name, type, mech == null ? KRB5 : mech);
    }

    public byte[] export() throws GSSException {
      if (mech == null) {
        throw new GSSException(GSSException.NAME_NOT_MN);
      }
      byte[] der = mech.getDER();
      byte[] n = name.getBytes();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(0x04);
      out.write(0x01);
      out.write(der.length >> 8);
      out.write(der.length);
      out.write(der, 0, der.length);
      out.write(n.length >> 24);
      out.write(n.length >> 16);
      out.write(n.length >> 8);
      out.write(n.length);
      out.write(n, 0, n.length);
      return out.toByteArray();
    }

    public String toString() {
      return name;
    }

    public Oid getStringNameType() throws GSSException {
      return type;
    }

    public boolean isAnonymous() {
      return type != null && type.equals(GSSName.NT_ANONYMOUS);
    }

    public boolean isMN() {
      return mech != null;
    }
  }

  private static final class ContextImpl implements GSSContext {
    private final GSSName peer;
    private final Oid mech;
    private final boolean initiator;
    private boolean disposed;

    ContextImpl(GSSName peer, Oid mech, boolean initiator) {
      this.peer = peer;
      this.mech = mech;
      this.initiator = initiator;
    }

    private GSSException noCred() {
      return new GSSException(GSSException.NO_CRED, -1, "Unable to obtain Kerberos credentials");
    }

    public byte[] initSecContext(byte[] inputBuf, int offset, int len) throws GSSException {
      throw noCred();
    }

    public int initSecContext(java.io.InputStream inStream, java.io.OutputStream outStream) throws GSSException {
      throw noCred();
    }

    public byte[] acceptSecContext(byte[] inTok, int offset, int len) throws GSSException {
      throw noCred();
    }

    public void acceptSecContext(java.io.InputStream inStream, java.io.OutputStream outStream) throws GSSException {
      throw noCred();
    }

    public boolean isEstablished() {
      return false;
    }

    public void dispose() throws GSSException {
      disposed = true;
    }

    public int getWrapSizeLimit(int qop, boolean confReq, int maxTokenSize) throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public byte[] wrap(byte[] inBuf, int offset, int len, org.ietf.jgss.MessageProp msgProp) throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public void wrap(java.io.InputStream inStream, java.io.OutputStream outStream, org.ietf.jgss.MessageProp msgProp)
        throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public byte[] unwrap(byte[] inBuf, int offset, int len, org.ietf.jgss.MessageProp msgProp) throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public void unwrap(java.io.InputStream inStream, java.io.OutputStream outStream, org.ietf.jgss.MessageProp msgProp)
        throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public byte[] getMIC(byte[] inMsg, int offset, int len, org.ietf.jgss.MessageProp msgProp) throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public void getMIC(java.io.InputStream inStream, java.io.OutputStream outStream, org.ietf.jgss.MessageProp msgProp)
        throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public void verifyMIC(byte[] inTok, int tokOffset, int tokLen, byte[] inMsg, int msgOffset, int msgLen,
        org.ietf.jgss.MessageProp msgProp) throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public void verifyMIC(java.io.InputStream tokStream, java.io.InputStream msgStream,
        org.ietf.jgss.MessageProp msgProp) throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public byte[] export() throws GSSException {
      throw new GSSException(GSSException.UNAVAILABLE);
    }

    public void requestMutualAuth(boolean state) throws GSSException {
    }

    public void requestReplayDet(boolean state) throws GSSException {
    }

    public void requestSequenceDet(boolean state) throws GSSException {
    }

    public void requestCredDeleg(boolean state) throws GSSException {
    }

    public void requestAnonymity(boolean state) throws GSSException {
    }

    public void requestConf(boolean state) throws GSSException {
    }

    public void requestInteg(boolean state) throws GSSException {
    }

    public void requestLifetime(int lifetime) throws GSSException {
    }

    public void setChannelBinding(org.ietf.jgss.ChannelBinding cb) throws GSSException {
    }

    public boolean getCredDelegState() {
      return false;
    }

    public boolean getMutualAuthState() {
      return false;
    }

    public boolean getReplayDetState() {
      return false;
    }

    public boolean getSequenceDetState() {
      return false;
    }

    public boolean getAnonymityState() {
      return false;
    }

    public boolean isTransferable() throws GSSException {
      return false;
    }

    public boolean isProtReady() {
      return false;
    }

    public boolean getConfState() {
      return false;
    }

    public boolean getIntegState() {
      return false;
    }

    public int getLifetime() {
      return 0;
    }

    public GSSName getSrcName() throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public GSSName getTargName() throws GSSException {
      return peer;
    }

    public Oid getMech() throws GSSException {
      return mech;
    }

    public GSSCredential getDelegCred() throws GSSException {
      throw new GSSException(GSSException.NO_CONTEXT);
    }

    public boolean isInitiator() throws GSSException {
      return initiator;
    }
  }
}
