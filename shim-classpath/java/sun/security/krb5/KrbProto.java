/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KrbProto {
  public static final int kForwardable = 1;
  public static final int kForwarded = 2;
  public static final int kCnameInAddlTkt = 14;
  public static final int kRenewable = 8;
  public static final int kCanonicalize = 15;
  public static final int kRenewableOk = 27;
  public static final int kRenew = 30;

  public interface KeyProvider {
    byte[] key(int etype, String salt, byte[] params) throws KrbException;
  }

  private static final SecureRandom kRandom = new SecureRandom();

  private KrbProto() {
  }

  public static byte[] padata(int type, byte[] value) {
    return Asn1.seq(Asn1.ctx(1, Asn1.integer(type)), Asn1.ctx(2, Asn1.octets(value)));
  }

  public static byte[] encData(int etype, int kvno, byte[] cipher) {
    return Asn1.seq(Asn1.ctx(0, Asn1.integer(etype)), kvno >= 0 ? Asn1.ctx(1, Asn1.integer(kvno)) : null,
        Asn1.ctx(2, Asn1.octets(cipher)));
  }

  public static byte[] keyBlock(int etype, byte[] key) {
    return Asn1.seq(Asn1.ctx(0, Asn1.integer(etype)), Asn1.ctx(1, Asn1.octets(key)));
  }

  static byte[] principal(PrincipalName p) {
    return p.encode();
  }

  private static byte[] addresses() {
    List<byte[]> out = new ArrayList<byte[]>();
    try {
      for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
        for (InetAddress a : Collections.list(ni.getInetAddresses())) {
          out.add(Asn1.seq(Asn1.ctx(0, Asn1.integer(a instanceof Inet6Address ? 24 : 2)),
              Asn1.ctx(1, Asn1.octets(a.getAddress()))));
        }
      }
    } catch (IOException e) {
      return null;
    }
    return out.isEmpty() ? null : Asn1.seq(out.toArray(new byte[0][]));
  }

  private static int options(Config cfg, boolean tgs) {
    int o = 0;
    if (Boolean.TRUE.equals(cfg.getBoolean("libdefaults", "forwardable"))) {
      o |= 0x80000000 >>> kForwardable;
    }
    if (cfg.getDurationSeconds(0, "libdefaults", "renew_lifetime") > 0) {
      o |= 0x80000000 >>> kRenewable;
    } else if (Boolean.TRUE.equals(cfg.getBoolean("libdefaults", "renewable"))) {
      o |= 0x80000000 >>> kRenewableOk;
    }
    if (Boolean.TRUE.equals(cfg.getBoolean("libdefaults", "canonicalize")) || tgs) {
      o |= 0x80000000 >>> kCanonicalize;
    }
    return o;
  }

  private static byte[] reqBody(Config cfg, PrincipalName cname, PrincipalName sname, String realm, int options,
      int[] etypes, int nonce, boolean tgs, byte[] additionalTicket) {
    long now = System.currentTimeMillis();
    int life = cfg.getDurationSeconds(0, "libdefaults", "ticket_lifetime");
    int renew = cfg.getDurationSeconds(0, "libdefaults", "renew_lifetime");
    byte[][] et = new byte[etypes.length][];
    for (int i = 0; i < et.length; i++) {
      et[i] = Asn1.integer(etypes[i]);
    }
    boolean noaddr = !Boolean.FALSE.equals(cfg.getBoolean("libdefaults", "noaddresses"));
    byte[] addrs = noaddr || tgs ? null : addresses();
    return Asn1.seq(Asn1.ctx(0, Asn1.bits32(options)), cname == null ? null : Asn1.ctx(1, cname.encode()),
        Asn1.ctx(2, Asn1.general(realm)), Asn1.ctx(3, sname.encode()),
        Asn1.ctx(5, Asn1.time(life > 0 ? now + life * 1000L : 0)),
        renew > 0 && (options & (0x80000000 >>> kRenewable)) != 0 ? Asn1.ctx(6, Asn1.time(now + renew * 1000L)) : null,
        Asn1.ctx(7, Asn1.integer(nonce)), Asn1.ctx(8, Asn1.seq(et)), addrs == null ? null : Asn1.ctx(9, addrs),
        additionalTicket == null ? null : Asn1.ctx(11, Asn1.seq(additionalTicket)));
  }

  public static KrbException toException(Asn1.Node err) throws IOException {
    int code = err.need(6).asInt();
    Asn1.Node t = err.field(11);
    byte[] ed = err.field(12) == null ? null : err.field(12).content();
    return KrbException.fromError(code, null, ed);
  }

  public static Creds asExchange(PrincipalName client, KeyProvider keys) throws KrbException {
    Config cfg = Config.getInstance();
    String realm = client.getRealmString() != null ? client.getRealmString() : cfg.defaultRealm();
    int[] etypes = cfg.etypes("default_tkt_enctypes");
    PrincipalName sname = new PrincipalName(PrincipalName.KRB_NT_SRV_INST, new String[] {"krbtgt", realm}, realm);
    int options = options(cfg, false);
    boolean canon = (options & (0x80000000 >>> kCanonicalize)) != 0;
    PrincipalName cn = new PrincipalName(PrincipalName.KRB_NT_PRINCIPAL, client.getNameStrings(), realm);
    byte[] preauth = null;
    String salt = null;
    byte[] params = null;
    int preEtype = 0;
    for (int attempt = 0; attempt < 3; attempt++) {
      int nonce = kRandom.nextInt() & 0x7fffffff;
      List<byte[]> pa = new ArrayList<byte[]>();
      if (preauth != null) {
        pa.add(preauth);
      }
      if (canon) {
        pa.add(padata(149, new byte[0]));
      }
      byte[] body = reqBody(cfg, cn, sname, realm, options, etypes, nonce, false, null);
      byte[] req = Asn1.app(10, Asn1.seq(Asn1.ctx(1, Asn1.integer(5)), Asn1.ctx(2, Asn1.integer(10)),
          pa.isEmpty() ? null : Asn1.ctx(3, Asn1.seq(pa.toArray(new byte[0][]))), Asn1.ctx(4, body)));
      Asn1.Node resp;
      try {
        resp = Asn1.parse(KdcComm.send(req, realm));
        if (resp.tag == 0x7e) {
          Asn1.Node err = resp.inner();
          int code = err.need(6).asInt();
          if (code == KrbException.KDC_ERR_PREAUTH_REQUIRED && preauth == null) {
            Object[] chosen = chooseEtype(err, etypes, cn, realm);
            preEtype = (Integer) chosen[0];
            salt = (String) chosen[1];
            params = (byte[]) chosen[2];
            byte[] k = keys.key(preEtype, salt, params);
            long now = System.currentTimeMillis();
            byte[] ts = Asn1.seq(Asn1.ctx(0, Asn1.time(now / 1000 * 1000)), Asn1.ctx(1, Asn1.integer(now % 1000 * 1000)));
            byte[] enc = Crypto.encrypt(preEtype, k, 1, ts);
            preauth = padata(2, encData(preEtype, -1, enc));
            continue;
          }
          throw toException(err);
        }
        return finishAs(resp, cfg, cn, keys, salt, params, nonce);
      } catch (GeneralSecurityException e) {
        throw new KrbCryptoException("Checksum failed", new GeneralSecurityException("Checksum failed"));
      } catch (IOException e) {
        KrbException ke = new KrbException(e.getMessage() == null ? e.toString() : e.getMessage());
        ke.initCause(e);
        throw ke;
      }
    }
    throw new KrbException(KrbException.KDC_ERR_PREAUTH_FAILED);
  }

  private static Object[] chooseEtype(Asn1.Node err, int[] etypes, PrincipalName cn, String realm)
      throws IOException {
    String dsalt = realm;
    for (String s : cn.getNameStrings()) {
      dsalt += s;
    }
    Asn1.Node ed = err.field(12);
    if (ed != null) {
      Asn1.Node md = Asn1.parse(ed.content());
      for (Asn1.Node p : md.children()) {
        int t = p.need(1).asInt();
        if (t == 19) {
          for (Asn1.Node e : Asn1.parse(p.need(2).content()).children()) {
            int et = e.need(0).asInt();
            if (contains(etypes, et)) {
              Asn1.Node s = e.field(1);
              Asn1.Node pr = e.field(2);
              return new Object[] {et, s == null ? dsalt : s.asString(), pr == null ? null : pr.content()};
            }
          }
        }
      }
    }
    return new Object[] {etypes[0], dsalt, Crypto.defaultString2KeyParams(etypes[0])};
  }

  private static boolean contains(int[] a, int v) {
    for (int x : a) {
      if (x == v) {
        return true;
      }
    }
    return false;
  }

  private static Creds finishAs(Asn1.Node resp, Config cfg, PrincipalName cn, KeyProvider keys, String salt,
      byte[] params, int nonce) throws IOException, GeneralSecurityException, KrbException {
    if (resp.tag != 0x6b) {
      throw new KrbException(KrbException.ASN1_BAD_ID);
    }
    Asn1.Node rep = resp.inner();
    Asn1.Node ep = rep.need(6);
    int et = ep.need(0).asInt();
    String repSalt = salt;
    byte[] repParams = params;
    Asn1.Node pas = rep.field(2);
    if (pas != null) {
      for (Asn1.Node p : pas.children()) {
        if (p.need(1).asInt() == 19) {
          for (Asn1.Node e : Asn1.parse(p.need(2).content()).children()) {
            if (e.need(0).asInt() == et) {
              repSalt = e.field(1) == null ? repSalt : e.field(1).asString();
              repParams = e.field(2) == null ? repParams : e.field(2).content();
            }
          }
        }
      }
    }
    if (repSalt == null) {
      repSalt = cn.getRealmString();
      for (String s : cn.getNameStrings()) {
        repSalt += s;
      }
    }
    byte[] k = keys.key(et, repSalt, repParams == null ? Crypto.defaultString2KeyParams(et) : repParams);
    if (k == null) {
      throw new KrbException(KrbException.KDC_ERR_ETYPE_NOSUPP);
    }
    byte[] plain = Crypto.decrypt(et, k, 3, ep.need(2).content());
    Asn1.Node enc = Asn1.parse(plain).inner();
    if (enc.need(2).asInt() != nonce) {
      throw new KrbException(KrbException.KRB_AP_ERR_MODIFIED);
    }
    return credsFrom(rep, enc);
  }

  private static Creds credsFrom(Asn1.Node rep, Asn1.Node enc) throws IOException {
    Creds c = new Creds();
    String crealm = rep.need(3).asString();
    c.client = PrincipalName.decode(rep.need(4), crealm);
    c.ticket = rep.need(5).encoded();
    Asn1.Node k = enc.need(0);
    c.keyType = k.need(0).asInt();
    c.key = k.need(1).content();
    c.flags = enc.need(4).asBits32();
    c.authTime = enc.need(5).asTime();
    c.startTime = enc.field(6) == null ? c.authTime : enc.field(6).asTime();
    c.endTime = enc.need(7).asTime();
    c.renewTill = enc.field(8) == null ? -1 : enc.field(8).asTime();
    Asn1.Node ca = enc.field(11);
    if (ca != null) {
      List<InetAddress> l = new ArrayList<InetAddress>();
      for (Asn1.Node a : ca.children()) {
        byte[] d = a.need(1).content();
        int t = a.need(0).asInt();
        if ((t == 2 && d.length == 4) || (t == 24 && d.length == 16)) {
          l.add(InetAddress.getByAddress(d));
        }
      }
      c.addresses = l.isEmpty() ? null : l.toArray(new InetAddress[0]);
    }
    c.server = PrincipalName.decode(enc.need(10), enc.need(9).asString());
    return c;
  }

  public static Creds tgsExchange(Creds tgt, PrincipalName server) throws KrbException {
    return tgs(tgt, server, 0, null, null);
  }

  public static Creds forwardedTgt(Creds tgt) throws KrbException {
    int options = (0x80000000 >>> kForwarded) | (tgt.flag(kForwardable) ? 0x80000000 >>> kForwardable : 0);
    return tgs(tgt, tgt.server, options, null, null);
  }

  public static Creds tgs(Creds tgt, PrincipalName server, int extraOptions, byte[] extraPadata,
      byte[] additionalTicket) throws KrbException {
    Config cfg = Config.getInstance();
    String realm = server.getRealmString() != null ? server.getRealmString() : tgt.client.getRealmString();
    try {
      int[] etypes = cfg.etypes(tgsKey(cfg));
      int options = options(cfg, true) | extraOptions;
      int nonce = kRandom.nextInt() & 0x7fffffff;
      byte[] body = reqBody(cfg, null, server, realm, options, etypes, nonce, true, additionalTicket);
      int ck = Crypto.defaultChecksum(tgt.keyType);
      byte[] sum = Crypto.checksum(ck, tgt.key, 6, body);
      long now = System.currentTimeMillis();
      byte[] auth = Asn1.app(2, Asn1.seq(Asn1.ctx(0, Asn1.integer(5)),
          Asn1.ctx(1, Asn1.general(tgt.client.getRealmString())), Asn1.ctx(2, tgt.client.encode()),
          Asn1.ctx(3, Asn1.seq(Asn1.ctx(0, Asn1.integer(ck)), Asn1.ctx(1, Asn1.octets(sum)))),
          Asn1.ctx(4, Asn1.integer(now % 1000 * 1000)), Asn1.ctx(5, Asn1.time(now / 1000 * 1000))));
      byte[] ap = apReq(tgt, auth, 7, 0);
      byte[] req = Asn1.app(12, Asn1.seq(Asn1.ctx(1, Asn1.integer(5)), Asn1.ctx(2, Asn1.integer(12)),
          Asn1.ctx(3, extraPadata == null ? Asn1.seq(padata(1, ap)) : Asn1.seq(padata(1, ap), extraPadata)),
          Asn1.ctx(4, body)));
      Asn1.Node resp = Asn1.parse(KdcComm.send(req, tgt.server.getRealmString()));
      if (resp.tag == 0x7e) {
        throw toException(resp.inner());
      }
      if (resp.tag != 0x6d) {
        throw new KrbException(KrbException.ASN1_BAD_ID);
      }
      Asn1.Node rep = resp.inner();
      Asn1.Node ep = rep.need(6);
      byte[] plain = Crypto.decrypt(ep.need(0).asInt(), tgt.key, 8, ep.need(2).content());
      Asn1.Node enc = Asn1.parse(plain).inner();
      if (enc.need(2).asInt() != nonce) {
        throw new KrbException(KrbException.KRB_AP_ERR_MODIFIED);
      }
      return credsFrom(rep, enc);
    } catch (IOException | GeneralSecurityException e) {
      KrbException ke = new KrbException(e.getMessage() == null ? e.toString() : e.getMessage());
      ke.initCause(e);
      throw ke;
    }
  }

  private static String tgsKey(Config cfg) {
    return cfg.has("libdefaults", "default_tgs_enctypes") ? "default_tgs_enctypes" : "default_tkt_enctypes";
  }

  public static Creds s4uSelf(Creds tgt, PrincipalName user) throws KrbException {
    try {
      String realm = user.getRealmString() != null ? user.getRealmString() : tgt.client.getRealmString();
      PrincipalName u = new PrincipalName(user.getNameType(), user.getNameStrings(), realm);
      StringBuilder names = new StringBuilder();
      for (String n : u.getNameStrings()) {
        names.append(n);
      }
      byte[] data = Asn1.cat(Crypto.le32(u.getNameType()),
          (names + realm + "Kerberos").getBytes(java.nio.charset.StandardCharsets.UTF_8));
      byte[] sum = Crypto.checksum(Crypto.CKSUM_HMAC_MD5, tgt.key, 17, data);
      byte[] forUser = Asn1.seq(Asn1.ctx(0, u.encode()), Asn1.ctx(1, Asn1.general(realm)),
          Asn1.ctx(2, Asn1.seq(Asn1.ctx(0, Asn1.integer(Crypto.CKSUM_HMAC_MD5)), Asn1.ctx(1, Asn1.octets(sum)))),
          Asn1.ctx(3, Asn1.general("Kerberos")));
      PrincipalName self = new PrincipalName(PrincipalName.KRB_NT_PRINCIPAL, tgt.client.getNameStrings(),
          tgt.client.getRealmString());
      return tgs(tgt, self, 0, padata(129, forUser), null);
    } catch (GeneralSecurityException e) {
      throw new KrbCryptoException(e.getMessage());
    }
  }

  public static Creds s4uProxy(Creds tgt, PrincipalName server, Creds selfTicket) throws KrbException {
    return tgs(tgt, server, 0x80000000 >>> kCnameInAddlTkt, null, selfTicket.ticket);
  }

  public static byte[] apReq(Creds c, byte[] authenticator, int usage, int apOptions) throws GeneralSecurityException {
    byte[] enc = Crypto.encrypt(c.keyType, c.key, usage, authenticator);
    return Asn1.app(14, Asn1.seq(Asn1.ctx(0, Asn1.integer(5)), Asn1.ctx(1, Asn1.integer(14)),
        Asn1.ctx(2, Asn1.bits32(apOptions)), Asn1.ctx(3, c.ticket), Asn1.ctx(4, encData(c.keyType, -1, enc))));
  }
}
