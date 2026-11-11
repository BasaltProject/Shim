/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
import java.io.*;
import java.security.*;
import java.util.*;
import javax.security.auth.*;
import javax.security.auth.kerberos.*;
import org.ietf.jgss.*;
import com.sun.security.jgss.*;

final class WeakGss {
  static String hex(byte[] b) { StringBuilder s = new StringBuilder(); for (byte x : b) s.append(String.format("%02x", x)); return s.toString(); }
  static void t(String n, java.util.concurrent.Callable<Object> c) { KdcLogin.t(n, c); }
  static void run(String et) throws Exception {

    final boolean dump = false;
    MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    kdc.addPrincipal("host/server.basalt.test", "svcpw");
    kdc.start();
    File dir = java.nio.file.Files.createTempDirectory("weak").toFile();
    kdc.writeConf(new File(dir, "krb5.conf"), " allow_weak_crypto = true\n default_tkt_enctypes = " + et + "\n default_tgs_enctypes = " + et + "\n permitted_enctypes = " + et + "\n");
    kdc.writeKeytab(new File(dir, "server.keytab"), "host/server.basalt.test");
    System.setProperty("java.security.krb5.conf", new File(dir, "krb5.conf").getPath());
    Map<String, String> o = new LinkedHashMap<String, String>();
    o.put("useTicketCache", "false"); o.put("storeKey", "true");
    final Subject alice = KdcLogin.loginAlice("a", o, "alicepw");
    Map<String, String> o2 = new LinkedHashMap<String, String>();
    o2.put("useKeyTab", "true"); o2.put("keyTab", new File(dir, "server.keytab").getPath()); o2.put("principal", "host/server.basalt.test"); o2.put("storeKey", "true"); o2.put("isInitiator", "false");
    final Subject server = KdcLogin.loginAlice("s", o2, null);
    final GSSManager m = GSSManager.getInstance();
    final GSSContext[] ctx = new GSSContext[2];
    final boolean mutual = !et.contains("nomutual");
    Subject.doAs(alice, new PrivilegedAction<Object>() { public Object run() {
      t("init", () -> {
        GSSContext x = m.createContext(m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, null, GSSContext.DEFAULT_LIFETIME);
        x.requestMutualAuth(true); x.requestConf(true); x.requestInteg(true);
        ctx[0] = x;
        byte[] tok = x.initSecContext(new byte[0], 0, 0);
        final byte[][] box = new byte[][] {tok};
        Subject.doAs(server, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
          GSSContext a = m.createContext((GSSCredential) null);
          ctx[1] = a;
          byte[] b = box[0];
          for (int i = 0; i < 4 && !(a.isEstablished()); i++) {
            byte[] r = a.acceptSecContext(b, 0, b.length);
            if (r == null) break;
            byte[] r2 = ctx[0].initSecContext(r, 0, r.length);
            if (r2 == null) break;
            b = r2;
          }
          return null; } });
        return "established " + ctx[0].isEstablished() + "/" + ctx[1].isEstablished();
      });
      return null; } });
    if (ctx[1] == null || !ctx[1].isEstablished()) { kdc.stop(); System.exit(0); }
    final GSSContext x = ctx[0], a = ctx[1];
    byte[] msg = "hello world 12345".getBytes();
    for (final boolean conf : new boolean[] {true, false}) {
      for (final String dir2 : new String[] {"i2a", "a2i"}) {
        final GSSContext s = dir2.equals("i2a") ? x : a, r = dir2.equals("i2a") ? a : x;
        t("wrap " + conf + " " + dir2, () -> {
          byte[] w = s.wrap(msg, 0, msg.length, new MessageProp(0, conf));
          MessageProp mp = new MessageProp(0, false);
          byte[] u = r.unwrap(w, 0, w.length, mp);
          return (dump ? hex(w) + " " : "") + w.length + " " + new String(u) + " priv=" + mp.getPrivacy() + " qop=" + mp.getQOP();
        });
      }
    }
    t("mic i2a", () -> { MessageProp mp = new MessageProp(0, false); byte[] t = x.getMIC(msg, 0, msg.length, mp); a.verifyMIC(t, 0, t.length, msg, 0, msg.length, new MessageProp(0, false)); return (dump ? hex(t) + " " : "") + t.length; });
    t("mic a2i", () -> { MessageProp mp = new MessageProp(0, false); byte[] t = a.getMIC(msg, 0, msg.length, mp); x.verifyMIC(t, 0, t.length, msg, 0, msg.length, new MessageProp(0, false)); return (dump ? hex(t) + " " : "") + t.length; });
    t("mic bad", () -> { byte[] t = x.getMIC(msg, 0, msg.length, new MessageProp(0, false)); t[t.length - 1] ^= 1; a.verifyMIC(t, 0, t.length, msg, 0, msg.length, new MessageProp(0, false)); return "ok?"; });
    t("wrap bad", () -> { byte[] t = x.wrap(msg, 0, msg.length, new MessageProp(0, true)); t[t.length - 1] ^= 1; a.unwrap(t, 0, t.length, new MessageProp(0, false)); return "ok?"; });
    t("limit", () -> x.getWrapSizeLimit(0, true, 100) + " " + x.getWrapSizeLimit(0, false, 100) + " " + x.getWrapSizeLimit(0, true, 1000));
    t("replay", () -> { byte[] t = x.wrap(msg, 0, msg.length, new MessageProp(0, true)); a.unwrap(t, 0, t.length, new MessageProp(0, false)); MessageProp mp = new MessageProp(0, false); a.unwrap(t, 0, t.length, mp); return mp.isDuplicateToken() + " " + mp.isOldToken() + " " + mp.isUnseqToken() + " " + mp.isGapToken(); });
    t("empty", () -> { byte[] t = x.wrap(new byte[0], 0, 0, new MessageProp(0, true)); MessageProp mp = new MessageProp(0, false); byte[] u = a.unwrap(t, 0, t.length, mp); return t.length + " " + u.length; });
    t("big", () -> { byte[] big = new byte[5000]; new Random(5).nextBytes(big); byte[] t = a.wrap(big, 0, big.length, new MessageProp(0, true)); byte[] u = x.unwrap(t, 0, t.length, new MessageProp(0, false)); return t.length + " " + Arrays.equals(big, u); });
    kdc.stop();
    System.exit(0);
  }
}
