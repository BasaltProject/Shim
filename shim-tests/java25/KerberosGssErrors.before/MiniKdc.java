/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

final class MiniKdc {
  static final class Princ {
    final String name;
    final Map<Integer, byte[]> keys = new LinkedHashMap<Integer, byte[]>();
    int kvno = 1;

    Princ(String name) {
      this.name = name;
    }
  }

  final String realm;
  final Map<String, Princ> db = new LinkedHashMap<String, Princ>();
  final List<String> events = new CopyOnWriteArrayList<String>();
  boolean requirePreauth = true;
  int udpLimit = 1465;
  int[] kdcEtypes = {18, 17, 20, 19, 23, 16};
  byte[] authData;
  volatile boolean stopped;
  int dumpN;
  DatagramSocket udp;
  ServerSocket tcp;
  int port;

  MiniKdc(String realm) throws IOException {
    this.realm = realm;
    udp = new DatagramSocket(0, InetAddress.getByName("127.0.0.1"));
    port = udp.getLocalPort();
    tcp = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
    addRandom("krbtgt/" + realm);
  }

  Princ addPrincipal(String name, String password) {
    Princ p = new Princ(name);
    String salt = realm + name.replace("/", "");
    for (int et : kdcEtypes) {
      p.keys.put(et, KdcCrypto.string2key(et, password.toCharArray(), salt, KdcCrypto.defaultString2KeyParams(et)));
    }
    db.put(name, p);
    return p;
  }

  Princ addRandom(String name) {
    Princ p = new Princ(name);
    for (int et : kdcEtypes) {
      p.keys.put(et, KdcCrypto.randomKey(et));
    }
    db.put(name, p);
    return p;
  }

  void writeConf(File f, String extra) throws IOException {
    String conf = "[libdefaults]\n default_realm = " + realm + "\n dns_lookup_kdc = false\n dns_lookup_realm = false\n"
        + " udp_preference_limit = " + udpLimit + "\n" + (extra == null ? "" : extra)
        + "\n[realms]\n " + realm + " = {\n  kdc = 127.0.0.1:" + port + "\n }\n"
        + "\n[domain_realm]\n .basalt.test = " + realm + "\n basalt.test = " + realm + "\n";
    FileOutputStream o = new FileOutputStream(f);
    o.write(conf.getBytes(StandardCharsets.UTF_8));
    o.close();
  }

