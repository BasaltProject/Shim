/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss.krb5;

import com.sun.security.jgss.AuthorizationDataEntry;
import com.sun.security.jgss.InquireType;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.EncryptionKey;
import javax.security.auth.kerberos.KerberosCredMessage;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;
import sun.security.jgss.CtxFlags;
import sun.security.jgss.GSSExceptionImpl;
import sun.security.jgss.GSSManagerImpl;
import sun.security.jgss.GSSNameImpl;
import sun.security.jgss.spi.GSSContextSpi;
import sun.security.krb5.Asn1;
import sun.security.krb5.CCache;
import sun.security.krb5.Config;
import sun.security.krb5.Creds;
import sun.security.krb5.Crypto;
import sun.security.krb5.KrbCred;
import sun.security.krb5.KrbException;
import sun.security.krb5.KrbProto;
import sun.security.krb5.PrincipalName;

public final class Krb5Context implements GSSContextSpi {
  private static final SecureRandom kRandom = new SecureRandom();
  private static final Set<String> kReplay = new HashSet<String>();
  private static final int kForwardable = 1;
  private static final int kOkAsDelegate = 15;
  private static final int kDeleg = 1;
  private static final int kMutual = 2;
  private static final int kReplayFlag = 4;
  private static final int kSeq = 8;
  private static final int kConf = 16;
  private static final int kInteg = 32;
  private static final int kAnon = 64;

  private final boolean initiator;
  private final CtxFlags req;
  private final GSSName targetName;
  private final PrincipalName wantClient;
  private final Creds cred;
  private final Subject credSubject;

  private int step;
  private boolean established;
  private boolean mutual;
  private boolean replay;
  private boolean seq;
  private boolean conf;
  private boolean integ;
  private boolean anon;
  private Krb5Protect prot;
  private PrincipalName src;
  private PrincipalName targ;
  private long endTime;
  private long authTime;
  private int tktFlags;
  private byte[] authz;
  private byte[] sessionKey;
  private int sessionEtype;
  private Creds service;
  private long ctime;
  private int cusec;
  private byte[] initSubkey;
  private int initSubkeyEtype;
  private long initSeq;
  private KerberosCredMessage delegSent;
  private boolean delegWire;
  private Creds initTgt;
  private Creds delegated;
  private boolean delegPolicy;

  public Krb5Context(boolean initiator, CtxFlags req, GSSName target, PrincipalName wantClient, Creds cred,
      Subject credSubject) {
    this.initiator = initiator;
    this.req = req;
    this.targetName = target;
    this.wantClient = wantClient;
    this.cred = cred;
    this.credSubject = credSubject;
  }

  public static byte[] frame(Oid mech, int id, byte[] body) {
    byte[] der = mech.getDER();
    byte[] inner = new byte[der.length + 2 + body.length];
    System.arraycopy(der, 0, inner, 0, der.length);
    inner[der.length] = (byte) (id >> 8);
    inner[der.length + 1] = (byte) id;
    System.arraycopy(body, 0, inner, der.length + 2, body.length);
    return Asn1.tlv(0x60, inner);
  }

