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

public class KerberosGssWrap {
  static void t(String n, java.util.concurrent.Callable<Object> c) {
    try { Object o = c.call(); System.out.println(n + " -> " + (o instanceof byte[] ? "bytes" + (((byte[]) o).length > 100 ? "long" : String.valueOf(((byte[]) o).length)) : o instanceof Object[] ? Arrays.toString((Object[]) o) : o)); }
    catch (Throwable e) { System.out.println(n + " !! " + e.getClass().getName() + ": " + (e instanceof ClassCastException ? "" : e.getMessage()) + (e instanceof GSSException ? " major=" + ((GSSException) e).getMajor() + " minor=" + ((GSSException) e).getMinor() + " " + ((GSSException) e).getMajorString() : "")); }
  }
  public static void main(String[] args) throws Exception {
    final MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    final MiniKdc.Princ svc = kdc.addPrincipal("host/server.basalt.test", "svcpw");
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
    final int before = alice.getPrivateCredentials().size();
    final GSSContext[] ic = new GSSContext[1];
    final GSSContext[] ac = new GSSContext[1];
    Subject.doAs(alice, new PrivilegedExceptionAction<Object>() {
      public Object run() throws Exception {
        GSSName target = m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE);
        GSSContext c = m.createContext(target, KdcLogin.KRB5, null, GSSContext.DEFAULT_LIFETIME);
        ic[0] = c;
        t("i.pre.established", () -> c.isEstablished());
        t("i.pre.src", () -> c.getSrcName());
        t("i.pre.targ", () -> c.getTargName());
        t("i.pre.lifetime", () -> c.getLifetime());
        t("i.pre.mech", () -> c.getMech());
        t("i.pre.initiator", () -> c.isInitiator());
        t("i.pre.wrap", () -> c.wrap(new byte[1], 0, 1, new MessageProp(true)));
        t("i.pre.deleg", () -> c.getCredDelegState());
        t("i.pre.mutual", () -> c.getMutualAuthState());
        t("i.pre.conf", () -> c.getConfState());
        t("i.pre.integ", () -> c.getIntegState());
        t("i.pre.replay", () -> c.getReplayDetState());
        t("i.pre.seq", () -> c.getSequenceDetState());
        t("i.pre.protready", () -> c.isProtReady());
        t("i.pre.transferable", () -> c.isTransferable());
        t("i.pre.export", () -> c.export());
        t("i.req", () -> { c.requestMutualAuth(true); c.requestConf(false); c.requestInteg(true); c.requestLifetime(60); c.requestReplayDet(false);  return c.getConfState() + " " + c.getMutualAuthState() + " " + c.getReplayDetState() + " " + c.getLifetime(); });
        t("i.req2", () -> { c.requestAnonymity(true); return c.getAnonymityState(); });
        t("i.req3", () -> { c.requestCredDeleg(true); return c.getCredDelegState(); });
        c.requestConf(true); c.requestReplayDet(true); c.requestSequenceDet(true);
        ic[0] = c;
        return null;
      }
    });
    final byte[][] tok = new byte[1][];
    Subject.doAs(alice, new PrivilegedExceptionAction<Object>() {
      public Object run() throws Exception {
        tok[0] = ic[0].initSecContext(new byte[0], 0, 0);
        System.out.println("alice creds before/after " + before + " " + alice.getPrivateCredentials().size());
        for (Object x : alice.getPrivateCredentials()) if (x instanceof KerberosTicket) System.out.println("  " + ((KerberosTicket) x).getServer() + " " + ((KerberosTicket) x).getSessionKeyType());
        t("i.mid.established", () -> ic[0].isEstablished());
        t("i.mid.lifetime", () -> ic[0].getLifetime() > 36000 - 100);
        t("i.mid.src", () -> ic[0].getSrcName());
        t("i.mid.wrap", () -> ic[0].wrap(new byte[1], 0, 1, new MessageProp(true)));
        t("i.mid.protready", () -> ic[0].isProtReady());
        t("i.mid.init2", () -> ic[0].initSecContext(new byte[3], 0, 3));
        return null;
      }
    });
    Subject.doAs(server, new PrivilegedExceptionAction<Object>() {
      public Object run() throws Exception {
        final GSSContext a = m.createContext((GSSCredential) null);
        ac[0] = a;
        t("a.pre.src", () -> a.getSrcName());
        t("a.pre.targ", () -> a.getTargName());
        t("a.pre.mech", () -> a.getMech());
        t("a.pre.initiator", () -> a.isInitiator());
        byte[] rep = a.acceptSecContext(tok[0], 0, tok[0].length);
        t("a.again", () -> a.acceptSecContext(tok[0], 0, tok[0].length));
        t("a.lifetime", () -> a.getLifetime() > 36000 - 100);
        t("a.export", () -> a.export());
        t("a.deleg", () -> a.getDelegCred());
        t("a.initiator", () -> a.isInitiator());
        t("a.transferable", () -> a.isTransferable());
        ic[0].initSecContext(rep, 0, rep.length);
        t("i.lifetime", () -> ic[0].getLifetime() > 36000 - 100);
        t("i.targ", () -> ic[0].getTargName());
        t("i.src", () -> ic[0].getSrcName());
        t("i.reinit", () -> ic[0].initSecContext(new byte[0], 0, 0));
        for (final int max : new int[] {0, 16, 40, 60, 69, 70, 100, 500, 1000, 4096, 65535}) {
          t("wsl " + max, () -> ic[0].getWrapSizeLimit(0, true, max) + " " + ic[0].getWrapSizeLimit(0, false, max));
        }
        t("wsl qop1", () -> ic[0].getWrapSizeLimit(1, true, 1000));
        t("wsl neg", () -> ic[0].getWrapSizeLimit(0, true, -1));
        final MessageProp mp = new MessageProp(0, true);
        byte[] w1 = ic[0].wrap("one".getBytes(), 0, 3, mp);
        byte[] w2 = ic[0].wrap("two".getBytes(), 0, 3, new MessageProp(0, false));
        byte[] w3 = ic[0].wrap("three".getBytes(), 0, 5, new MessageProp(0, true));
        t("w qop1", () -> ic[0].wrap("x".getBytes(), 0, 1, new MessageProp(1, true)));
        t("w sizes", () -> w1.length + " " + w2.length + " " + w3.length);
        t("w2 hdr", () -> KdcTokens.hex(w2, 0, 12));
        final MessageProp r = new MessageProp(0, false);
        t("unwrap2", () -> new String(a.unwrap(w2, 0, w2.length, r)) + " " + r.getPrivacy() + " " + r.isDuplicateToken() + r.isGapToken() + r.isOldToken() + r.isUnseqToken());
        t("unwrap3", () -> { MessageProp q = new MessageProp(0, false); String s = new String(a.unwrap(w3, 0, w3.length, q)); return s + " " + q.getPrivacy() + " " + q.isDuplicateToken() + q.isGapToken() + q.isOldToken() + q.isUnseqToken(); });
        t("unwrap1 late", () -> { MessageProp q = new MessageProp(0, false); String s = new String(a.unwrap(w1, 0, w1.length, q)); return s + " " + q.getPrivacy() + " " + q.isDuplicateToken() + q.isGapToken() + q.isOldToken() + q.isUnseqToken(); });
        t("unwrap3 dup", () -> { MessageProp q = new MessageProp(0, false); String s = new String(a.unwrap(w3, 0, w3.length, q)); return s + " " + q.getPrivacy() + " " + q.isDuplicateToken() + q.isGapToken() + q.isOldToken() + q.isUnseqToken(); });
        t("unwrap tampered", () -> { byte[] x = w3.clone(); x[x.length - 1] ^= 1; return a.unwrap(x, 0, x.length, new MessageProp(0, false)); });
        t("unwrap short", () -> a.unwrap(new byte[5], 0, 5, new MessageProp(0, false)));
        t("unwrap junk", () -> a.unwrap(new byte[40], 0, 40, new MessageProp(0, false)));
        t("unwrap own", () -> ic[0].unwrap(w3, 0, w3.length, new MessageProp(0, false)));
        t("verify bad", () -> { a.verifyMIC(new byte[28], 0, 28, new byte[1], 0, 1, new MessageProp(0, false)); return "x"; });
        t("mic qop", () -> a.getMIC(new byte[1], 0, 1, new MessageProp(2, false)));
        t("mic empty", () -> { byte[] mm = a.getMIC(new byte[0], 0, 0, new MessageProp(0, false)); ic[0].verifyMIC(mm, 0, mm.length, new byte[0], 0, 0, new MessageProp(0, false)); return mm.length; });
        t("verify wrongdata", () -> { byte[] mm = a.getMIC("aaa".getBytes(), 0, 3, new MessageProp(0, false)); ic[0].verifyMIC(mm, 0, mm.length, "aab".getBytes(), 0, 3, new MessageProp(0, false)); return "x"; });
        t("inquire key", () -> { Object k = ((ExtendedGSSContext) ic[0]).inquireSecContext(InquireType.KRB5_GET_SESSION_KEY); return k.getClass().getName() + " " + ((javax.crypto.SecretKey) k).getAlgorithm() + " " + ((javax.crypto.SecretKey) k).getEncoded().length + " " + ((javax.crypto.SecretKey) k).getFormat(); });
        t("inquire keyex", () -> { Object k = ((ExtendedGSSContext) ic[0]).inquireSecContext(InquireType.KRB5_GET_SESSION_KEY_EX); return k.getClass().getName() + " " + ((javax.crypto.SecretKey) k).getAlgorithm(); });
        t("inquire flags", () -> Arrays.toString((boolean[]) ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_TKT_FLAGS)));
        t("inquire authz", () -> ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_AUTHZ_DATA));
        t("inquire authz i", () -> ((ExtendedGSSContext) ic[0]).inquireSecContext(InquireType.KRB5_GET_AUTHZ_DATA));
        t("inquire authtime", () -> ((String) ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_AUTHTIME)).length());
        t("inquire authtime i", () -> ((ExtendedGSSContext) ic[0]).inquireSecContext(InquireType.KRB5_GET_AUTHTIME).toString().length());
        t("inquire cred", () -> ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_KRB_CRED));
        t("inquire key a", () -> { Object k = ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_SESSION_KEY); return ((javax.crypto.SecretKey) k).getEncoded().length; });
        t("dispose", () -> { ic[0].dispose(); return ic[0].isEstablished(); });
        t("after wrap", () -> ic[0].wrap(new byte[1], 0, 1, new MessageProp(true)));
        t("after dispose2", () -> { ic[0].dispose(); return 1; });
        t("after src", () -> ic[0].getSrcName());
        return null;
      }
    });
    kdc.stop();
  }
}
