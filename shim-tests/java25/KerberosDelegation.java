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

public class KerberosDelegation {
  static void t(String n, java.util.concurrent.Callable<Object> c) { KdcLogin.t(n, c); }
  public static void main(String[] args) throws Exception {
    String extra = args.length > 0 ? args[0].replace("\\n", "\n") : "";
    final String mechS = args.length > 1 ? args[1] : "krb5";
    final Oid mech = mechS.equals("spnego") ? KdcLogin.SPNEGO : KdcLogin.KRB5;
    MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    kdc.addPrincipal("host/server.basalt.test", "svcpw");
    kdc.addPrincipal("HTTP/back.basalt.test", "httppw");
    kdc.start();
    File dir = java.nio.file.Files.createTempDirectory("dlg").toFile();
    kdc.writeConf(new File(dir, "krb5.conf"), " forwardable = true\n" + extra);
    kdc.writeKeytab(new File(dir, "server.keytab"), "host/server.basalt.test");
    kdc.writeKeytab(new File(dir, "back.keytab"), "HTTP/back.basalt.test");
    System.setProperty("java.security.krb5.conf", new File(dir, "krb5.conf").getPath());
    Map<String, String> o = new LinkedHashMap<String, String>();
    o.put("useTicketCache", "false"); o.put("storeKey", "true");
    final Subject alice = KdcLogin.loginAlice("a", o, "alicepw");
    Map<String, String> o2 = new LinkedHashMap<String, String>();
    o2.put("useKeyTab", "true"); o2.put("keyTab", new File(dir, "server.keytab").getPath()); o2.put("principal", "host/server.basalt.test"); o2.put("storeKey", "true"); o2.put("isInitiator", "false");
    final Subject server = KdcLogin.loginAlice("s", o2, null);
    Map<String, String> o3 = new LinkedHashMap<String, String>();
    o3.put("useKeyTab", "true"); o3.put("keyTab", new File(dir, "back.keytab").getPath()); o3.put("principal", "HTTP/back.basalt.test"); o3.put("storeKey", "true"); o3.put("isInitiator", "false");
    final Subject back = KdcLogin.loginAlice("b", o3, null);
    final GSSManager m = GSSManager.getInstance();
    final boolean[] wantDeleg = {true};
    final GSSContext[] ctx = new GSSContext[2];
    final GSSName tgt = m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE);
    for (final int round : new int[] {0, 1, 2}) {
      System.out.println("== round " + round);
      Subject.doAs(alice, new PrivilegedAction<Object>() { public Object run() {
        t("init", () -> {
          GSSContext x = m.createContext(tgt, mech, null, GSSContext.DEFAULT_LIFETIME);
          x.requestMutualAuth(true);
          if (round != 1) x.requestCredDeleg(true);
          if (round == 2) ((ExtendedGSSContext) x).requestDelegPolicy(true);
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
          return "established " + ctx[0].isEstablished() + "/" + ctx[1].isEstablished() + " deleg i=" + ctx[0].getCredDelegState() + " a=" + ctx[1].getCredDelegState() + " policy=" + ((ExtendedGSSContext) ctx[0]).getDelegPolicyState();
        });
        return null; } });
      if (ctx[1] == null) continue;
      final GSSContext a = ctx[1];
      t("srcName", () -> a.getSrcName());
      t("delegCred-i", () -> ctx[0].getDelegCred());
      t("delegCred", () -> { GSSCredential c = a.getDelegCred(); return c == null ? "null" : c.getName() + " " + c.getUsage() + " " + java.util.Arrays.toString(c.getMechs()) + " life>0=" + (c.getRemainingLifetime() > 0) + " " + c.toString().replaceAll("\\n", "|"); });
      t("krbcred-acceptor", () -> { Object k = ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_KRB_CRED); return k == null ? "null" : ((KerberosCredMessage) k).getSender() + " " + ((KerberosCredMessage) k).getRecipient() + " " + (((KerberosCredMessage) k).getEncoded().length > 100); });
      t("krbcred-init", () -> { Object k = ((ExtendedGSSContext) ctx[0]).inquireSecContext(InquireType.KRB5_GET_KRB_CRED); return k == null ? "null" : ((KerberosCredMessage) k).getSender() + " " + ((KerberosCredMessage) k).getRecipient() + " " + ((KerberosCredMessage) k).getEncoded().length + " " + k; });
      t("tktflags", () -> { boolean[] f = (boolean[]) ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_TKT_FLAGS); return KdcLogin.flags(f); });
      // use delegated credential to talk to the back-end
      t("back", () -> {
        final GSSCredential dc = a.getDelegCred();
        if (dc == null) return "no cred";
        return Subject.doAs(new Subject(), new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
          GSSContext x = m.createContext(m.createName("HTTP@back.basalt.test", GSSName.NT_HOSTBASED_SERVICE), KdcLogin.KRB5, dc, GSSContext.DEFAULT_LIFETIME);
          x.requestMutualAuth(true);
          byte[] tk = x.initSecContext(new byte[0], 0, 0);
          return Subject.doAs(back, new PrivilegedExceptionAction<Object>() { public Object run() throws Exception {
            GSSContext y = m.createContext((GSSCredential) null);
            byte[] r = y.acceptSecContext(tk, 0, tk.length);
            x.initSecContext(r, 0, r.length);
            return "back established " + y.isEstablished() + " src=" + y.getSrcName() + " " + x.getSrcName(); } }); } });
      });
    }
    kdc.stop();
    System.exit(0);
  }
}
