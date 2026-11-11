/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.jgss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.security.auth.Subject;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import sun.security.jgss.CtxFlags;
import sun.security.jgss.GSSCredentialImpl;
import sun.security.jgss.GSSExceptionImpl;
import sun.security.jgss.GSSManagerImpl;
import sun.security.jgss.krb5.Krb5Context;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.jgss.spnego.SpNegoContext;
import sun.security.krb5.Creds;
import sun.security.krb5.PrincipalName;

public final class ExtendedGSSContextImpl implements ExtendedGSSContext {
  private final boolean initiator;
  private final GSSName peer;
  private final Oid mech;
  private final GSSCredentialImpl cred;
  private final CtxFlags flags = new CtxFlags();
  private GSSContextSpi mechCtxt;

  public ExtendedGSSContextImpl(GSSName peer, Oid mech, GSSCredentialImpl cred, int lifetime) {
    this.initiator = true;
    this.peer = peer;
    this.mech = mech;
    this.cred = cred;
    flags.lifetime = lifetime;
  }

  public ExtendedGSSContextImpl(GSSCredentialImpl cred) {
    this.initiator = false;
    this.peer = null;
    this.mech = null;
    this.cred = cred;
  }

  private GSSContextSpi create(Oid m) throws GSSException {
    if (!m.equals(GSSManagerImpl.KRB5) && !m.equals(GSSManagerImpl.SPNEGO)) {
      throw new GSSExceptionImpl(GSSException.BAD_MECH, m);
    }
    PrincipalName want = null;
    Creds tgt = null;
    Subject subj = null;
    if (cred != null) {
      GSSCredentialImpl.Elem e = cred.elemFor(m, initiator);
      if (e == null) {
        e = cred.elemFor(GSSManagerImpl.KRB5, initiator);
      }
      if (e == null) {
        throw new GSSException(GSSException.NO_CRED, -1, "No credentials for the requested usage");
      }
      want = e.name.principal();
      tgt = e.tgt;
      subj = e.subject;
    }
    if (m.equals(GSSManagerImpl.SPNEGO)) {
      return new SpNegoContext(initiator, flags, peer, want, tgt, subj);
    }
    return new Krb5Context(initiator, flags, peer, want, tgt, subj);
  }

  private GSSContextSpi need() throws GSSException {
    if (mechCtxt == null) {
      throw new GSSExceptionImpl(GSSException.NO_CONTEXT, "No mechanism context yet!");
    }
    return mechCtxt;
  }

  public byte[] initSecContext(byte[] in, int off, int len) throws GSSException {
    if (!initiator) {
      throw new GSSExceptionImpl(GSSException.FAILURE, "Illegal call to initSecContext");
    }
    if (mechCtxt == null) {
      mechCtxt = create(mech);
    }
    return mechCtxt.init(in, off, len);
  }

  public int initSecContext(InputStream in, OutputStream out) throws GSSException {
    byte[] input = readAll(in);
    byte[] t = initSecContext(input, 0, input.length);
    return write(out, t);
  }

  public byte[] acceptSecContext(byte[] in, int off, int len) throws GSSException {
    if (initiator) {
      throw new GSSExceptionImpl(GSSException.FAILURE, "Illegal call to acceptSecContext");
    }
    if (mechCtxt == null) {
      mechCtxt = create(GSSManagerImpl.sniffMech(in, off, len));
    }
    return mechCtxt.accept(in, off, len);
  }

  public void acceptSecContext(InputStream in, OutputStream out) throws GSSException {
    byte[] input = readAll(in);
    byte[] t = acceptSecContext(input, 0, input.length);
    write(out, t);
  }

  static byte[] readAll(InputStream in) throws GSSException {
    try {
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int n;
      while ((n = in.read(buf)) > 0) {
        b.write(buf, 0, n);
      }
      return b.toByteArray();
    } catch (IOException e) {
      throw new GSSExceptionImpl(GSSException.FAILURE, e);
    }
  }

  static int write(OutputStream out, byte[] t) throws GSSException {
    if (t == null) {
      return 0;
    }
    try {
      out.write(t);
      return t.length;
    } catch (IOException e) {
      throw new GSSExceptionImpl(GSSException.FAILURE, e);
    }
  }

  public boolean isEstablished() {
    return mechCtxt != null && mechCtxt.isEstablished();
  }

  public void dispose() throws GSSException {
    if (mechCtxt != null) {
      mechCtxt.dispose();
      mechCtxt = null;
    }
  }

