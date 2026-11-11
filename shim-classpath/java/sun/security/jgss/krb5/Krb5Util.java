/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss.krb5;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.kerberos.KeyTab;
import sun.security.krb5.Creds;
import sun.security.krb5.PrincipalName;

public final class Krb5Util {
  private Krb5Util() {
  }

  public static Subject currentSubject() {
    try {
      return Subject.current();
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static PrincipalName principal(KerberosPrincipal p) {
    try {
      return new PrincipalName(p.getName(), p.getNameType());
    } catch (sun.security.krb5.KrbException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Creds creds(KerberosTicket t) {
    Creds c = new Creds();
    c.ticket = t.getEncoded();
    c.client = principal(t.getClient());
    c.server = principal(t.getServer());
    c.keyType = t.getSessionKeyType();
    c.key = t.getSessionKey().getEncoded();
    c.flags = 0;
    boolean[] f = t.getFlags();
    for (int i = 0; i < f.length && i < 32; i++) {
      if (f[i]) {
        c.flags |= 0x80000000 >>> i;
      }
    }
    c.authTime = t.getAuthTime() == null ? 0 : t.getAuthTime().getTime();
    c.startTime = t.getStartTime() == null ? c.authTime : t.getStartTime().getTime();
    c.endTime = t.getEndTime().getTime();
    c.renewTill = t.getRenewTill() == null ? -1 : t.getRenewTill().getTime();
    c.addresses = t.getClientAddresses();
    return c;
  }

  public static KerberosTicket ticket(Creds c) {
    return new KerberosTicket(c.ticket, new KerberosPrincipal(c.client.toString(), c.client.getNameType()),
        new KerberosPrincipal(c.server.toString(), c.server.getNameType()), c.key, c.keyType, c.flagArray(),
        new Date(c.authTime), new Date(c.startTime), new Date(c.endTime),
        c.renewTill < 0 ? null : new Date(c.renewTill), c.addresses);
  }

  public static org.ietf.jgss.GSSCredential initCredential(Creds tgt) throws org.ietf.jgss.GSSException {
    sun.security.jgss.GSSCredentialImpl c = new sun.security.jgss.GSSCredentialImpl(null);
    sun.security.jgss.GSSNameImpl name = sun.security.jgss.GSSNameImpl.of(tgt.client);
    c.addElemPublic(sun.security.jgss.GSSManagerImpl.KRB5, org.ietf.jgss.GSSCredential.INITIATE_ONLY, name, tgt, null);
    c.addElemPublic(sun.security.jgss.GSSManagerImpl.SPNEGO, org.ietf.jgss.GSSCredential.INITIATE_ONLY, name, tgt,
        null);
    return c;
  }

  private static boolean usable(KerberosTicket t) {
    return !t.isDestroyed() && t.getEndTime().getTime() > System.currentTimeMillis();
  }

  public static Creds findTgt(Subject s, PrincipalName client) {
    if (s == null) {
      return null;
    }
    for (Object o : s.getPrivateCredentials()) {
      if (!(o instanceof KerberosTicket)) {
        continue;
      }
      KerberosTicket t = (KerberosTicket) o;
      if (!usable(t)) {
        continue;
      }
      String[] n = principal(t.getServer()).getNameStrings();
      if (n.length != 2 || !n[0].equals("krbtgt") || !n[1].equals(t.getServer().getRealm())) {
        continue;
      }
      if (client != null && !principal(t.getClient()).equals(client)) {
        continue;
      }
      return creds(t);
    }
    return null;
  }

  public static Creds findService(Subject s, PrincipalName client, PrincipalName server) {
    if (s == null) {
      return null;
    }
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KerberosTicket) {
        KerberosTicket t = (KerberosTicket) o;
        if (usable(t) && principal(t.getServer()).equals(server) && principal(t.getClient()).equals(client)) {
          return creds(t);
        }
      }
    }
    return null;
  }

  public static List<PrincipalName> acceptNames(Subject s) {
    List<PrincipalName> out = new ArrayList<PrincipalName>();
    if (s == null) {
      return out;
    }
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KerberosKey) {
        PrincipalName p = principal(((KerberosKey) o).getPrincipal());
        if (!out.contains(p)) {
          out.add(p);
        }
      }
    }
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KeyTab) {
        KeyTab kt = (KeyTab) o;
        KerberosPrincipal bound = kt.getPrincipal();
        if (bound != null && !out.contains(principal(bound))) {
          out.add(principal(bound));
        }
      }
    }
    for (KerberosPrincipal p : s.getPrincipals(KerberosPrincipal.class)) {
      if (!out.contains(principal(p))) {
        out.add(principal(p));
      }
    }
    return out;
  }

  public static boolean hasAcceptKey(Subject s, PrincipalName name) {
    return findKey(s, name, -1, -1) != null;
  }

  public static byte[] findKey(Subject s, PrincipalName server, int etype, int kvno) {
    if (s == null) {
      return null;
    }
    KerberosPrincipal kp = new KerberosPrincipal(server.toString(), server.getNameType());
    byte[] any = null;
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KerberosKey) {
        KerberosKey k = (KerberosKey) o;
        if (k.isDestroyed() || (etype >= 0 && k.getKeyType() != etype)) {
          continue;
        }
        if (k.getPrincipal().equals(kp)) {
          return etype < 0 ? new byte[0] : k.getEncoded();
        }
        if (any == null) {
          any = etype < 0 ? new byte[0] : k.getEncoded();
        }
      }
    }
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KeyTab) {
        for (KerberosKey k : ((KeyTab) o).getKeys(kp)) {
          if (etype < 0 || k.getKeyType() == etype) {
            return etype < 0 ? new byte[0] : k.getEncoded();
          }
        }
      }
    }
    return any;
  }

  public static boolean hasAnyAcceptKey(Subject s) {
    if (s == null) {
      return false;
    }
    for (Object o : s.getPrivateCredentials()) {
      if (o instanceof KerberosKey || o instanceof KeyTab) {
        return true;
      }
    }
    return false;
  }
}
