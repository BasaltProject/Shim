/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
// jvm-args: --add-exports java.security.jgss/sun.security.krb5=ALL-UNNAMED
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;

public class KerberosLogin {
  static File dir;

  static Map<String, String> opts(String... kv) {
    Map<String, String> m = new LinkedHashMap<String, String>();
    m.put("useTicketCache", "false");
    for (int i = 0; i < kv.length; i += 2) {
      m.put(kv[i], kv[i + 1]);
    }
    return m;
  }

  static void attempt(String label, Map<String, String> o, String user, String pw, MiniKdc kdc) {
    kdc.events.clear();
    try {
      Subject s = KdcLogin.login("login", o, user, pw);
      System.out.println(label + " -> " + KdcLogin.describe(s));
    } catch (Throwable e) {
      System.out.println(label + " !! " + KdcLogin.chain(e));
    }
    System.out.println("  kdc " + KdcLogin.scrub(kdc.events.toString()));
  }

  static MiniKdc newKdc(String conf) throws Exception {
    MiniKdc kdc = new MiniKdc("BASALT.TEST");
    kdc.addPrincipal("alice", "alicepw");
    kdc.addPrincipal("host/server.basalt.test", "svcpw");
    kdc.start();
    kdc.writeConf(new File(dir, "krb5.conf"), conf);
    System.setProperty("java.security.krb5.conf", new File(dir, "krb5.conf").getPath());
    refresh();
    return kdc;
  }

  static void refresh() throws Exception {
    Class<?> c = Class.forName("sun.security.krb5.Config");
    c.getMethod("refresh").invoke(null);
  }

  public static void main(String[] args) throws Exception {
    dir = Files.createTempDirectory("krb5login").toFile();
    MiniKdc kdc = newKdc("");
    for (int et : new int[] {1, 3, 16, 17, 18, 19, 20, 23}) {
      KerberosKey k = new KerberosKey(new KerberosPrincipal("alice@BASALT.TEST"), "alicepw".toCharArray(),
          et == 1 || et == 3 ? "DES" : et == 16 ? "DESede" : et == 17 ? "AES128" : et == 18 ? "AES256"
          : et == 23 ? "ArcFourHmac" : et == 19 ? "aes128-cts-hmac-sha256-128" : "aes256-cts-hmac-sha384-192");
      StringBuilder hex = new StringBuilder();
      for (byte b : k.getEncoded()) {
        hex.append(String.format("%02x", b));
      }
      System.out.println("key " + et + " " + k.getAlgorithm() + " " + k.getKeyType() + " " + hex);
    }
    attempt("password", opts("storeKey", "true"), "alice", "alicepw", kdc);
    attempt("wrong password", opts(), "alice", "bad", kdc);
    attempt("unknown user", opts(), "nobody", "x", kdc);
    attempt("no prompt", opts("doNotPrompt", "true"), "alice", "alicepw", kdc);
    attempt("principal option", opts("principal", "alice@BASALT.TEST"), "ignored", "alicepw", kdc);
    attempt("other realm", opts("principal", "bob@OTHER.REALM"), "bob", "x", kdc);
    kdc.writeKeytab(new File(dir, "server.keytab"), "host/server.basalt.test");
    String kt = new File(dir, "server.keytab").getPath();
    attempt("keytab acceptor", opts("useKeyTab", "true", "keyTab", kt, "principal", "host/server.basalt.test",
        "storeKey", "true", "isInitiator", "false"), "x", "x", kdc);
    attempt("keytab initiator", opts("useKeyTab", "true", "keyTab", kt, "principal", "host/server.basalt.test"),
        "x", "x", kdc);
    attempt("keytab missing", opts("useKeyTab", "true", "keyTab", "/nonexistent.keytab", "principal", "alice",
        "doNotPrompt", "true"), "alice", "alicepw", kdc);
    attempt("keytab missing fallback", opts("useKeyTab", "true", "keyTab", "/nonexistent.keytab", "principal",
        "alice"), "alice", "alicepw", kdc);
    attempt("cache missing", opts("useTicketCache", "true", "ticketCache", "/nonexistent.cc", "doNotPrompt",
        "true"), "alice", "alicepw", kdc);
    attempt("cache missing fallback", opts("useTicketCache", "true", "ticketCache", "/nonexistent.cc"), "alice",
        "alicepw", kdc);
    Subject first = KdcLogin.login("login", opts(), "alice", "alicepw");
    for (Object c : first.getPrivateCredentials()) {
      if (c instanceof KerberosTicket) {
        File cc = new File(dir, "cc");
        KdcLogin.writeCcache(cc, (KerberosTicket) c);
        kdc.events.clear();
        attempt("ticket cache", opts("useTicketCache", "true", "ticketCache", cc.getPath(), "doNotPrompt", "true"),
            "x", "x", kdc);
        attempt("ticket cache principal", opts("useTicketCache", "true", "ticketCache", cc.getPath(), "principal",
            "alice", "doNotPrompt", "true"), "x", "x", kdc);
        attempt("ticket cache wrong principal", opts("useTicketCache", "true", "ticketCache", cc.getPath(),
            "principal", "bob@BASALT.TEST", "doNotPrompt", "true"), "x", "x", kdc);
      }
    }
    attempt("config error", opts("doNotPrompt", "true"), "alice", "alicepw", kdc);
    kdc.stop();
    String[] confs = {" default_tkt_enctypes = rc4-hmac aes128-cts\n forwardable = true\n renewable = true\n",
        " default_tkt_enctypes = aes256-cts-hmac-sha1-96\n permitted_enctypes = aes256-cts-hmac-sha1-96 rc4-hmac\n"
        + " canonicalize = true\n"};
    for (String conf : confs) {
      MiniKdc k = newKdc(conf);
      System.out.println("conf [" + conf.replace("\n", ";") + "]");
      attempt("  login", opts("storeKey", "true"), "alice", "alicepw", k);
      k.stop();
    }
    MiniKdc nopre = newKdc("");
    nopre.requirePreauth = false;
    attempt("no preauth", opts(), "alice", "alicepw", nopre);
    attempt("no preauth wrong password", opts(), "alice", "bad", nopre);
    nopre.stop();
    System.out.println("done");
  }
}