  public int getWrapSizeLimit(int qop, boolean confReq, int maxTokenSize) throws GSSException {
    return need().getWrapSizeLimit(qop, confReq, maxTokenSize);
  }

  public byte[] wrap(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    return need().wrap(in, off, len, prop);
  }

  public void wrap(InputStream in, OutputStream out, MessageProp prop) throws GSSException {
    byte[] d = readAll(in);
    write(out, wrap(d, 0, d.length, prop));
  }

  public byte[] unwrap(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    return need().unwrap(in, off, len, prop);
  }

  public void unwrap(InputStream in, OutputStream out, MessageProp prop) throws GSSException {
    byte[] d = readAll(in);
    write(out, unwrap(d, 0, d.length, prop));
  }

  public byte[] getMIC(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    return need().getMIC(in, off, len, prop);
  }

  public void getMIC(InputStream in, OutputStream out, MessageProp prop) throws GSSException {
    byte[] d = readAll(in);
    write(out, getMIC(d, 0, d.length, prop));
  }

  public void verifyMIC(byte[] tok, int tokOff, int tokLen, byte[] msg, int msgOff, int msgLen, MessageProp prop)
      throws GSSException {
    need().verifyMIC(tok, tokOff, tokLen, msg, msgOff, msgLen, prop);
  }

  public void verifyMIC(InputStream tok, InputStream msg, MessageProp prop) throws GSSException {
    byte[] t = readAll(tok);
    byte[] m = readAll(msg);
    verifyMIC(t, 0, t.length, m, 0, m.length, prop);
  }

  public byte[] export() throws GSSException {
    mechCtxt.isTransferable();
    return null;
  }

  public void requestMutualAuth(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.mutual = state;
    }
  }

  public void requestReplayDet(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.replay = state;
    }
  }

  public void requestSequenceDet(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.seq = state;
    }
  }

  public void requestCredDeleg(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.deleg = state;
    }
  }

  public void requestAnonymity(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.anon = state;
    }
  }

  public void requestConf(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.conf = state;
    }
  }

  public void requestInteg(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.integ = state;
    }
  }

  public void requestLifetime(int lifetime) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.lifetime = lifetime;
    }
  }

  public void setChannelBinding(ChannelBinding cb) throws GSSException {
    if (mechCtxt == null) {
      flags.cb = cb;
    }
  }

  public boolean getCredDelegState() {
    return mechCtxt != null ? mechCtxt.getCredDeleg() : flags.deleg;
  }

  public boolean getMutualAuthState() {
    return mechCtxt != null ? mechCtxt.getMutualAuth() : flags.mutual;
  }

  public boolean getReplayDetState() {
    return mechCtxt != null ? mechCtxt.getReplayDet() : flags.replay;
  }

  public boolean getSequenceDetState() {
    return mechCtxt != null ? mechCtxt.getSequenceDet() : flags.seq;
  }

  public boolean getAnonymityState() {
    return mechCtxt != null ? mechCtxt.getAnonymity() : flags.anon;
  }

  public boolean isTransferable() throws GSSException {
    return mechCtxt != null && mechCtxt.isTransferable();
  }

  public boolean isProtReady() {
    return mechCtxt != null && mechCtxt.isProtReady();
  }

  public boolean getConfState() {
    return mechCtxt != null ? mechCtxt.getConf() : flags.conf;
  }

  public boolean getIntegState() {
    return mechCtxt != null ? mechCtxt.getInteg() : flags.integ;
  }

  public int getLifetime() {
    return mechCtxt != null ? mechCtxt.getLifetime() : flags.lifetime;
  }

  public GSSName getSrcName() throws GSSException {
    return mechCtxt.getSrcName();
  }

  public GSSName getTargName() throws GSSException {
    if (mechCtxt == null && initiator) {
      return peer;
    }
    return mechCtxt.getTargName();
  }

  public Oid getMech() throws GSSException {
    return mechCtxt != null ? mechCtxt.getMech() : mech;
  }

  public GSSCredential getDelegCred() throws GSSException {
    return need().getDelegCred();
  }

  public boolean isInitiator() throws GSSException {
    return initiator;
  }

  public Object inquireSecContext(InquireType type) throws GSSException {
    return need().inquire(type);
  }

  public void requestDelegPolicy(boolean state) throws GSSException {
    if (mechCtxt == null && initiator) {
      flags.delegPolicy = state;
    }
  }

  public boolean getDelegPolicyState() {
    return mechCtxt != null ? mechCtxt.getDelegPolicy() : flags.delegPolicy;
  }
}
