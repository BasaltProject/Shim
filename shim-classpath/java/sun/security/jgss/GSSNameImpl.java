/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.krb5.Config;
import sun.security.krb5.KrbException;
import sun.security.krb5.PrincipalName;

public final class GSSNameImpl implements GSSName {
  public static final Oid KRB5 = GSSManagerImpl.KRB5;
  public static final Oid KRB5_NT_PRINCIPAL_NAME = GSSManagerImpl.KRB5_NT_PRINCIPAL_NAME;

  private final String name;
  private final Oid type;

  public GSSNameImpl(String name, Oid type) throws GSSException {
    if (name == null) {
      throw new GSSExceptionImpl(GSSException.BAD_NAME, "Cannot import null name");
    }
    Oid t = type == null ? KRB5_NT_PRINCIPAL_NAME : type;
    if (!supports(t)) {
      GSSException e = new GSSException(GSSException.BAD_NAMETYPE, -1, t + " is an unsupported nametype");
      throw e;
    }
    this.name = name;
    this.type = t;
  }

  static boolean supports(Oid t) {
    return t.equals(GSSName.NT_HOSTBASED_SERVICE) || t.equals(GSSName.NT_USER_NAME)
        || t.equals(KRB5_NT_PRINCIPAL_NAME) || t.equals(GSSName.NT_EXPORT_NAME);
  }

  public static GSSNameImpl fromExport(byte[] b) throws GSSException {
    if (b.length < 4 || b[0] != 0x04 || b[1] != 0x01) {
      throw new GSSException(GSSException.BAD_NAME);
    }
    int oidLen = ((b[2] & 0xff) << 8) | (b[3] & 0xff);
    if (oidLen < 2 || 4 + oidLen + 4 > b.length) {
      throw new GSSException(GSSException.BAD_NAME);
    }
    byte[] der = new byte[oidLen];
    System.arraycopy(b, 4, der, 0, oidLen);
    Oid mech = new Oid(der);
    if (!mech.equals(KRB5) && !mech.equals(GSSManagerImpl.SPNEGO)) {
      throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
    }
    int p = 4 + oidLen;
    int n = ((b[p] & 0xff) << 24) | ((b[p + 1] & 0xff) << 16) | ((b[p + 2] & 0xff) << 8) | (b[p + 3] & 0xff);
    if (n < 0 || p + 4 + n > b.length) {
      throw new GSSException(GSSException.BAD_NAME);
    }
    return new GSSNameImpl(new String(b, p + 4, n, StandardCharsets.UTF_8), GSSName.NT_EXPORT_NAME);
  }

  public PrincipalName principal() throws GSSException {
    try {
      Config cfg = Config.getInstance();
      if (type.equals(GSSName.NT_HOSTBASED_SERVICE)) {
        int at = name.indexOf('@');
        String service = at < 0 ? name : name.substring(0, at);
        String host = at < 0 ? null : name.substring(at + 1).toLowerCase(Locale.ROOT);
        if (host == null) {
          host = java.net.InetAddress.getLocalHost().getCanonicalHostName().toLowerCase(Locale.ROOT);
        }
        String realm = cfg.realmOfHost(host);
        if (realm == null) {
          realm = cfg.defaultRealm();
        }
        return new PrincipalName(PrincipalName.KRB_NT_UNKNOWN, new String[] {service, host}, realm);
      }
      return new PrincipalName(name, PrincipalName.KRB_NT_PRINCIPAL, cfg.defaultRealm());
    } catch (KrbException | java.net.UnknownHostException e) {
      GSSException g = new GSSException(GSSException.BAD_NAME, -1, e.getMessage());
      g.initCause(e);
      throw g;
    }
  }

  public static GSSNameImpl of(PrincipalName p) throws GSSException {
    return new GSSNameImpl(p.toString(), KRB5_NT_PRINCIPAL_NAME);
  }

  public boolean equals(GSSName other) throws GSSException {
    return equals((Object) other);
  }

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GSSNameImpl)) {
      return false;
    }
    GSSNameImpl n = (GSSNameImpl) other;
    try {
      return principal().equals(n.principal());
    } catch (GSSException e) {
      return name.equals(n.name);
    }
  }

  public int hashCode() {
    return 1;
  }

  public GSSName canonicalize(Oid mech) throws GSSException {
    if (mech != null && !mech.equals(KRB5) && !mech.equals(GSSManagerImpl.SPNEGO)) {
      throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
    }
    return new GSSNameImpl(name, type);
  }

  public byte[] export() throws GSSException {
    byte[] der = KRB5.getDER();
    byte[] n = principal().toString().getBytes(StandardCharsets.UTF_8);
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

  public Oid getStringNameType() {
    return type;
  }

  public boolean isAnonymous() {
    return false;
  }

  public boolean isMN() {
    return true;
  }
}
