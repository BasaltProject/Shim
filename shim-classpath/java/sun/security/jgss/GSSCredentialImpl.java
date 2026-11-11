/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss;

import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import sun.security.krb5.Creds;

public final class GSSCredentialImpl implements com.sun.security.jgss.ExtendedGSSCredential {
  public static final class Elem {
    public final Oid mech;
    public final int usage;
    public final GSSNameImpl name;
    public final Creds tgt;
    public final Subject subject;
    public final long endTime;
    public final String clazz;

    Elem(Oid mech, int usage, GSSNameImpl name, Creds tgt, Subject subject) {
      this.mech = mech;
      this.usage = usage;
      this.name = name;
      this.tgt = tgt;
      this.subject = subject;
      this.endTime = tgt == null ? Long.MAX_VALUE : tgt.endTime;
      boolean spnego = mech.equals(GSSManagerImpl.SPNEGO);
      this.clazz = spnego ? "sun.security.jgss.spnego.SpNegoCredElement"
          : tgt != null && tgt.s4uTicket != null ? "sun.security.jgss.krb5.Krb5ProxyCredential"
          : usage == GSSCredential.ACCEPT_ONLY ? "sun.security.jgss.krb5.Krb5AcceptCredential"
          : "sun.security.jgss.krb5.Krb5InitCredential";
    }
  }

  private final GSSNameImpl given;
  private final List<Elem> elems = new ArrayList<Elem>();
  private boolean disposed;

  public GSSCredentialImpl(GSSNameImpl given) {
    this.given = given;
  }

  public void addElemPublic(Oid mech, int usage, GSSNameImpl name, Creds tgt, Subject s) {
    elems.add(new Elem(mech, usage, name, tgt, s));
  }

  public List<Elem> elems() {
    return elems;
  }

  public Elem elemFor(Oid mech, boolean init) {
    for (Elem e : elems) {
      if (e.mech.equals(mech) && (e.usage == (init ? INITIATE_ONLY : ACCEPT_ONLY))) {
        return e;
      }
    }
    return null;
  }

  private void check() {
    if (disposed) {
      throw new IllegalStateException("This credential is no longer valid");
    }
  }

  public void dispose() throws GSSException {
    disposed = true;
  }

  public GSSName getName() throws GSSException {
    check();
    return given != null ? given : elems.get(0).name;
  }

  public GSSName getName(Oid mech) throws GSSException {
    check();
    for (Elem e : elems) {
      if (e.mech.equals(mech)) {
        return given != null ? given : e.name;
      }
    }
    throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
  }

  public int getRemainingLifetime() throws GSSException {
    check();
    long min = Long.MAX_VALUE;
    for (Elem e : elems) {
      min = Math.min(min, e.endTime);
    }
    return seconds(min);
  }

  private static int seconds(long end) {
    if (end == Long.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    long s = (end - System.currentTimeMillis()) / 1000;
    return (int) Math.max(0, Math.min(s, Integer.MAX_VALUE));
  }

  public int getRemainingInitLifetime(Oid mech) throws GSSException {
    check();
    boolean known = false;
    for (Elem e : elems) {
      if (e.mech.equals(mech)) {
        known = true;
        if (e.usage == INITIATE_ONLY) {
          return seconds(e.endTime);
        }
      }
    }
    if (!known) {
      throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
    }
    return 0;
  }

  public int getRemainingAcceptLifetime(Oid mech) throws GSSException {
    check();
    for (Elem e : elems) {
      if (e.mech.equals(mech) && e.usage == ACCEPT_ONLY) {
        return Integer.MAX_VALUE;
      }
    }
    throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
  }

  public int getUsage() throws GSSException {
    check();
    boolean i = false;
    boolean a = false;
    for (Elem e : elems) {
      i |= e.usage == INITIATE_ONLY;
      a |= e.usage == ACCEPT_ONLY;
    }
    return i && a ? INITIATE_AND_ACCEPT : i ? INITIATE_ONLY : ACCEPT_ONLY;
  }

  public int getUsage(Oid mech) throws GSSException {
    check();
    boolean i = false;
    boolean a = false;
    boolean known = false;
    for (Elem e : elems) {
      if (e.mech.equals(mech)) {
        known = true;
        i |= e.usage == INITIATE_ONLY;
        a |= e.usage == ACCEPT_ONLY;
      }
    }
    if (!known) {
      throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
    }
    return i && a ? INITIATE_AND_ACCEPT : i ? INITIATE_ONLY : ACCEPT_ONLY;
  }

  public Oid[] getMechs() throws GSSException {
    check();
    List<Oid> out = new ArrayList<Oid>();
    for (Elem e : elems) {
      if (!out.contains(e.mech)) {
        out.add(e.mech);
      }
    }
    return out.toArray(new Oid[0]);
  }

  public void add(GSSName name, int initLifetime, int acceptLifetime, Oid mech, int usage) throws GSSException {
    check();
    throw new GSSExceptionImpl(GSSException.DUPLICATE_ELEMENT, mech);
  }

  public GSSCredential impersonate(GSSName name) throws GSSException {
    check();
    if (name == null) {
      throw new NullPointerException("Cannot invoke \"sun.security.jgss.krb5.Krb5NameElement.getKrb5PrincipalName()\" "
          + "because \"kname\" is null");
    }
    Elem self = null;
    for (Elem e : elems) {
      if (e.usage == INITIATE_ONLY && e.tgt != null && e.tgt.s4uTicket == null) {
        self = e;
        break;
      }
    }
    if (self == null) {
      throw new GSSException(GSSException.FAILURE, -1, "Only an initiate credentials can impersonate");
    }
    GSSNameImpl user = (GSSNameImpl) name;
    Creds ticket;
    try {
      ticket = sun.security.krb5.KrbProto.s4uSelf(self.tgt, user.principal());
    } catch (sun.security.krb5.KrbException ex) {
      GSSException g = new GSSException(GSSException.FAILURE, -1, "Attempt to obtain S4U2self credentials failed!");
      g.initCause(ex);
      throw g;
    }
    Creds proxy = self.tgt.withS4u(ticket, user);
    GSSCredentialImpl out = new GSSCredentialImpl(user);
    out.addElemPublic(GSSManagerImpl.KRB5, INITIATE_ONLY, user, proxy, self.subject);
    out.addElemPublic(GSSManagerImpl.SPNEGO, INITIATE_ONLY, user, proxy, self.subject);
    return out;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GSSCredentialImpl)) {
      return false;
    }
    GSSCredentialImpl c = (GSSCredentialImpl) o;
    try {
      return getName().equals(c.getName()) && getUsage() == c.getUsage() && elems.size() == c.elems.size();
    } catch (GSSException | IllegalStateException e) {
      return false;
    }
  }

  public int hashCode() {
    return 1;
  }

  public String toString() {
    if (disposed) {
      return "[GSSCredential: disposed]";
    }
    StringBuilder sb = new StringBuilder("[GSSCredential: ");
    for (Elem e : elems) {
      sb.append('\n').append(e.name).append(' ').append(e.mech).append(' ');
      sb.append(e.usage == INITIATE_ONLY ? "Initiate" : e.usage == ACCEPT_ONLY ? "Accept" : "Initiate and Accept");
      sb.append(" [class ").append(e.clazz).append(']');
    }
    return sb.append(']').toString();
  }
}
