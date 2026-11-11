/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;
import javax.net.ssl.*;

public class TlsCipherSuitesModern {
  static final String STORE =
      "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" +
      "EA==";

  static KeyStore load() throws Exception {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(STORE)), "changeit".toCharArray());
    return ks;
  }

  static final Map<String, KeyManager[]> keyManagers = new HashMap<>();
  static TrustManager[] trustManagers;

  static SSLContext ctx(String proto, String only) throws Exception {
    if (trustManagers == null) {
      KeyStore ks = load();
      KeyStore trust = KeyStore.getInstance("PKCS12");
      trust.load(null, null);
      for (String alias : Collections.list(ks.aliases())) {
        trust.setCertificateEntry(alias, ks.getCertificate(alias));
      }
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
      tmf.init(trust);
      trustManagers = tmf.getTrustManagers();
    }
    KeyManager[] kms = keyManagers.get(String.valueOf(only));
    if (kms == null) {
      KeyStore ks = load();
      for (String alias : Collections.list(ks.aliases())) {
        if (only != null && !alias.equals(only)) {
          ks.deleteEntry(alias);
        }
      }
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, "changeit".toCharArray());
      kms = kmf.getKeyManagers();
      keyManagers.put(String.valueOf(only), kms);
    }
    SSLContext c = SSLContext.getInstance(proto);
    c.init(kms, trustManagers, null);
    return c;
  }

  static String alias(String suite) {
    return suite.contains("ECDSA") ? "ec" : suite.contains("DSS") ? "dsa" : "rsa";
  }

  static String socket(String suite, String[] protos, String[] clientSuites, String only) {
    try {
      SSLContext sc = ctx("TLS", only), cc = ctx("TLS", null);
      SSLServerSocket ss = (SSLServerSocket) sc.getServerSocketFactory().createServerSocket(0, 5,
          InetAddress.getLoopbackAddress());
      if (suite != null) {
        ss.setEnabledCipherSuites(new String[] {suite});
      }
      final String[] server = new String[1];
      Thread t = new Thread(() -> {
        try (SSLSocket s = (SSLSocket) ss.accept()) {
          BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
          String line = r.readLine();
          s.getOutputStream().write(("echo:" + line + "\n").getBytes());
          s.getOutputStream().flush();
          server[0] = s.getSession().getProtocol() + " " + s.getSession().getCipherSuite();
        } catch (Throwable e) {
          server[0] = "!" + e.getClass().getName();
        }
      });
      t.start();
      String client;
      try (SSLSocket c = (SSLSocket) cc.getSocketFactory().createSocket(InetAddress.getLoopbackAddress(),
          ss.getLocalPort())) {
        c.setSoTimeout(20000);
        c.setEnabledProtocols(protos);
        if (clientSuites != null) {
          c.setEnabledCipherSuites(clientSuites);
        }
        c.getOutputStream().write("hello\n".getBytes());
        c.getOutputStream().flush();
        String line = new BufferedReader(new InputStreamReader(c.getInputStream())).readLine();
        client = c.getSession().getProtocol() + " " + c.getSession().getCipherSuite() + " " + line;
      } catch (Throwable e) {
        client = "!" + e.getClass().getName();
      }
      t.join(20000);
      ss.close();
      return client + " | " + server[0];
    } catch (Throwable e) {
      return "setup !" + e;
    }
  }

  static byte[] wrap(SSLEngine e, ByteBuffer src) throws Exception {
    ByteBuffer buf = ByteBuffer.allocate(20000);
    SSLEngineResult r = e.wrap(src, buf);
    buf.flip();
    byte[] p = new byte[buf.remaining()];
    buf.get(p);
    return r.getStatus() == SSLEngineResult.Status.OK || r.getStatus() == SSLEngineResult.Status.CLOSED ? p : null;
  }

  static String dtls(String suite) {
    try {
      SSLEngine c = ctx("DTLS", null).createSSLEngine("localhost", 1), s = ctx("DTLS", alias(suite)).createSSLEngine();
      c.setUseClientMode(true);
      s.setUseClientMode(false);
      c.setEnabledCipherSuites(new String[] {suite});
      s.setEnabledCipherSuites(new String[] {suite});
      c.beginHandshake();
      s.beginHandshake();
      SSLEngine[] e = {c, s};
      List<List<byte[]>> inbox = new ArrayList<>();
      inbox.add(new ArrayList<byte[]>());
      inbox.add(new ArrayList<byte[]>());
      ByteBuffer empty = ByteBuffer.allocate(0);
      for (int round = 0; round < 100; round++) {
        boolean progress = false;
        for (int w = 0; w < 2; w++) {
          SSLEngine eng = e[w];
          for (int guard = 0; guard < 20; guard++) {
            SSLEngineResult.HandshakeStatus hs = eng.getHandshakeStatus();
            if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
              Runnable r;
              while ((r = eng.getDelegatedTask()) != null) {
                r.run();
              }
              progress = true;
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
              byte[] p = wrap(eng, empty);
              progress = true;
              if (p != null && p.length > 0) {
                inbox.get(1 - w).add(p);
              }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP
                || hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN) {
              List<byte[]> in = inbox.get(w);
              ByteBuffer pk = in.isEmpty() ? ByteBuffer.allocate(0) : ByteBuffer.wrap(in.remove(0));
              if (pk.remaining() == 0 && hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                break;
              }
              eng.unwrap(pk, ByteBuffer.allocate(20000));
              progress = true;
            } else {
              break;
            }
          }
        }
        if (c.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
            && s.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
            && !c.getSession().getProtocol().equals("NONE")) {
          break;
        }
        if (!progress) {
          break;
        }
      }
      byte[] p = wrap(c, ByteBuffer.wrap("hello dtls".getBytes()));
      ByteBuffer app = ByteBuffer.allocate(20000);
      s.unwrap(ByteBuffer.wrap(p), app);
      return c.getSession().getProtocol() + " " + c.getSession().getCipherSuite() + " "
          + new String(app.array(), 0, app.position());
    } catch (Throwable t) {
      return "!" + t.getClass().getName() + ":" + t.getMessage();
    }
  }

  static String dtlsProtocols(String[] protos) {
    try {
      SSLEngine c = ctx("DTLS", null).createSSLEngine("localhost", 1), s = ctx("DTLS", "rsa").createSSLEngine();
      c.setUseClientMode(true);
      s.setUseClientMode(false);
      c.setEnabledProtocols(protos);
      System.out.println(Arrays.toString(c.getEnabledProtocols()));
      c.beginHandshake();
      ByteBuffer out = ByteBuffer.allocate(20000);
      SSLEngineResult r = c.wrap(ByteBuffer.allocate(0), out);
      return r.getStatus() + " " + r.getHandshakeStatus() + " " + (out.position() > 0);
    } catch (Throwable t) {
      return "!" + t.getClass().getName() + ":" + t.getMessage();
    }
  }

  public static void main(String[] a) throws Exception {
    for (String proto : new String[] {"TLS", "DTLS"}) {
      SSLContext c = SSLContext.getInstance(proto);
      c.init(null, null, null);
      System.out.println(proto + " default " + Arrays.toString(c.getDefaultSSLParameters().getCipherSuites()));
      System.out.println(proto + " supported " + Arrays.toString(c.getSupportedSSLParameters().getCipherSuites()));
      SSLEngine e = c.createSSLEngine();
      System.out.println(proto + " engine " + Arrays.toString(e.getEnabledCipherSuites()));
    }
    String[] tls12 = {"TLSv1.2"};
    SSLContext sup = SSLContext.getInstance("TLS");
    sup.init(null, null, null);
    for (String s : sup.getSupportedSSLParameters().getCipherSuites()) {
      if (s.startsWith("TLS_") && !s.startsWith("TLS_AES") && !s.startsWith("TLS_CHACHA") && !s.contains("SCSV")) {
        System.out.println("tls " + s + " " + socket(s, tls12, new String[] {s}, alias(s)));
        System.out.println("dtls " + s + " " + dtls(s));
      }
    }
    for (String[] protos : new String[][] {{"DTLSv1.0"}, {"DTLSv1.2", "DTLSv1.0"}}) {
      System.out.println("dtls protocols " + Arrays.toString(protos) + " " + dtlsProtocols(protos));
    }
    System.out.println("default rsa 1.2 " + socket(null, tls12, null, "rsa"));
    System.out.println("default dsa 1.2 " + socket(null, tls12, null, "dsa"));
    System.out.println("default ec 1.2 " + socket(null, tls12, null, "ec"));
    System.out.println("default dsa 1.3 " + socket(null, new String[] {"TLSv1.3", "TLSv1.2"}, null, "dsa"));
    System.out.println("dhe vs ec key " + socket(null, tls12, new String[] {"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256"}, "ec"));
    System.out.println("cbc only " + socket(null, tls12, new String[] {"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256"}, "rsa"));
    System.out.println("scsv only " + socket(null, tls12, new String[] {"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"}, "rsa"));
  }
}
