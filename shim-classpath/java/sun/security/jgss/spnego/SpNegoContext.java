/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss.spnego;

import com.sun.security.jgss.InquireType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import sun.security.jgss.CtxFlags;
import sun.security.jgss.GSSManagerImpl;
import sun.security.jgss.krb5.Krb5Context;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.krb5.Asn1;
import sun.security.krb5.Creds;
import sun.security.krb5.PrincipalName;

public final class SpNegoContext implements GSSContextSpi {
  private static final int kAcceptCompleted = 0;
  private static final int kAcceptIncomplete = 1;
  private static final int kReject = 2;

  private final boolean initiator;
  private final CtxFlags flags;
  private final Krb5Context inner;
  private boolean established;
  private boolean started;
  private byte[] mechTypesDer;

  public SpNegoContext(boolean initiator, CtxFlags f, GSSName peer, PrincipalName want, Creds tgt, Subject s) {
    this.initiator = initiator;
    this.flags = f;
    this.inner = new Krb5Context(initiator, f, peer, want, tgt, s);
  }

  private static GSSException defective(String m) {
    return new GSSException(GSSException.DEFECTIVE_TOKEN, -1, m);
  }

  private static byte[] enumerated(int v) {
    return Asn1.tlv(0x0a, new byte[] {(byte) v});
  }

  private byte[] reqFlags() {
    int v = (flags.deleg ? 0x80 : 0) | (flags.mutual ? 0x40 : 0) | (flags.replay ? 0x20 : 0) | (flags.seq ? 0x10 : 0)
        | (flags.anon ? 0x08 : 0) | (flags.conf ? 0x04 : 0) | (flags.integ ? 0x02 : 0);
    return Asn1.tlv(Asn1.kBitString, new byte[] {1, (byte) v});
  }

  private static byte[] resp(int state, Oid mech, byte[] token, byte[] mic) {
    return Asn1.tlv(0xa1, Asn1.seq(Asn1.ctx(0, enumerated(state)), mech == null ? null : Asn1.ctx(1, mech.getDER()),
        token == null ? null : Asn1.ctx(2, Asn1.octets(token)), mic == null ? null : Asn1.ctx(3, Asn1.octets(mic))));
  }

  public byte[] init(byte[] in, int off, int len) throws GSSException {
    if (!initiator || established) {
      throw new GSSException(GSSException.FAILURE, -1, "Illegal call to initSecContext");
    }
    if (!started) {
      started = true;
      byte[] token = inner.init(in, 0, 0);
      mechTypesDer = Asn1.seq(GSSManagerImpl.KRB5.getDER());
      byte[] init = Asn1.tlv(0xa0, Asn1.seq(Asn1.ctx(0, mechTypesDer), Asn1.ctx(1, reqFlags()),
          token == null ? null : Asn1.ctx(2, Asn1.octets(token))));
      return Asn1.tlv(0x60, Asn1.cat(GSSManagerImpl.SPNEGO.getDER(), init));
    }
    try {
      Asn1.Node n = Asn1.read(in, off, off + len);
      if (n.tag != 0xa1) {
        throw defective("SPNEGO NegTokenResp expected");
      }
      Asn1.Node seq = n.inner();
      Asn1.Node st = seq.field(0);
      int state = st == null ? kAcceptIncomplete : (st.content()[0] & 0xff);
      if (state == kReject) {
        throw new GSSException(GSSException.FAILURE, -1, "SPNEGO negotiation rejected by the acceptor");
      }
      Asn1.Node rt = seq.field(2);
      byte[] out = null;
      if (rt != null) {
        byte[] t = rt.content();
        out = inner.init(t, 0, t.length);
      }
      if (inner.isEstablished()) {
        established = true;
        return out == null ? null : resp(kAcceptCompleted, null, out, null);
      }
      return out == null ? null : resp(kAcceptIncomplete, null, out, null);
    } catch (IOException e) {
      throw defective("Invalid SPNEGO token");
    }
  }

  public byte[] accept(byte[] in, int off, int len) throws GSSException {
    if (initiator || established) {
      throw new GSSException(GSSException.FAILURE, -1, "Illegal call to acceptSecContext");
    }
    try {
      if (!started) {
        started = true;
        return acceptFirst(in, off, len);
      }
      Asn1.Node n = Asn1.read(in, off, off + len);
      if (n.tag != 0xa1) {
        throw defective("SPNEGO NegTokenResp expected");
      }
      Asn1.Node rt = n.inner().field(2);
      byte[] out = null;
      if (rt != null) {
        byte[] t = rt.content();
        out = inner.accept(t, 0, t.length);
      }
      return finish(out, false);
    } catch (IOException e) {
      throw defective("Invalid SPNEGO token");
    }
  }