  void writeKeytab(File f, String... names) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    DataOutputStream d = new DataOutputStream(out);
    d.writeShort(0x0502);
    for (String n : names) {
      Princ p = db.get(n);
      for (Map.Entry<Integer, byte[]> e : p.keys.entrySet()) {
        ByteArrayOutputStream rec = new ByteArrayOutputStream();
        DataOutputStream r = new DataOutputStream(rec);
        String[] comps = n.split("/");
        r.writeShort(comps.length);
        byte[] rb = realm.getBytes(StandardCharsets.UTF_8);
        r.writeShort(rb.length);
        r.write(rb);
        for (String c : comps) {
          byte[] cb = c.getBytes(StandardCharsets.UTF_8);
          r.writeShort(cb.length);
          r.write(cb);
        }
        r.writeInt(comps.length > 1 ? 3 : 1);
        r.writeInt(1700000000);
        r.writeByte(p.kvno);
        r.writeShort(e.getKey());
        r.writeShort(e.getValue().length);
        r.write(e.getValue());
        r.writeInt(p.kvno);
        d.writeInt(rec.size());
        d.write(rec.toByteArray());
      }
    }
    FileOutputStream o = new FileOutputStream(f);
    o.write(out.toByteArray());
    o.close();
  }

  void start() {
    Thread u = new Thread(new Runnable() {
      public void run() {
        byte[] buf = new byte[65535];
        while (!stopped) {
          try {
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            udp.receive(p);
            byte[] req = Arrays.copyOf(p.getData(), p.getLength());
            byte[] resp = handle(req, true);
            if (resp != null) {
              udp.send(new DatagramPacket(resp, resp.length, p.getSocketAddress()));
            }
          } catch (IOException e) {
            if (stopped) {
              return;
            }
          }
        }
      }
    });
    u.setDaemon(true);
    u.start();
    Thread t = new Thread(new Runnable() {
      public void run() {
        while (!stopped) {
          try {
            final Socket s = tcp.accept();
            Thread c = new Thread(new Runnable() {
              public void run() {
                try {
                  DataInputStream in = new DataInputStream(s.getInputStream());
                  int n = in.readInt();
                  byte[] req = new byte[n];
                  in.readFully(req);
                  byte[] resp = handle(req, false);
                  DataOutputStream out = new DataOutputStream(s.getOutputStream());
                  out.writeInt(resp.length);
                  out.write(resp);
                  out.flush();
                  s.close();
                } catch (IOException e) {
                }
              }
            });
            c.setDaemon(true);
            c.start();
          } catch (IOException e) {
            if (stopped) {
              return;
            }
          }
        }
      }
    });
    t.setDaemon(true);
    t.start();
  }

  void stop() {
    stopped = true;
    udp.close();
    try {
      tcp.close();
    } catch (IOException e) {
    }
  }

  static String princName(KdcAsn1.Node n) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (KdcAsn1.Node c : n.need(1).children()) {
      if (sb.length() > 0) {
        sb.append('/');
      }
      sb.append(c.asString());
    }
    return sb.toString();
  }

  static byte[] principal(int type, String name) {
    List<byte[]> parts = new ArrayList<byte[]>();
    for (String s : name.split("/")) {
      parts.add(KdcAsn1.general(s));
    }
    return KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(type)), KdcAsn1.ctx(1, KdcAsn1.seq(parts.toArray(new byte[0][]))));
  }

  static byte[] encData(int etype, int kvno, byte[] cipher) {
    return KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(etype)), kvno >= 0 ? KdcAsn1.ctx(1, KdcAsn1.integer(kvno)) : null,
        KdcAsn1.ctx(2, KdcAsn1.octets(cipher)));
  }

  static byte[] keyBlock(int etype, byte[] key) {
    return KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(etype)), KdcAsn1.ctx(1, KdcAsn1.octets(key)));
  }

  byte[] error(int code, String cname, String sname, byte[] edata, String text) {
    long now = System.currentTimeMillis();
    return KdcAsn1.app(30, KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(5)), KdcAsn1.ctx(1, KdcAsn1.integer(30)),
        KdcAsn1.ctx(4, KdcAsn1.time(now)), KdcAsn1.ctx(5, KdcAsn1.integer((now % 1000) * 1000)),
        KdcAsn1.ctx(6, KdcAsn1.integer(code)), cname != null ? KdcAsn1.ctx(7, KdcAsn1.general(realm)) : null,
        cname != null ? KdcAsn1.ctx(8, principal(1, cname)) : null, KdcAsn1.ctx(9, KdcAsn1.general(realm)),
        KdcAsn1.ctx(10, principal(sname != null && sname.contains("/") ? 2 : 1, sname == null ? "krbtgt/" + realm : sname)),
        text != null ? KdcAsn1.ctx(11, KdcAsn1.general(text)) : null, edata != null ? KdcAsn1.ctx(12, KdcAsn1.octets(edata)) : null));
  }

  byte[] handle(byte[] req, boolean overUdp) {
    try {
      KdcAsn1.Node root = KdcAsn1.parse(req);
      byte[] resp;
      if (root.tag == 0x6a) {
        resp = asReq(root.inner());
      } else if (root.tag == 0x6c) {
        resp = tgsReq(root.inner());
      } else {
        return error(60, null, null, null, "bad message");
      }
      if (overUdp && resp.length > udpLimit) {
        events.add("RESPONSE_TOO_BIG");
        return error(52, null, null, null, null);
      }
      return resp;
    } catch (Exception e) {
      events.add("KDC failure " + e);
      return error(60, null, null, null, e.toString());
    }
  }

  static String optionNames(int o) {
    String[] n = {"reserved", "forwardable", "forwarded", "proxiable", "proxy", "allow-postdate", "postdated", "unused7",
        "renewable", "unused9", "unused10", "opt-hardware-auth", "unused12", "unused13", "cname-in-addl-tkt",
        "canonicalize", "request-anonymous", "unused17", "unused18", "unused19", "unused20", "unused21", "unused22",
        "unused23", "unused24", "unused25", "disable-transited-check", "renewable-ok", "enc-tkt-in-skey", "unused29",
        "renew", "validate"};
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 32; i++) {
      if ((o & (0x80000000 >>> i)) != 0) {
        sb.append(sb.length() > 0 ? "," : "").append(n[i]);
      }
    }
    return sb.toString();
  }

  byte[] etypeInfo2(int[] clientEtypes, Princ client) {
    List<byte[]> entries = new ArrayList<byte[]>();
    String salt = realm + client.name.replace("/", "");
    for (int et : clientEtypes) {
      if (client.keys.containsKey(et)) {
        byte[] p = KdcCrypto.defaultString2KeyParams(et);
        entries.add(KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(et)), KdcAsn1.ctx(1, KdcAsn1.general(salt)),
            p != null ? KdcAsn1.ctx(2, KdcAsn1.octets(p)) : null));
      }
    }
    return KdcAsn1.seq(entries.toArray(new byte[0][]));
  }

  static byte[] padata(int type, byte[] value) {
    return KdcAsn1.seq(KdcAsn1.ctx(1, KdcAsn1.integer(type)), KdcAsn1.ctx(2, KdcAsn1.octets(value)));
  }

  int[] readEtypes(KdcAsn1.Node body) throws IOException {
    List<KdcAsn1.Node> l = body.need(8).children();
    int[] r = new int[l.size()];
    for (int i = 0; i < r.length; i++) {
      r[i] = l.get(i).asInt();
    }
    return r;
  }

  int pickEtype(int[] clientEtypes, Princ... all) {
    for (int et : clientEtypes) {
      boolean ok = KdcCrypto.isSupported(et);
      for (Princ p : all) {
        ok &= p.keys.containsKey(et);
      }
      if (ok) {
        return et;
      }
    }
    return -1;
  }

  byte[] asReq(KdcAsn1.Node r) throws Exception {
    KdcAsn1.Node body = r.need(4);
    String cname = princName(body.need(1));
    String sname = princName(body.need(3));
    int[] etypes = readEtypes(body);
    int options = body.need(0).asBits32();
    KdcAsn1.Node pa = r.field(3);
    StringBuilder pad = new StringBuilder();
    KdcAsn1.Node ts = null;
    if (pa != null) {
      for (KdcAsn1.Node p : pa.children()) {
        int t = p.need(1).asInt();
        pad.append(pad.length() > 0 ? "," : "").append(t);
        if (t == 2) {
          ts = KdcAsn1.parse(p.need(2).content());
        }
      }
    }
    KdcAsn1.Node addrs = body.field(9);
    long till = body.need(5).asTime();
    KdcAsn1.Node rtime = body.field(6);
    events.add("AS-REQ cname=" + cname + " sname=" + sname + " options=" + optionNames(options) + " etypes="
        + Arrays.toString(etypes) + " padata=[" + pad + "] addresses=" + (addrs == null ? 0 : addrs.children().size())
        + " rtime=" + (rtime != null) + " till=" + body.need(5).asTime() + " cnameType=" + r.need(4).need(1).need(0).asInt() + " snameType=" + body.need(3).need(0).asInt() + " from=" + (body.field(4) != null) + " nonce=" + (body.field(7) != null));
    Princ client = db.get(cname);
    if (client == null) {
      return error(6, cname, sname, null, null);
    }
    Princ server = db.get(sname);
    if (server == null) {
      return error(7, cname, sname, null, null);
    }
    boolean preauthed = false;
    if (ts != null) {
      int et = ts.need(0).asInt();
      byte[] key = client.keys.get(et);
      try {
        byte[] plain = KdcCrypto.decrypt(et, key, 1, ts.need(2).content());
        KdcAsn1.parse(plain);
        preauthed = true;
      } catch (Exception e) {
        events.add("preauth failed");
        return error(24, cname, sname, null, null);
      }
    } else if (requirePreauth) {
      byte[] md = KdcAsn1.seq(padata(19, etypeInfo2(etypes, client)), padata(2, new byte[0]));
      return error(25, cname, sname, md, null);
    }
    int et = pickEtype(etypes, client, server);
    if (et < 0) {
      return error(14, cname, sname, null, null);
    }
    byte[] sessionKey = KdcCrypto.randomKey(et);
    long now = System.currentTimeMillis() / 1000 * 1000;
    long endtime = till <= now ? now + 10 * 3600 * 1000L : Math.min(till, now + 10 * 3600 * 1000L);
    int flags = 0x80000000 >>> 9;
    if (preauthed) {
      flags |= 0x80000000 >>> 10;
    }
    if ((options & (0x80000000 >>> 1)) != 0) {
      flags |= 0x80000000 >>> 1;
    }
    if ((options & (0x80000000 >>> 3)) != 0) {
      flags |= 0x80000000 >>> 3;
    }
    boolean renewable = (options & (0x80000000 >>> 8)) != 0 || (options & (0x80000000 >>> 27)) != 0;
    long renewTill = 0;
    if (renewable) {
      flags |= 0x80000000 >>> 8;
      renewTill = Math.min(rtime != null ? rtime.asTime() : till, now + 7 * 24 * 3600 * 1000L);
    }
    byte[] ticket = makeTicket(server, sname, flags, et, sessionKey, cname, now, endtime, renewable ? renewTill : 0,
        addrs);
    byte[] lastReq = KdcAsn1.seq(KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(0)), KdcAsn1.ctx(1, KdcAsn1.time(now))));
    byte[] encPart = KdcAsn1.app(25, KdcAsn1.seq(KdcAsn1.ctx(0, keyBlock(et, sessionKey)), KdcAsn1.ctx(1, lastReq),
        KdcAsn1.ctx(2, KdcAsn1.integer(body.need(7).asInt() & 0xffffffffL)), KdcAsn1.ctx(4, KdcAsn1.bits32(flags)),
        KdcAsn1.ctx(5, KdcAsn1.time(now)), KdcAsn1.ctx(7, KdcAsn1.time(endtime)),
        renewable ? KdcAsn1.ctx(8, KdcAsn1.time(renewTill)) : null, KdcAsn1.ctx(9, KdcAsn1.general(realm)),
        KdcAsn1.ctx(10, principal(sname.contains("/") ? 2 : 1, sname)), addrs != null ? KdcAsn1.ctx(11, addrs.encoded()) : null));
    int cet = pickEtype(etypes, client);
    byte[] enc = KdcCrypto.encrypt(cet, client.keys.get(cet), 3, encPart);
    byte[] salt = KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(cet)), KdcAsn1.ctx(1, KdcAsn1.general(realm + client.name.replace("/", ""))));
    return KdcAsn1.app(11, KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(5)), KdcAsn1.ctx(1, KdcAsn1.integer(11)),
        KdcAsn1.ctx(2, KdcAsn1.seq(padata(19, KdcAsn1.seq(KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(cet)),
            KdcAsn1.ctx(1, KdcAsn1.general(realm + client.name.replace("/", "")))))))),
        KdcAsn1.ctx(3, KdcAsn1.general(realm)), KdcAsn1.ctx(4, principal(1, cname)), KdcAsn1.ctx(5, ticket),
        KdcAsn1.ctx(6, encData(cet, client.kvno, enc))));
  }

  byte[] makeTicket(Princ server, String sname, int flags, int sessionEtype, byte[] sessionKey, String cname,
      long authtime, long endtime, long renewTill, KdcAsn1.Node addrs) throws Exception {
    byte[] encTkt = KdcAsn1.app(3, KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.bits32(flags)), KdcAsn1.ctx(1, keyBlock(sessionEtype, sessionKey)),
        KdcAsn1.ctx(2, KdcAsn1.general(realm)), KdcAsn1.ctx(3, principal(1, cname)),
        KdcAsn1.ctx(4, KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(1)), KdcAsn1.ctx(1, KdcAsn1.octets(new byte[0])))),
        KdcAsn1.ctx(5, KdcAsn1.time(authtime)), KdcAsn1.ctx(7, KdcAsn1.time(endtime)),
        renewTill != 0 ? KdcAsn1.ctx(8, KdcAsn1.time(renewTill)) : null, addrs != null ? KdcAsn1.ctx(9, addrs.encoded()) : null,
        authData != null && !sname.startsWith("krbtgt/") ? KdcAsn1.ctx(10, authData) : null));
    int set = server.keys.containsKey(sessionEtype) ? sessionEtype : server.keys.keySet().iterator().next();
    byte[] enc = KdcCrypto.encrypt(set, server.keys.get(set), 2, encTkt);
    return KdcAsn1.app(1, KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(5)), KdcAsn1.ctx(1, KdcAsn1.general(realm)),
        KdcAsn1.ctx(2, principal(sname.contains("/") ? 2 : 1, sname)), KdcAsn1.ctx(3, encData(set, server.kvno, enc))));
  }

  byte[] tgsReq(KdcAsn1.Node r) throws Exception {
    KdcAsn1.Node body = r.need(4);
    String sname = princName(body.need(3));
    int[] etypes = readEtypes(body);
    int options = body.need(0).asBits32();
    KdcAsn1.Node pa = null;
    KdcAsn1.Node forUser = null;
    byte[] forUserRaw = null;
    for (KdcAsn1.Node p : r.need(3).children()) {
      if (p.need(1).asInt() == 1) {
        pa = KdcAsn1.parse(p.need(2).content());
      }
      if (p.need(1).asInt() == 129) {
        forUserRaw = p.need(2).content();
        forUser = KdcAsn1.parse(forUserRaw);
      }
    }
    KdcAsn1.Node ap = pa.inner();
    KdcAsn1.Node tkt = ap.need(3).inner();
    KdcAsn1.Node tktEnc = tkt.need(3);
    Princ tgs = db.get("krbtgt/" + realm);
    byte[] encTktBytes = KdcCrypto.decrypt(tktEnc.need(0).asInt(), tgs.keys.get(tktEnc.need(0).asInt()), 2,
        tktEnc.need(2).content());
    KdcAsn1.Node et = KdcAsn1.parse(encTktBytes).inner();
    KdcAsn1.Node k = et.need(1);
    int stype = k.need(0).asInt();
    byte[] skey = k.need(1).content();
    String cname = princName(et.need(3));
    final String requester = cname;
    KdcAsn1.Node auth = ap.need(4);
    byte[] authPlain = KdcCrypto.decrypt(auth.need(0).asInt(), skey, 7, auth.need(2).content());
    KdcAsn1.Node a = KdcAsn1.parse(authPlain).inner();
    KdcAsn1.Node sub = a.field(6);
    int flags = et.need(0).asBits32();
    long now = System.currentTimeMillis() / 1000 * 1000;
    events.add("TGS-REQ cname=" + cname + " sname=" + sname + " options=" + optionNames(options) + " etypes="
        + Arrays.toString(etypes) + " subkey=" + (sub != null) + " ticketFlags=" + Integer.toHexString(flags)
        + " addresses=" + (body.field(9) != null) + " till=" + body.need(5).asTime() + " rtime=" + (body.field(6) != null) + " snameType=" + body.need(3).need(0).asInt() + " padata=" + r.need(3).children().size() + " cksum=" + (a.field(3) != null ? a.field(3).need(0).asInt() : -1));
    Princ server = db.get(sname);
    if (server == null) {
      return error(7, cname, sname, null, null);
    }
    boolean s4uSelf = false;
    boolean s4uProxy = false;
    if (forUser != null) {
      KdcAsn1.Node fu = forUser;
      String user = princName(fu.need(0));
      String urealm = fu.need(1).asString();
      java.io.ByteArrayOutputStream cs = new java.io.ByteArrayOutputStream();
      int ntype = fu.need(0).need(0).asInt();
      cs.write(ntype);
      cs.write(ntype >> 8);
      cs.write(ntype >> 16);
      cs.write(ntype >> 24);
      for (KdcAsn1.Node nn : fu.need(0).need(1).children()) {
        cs.write(nn.asString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
      }
      cs.write(urealm.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      cs.write("Kerberos".getBytes(java.nio.charset.StandardCharsets.UTF_8));
      byte[] want = KdcCrypto.checksum(KdcCrypto.CKSUM_HMAC_MD5, skey, 17, cs.toByteArray());
      byte[] got = fu.need(2).need(1).content();
      events.add("S4U2self user=" + user + " realm=" + urealm + " cksumType=" + fu.need(2).need(0).asInt() + " cksumOk="
          + java.util.Arrays.equals(want, got) + " auth=" + fu.need(3).asString());
      if (!java.util.Arrays.equals(want, got)) {
        return error(41, cname, sname, null, null);
      }
      if (!sname.equals(requester)) {
        return error(13, cname, sname, null, null);
      }
      cname = user;
      s4uSelf = true;
    }
    byte[] proxyFlagsHolder = null;
    if ((options & (0x80000000 >>> 14)) != 0) {
      KdcAsn1.Node addl = body.field(11);
      if (addl == null) {
        return error(13, cname, sname, null, null);
      }
      KdcAsn1.Node at = addl.children().get(0).inner();
      KdcAsn1.Node ae = at.need(3);
      Princ rp = db.get(requester);
      byte[] ab = KdcCrypto.decrypt(ae.need(0).asInt(), rp.keys.get(ae.need(0).asInt()), 2, ae.need(2).content());
      KdcAsn1.Node aet = KdcAsn1.parse(ab).inner();
      int af = aet.need(0).asBits32();
      cname = princName(aet.need(3));
      events.add("S4U2proxy impersonating=" + cname + " target=" + sname + " addlFlags=" + Integer.toHexString(af));
      if ((af & (0x80000000 >>> 1)) == 0) {
        return error(13, cname, sname, null, null);
      }
      s4uProxy = true;
    }
    int sessEt = pickEtype(etypes, server);
    if (sessEt < 0) {
      return error(14, cname, sname, null, null);
    }
    byte[] sessionKey = KdcCrypto.randomKey(sessEt);
    int newFlags = flags & ~(0x80000000 >>> 9) & ~(0x80000000 >>> 10);
    if ((options & (0x80000000 >>> 1)) == 0) {
      newFlags &= ~(0x80000000 >>> 1);
    }
    if (s4uSelf) {
      newFlags |= 0x80000000 >>> 1;
    }
    if ((options & (0x80000000 >>> 2)) != 0) {
      if ((flags & (0x80000000 >>> 1)) == 0) {
        return error(13, cname, sname, null, null);
      }
      newFlags |= 0x80000000 >>> 2;
    }
    long till = body.need(5).asTime();
    long endtime = Math.min(et.need(7).asTime(), till <= now ? Long.MAX_VALUE : till);
    long renewTill = (flags & (0x80000000 >>> 8)) != 0 && et.field(8) != null ? et.field(8).asTime() : 0;
    if ((options & (0x80000000 >>> 8)) == 0) {
      newFlags &= ~(0x80000000 >>> 8);
      renewTill = 0;
    }
    byte[] ticket = makeTicket(server, sname, newFlags, sessEt, sessionKey, cname, et.need(5).asTime(), endtime, renewTill,
        et.field(9));
    byte[] lastReq = KdcAsn1.seq(KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(0)), KdcAsn1.ctx(1, KdcAsn1.time(now))));
    byte[] encPart = KdcAsn1.app(26, KdcAsn1.seq(KdcAsn1.ctx(0, keyBlock(sessEt, sessionKey)), KdcAsn1.ctx(1, lastReq),
        KdcAsn1.ctx(2, KdcAsn1.integer(body.need(7).asInt() & 0xffffffffL)), KdcAsn1.ctx(4, KdcAsn1.bits32(newFlags)),
        KdcAsn1.ctx(5, KdcAsn1.time(et.need(5).asTime())), KdcAsn1.ctx(7, KdcAsn1.time(endtime)),
        renewTill != 0 ? KdcAsn1.ctx(8, KdcAsn1.time(renewTill)) : null, KdcAsn1.ctx(9, KdcAsn1.general(realm)),
        KdcAsn1.ctx(10, principal(sname.contains("/") ? 2 : 1, sname)), et.field(9) != null ? KdcAsn1.ctx(11, et.field(9).encoded()) : null));
    byte[] repKey = sub != null ? sub.need(1).content() : skey;
    int repEt = sub != null ? sub.need(0).asInt() : stype;
    byte[] enc = KdcCrypto.encrypt(repEt, repKey, sub != null ? 9 : 8, encPart);
    return KdcAsn1.app(13, KdcAsn1.seq(KdcAsn1.ctx(0, KdcAsn1.integer(5)), KdcAsn1.ctx(1, KdcAsn1.integer(13)),
        s4uSelf ? KdcAsn1.ctx(2, KdcAsn1.seq(padata(129, forUserRaw))) : null,
        KdcAsn1.ctx(3, KdcAsn1.general(realm)), KdcAsn1.ctx(4, principal(1, cname)), KdcAsn1.ctx(5, ticket),
        KdcAsn1.ctx(6, encData(repEt, -1, enc))));
  }
}