  static int[] unframe(byte[] in, int off, int len) throws GSSException {
    try {
      if (len < 2 || in[off] != 0x60) {
        throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1, "GSSHeader did not find the right tag");
      }
      Asn1.Node n = Asn1.read(in, off, off + len);
      int p = n.off;
      Asn1.Node o = Asn1.read(in, p, n.off + n.len);
      Oid mech = new Oid(java.util.Arrays.copyOfRange(in, p, p + o.total));
      if (!mech.equals(GSSManagerImpl.KRB5) && !mech.equals(GSSManagerImpl.KRB5_MS)) {
        throw new GSSExceptionImpl(GSSException.BAD_MECH, mech);
      }
      p += o.total;
      return new int[] {p + 2, ((in[p] & 0xff) << 8) | (in[p + 1] & 0xff), n.off + n.len};
    } catch (IOException e) {
      throw new GSSException(GSSException.FAILURE, -1, null);
    }
  }

  private static GSSException krb(int major, KrbException e) {
    GSSException g = new GSSException(major, -1, e.getMessage());
    g.initCause(e);
    return g;
  }

  public byte[] init(byte[] in, int off, int len) throws GSSException {
    if (!initiator || established) {
      throw new GSSExceptionImpl(GSSException.FAILURE, "Illegal call to initSecContext");
    }
    if (step == 0) {
      return firstInit();
    }
    return finishInit(in, off, len);
  }

  private Creds findTgt() throws GSSException {
    Subject s = credSubject != null ? credSubject : Krb5Util.currentSubject();
    Creds tgt = Krb5Util.findTgt(s, wantClient);
    if (tgt == null && !"true".equalsIgnoreCase(System.getProperty("javax.security.auth.useSubjectCredsOnly", "true"))) {
      try {
        String path = CCache.defaultPath();
        if (path != null && new java.io.File(path).isFile()) {
          for (Creds c : CCache.read(new java.io.File(path)).creds) {
            if (c.server.isKrbtgt() && c.endTime > System.currentTimeMillis()) {
              return c;
            }
          }
        }
      } catch (IOException e) {
        // fall through to the missing credential error
      }
    }
    if (tgt == null) {
      throw new GSSException(GSSException.NO_CRED, -1, "Failed to find any Kerberos tgt");
    }
    return tgt;
  }

  private byte[] firstInit() throws GSSException {
    Creds tgt = cred != null && cred.isTgt() ? cred : findTgt();
    PrincipalName server = ((GSSNameImpl) targetName).principal();
    Subject subj = credSubject != null ? credSubject : Krb5Util.currentSubject();
    Creds svc = tgt.s4uTicket != null ? null : Krb5Util.findService(subj, tgt.client, server);
    if (svc == null) {
      try {
        svc = tgt.s4uTicket != null ? KrbProto.s4uProxy(tgt, server, tgt.s4uTicket) : acquire(tgt, server);
      } catch (KrbException e) {
        throw krb(GSSException.NO_CRED, e);
      }
      if (subj != null && !subj.isReadOnly() && tgt.s4uTicket == null) {
        try {
          subj.getPrivateCredentials().add(Krb5Util.ticket(svc));
        } catch (SecurityException | IllegalStateException e) {
          // the subject cannot cache the ticket
        }
      }
    }
    service = svc;
    src = svc.client;
    sessionKey = svc.key;
    sessionEtype = svc.keyType;
    endTime = svc.endTime;
    authTime = svc.authTime;
    tktFlags = svc.flags;
    long now = System.currentTimeMillis();
    ctime = now / 1000 * 1000;
    cusec = (int) (now % 1000) * 1000;
    initSubkeyEtype = sessionEtype;
    initSubkey = Crypto.randomKey(initSubkeyEtype);
    initSeq = kRandom.nextInt() & 0x7fffffffL;
    int gssFlags = (req.mutual ? kMutual : 0) | (req.replay ? kReplayFlag : 0) | (req.seq ? kSeq : 0)
        | (req.conf ? kConf : 0) | (req.integ ? kInteg : 0) | (req.anon ? kAnon : 0);
    initTgt = tgt;
    byte[] credMsg = null;
    boolean wantDeleg = req.deleg || (req.delegPolicy && svc.flag(kOkAsDelegate));
    if (wantDeleg && tgt.flag(kForwardable)) {
      try {
        credMsg = KrbCred.encode(KrbProto.forwardedTgt(tgt), svc);
      } catch (KrbException e) {
        throw krb(GSSException.FAILURE, e);
      }
      gssFlags |= kDeleg;
      delegSent = credMessage(svc, credMsg);
      delegWire = true;
    }
    delegPolicy = req.delegPolicy && svc.flag(kOkAsDelegate);
    byte[] cksum = new byte[credMsg == null ? 24 : 28 + credMsg.length];
    if (credMsg != null) {
      cksum[24] = 1;
      cksum[26] = (byte) credMsg.length;
      cksum[27] = (byte) (credMsg.length >> 8);
      System.arraycopy(credMsg, 0, cksum, 28, credMsg.length);
    }
    cksum[0] = 16;
    byte[] bnd = req.cb == null ? null : channelBinding(req.cb);
    if (bnd != null) {
      System.arraycopy(bnd, 0, cksum, 4, 16);
    }
    cksum[20] = (byte) gssFlags;
    cksum[21] = (byte) (gssFlags >> 8);
    byte[] auth = Asn1.app(2, Asn1.seq(Asn1.ctx(0, Asn1.integer(5)),
        Asn1.ctx(1, Asn1.general(svc.client.getRealmString())), Asn1.ctx(2, svc.client.encode()),
        Asn1.ctx(3, Asn1.seq(Asn1.ctx(0, Asn1.integer(0x8003)), Asn1.ctx(1, Asn1.octets(cksum)))),
        Asn1.ctx(4, Asn1.integer(cusec)), Asn1.ctx(5, Asn1.time(ctime)),
        Asn1.ctx(6, KrbProto.keyBlock(initSubkeyEtype, initSubkey)), Asn1.ctx(7, Asn1.integer(initSeq))));
    try {
      byte[] ap = KrbProto.apReq(svc, auth, 11, req.mutual ? 0x20000000 : 0);
      byte[] tok = frame(GSSManagerImpl.KRB5, 0x0100, ap);
      mutual = req.mutual;
      replay = req.replay;
      seq = req.seq;
      conf = req.conf;
      integ = req.integ;
      anon = req.anon;
      step = 1;
      if (!req.mutual) {
        prot = new Krb5Protect(initSubkeyEtype, initSubkey, true, false, initSeq, 0, replay, seq);
        established = true;
        targ = svc.server;
        step = 2;
      }
      return tok;
    } catch (GeneralSecurityException e) {
      throw new GSSException(GSSException.FAILURE, -1, e.getMessage());
    }
  }

  private static Creds acquire(Creds tgt, PrincipalName server) throws KrbException {
    String realm = server.getRealmString();
    Creds t = tgt;
    if (realm != null && !realm.equals(tgt.server.getRealmString())) {
      PrincipalName cross = new PrincipalName(PrincipalName.KRB_NT_SRV_INST, new String[] {"krbtgt", realm},
          tgt.server.getRealmString());
      t = KrbProto.tgsExchange(tgt, cross);
    }
    return KrbProto.tgsExchange(t, server);
  }

  private byte[] channelBinding(org.ietf.jgss.ChannelBinding cb) throws GSSException {
    java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
    writeLe(o, cb.getInitiatorAddress() == null ? 0 : 2);
    if (cb.getInitiatorAddress() != null) {
      byte[] a = cb.getInitiatorAddress().getAddress();
      writeLe(o, a.length);
      o.write(a, 0, a.length);
    }
    writeLe(o, cb.getAcceptorAddress() == null ? 0 : 2);
    if (cb.getAcceptorAddress() != null) {
      byte[] a = cb.getAcceptorAddress().getAddress();
      writeLe(o, a.length);
      o.write(a, 0, a.length);
    }
    byte[] d = cb.getApplicationData();
    writeLe(o, d == null ? 0 : d.length);
    if (d != null) {
      o.write(d, 0, d.length);
    }
    try {
      return java.security.MessageDigest.getInstance("MD5").digest(o.toByteArray());
    } catch (GeneralSecurityException e) {
      throw new GSSException(GSSException.FAILURE, -1, e.getMessage());
    }
  }

  private static void writeLe(java.io.ByteArrayOutputStream o, int v) {
    o.write(v);
    o.write(v >> 8);
    o.write(v >> 16);
    o.write(v >> 24);
  }

  private byte[] finishInit(byte[] in, int off, int len) throws GSSException {
    int[] h = unframe(in, off, len);
    try {
      if (h[1] == 0x0300) {
        Asn1.Node err = Asn1.read(in, h[0], h[2]).inner();
        throw krb(GSSException.FAILURE, KrbProto.toException(err));
      }
      if (h[1] != 0x0200) {
        throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1, "Defective Token ID!");
      }
      Asn1.Node rep = Asn1.read(in, h[0], h[2]).inner();
      Asn1.Node ep = rep.need(2);
      byte[] plain = Crypto.decrypt(ep.need(0).asInt(), sessionKey, 12, ep.need(2).content());
      Asn1.Node e = Asn1.parse(plain).inner();
      if (e.need(0).asTime() != ctime || e.need(1).asInt() != cusec) {
        throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1, "Mutual authentication failed");
      }
      int etype = initSubkeyEtype;
      byte[] key = initSubkey;
      boolean acceptorSub = false;
      Asn1.Node sk = e.field(2);
      if (sk != null) {
        etype = sk.need(0).asInt();
        key = sk.need(1).content();
        acceptorSub = true;
      }
      long peerSeq = e.field(3) == null ? 0 : e.field(3).asLong();
      prot = new Krb5Protect(etype, key, true, acceptorSub, initSeq, peerSeq, replay, seq);
      targ = service.server;
      established = true;
      step = 2;
      return null;
    } catch (IOException | GeneralSecurityException ex) {
      GSSException g = new GSSException(GSSException.DEFECTIVE_TOKEN, -1, "Mutual authentication failed");
      g.initCause(ex);
      throw g;
    }
  }

  public byte[] accept(byte[] in, int off, int len) throws GSSException {
    if (initiator || established || step != 0) {
      throw new GSSExceptionImpl(GSSException.FAILURE, "Illegal call to acceptSecContext");
    }
    Subject subj = credSubject != null ? credSubject : Krb5Util.currentSubject();
    if (!Krb5Util.hasAnyAcceptKey(subj)) {
      throw new GSSException(GSSException.NO_CRED, -1, "Failed to find any Kerberos credentials");
    }
    int[] h = unframe(in, off, len);
    if (h[1] != 0x0100) {
      throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1, "Defective Token ID!");
    }
    try {
      Asn1.Node ap = Asn1.read(in, h[0], h[2]).inner();
      if (ap.need(1).asInt() != 14) {
        throw new KrbException(KrbException.KRB_AP_ERR_MSG_TYPE);
      }
      int apOptions = ap.need(2).asBits32();
      Asn1.Node tkt = ap.need(3).inner();
      PrincipalName server = PrincipalName.decode(tkt.need(2), tkt.need(1).asString());
      Asn1.Node te = tkt.need(3);
      int tet = te.need(0).asInt();
      int kvno = te.field(1) == null ? 0 : te.field(1).asInt();
      if (wantClientCheckFails(subj, server)) {
        throw new KrbException(KrbException.KRB_AP_ERR_NOT_US);
      }
      byte[] skey = Krb5Util.findKey(subj, server, tet, kvno);
      if (skey == null) {
        throw new KrbException("Invalid argument (400) - Cannot find key of appropriate type to decrypt AP-REQ - "
            + Crypto.description(tet));
      }
      byte[] encTkt = Crypto.decrypt(tet, skey, 2, te.need(2).content());
      Asn1.Node et = Asn1.parse(encTkt).inner();
      tktFlags = et.need(0).asBits32();
      Asn1.Node kb = et.need(1);
      sessionEtype = kb.need(0).asInt();
      sessionKey = kb.need(1).content();
      PrincipalName client = PrincipalName.decode(et.need(3), et.need(2).asString());
      authTime = et.need(5).asTime();
      long start = et.field(6) == null ? authTime : et.field(6).asTime();
      endTime = et.need(7).asTime();
      Asn1.Node ad = et.field(10);
      authz = ad == null ? null : ad.encoded();
      Asn1.Node ae = ap.need(4);
      byte[] ab = Crypto.decrypt(ae.need(0).asInt(), sessionKey, 11, ae.need(2).content());
      Asn1.Node au = Asn1.parse(ab).inner();
      PrincipalName ac = PrincipalName.decode(au.need(2), au.need(1).asString());
      if (!ac.equals(client)) {
        throw new KrbException(KrbException.KRB_AP_ERR_BADMATCH);
      }
      long skew = Config.getInstance().getInt(300, "libdefaults", "clockskew") * 1000L;
      long now = System.currentTimeMillis();
      long ct = au.need(5).asTime();
      int cu = au.need(4).asInt();
      if (Math.abs(now - (ct + cu / 1000)) > skew) {
        throw new KrbException(KrbException.KRB_AP_ERR_SKEW);
      }
      if (start - skew > now) {
        throw new KrbException(KrbException.KRB_AP_ERR_TKT_NYV);
      }
      if (endTime + skew < now) {
        throw new KrbException(KrbException.KRB_AP_ERR_TKT_EXPIRED);
      }
      String rkey = client + "|" + ct + "|" + cu + "|" + server;
      synchronized (kReplay) {
        if (!kReplay.add(rkey)) {
          throw new KrbException(KrbException.KRB_AP_ERR_REPEAT);
        }
      }
      int gssFlags = 0;
      Asn1.Node ck = au.field(3);
      if (ck != null && ck.need(0).asInt() == 0x8003) {
        byte[] v = ck.need(1).content();
        if (v.length >= 24) {
          gssFlags = (v[20] & 0xff) | ((v[21] & 0xff) << 8);
          if ((gssFlags & kDeleg) != 0 && v.length >= 28 && v[24] == 1) {
            int dl = (v[26] & 0xff) | ((v[27] & 0xff) << 8);
            Creds sealer = new Creds();
            sealer.key = sessionKey;
            sealer.keyType = sessionEtype;
            delegated = KrbCred.decode(java.util.Arrays.copyOfRange(v, 28, 28 + dl), sealer);
          }
          if (req.cb != null) {
            byte[] want = channelBinding(req.cb);
            byte[] got = java.util.Arrays.copyOfRange(v, 4, 20);
            if (!java.util.Arrays.equals(want, got)) {
              throw new GSSException(GSSException.BAD_BINDINGS, -1, java.util.Arrays.equals(got, new byte[16])
                  ? "Token missing ChannelBinding!" : "Bytes mismatch!");
            }
          }
        }
      }
      Asn1.Node isub = au.field(6);
      int etype = sessionEtype;
      byte[] key = sessionKey;
      if (isub != null) {
        etype = isub.need(0).asInt();
        key = isub.need(1).content();
      }
      long peerSeq = au.field(7) == null ? 0 : au.field(7).asLong();
      boolean wantMutual = (apOptions & 0x20000000) != 0;
      mutual = wantMutual;
      replay = (gssFlags & kReplayFlag) != 0;
      seq = (gssFlags & kSeq) != 0;
      conf = (gssFlags & kConf) != 0;
      integ = (gssFlags & kInteg) != 0;
      anon = (gssFlags & kAnon) != 0;
      src = client;
      targ = server;
      long mySeq = wantMutual ? kRandom.nextInt() & 0x7fffffffL : peerSeq;
      prot = new Krb5Protect(etype, key, false, false, mySeq, peerSeq, replay, seq);
      established = true;
      step = 2;
      if (!wantMutual) {
        return null;
      }
      byte[] encRep = Asn1.app(27, Asn1.seq(Asn1.ctx(0, Asn1.time(ct)), Asn1.ctx(1, Asn1.integer(cu)),
          Asn1.ctx(3, Asn1.integer(mySeq))));
      byte[] enc = Crypto.encrypt(sessionEtype, sessionKey, 12, encRep);
      byte[] rep = Asn1.app(15, Asn1.seq(Asn1.ctx(0, Asn1.integer(5)), Asn1.ctx(1, Asn1.integer(15)),
          Asn1.ctx(2, KrbProto.encData(sessionEtype, -1, enc))));
      return frame(GSSManagerImpl.KRB5, 0x0200, rep);
    } catch (KrbException e) {
      throw krb(GSSException.FAILURE, e);
    } catch (GeneralSecurityException e) {
      GSSException g = new GSSException(GSSException.FAILURE, -1, "Checksum failed");
      g.initCause(e);
      throw g;
    } catch (IOException e) {
      String m = e.getMessage() != null && e.getMessage().startsWith("truncated") ? null
          : "Identifier doesn't match expected value (906)";
      GSSException g = new GSSException(GSSException.FAILURE, -1, m);
      g.initCause(e);
      throw g;
    }
  }

  private boolean wantClientCheckFails(Subject subj, PrincipalName server) {
    return wantClient != null && !wantClient.equals(server);
  }

  public boolean isEstablished() {
    return established;
  }

  public boolean isProtReady() {
    return established;
  }

  private void checkReady(String what) throws GSSException {
    if (!established) {
      throw new GSSException(GSSException.NO_CONTEXT, -1, what + " called in invalid state!");
    }
  }

  public byte[] wrap(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    checkReady("Wrap");
    boolean c = prop != null ? prop.getPrivacy() : conf;
    byte[] t = prot.wrap(in, off, len, c && conf);
    if (prop != null) {
      prop.setPrivacy(c && conf);
      prop.setQOP(0);
    }
    return t;
  }

  public byte[] unwrap(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    checkReady("Unwrap");
    return prot.unwrap(in, off, len, prop);
  }

  public byte[] getMIC(byte[] in, int off, int len, MessageProp prop) throws GSSException {
    checkReady("getMIC");
    return prot.getMIC(in, off, len);
  }

  public void verifyMIC(byte[] tok, int tokOff, int tokLen, byte[] msg, int msgOff, int msgLen, MessageProp prop)
      throws GSSException {
    checkReady("verifyMIC");
    prot.verifyMIC(tok, tokOff, tokLen, msg, msgOff, msgLen, prop);
  }

  public int getWrapSizeLimit(int qop, boolean confReq, int max) throws GSSException {
    checkReady("getWrapSizeLimit");
    return prot.wrapSizeLimit(max);
  }

  public GSSName getSrcName() throws GSSException {
    if (initiator && initTgt != null && initTgt.s4uName != null) {
      return initTgt.s4uName;
    }
    return GSSNameImpl.of(src);
  }

  public GSSName getTargName() throws GSSException {
    if (initiator) {
      return targetName;
    }
    return GSSNameImpl.of(targ);
  }

  public int getLifetime() {
    if (!established && !initiator) {
      return 0;
    }
    if (endTime == 0) {
      return 0;
    }
    long s = (endTime - System.currentTimeMillis()) / 1000;
    return s > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(s, 0);
  }

  public Oid getMech() {
    return GSSManagerImpl.KRB5;
  }

  public boolean getMutualAuth() {
    return mutual;
  }

  public boolean getReplayDet() {
    return replay;
  }

  public boolean getSequenceDet() {
    return seq;
  }

  public boolean getConf() {
    return conf;
  }

  public boolean getInteg() {
    return integ;
  }

  private static KerberosCredMessage credMessage(Creds svc, byte[] msg) {
    return new KerberosCredMessage(new KerberosPrincipal(svc.client.toString(), svc.client.getNameType()),
        new KerberosPrincipal(svc.server.toString(), svc.server.getNameType()), msg);
  }

  private KerberosCredMessage lazyCredMessage() throws GSSException {
    if (delegSent == null && initTgt != null && initTgt.flag(kForwardable)) {
      try {
        delegSent = credMessage(service, KrbCred.encode(KrbProto.forwardedTgt(initTgt), service));
      } catch (KrbException e) {
        throw krb(GSSException.FAILURE, e);
      }
    }
    return delegSent;
  }

  public boolean getCredDeleg() {
    return delegWire || delegated != null;
  }

  public boolean getDelegPolicy() {
    return delegPolicy;
  }

  public GSSCredential getDelegCred() throws GSSException {
    if (delegated == null) {
      throw new GSSException(GSSException.NO_CRED);
    }
    return Krb5Util.initCredential(delegated);
  }

  public boolean getAnonymity() {
    return anon;
  }

  public boolean isTransferable() {
    return false;
  }

  public void dispose() {
    established = false;
    prot = null;
  }

  private static final class KerberosSessionKey implements Key {
    private static final long serialVersionUID = 1L;
    private final int etype;
    private final byte[] bytes;

    KerberosSessionKey(int etype, byte[] bytes) {
      this.etype = etype;
      this.bytes = bytes.clone();
    }

    public String getAlgorithm() {
      return Crypto.name(etype);
    }

    public String getFormat() {
      return "RAW";
    }

    public byte[] getEncoded() {
      return bytes.clone();
    }
  }

  public Object inquire(InquireType type) throws GSSException {
    switch (type) {
      case KRB5_GET_SESSION_KEY:
        return new KerberosSessionKey(prot.etype(), prot.key());
      case KRB5_GET_SESSION_KEY_EX:
        return new EncryptionKey(prot.key(), prot.etype());
      case KRB5_GET_TKT_FLAGS: {
        boolean[] f = new boolean[32];
        for (int i = 0; i < 32; i++) {
          f[i] = (tktFlags & (0x80000000 >>> i)) != 0;
        }
        return f;
      }
      case KRB5_GET_AUTHZ_DATA:
        if (initiator) {
          throw new GSSException(GSSException.UNAVAILABLE, -1, "AuthzData not available on initiator side.");
        }
        return authzEntries();
      case KRB5_GET_AUTHTIME: {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new java.util.Date(authTime));
      }
      case KRB5_GET_KRB_CRED:
        if (!initiator) {
          throw new GSSException(GSSException.UNAVAILABLE, -1, "KRB_CRED not available on acceptor side.");
        }
        return lazyCredMessage();
      default:
        throw new GSSException(GSSException.UNAVAILABLE, -1, "Inquire type not supported.");
    }
  }

  private AuthorizationDataEntry[] authzEntries() throws GSSException {
    if (authz == null) {
      return null;
    }
    try {
      List<AuthorizationDataEntry> out = new ArrayList<AuthorizationDataEntry>();
      collect(Asn1.parse(authz), out);
      return out.toArray(new AuthorizationDataEntry[0]);
    } catch (IOException e) {
      return null;
    }
  }

  private static void collect(Asn1.Node seq, List<AuthorizationDataEntry> out) throws IOException {
    for (Asn1.Node e : seq.children()) {
      int t = e.need(0).asInt();
      byte[] d = e.need(1).content();
      if (t == 1) {
        collect(Asn1.parse(d), out);
      } else {
        out.add(new AuthorizationDataEntry(t, d));
      }
    }
  }
}