  private byte[] finish(byte[] out, boolean first) {
    if (inner.isEstablished()) {
      established = true;
      return resp(kAcceptCompleted, first ? GSSManagerImpl.KRB5 : null, out, null);
    }
    return resp(kAcceptIncomplete, first ? GSSManagerImpl.KRB5 : null, out, null);
  }

  private byte[] acceptFirst(byte[] in, int off, int len) throws IOException, GSSException {
    int p = off;
    int end = off + len;
    if (in[p] == 0x60) {
      Asn1.Node app = Asn1.read(in, p, end);
      Asn1.Node o = Asn1.read(in, app.off, app.off + app.len);
      p = app.off + o.total;
      end = app.off + app.len;
    }
    if (p >= end) {
      throw defective("Invalid SPNEGO NegTokenInit token : Too short");
    }
    Asn1.Node tok = Asn1.read(in, p, end);
    if (tok.tag != 0xa0) {
      throw defective("SPNEGO NegTokenInit expected");
    }
    Asn1.Node seq = tok.inner();
    Asn1.Node mt = seq.need(0);
    List<Oid> mechs = new ArrayList<Oid>();
    for (Asn1.Node m : mt.children()) {
      mechs.add(new Oid(m.encoded()));
    }
    Oid chosen = null;
    for (Oid m : mechs) {
      if (m.equals(GSSManagerImpl.KRB5) || m.equals(GSSManagerImpl.KRB5_MS)) {
        chosen = m;
        break;
      }
    }
    if (chosen == null) {
      established = false;
      return resp(kReject, null, null, null);
    }
    Asn1.Node mtok = seq.field(2);
    byte[] out = null;
    if (mtok != null && chosen.equals(mechs.get(0))) {
      byte[] t = mtok.content();
      out = inner.accept(t, 0, t.length);
    }
    return finish(out, true);
  }

  public boolean isEstablished() {
    return established;
  }

  public boolean isProtReady() {
    return established;
  }

  public byte[] wrap(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    return inner.wrap(in, off, len, prop);
  }

  public byte[] unwrap(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    return inner.unwrap(in, off, len, prop);
  }

  public byte[] getMIC(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    return inner.getMIC(in, off, len, prop);
  }

  public void verifyMIC(byte[] tok, int tokOff, int tokLen, byte[] msg, int msgOff, int msgLen, MessageProp prop)
      throws GSSException {
    inner.verifyMIC(tok, tokOff, tokLen, msg, msgOff, msgLen, prop);
  }

  public int getWrapSizeLimit(int qop, boolean conf, int max) throws GSSException {
    return inner.getWrapSizeLimit(qop, conf, max);
  }

  public GSSName getSrcName() throws GSSException {
    return inner.getSrcName();
  }

  public GSSName getTargName() throws GSSException {
    return inner.getTargName();
  }

  public int getLifetime() {
    return initiator ? inner.getLifetime() : Integer.MAX_VALUE;
  }

  public Oid getMech() {
    return established ? GSSManagerImpl.KRB5 : GSSManagerImpl.SPNEGO;
  }

  public boolean getMutualAuth() {
    return inner.getMutualAuth();
  }

  public boolean getReplayDet() {
    return inner.getReplayDet();
  }

  public boolean getSequenceDet() {
    return inner.getSequenceDet();
  }

  public boolean getConf() {
    return inner.getConf();
  }

  public boolean getInteg() {
    return inner.getInteg();
  }

  public boolean getCredDeleg() {
    return inner.getCredDeleg();
  }

  public boolean getDelegPolicy() {
    return inner.getDelegPolicy();
  }

  public org.ietf.jgss.GSSCredential getDelegCred() throws GSSException {
    return inner.getDelegCred();
  }

  public boolean getAnonymity() {
    return inner.getAnonymity();
  }

  public boolean isTransferable() {
    return false;
  }

  public Object inquire(InquireType type) throws GSSException {
    return inner.inquire(type);
  }

  public void dispose() {
    established = false;
    inner.dispose();
  }
}
