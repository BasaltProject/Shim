/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
// jvm-args: --add-exports java.security.jgss/sun.security.krb5=ALL-UNNAMED
import java.io.*;
import java.security.*;
import java.util.*;
import javax.security.auth.*;
import javax.security.auth.kerberos.*;
import org.ietf.jgss.*;
import com.sun.security.jgss.*;

public class KerberosGssErrors {
  static void t(String n, java.util.concurrent.Callable<Object> c) { KdcLogin.t(n, c); }
  static byte[] initTok(final Subject alice, final GSSManager m, final String target, final Oid mech, final boolean mutual) throws Exception {
    return (byte[]) Subject.doAs(alice, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
      GSSContext c = m.createContext(m.createName(target, GSSName.NT_HOSTBASED_SERVICE), mech, null, 0);
      c.requestMutualAuth(mutual);
      return c.initSecContext(new byte[0], 0, 0); } });
  }
  static Object accept(Subject s, final GSSManager m, final byte[] tok) throws Exception {
    return Subject.doAs(s, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
      GSSContext a = m.createContext((GSSCredential) null);
      byte[] r = a.acceptSecContext(tok, 0, tok.length);
      return (r == null ? "null" : "tok" + (r.length > 100 ? "long" : String.valueOf(r.length))) + " " + a.isEstablished() + " " + a.getSrcName(); } });
  }
  public static void main(String[] args) throws Exception {
    final MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    final MiniKdc.Princ svc = kdc.addPrincipal("host/server.basalt.test", "svcpw");
    final MiniKdc.Princ http = kdc.addPrincipal("HTTP/server.basalt.test", "httppw");
    kdc.start();
    File dir = java.nio.file.Files.createTempDirectory("krb5gss").toFile();
    File conf = new File(dir, "krb5.conf");
    kdc.writeConf(conf, "");
    File kt = new File(dir, "server.keytab");
    kdc.writeKeytab(kt, "host/server.basalt.test");
    System.setProperty("java.security.krb5.conf", conf.getPath());
    Map<String, String> o = new LinkedHashMap<String, String>();
    o.put("useTicketCache", "false"); o.put("storeKey", "true");
    final Subject alice = KdcLogin.loginAlice("a", o, "alicepw");
    Map<String, String> o2 = new LinkedHashMap<String, String>();
    o2.put("useKeyTab", "true"); o2.put("keyTab", kt.getPath()); o2.put("principal", "host/server.basalt.test"); o2.put("storeKey", "true"); o2.put("isInitiator", "false");
    final Subject server = KdcLogin.loginAlice("s", o2, null);
    final GSSManager m = GSSManager.getInstance();
    final KerberosPrincipal hp = new KerberosPrincipal("host/server.basalt.test@BASALT.TEST", 2);
    // wrong key
    final Subject wrong = new Subject();
    wrong.getPrivateCredentials().add(new KerberosKey(hp, new byte[32], 18, 1));
    wrong.getPrincipals().add(hp);
    final Subject rc4only = new Subject();
    rc4only.getPrivateCredentials().add(new KerberosKey(hp, new byte[16], 23, 1));
    final Subject badkvno = new Subject();
    badkvno.getPrivateCredentials().add(new KerberosKey(hp, svc.keys.get(18), 18, 7));
    final Subject httpOnly = new Subject();
    httpOnly.getPrivateCredentials().add(new KerberosKey(new KerberosPrincipal("HTTP/server.basalt.test@BASALT.TEST", 2), http.keys.get(18), 18, 1));
    final Subject empty = new Subject();
    final byte[] tok = initTok(alice, m, "host@server.basalt.test", KdcLogin.KRB5, true);
    t("wrongkey", () -> accept(wrong, m, tok));
    t("rc4only", () -> accept(rc4only, m, tok));
    t("badkvno", () -> accept(badkvno, m, tok));
    t("httponly", () -> accept(httpOnly, m, tok));
    t("empty", () -> accept(empty, m, tok));
    t("nosubject", () -> { GSSContext a = m.createContext((GSSCredential) null); return a.acceptSecContext(tok, 0, tok.length); });
    t("ok", () -> accept(server, m, tok));
    t("replay", () -> accept(server, m, tok));
    t("empty tok", () -> accept(server, m, new byte[0]));
    t("junk tok", () -> accept(server, m, new byte[] {1, 2, 3, 4, 5}));
    t("bad oid", () -> { byte[] x = tok.clone(); x[6] ^= 1; return accept(server, m, x); });
    t("bad tokid", () -> { byte[] x = tok.clone(); x[tok.length > 0 ? 13 : 0] ^= 1; return accept(server, m, x); });
    t("truncated", () -> accept(server, m, Arrays.copyOf(tok, 100)));
    t("bitflip", () -> { byte[] x = tok.clone(); x[x.length - 5] ^= 1; return accept(server, m, x); });
    t("bitflip tkt", () -> { byte[] x = tok.clone(); x[60] ^= 1; return accept(server, m, x); });
    t("unknown svc", () -> initTok(alice, m, "nosuch@server.basalt.test", KdcLogin.KRB5, true));
    t("no subject init", () -> { GSSContext c = m.createContext(m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, null, 0); return c.initSecContext(new byte[0], 0, 0); });
    t("empty subject init", () -> initTok(new Subject(), m, "host@server.basalt.test", KdcLogin.KRB5, true));
    t("init with bad mech", () -> m.createContext(m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE), new Oid("1.2.3"), null, 0));
    t("init null peer", () -> m.createContext((GSSName) null, KdcLogin.KRB5, null, 0));
    t("cb", () -> Subject.doAs(alice, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
      GSSContext c = m.createContext(m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, null, 0);
      c.setChannelBinding(new ChannelBinding(java.net.InetAddress.getByName("127.0.0.1"), java.net.InetAddress.getByName("127.0.0.2"), "data".getBytes()));
      final byte[] tk = c.initSecContext(new byte[0], 0, 0);
      return Subject.doAs(server, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
        GSSContext a = m.createContext((GSSCredential) null);
        a.setChannelBinding(new ChannelBinding(java.net.InetAddress.getByName("127.0.0.1"), java.net.InetAddress.getByName("127.0.0.2"), "other".getBytes()));
        try { a.acceptSecContext(tk, 0, tk.length); return "accepted"; } catch (GSSException e) { return e.getMessage() + " " + e.getMajor() + " " + e.getMinor(); } } }); } }));
    t("cb ok", () -> Subject.doAs(alice, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
      GSSContext c = m.createContext(m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, null, 0);
      c.setChannelBinding(new ChannelBinding(java.net.InetAddress.getByName("127.0.0.1"), java.net.InetAddress.getByName("127.0.0.2"), "data".getBytes()));
      final byte[] tk = c.initSecContext(new byte[0], 0, 0);
      return Subject.doAs(server, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
        GSSContext a = m.createContext((GSSCredential) null);
        a.setChannelBinding(new ChannelBinding(java.net.InetAddress.getByName("127.0.0.1"), java.net.InetAddress.getByName("127.0.0.2"), "data".getBytes()));
        try { a.acceptSecContext(tk, 0, tk.length); return "accepted"; } catch (GSSException e) { return e.getMessage() + " " + e.getMajor() + " " + e.getMinor(); } } }); } }));
    t("cb acceptor only", () -> Subject.doAs(alice, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
      GSSContext c = m.createContext(m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, null, 0);
      final byte[] tk = c.initSecContext(new byte[0], 0, 0);
      return Subject.doAs(server, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
        GSSContext a = m.createContext((GSSCredential) null);
        a.setChannelBinding(new ChannelBinding("data".getBytes()));
        try { a.acceptSecContext(tk, 0, tk.length); return "accepted"; } catch (GSSException e) { return e.getMessage() + " " + e.getMajor() + " " + e.getMinor(); } } }); } }));
    // spnego error cases
    final byte[] stok = initTok(alice, m, "host@server.basalt.test", KdcLogin.SPNEGO, true);
    t("spnego wrongkey", () -> accept(wrong, m, stok));
    t("spnego junk", () -> accept(server, m, new byte[] {0x60, 0x08, 0x06, 0x06, 0x2b, 0x06, 0x01, 0x05, 0x05, 0x02}));
    kdc.stop();
  }
}
