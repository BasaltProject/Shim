/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss;

import java.io.IOException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.security.auth.Subject;
import com.sun.security.jgss.ExtendedGSSContextImpl;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.jgss.krb5.Krb5Util;
import sun.security.krb5.Asn1;
import sun.security.krb5.Creds;
import sun.security.krb5.PrincipalName;

public final class GSSManagerImpl extends GSSManager {
  public static final Oid KRB5 = oid("1.2.840.113554.1.2.2");
  public static final Oid KRB5_MS = oid("1.2.840.48018.1.2.2");
  public static final Oid SPNEGO = oid("1.3.6.1.5.5.2");
  public static final Oid KRB5_NT_PRINCIPAL_NAME = oid("1.2.840.113554.1.2.2.1");

  private static Oid oid(String s) {
    try {
      return new Oid(s);
    } catch (GSSException e) {
      throw new IllegalStateException(e);
    }
  }

  public GSSManagerImpl() {
  }

  public static Oid sniffMech(byte[] in, int off, int len) {
    try {
      if (len > 2 && in[off] == 0x60) {
        Asn1.Node n = Asn1.read(in, off, off + len);
        Asn1.Node o = Asn1.read(in, n.off, n.off + n.len);
        Oid m = new Oid(Arrays.copyOfRange(in, n.off, n.off + o.total));
        return m.equals(SPNEGO) ? SPNEGO : KRB5;
      }
      if (len > 0 && (in[off] == (byte) 0xa0 || in[off] == (byte) 0xa1)) {
        return SPNEGO;
      }
    } catch (IOException | GSSException e) {
      return KRB5;
    }
    return KRB5;
  }

  public Oid[] getMechs() {
    return new Oid[] {KRB5, SPNEGO};
  }

  public Oid[] getNamesForMech(Oid mech) throws GSSException {
    if (mech.equals(KRB5)) {
      return new Oid[] {GSSName.NT_USER_NAME, GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_EXPORT_NAME,
          KRB5_NT_PRINCIPAL_NAME};
    }
    if (mech.equals(SPNEGO)) {
      return new Oid[] {GSSName.NT_USER_NAME, GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_EXPORT_NAME};
    }
    throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
  }

  public Oid[] getMechsForName(Oid nameType) {
    if (nameType.equals(KRB5_NT_PRINCIPAL_NAME)) {
      return new Oid[] {KRB5};
    }
    if (nameType.equals(GSSName.NT_HOSTBASED_SERVICE) || nameType.equals(GSSName.NT_USER_NAME)
        || nameType.equals(GSSName.NT_EXPORT_NAME)) {
      return new Oid[] {KRB5, SPNEGO};
    }
    return new Oid[0];
  }

  private static void checkMech(Oid mech) throws GSSException {
    if (mech != null && !mech.equals(KRB5) && !mech.equals(SPNEGO)) {
      throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
    }
  }

  public GSSName createName(String nameStr, Oid nameType) throws GSSException {
    return new GSSNameImpl(nameStr, nameType);
  }

  public GSSName createName(byte[] name, Oid nameType) throws GSSException {
    if (name == null) {
      throw new GSSExceptionImpl(GSSException.BAD_NAME, "Cannot import null name");
    }
    if (nameType != null && nameType.equals(GSSName.NT_EXPORT_NAME)) {
      return GSSNameImpl.fromExport(name);
    }
    return new GSSNameImpl(new String(name, java.nio.charset.StandardCharsets.UTF_8), nameType);
  }

  public GSSName createName(String nameStr, Oid nameType, Oid mech) throws GSSException {
    checkMech(mech);
    return new GSSNameImpl(nameStr, nameType);
  }

  public GSSName createName(byte[] name, Oid nameType, Oid mech) throws GSSException {
    checkMech(mech);
    return createName(name, nameType);
  }

  public GSSCredential createCredential(int usage) throws GSSException {
    return createCredential(null, GSSCredential.DEFAULT_LIFETIME, (Oid[]) null, usage);
  }

  public GSSCredential createCredential(GSSName aName, int lifetime, Oid mech, int usage) throws GSSException {
    return createCredential(aName, lifetime, mech == null ? null : new Oid[] {mech}, usage);
  }

  public GSSCredential createCredential(GSSName aName, int lifetime, Oid[] mechs, int usage) throws GSSException {
    if (usage < GSSCredential.INITIATE_AND_ACCEPT || usage > GSSCredential.ACCEPT_ONLY) {
      throw new GSSException(GSSException.FAILURE, -1, "Invalid usage mode requested - " + usage);
    }
    boolean explicit = mechs != null;
    Oid[] ms = explicit ? mechs : new Oid[] {KRB5, SPNEGO};
    for (Oid m : ms) {
      checkMech(m);
    }
    GSSNameImpl name = aName == null ? null : (GSSNameImpl) aName;
    GSSCredentialImpl cred = new GSSCredentialImpl(name);
    GSSException last = null;
    for (Oid m : ms) {
      try {
        addKrb5(cred, m, name, usage);
      } catch (GSSException e) {
        last = e;
      }
    }
    if (cred.elems().isEmpty()) {
      if (explicit && last != null) {
        throw last;
      }
      throw new GSSException(GSSException.NO_CRED);
    }
    return cred;
  }

  private void addKrb5(GSSCredentialImpl cred, Oid mech, GSSNameImpl name, int usage) throws GSSException {
    Subject s = Krb5Util.currentSubject();
    PrincipalName want = name == null ? null : name.principal();
    boolean init = usage != GSSCredential.ACCEPT_ONLY;
    boolean accept = usage != GSSCredential.INITIATE_ONLY;
    GSSException failure = null;
    boolean any = false;
    if (init) {
      Creds tgt = Krb5Util.findTgt(s, want);
      if (tgt != null) {
        cred.addElemPublic(mech, GSSCredential.INITIATE_ONLY, GSSNameImpl.of(tgt.client), tgt, s);
        any = true;
      } else {
        failure = new GSSException(GSSException.NO_CRED, -1, "Failed to find any Kerberos tgt");
      }
    }
    if (accept) {
      List<PrincipalName> names = Krb5Util.acceptNames(s);
      PrincipalName pick = null;
      for (PrincipalName p : names) {
        if (want == null || p.equals(want)) {
          pick = p;
          break;
        }
      }
      if (pick != null) {
        cred.addElemPublic(mech, GSSCredential.ACCEPT_ONLY, GSSNameImpl.of(pick), null, s);
        any = true;
      } else if (failure == null) {
        failure = new GSSException(GSSException.NO_CRED, -1, "Failed to find any Kerberos Key");
      }
    }
    if (!any) {
      throw failure;
    }
  }

  public GSSContext createContext(GSSName peer, Oid mech, GSSCredential myCred, int lifetime) throws GSSException {
    Oid m = mech == null ? KRB5 : mech;
    if (peer == null) {
      throw new GSSException(GSSException.BAD_NAME);
    }
    return new ExtendedGSSContextImpl(peer, m, (GSSCredentialImpl) myCred, lifetime);
  }

  public GSSContext createContext(GSSCredential myCred) throws GSSException {
    return new ExtendedGSSContextImpl((GSSCredentialImpl) myCred);
  }

  public GSSContext createContext(byte[] interProcessToken) throws GSSException {
    throw new GSSException(GSSException.UNAVAILABLE, -1, "GSSContext import is not supported");
  }

  public void addProviderAtFront(Provider p, Oid mech) throws GSSException {
  }

  public void addProviderAtEnd(Provider p, Oid mech) throws GSSException {
  }
}
