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

public class KerberosSpnego {
  static String dump(byte[] b, int off, int end, String ind, int depth) {
    StringBuilder sb = new StringBuilder();
    int p = off;
    while (p < end) {
      int tag = b[p++] & 0xff;
      int l = b[p++] & 0xff;
      if (l >= 0x80) { int n = l & 0x7f; l = 0; for (int i = 0; i < n; i++) l = (l << 8) | (b[p++] & 0xff); }
      boolean cons = (tag & 0x20) != 0;
      sb.append(ind).append(String.format("%02x", tag)).append(" len=").append(l);
      if (cons && depth < 4 && !(tag == 0xa2 && false)) {
        sb.append("\n").append(dump(b, p, p + l, ind + "  ", depth + 1));
      } else if (tag == 0x04 && l > 20 && depth < 4) {
        sb.append(" octets[").append(l).append("] ").append(KdcTokens.hex(b, p, 6));
        if ((b[p] & 0xff) == 0x60 || (b[p] & 0xff) == 0xa0 || (b[p] & 0xff) == 0x30) sb.append(" (tok)");
        sb.append("\n");
      } else { sb.append(" ").append(KdcTokens.hex(b, p, Math.min(l, 24))).append("\n"); }
      p += l;
    }
    return sb.toString();
  }
  public static void main(String[] args) throws Exception {
    final java.io.PrintStream raw = System.out;
    System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
      private final StringBuilder line = new StringBuilder();
      public void write(int b) {
        if (b == '\n') {
          raw.println(line.toString().replaceAll("len=\\d+", "len=N").replaceAll("(octets\\[)\\d+\\] [0-9a-f]+", "$1N]")
              .replaceAll("(len=N) [0-9a-f]{20,}", "$1"));
          line.setLength(0);
        } else {
          line.append((char) b);
        }
      }
    }, true));
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
    final boolean mutual = args.length < 2 || !args[1].equals("nomutual");
    final GSSContext[] ic = new GSSContext[1];
    final byte[][] tok = new byte[1][];
    Subject.doAs(alice, new PrivilegedExceptionAction<Object>() {
      public Object run() throws Exception {
        GSSName target = m.createName("host@server.basalt.test", GSSName.NT_HOSTBASED_SERVICE);
        GSSContext c = m.createContext(target, KdcLogin.SPNEGO, null, GSSContext.DEFAULT_LIFETIME);
        c.requestMutualAuth(mutual);
        ic[0] = c;
        KdcLogin.t("pre mech", () -> c.getMech());
        tok[0] = c.initSecContext(new byte[0], 0, 0);
        System.out.println("INIT1 len=" + tok[0].length + "\n" + dump(tok[0], 0, tok[0].length, "", 0));
        KdcLogin.t("i1 est", () -> c.isEstablished());
        return null;
      }
    });
    Subject.doAs(server, new PrivilegedExceptionAction<Object>() {
      public Object run() throws Exception {
        final GSSContext a = m.createContext((GSSCredential) null);
        byte[] t = tok[0];
        int round = 0;
        while (true) {
          byte[] out = a.acceptSecContext(t, 0, t.length);
          System.out.println("ACC" + round + " est=" + a.isEstablished() + " out=" + (out == null ? "null" : "len=" + out.length + "\n" + dump(out, 0, out.length, "", 0)));
          if (out == null) break;
          byte[] n = ic[0].initSecContext(out, 0, out.length);
          System.out.println("INIT" + (round + 2) + " est=" + ic[0].isEstablished() + " out=" + (n == null ? "null" : "len=" + n.length + "\n" + dump(n, 0, n.length, "", 0)));
          if (n == null) break;
          t = n;
          round++;
        }
        KdcLogin.t("a.est", () -> a.isEstablished());
        KdcLogin.t("i.est", () -> ic[0].isEstablished());
        KdcLogin.t("a.mech", () -> a.getMech());
        KdcLogin.t("i.mech", () -> ic[0].getMech());
        KdcLogin.t("a.src", () -> a.getSrcName());
        KdcLogin.t("a.targ", () -> a.getTargName());
        KdcLogin.t("i.src", () -> ic[0].getSrcName());
        KdcLogin.t("i.targ", () -> ic[0].getTargName());
        KdcLogin.t("a.flags", () -> a.getMutualAuthState() + " " + a.getConfState() + " " + a.getIntegState() + " " + a.getReplayDetState() + " " + a.getSequenceDetState() + " " + a.getCredDelegState() + " " + a.isProtReady() + " " + a.getLifetime());
        KdcLogin.t("i.flags", () -> ic[0].getMutualAuthState() + " " + ic[0].getConfState() + " " + ic[0].getIntegState() + " " + ic[0].getReplayDetState() + " " + ic[0].getSequenceDetState() + " " + ic[0].getCredDelegState() + " " + ic[0].isProtReady());
        byte[] w = ic[0].wrap("hello".getBytes(), 0, 5, new MessageProp(true));
        KdcLogin.t("wrap hdr", () -> KdcTokens.hex(w, 0, 4) + " " + w.length);
        KdcLogin.t("unwrap", () -> new String(a.unwrap(w, 0, w.length, new MessageProp(0, false))));
        byte[] mic = a.getMIC("x".getBytes(), 0, 1, new MessageProp(0, false));
        KdcLogin.t("mic", () -> { ic[0].verifyMIC(mic, 0, mic.length, "x".getBytes(), 0, 1, new MessageProp(0, false)); return mic.length; });
        KdcLogin.t("wsl", () -> ic[0].getWrapSizeLimit(0, true, 1000));
        KdcLogin.t("inq", () -> ((boolean[]) ((ExtendedGSSContext) a).inquireSecContext(InquireType.KRB5_GET_TKT_FLAGS)).length);
        return null;
      }
    });
    kdc.stop();
  }
}
