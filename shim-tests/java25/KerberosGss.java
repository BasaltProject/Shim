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

public class KerberosGss {
  static final Oid KRB5, SPNEGO;
  static { try { KRB5 = new Oid("1.2.840.113554.1.2.2"); SPNEGO = new Oid("1.3.6.1.5.5.2"); } catch (Exception e) { throw new RuntimeException(e); } }
  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try { Object o = c.call(); System.out.println(n + " -> " + (o instanceof byte[] ? "bytes" + ((byte[]) o).length : o instanceof Object[] ? Arrays.toString((Object[]) o) : o)); }
    catch (Throwable e0) { Throwable e = e0; while (e instanceof java.security.PrivilegedActionException && e.getCause() != null) e = e.getCause(); System.out.println(n + " !! " + e.getClass().getName() + ": " + e.getMessage() + (e instanceof GSSException ? " major=" + ((GSSException) e).getMajor() + " minor=" + ((GSSException) e).getMinor() + " " + ((GSSException) e).getMajorString() : "")); }
  }
  public static void main(String[] args) throws Exception {
    MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    MiniKdc.Princ svc = kdc.addPrincipal("host/server.basalt.test", "svcpw");
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
    t("mechs", () -> m.getMechs());
    t("namesForMech", () -> m.getNamesForMech(KRB5));
    t("namesForMech2", () -> m.getNamesForMech(SPNEGO));
    t("mechsForName", () -> m.getMechsForName(GSSName.NT_HOSTBASED_SERVICE));
    t("mechsForName2", () -> m.getMechsForName(GSSName.NT_EXPORT_NAME));
    t("name1", () -> { GSSName n = m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE); return n + " " + n.getStringNameType() + " mn=" + n.isMN() + " anon=" + n.isAnonymous(); });
    t("name2", () -> { GSSName n = m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE).canonicalize(KRB5); return n + " " + n.getStringNameType() + " mn=" + n.isMN(); });
    t("name3", () -> { GSSName n = m.createName("alice", GSSName.NT_USER_NAME); GSSName c = n.canonicalize(KRB5); return n + " " + n.getStringNameType() + " " + c + " " + c.getStringNameType() + " " + c.isMN(); });
    t("name4", () -> { GSSName n = m.createName("alice@BASALT.TEST", new Oid("1.2.840.113554.1.2.2.1")); return n + " " + n.getStringNameType() + " " + n.canonicalize(KRB5).export().length; });
    t("name5", () -> { GSSName n = m.createName("alice@BASALT.TEST", null); return n + " " + n.getStringNameType(); });
    t("name6", () -> { GSSName n = m.createName("alice@BASALT.TEST", GSSName.NT_USER_NAME, KRB5); return n + " " + n.isMN() + " " + Arrays.toString(n.export()); });
    t("name7", () -> m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE).export());
    t("name8", () -> { GSSName a = m.createName("alice@BASALT.TEST", GSSName.NT_USER_NAME); GSSName b = m.createName("alice@BASALT.TEST", GSSName.NT_USER_NAME, KRB5); return a.equals(b) + " " + b.equals(a) + " " + a.equals(a) + " " + a.hashCode() + " " + b.hashCode() + " " + a.equals((Object) "x"); });
    t("name9", () -> m.createName((String) null, GSSName.NT_USER_NAME));
    t("name10", () -> m.createName("a", GSSName.NT_ANONYMOUS).isAnonymous());
    t("name11", () -> { GSSName n = m.createName("svc/host.basalt.test@BASALT.TEST", new Oid("1.2.840.113554.1.2.2.2")); return n + " " + n.canonicalize(KRB5) ; });
    t("name12", () -> { GSSName n = m.createName("alice", GSSName.NT_USER_NAME, new Oid("1.2.3.4")); return n; });
    t("name13", () -> { GSSName n = m.createName("alice@BASALT.TEST", GSSName.NT_USER_NAME).canonicalize(SPNEGO); return n + " " + n.isMN() + " " + Arrays.toString(n.export()); });
    t("name14", () -> m.createName("alice", GSSName.NT_USER_NAME).export());
    t("nameexp", () -> { byte[] e = m.createName("alice@BASALT.TEST", GSSName.NT_USER_NAME, KRB5).export(); GSSName n = m.createName(e, GSSName.NT_EXPORT_NAME); return n + " " + n.getStringNameType() + " " + n.isMN(); });
    t("cred noSubject", () -> m.createCredential(GSSCredential.INITIATE_ONLY));
    final String[] result = new String[1];
    Subject.doAs(alice, new PrivilegedAction<Object>() {
      public Object run() {
        t("cred", () -> { GSSCredential c = m.createCredential(GSSCredential.INITIATE_ONLY); return c.getName() + " " + c.getUsage() + " " + Arrays.toString(c.getMechs()) + " " + (c.getRemainingLifetime() > 0) + " " + (c.getRemainingInitLifetime(KRB5) > 0) + " " + c.getRemainingAcceptLifetime(KRB5) + " " + c.getUsage(KRB5); });
        t("cred2", () -> { GSSCredential c = m.createCredential(null, GSSCredential.DEFAULT_LIFETIME, KRB5, GSSCredential.INITIATE_AND_ACCEPT); return c.getName() + " " + c.getUsage(); });
        t("cred3", () -> { GSSCredential c = m.createCredential(m.createName("bob", GSSName.NT_USER_NAME), GSSCredential.DEFAULT_LIFETIME, KRB5, GSSCredential.INITIATE_ONLY); return c; });
        t("cred4", () -> { GSSCredential c = m.createCredential(m.createName("alice", GSSName.NT_USER_NAME), 100, KRB5, GSSCredential.INITIATE_ONLY); return c.getName() + " " + (c.getRemainingLifetime() <= 100); });
        t("cred5", () -> m.createCredential(GSSCredential.ACCEPT_ONLY));
        t("cred6", () -> { GSSCredential c = m.createCredential(GSSCredential.INITIATE_ONLY); c.dispose(); return c.getName(); });
        t("cred7", () -> { GSSCredential c = m.createCredential(null, 0, new Oid[] {KRB5, SPNEGO}, GSSCredential.INITIATE_ONLY); return Arrays.toString(c.getMechs()) + c.getName(KRB5) + " " + c.getName(SPNEGO); });
        return null;
      }
    });
    kdc.stop();
  }
}
