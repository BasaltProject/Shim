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

public class KerberosS4U {
  static void t(String n, java.util.concurrent.Callable<Object> c) { KdcLogin.t(n, c); }
  public static void main(String[] args) throws Exception {
    MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    kdc.addPrincipal("host/server.basalt.test", "svcpw");
    kdc.addPrincipal("HTTP/back.basalt.test", "httppw");
    kdc.start();
    File dir = java.nio.file.Files.createTempDirectory("s4u").toFile();
    kdc.writeConf(new File(dir, "krb5.conf"), " forwardable = true\n");
    kdc.writeKeytab(new File(dir, "server.keytab"), "host/server.basalt.test");
    kdc.writeKeytab(new File(dir, "back.keytab"), "HTTP/back.basalt.test");
    System.setProperty("java.security.krb5.conf", new File(dir, "krb5.conf").getPath());
    Map<String, String> o2 = new LinkedHashMap<String, String>();
    o2.put("useKeyTab", "true"); o2.put("keyTab", new File(dir, "server.keytab").getPath()); o2.put("principal", "host/server.basalt.test"); o2.put("storeKey", "true"); o2.put("isInitiator", "true");
    final Subject server = KdcLogin.loginAlice("s", o2, null);
    Map<String, String> o3 = new LinkedHashMap<String, String>();
    o3.put("useKeyTab", "true"); o3.put("keyTab", new File(dir, "back.keytab").getPath()); o3.put("principal", "HTTP/back.basalt.test"); o3.put("storeKey", "true"); o3.put("isInitiator", "false");
    final Subject back = KdcLogin.loginAlice("b", o3, null);
    final GSSManager m = GSSManager.getInstance();
    Subject.doAs(server, new PrivilegedAction<Object>() { public Object run() {
      final GSSCredential[] imp = new GSSCredential[1];
      t("self", () -> { GSSCredential c = m.createCredential(null, GSSCredential.DEFAULT_LIFETIME, KdcLogin.KRB5, GSSCredential.INITIATE_ONLY); return c.getName() + " " + (c instanceof ExtendedGSSCredential); });
      t("impersonate", () -> {
        GSSCredential c = m.createCredential(null, GSSCredential.DEFAULT_LIFETIME, KdcLogin.KRB5, GSSCredential.INITIATE_ONLY);
        imp[0] = ((ExtendedGSSCredential) c).impersonate(m.createName("alice", GSSName.NT_USER_NAME));
        return imp[0].getName() + " usage=" + imp[0].getUsage() + " mechs=" + Arrays.toString(imp[0].getMechs()) + " life>0=" + (imp[0].getRemainingLifetime() > 0) + " " + imp[0].toString().replace("\n", "|");
      });
      t("impersonate-null", () -> { GSSCredential c = m.createCredential(null, GSSCredential.DEFAULT_LIFETIME, KdcLogin.KRB5, GSSCredential.INITIATE_ONLY); return ((ExtendedGSSCredential) c).impersonate(null); });
      t("impersonate-unknown", () -> { GSSCredential c = m.createCredential(null, GSSCredential.DEFAULT_LIFETIME, KdcLogin.KRB5, GSSCredential.INITIATE_ONLY); return ((ExtendedGSSCredential) c).impersonate(m.createName("nobody", GSSName.NT_USER_NAME)); });
      t("impersonate-twice", () -> imp[0] == null ? "skip" : ((ExtendedGSSCredential) imp[0]).impersonate(m.createName("alice", GSSName.NT_USER_NAME)));
      t("s4u2proxy", () -> {
        GSSContext x = m.createContext(m.createName("HTTP@back.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, imp[0], GSSContext.DEFAULT_LIFETIME);
        x.requestMutualAuth(true);
        byte[] tk = x.initSecContext(new byte[0], 0, 0);
        return Subject.doAs(back, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
          GSSContext y = m.createContext((GSSCredential) null);
          byte[] r = y.acceptSecContext(tk, 0, tk.length);
          if (r != null) x.initSecContext(r, 0, r.length);
          byte[] w = x.wrap("hi".getBytes(), 0, 2, new MessageProp(0, true));
          return "back established " + y.isEstablished() + " " + x.isEstablished() + " src=" + y.getSrcName() + " " + x.getSrcName() + " targ=" + x.getTargName() + " unwrap=" + new String(y.unwrap(w, 0, w.length, new MessageProp(0, false))); } }); });
      t("s4u2proxy-spnego", () -> {
        GSSContext x = m.createContext(m.createName("HTTP@back.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.SPNEGO, imp[0], GSSContext.DEFAULT_LIFETIME);
        byte[] tk = x.initSecContext(new byte[0], 0, 0);
        return "tok " + (tk != null);
      });
      return null; } });
    kdc.stop();
    System.exit(0);
  }
}
