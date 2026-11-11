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
import javax.security.auth.callback.*;
import javax.security.sasl.*;
import org.ietf.jgss.*;

public class KerberosSasl {
  static String hex(byte[] b) { if (b == null) return "null"; StringBuilder s = new StringBuilder(); for (int i = 0; i < b.length && i < 12; i++) s.append(String.format("%02x", b[i])); return (b.length > 100 ? "long" : String.valueOf(b.length)) + ":" + (b.length > 100 ? s.substring(0, 2) : s); }
  static String props(SaslClient c) throws Exception { return c.isComplete() + " qop=" + c.getNegotiatedProperty(Sasl.QOP) + " maxbuf=" + c.getNegotiatedProperty(Sasl.MAX_BUFFER) + " raw=" + c.getNegotiatedProperty(Sasl.RAW_SEND_SIZE) + " strength=" + c.getNegotiatedProperty(Sasl.STRENGTH) + " srvauth=" + c.getNegotiatedProperty(Sasl.SERVER_AUTH) + " reuse=" + c.getNegotiatedProperty(Sasl.REUSE) + " bound=" + c.getNegotiatedProperty(Sasl.BOUND_SERVER_NAME) + " policy=" + c.getNegotiatedProperty(Sasl.POLICY_NOPLAINTEXT) + " credsprop=" + c.getNegotiatedProperty(Sasl.CREDENTIALS); }
  static String sprops(SaslServer c) throws Exception { return c.isComplete() + " qop=" + c.getNegotiatedProperty(Sasl.QOP) + " maxbuf=" + c.getNegotiatedProperty(Sasl.MAX_BUFFER) + " raw=" + c.getNegotiatedProperty(Sasl.RAW_SEND_SIZE) + " strength=" + c.getNegotiatedProperty(Sasl.STRENGTH) + " srvauth=" + c.getNegotiatedProperty(Sasl.SERVER_AUTH) + " bound=" + c.getNegotiatedProperty(Sasl.BOUND_SERVER_NAME) + " authz=" + c.getAuthorizationID(); }
  static Map<Integer, byte[]> kdcKeys;
  static void run(final String label, final Subject alice, final Subject server, final Map<String, Object> cprops, final Map<String, Object> sprops, final String authzid, final String target, final boolean authorize) {
    System.out.println("== " + label);
    try {
      final SaslClient[] cl = new SaslClient[1];
      final SaslServer[] sv = new SaslServer[1];
      final String[] authzSeen = new String[2];
      Subject.doAs(alice, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
        cl[0] = Sasl.createSaslClient(new String[] {"GSSAPI"}, authzid, "ldap", target, cprops, null);
        return null; } });
      System.out.println("client " + (cl[0] == null ? "null" : cl[0].getMechanismName() + " initial=" + cl[0].hasInitialResponse() + " complete=" + cl[0].isComplete()));
      Subject.doAs(server, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
        sv[0] = Sasl.createSaslServer("GSSAPI", "ldap", "server.basalt.test", sprops, new CallbackHandler() { public void handle(Callback[] cbs) {
          for (Callback c : cbs) if (c instanceof AuthorizeCallback) { AuthorizeCallback a = (AuthorizeCallback) c; authzSeen[0] = a.getAuthenticationID(); authzSeen[1] = a.getAuthorizationID(); a.setAuthorized(authorize); a.setAuthorizedID(a.getAuthorizationID()); }
        } });
        return null; } });
      System.out.println("server " + (sv[0] == null ? "null" : sv[0].getMechanismName()));
      try { cl[0].getNegotiatedProperty(Sasl.QOP); } catch (Exception e) { System.out.println("early prop " + e); }
      try { cl[0].wrap(new byte[1], 0, 1); } catch (Exception e) { System.out.println("early wrap " + e); }
      try { cl[0].unwrap(new byte[1], 0, 1); } catch (Exception e) { System.out.println("early unwrap " + e); }
      byte[] ctok = Subject.doAs(alice, new PrivilegedExceptionAction<byte[]>() { public byte[] run() throws Exception { return cl[0].evaluateChallenge(new byte[0]); } });
      System.out.println("c1 " + hex(ctok));
      try { System.out.println(KdcTokens.describeInit(ctok, kdcKeys)); } catch (Throwable e) { System.out.println("describe " + e); }
      byte[] in = ctok;
      int step = 0;
      while (!(cl[0].isComplete() && sv[0].isComplete()) && step < 8) {
        final byte[] sin = in;
        byte[] so = Subject.doAs(server, new PrivilegedExceptionAction<byte[]>() { public byte[] run() throws Exception { return sv[0].evaluateResponse(sin); } });
        System.out.println("s" + step + " " + hex(so) + " complete=" + sv[0].isComplete());
        if (so == null) break;
        final byte[] cin = so;
        byte[] co = Subject.doAs(alice, new PrivilegedExceptionAction<byte[]>() { public byte[] run() throws Exception { return cl[0].evaluateChallenge(cin); } });
        System.out.println("c" + step + " " + hex(co) + " complete=" + cl[0].isComplete());
        if (co == null) break;
        in = co;
        step++;
      }
      System.out.println("client " + props(cl[0]));
      System.out.println("server " + sprops(sv[0]));
      System.out.println("authz seen " + authzSeen[0] + " " + authzSeen[1]);
      if (!"auth-int".equals(cl[0].getNegotiatedProperty(Sasl.QOP)) && !"auth-conf".equals(cl[0].getNegotiatedProperty(Sasl.QOP))) {
        try { cl[0].wrap(new byte[1], 0, 1); } catch (Exception e) { System.out.println("nolayer wrap " + e); }
        try { cl[0].unwrap(new byte[1], 0, 1); } catch (Exception e) { System.out.println("nolayer unwrap " + e); }
        return;
      }
      byte[] w = cl[0].wrap("hello sasl".getBytes(), 0, 10);
      System.out.println("wrap " + hex(w));
      byte[] u = sv[0].unwrap(w, 0, w.length);
      System.out.println("unwrap " + new String(u));
      byte[] w2 = sv[0].wrap("reply".getBytes(), 0, 5);
      System.out.println("wrap2 " + hex(w2) + " " + new String(cl[0].unwrap(w2, 0, w2.length)));
      try { cl[0].evaluateChallenge(new byte[0]); } catch (Exception e) { System.out.println("extra eval " + e); }
      cl[0].dispose(); sv[0].dispose();
    } catch (Throwable e0) { Throwable e = e0; while (e instanceof PrivilegedActionException && e.getCause() != null) e = e.getCause(); System.out.println("!! " + e.getClass().getName() + ": " + e.getMessage() + (e.getCause() != null ? " / " + e.getCause() : "")); }
  }
  public static void main(String[] args) throws Exception {
    MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    kdc.addPrincipal("ldap/server.basalt.test", "svcpw");
    kdc.start();
    File dir = java.nio.file.Files.createTempDirectory("krb5gss").toFile();
    File conf = new File(dir, "krb5.conf");
    kdc.writeConf(conf, "");
    File kt = new File(dir, "server.keytab");
    kdc.writeKeytab(kt, "ldap/server.basalt.test");
    System.setProperty("java.security.krb5.conf", conf.getPath());
    Map<String, String> o = new LinkedHashMap<String, String>();
    o.put("useTicketCache", "false"); o.put("storeKey", "true");
    kdcKeys = kdc.db.get("ldap/server.basalt.test").keys;
    Subject alice = KdcLogin.loginAlice("a", o, "alicepw");
    Map<String, String> o2 = new LinkedHashMap<String, String>();
    o2.put("useKeyTab", "true"); o2.put("keyTab", kt.getPath()); o2.put("principal", "ldap/server.basalt.test"); o2.put("storeKey", "true"); o2.put("isInitiator", "false");
    Subject server = KdcLogin.loginAlice("s", o2, null);
    System.out.println("factories: " + names(Sasl.getSaslClientFactories()));
    System.out.println("sfactories: " + snames(Sasl.getSaslServerFactories()));
    Map<String, Object> none = new HashMap<String, Object>();
    run("default", alice, server, new HashMap<String, Object>(), new HashMap<String, Object>(), null, "server.basalt.test", true);
    Map<String, Object> c1 = new HashMap<String, Object>(); c1.put(Sasl.QOP, "auth-int");
    Map<String, Object> s1 = new HashMap<String, Object>(); s1.put(Sasl.QOP, "auth-int,auth");
    run("auth-int", alice, server, c1, s1, null, "server.basalt.test", true);
    Map<String, Object> c2 = new HashMap<String, Object>(); c2.put(Sasl.QOP, "auth-conf"); c2.put(Sasl.MAX_BUFFER, "1000");
    Map<String, Object> s2 = new HashMap<String, Object>(); s2.put(Sasl.QOP, "auth-conf,auth-int,auth"); s2.put(Sasl.MAX_BUFFER, "5000");
    run("auth-conf", alice, server, c2, s2, "alice@BASALT.TEST", "server.basalt.test", true);
    run("authz denied", alice, server, new HashMap<String, Object>(), new HashMap<String, Object>(), "bob", "server.basalt.test", false);
    Map<String, Object> c4 = new HashMap<String, Object>(); c4.put(Sasl.QOP, "auth-conf");
    Map<String, Object> s4 = new HashMap<String, Object>(); s4.put(Sasl.QOP, "auth");
    run("qop mismatch", alice, server, c4, s4, null, "server.basalt.test", true);
    Map<String, Object> c5 = new HashMap<String, Object>(); c5.put(Sasl.SERVER_AUTH, "false");
    run("no server auth", alice, server, c5, new HashMap<String, Object>(), null, "server.basalt.test", true);
    Map<String, Object> c6 = new HashMap<String, Object>(); c6.put(Sasl.QOP, "auth-conf,auth-int"); c6.put(Sasl.STRENGTH, "high");
    run("strength", alice, server, c6, s2, null, "server.basalt.test", true);
    Map<String, Object> c8 = new HashMap<String, Object>(); c8.put(Sasl.QOP, "auth,auth-int");
    run("auth,auth-int", alice, server, c8, s1, null, "server.basalt.test", true);
    Map<String, Object> c9 = new HashMap<String, Object>(); c9.put(Sasl.QOP, "auth-int,auth");
    run("auth-int,auth", alice, server, c9, new HashMap<String, Object>(), null, "server.basalt.test", true);
    Map<String, Object> c10 = new HashMap<String, Object>(); c10.put(Sasl.SERVER_AUTH, "true");
    run("server auth true", alice, server, c10, new HashMap<String, Object>(), null, "server.basalt.test", true);
    Map<String, Object> c11 = new HashMap<String, Object>(); c11.put(Sasl.QOP, "auth-int"); c11.put("javax.security.sasl.sendmaxbuffer", "2000");
    Map<String, Object> s11 = new HashMap<String, Object>(); s11.put(Sasl.QOP, "auth-int"); s11.put("javax.security.sasl.sendmaxbuffer", "3000");
    run("sendmaxbuffer", alice, server, c11, s11, null, "server.basalt.test", true);
    run("wrong host", alice, server, new HashMap<String, Object>(), new HashMap<String, Object>(), null, "nohost.basalt.test", true);
    run("empty subject", new Subject(), server, new HashMap<String, Object>(), new HashMap<String, Object>(), null, "server.basalt.test", true);
    Map<String, Object> c7 = new HashMap<String, Object>(); c7.put(Sasl.QOP, "bogus");
    run("bad qop", alice, server, c7, new HashMap<String, Object>(), null, "server.basalt.test", true);
    kdc.stop();
  }
  static String names(Enumeration<SaslClientFactory> e) { List<String> l = new ArrayList<String>(); while (e.hasMoreElements()) l.add(Arrays.toString(e.nextElement().getMechanismNames(null))); Collections.sort(l); return l.toString(); }
  static String snames(Enumeration<SaslServerFactory> e) { List<String> l = new ArrayList<String>(); while (e.hasMoreElements()) l.add(Arrays.toString(e.nextElement().getMechanismNames(null))); Collections.sort(l); return l.toString(); }
}
