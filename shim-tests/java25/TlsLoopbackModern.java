/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.net.ssl.*; import java.io.*; import java.net.*; import java.nio.*; import java.security.*; import java.security.cert.*; import java.util.*; import java.util.concurrent.*;
public class TlsLoopbackModern {
  static final String SERVER_P12 = "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" +
      "BAj7wcMWKO5MswICCAA=";
  static final String ROOT_PEM = "-----BEGIN CERTIFICATE-----\nMIIDajCCAlKgAwIBAgIJAMrANhXiiL31MA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNV\nBAYTAkVTMRMwEQYDVQQKEwpTaGltIFRlc3RzMRAwDgYDVQQLEwdUZXN0aW5nMRow\nGAYDVQQDExFTaGltIFRlc3QgUm9vdCBDQTAgFw0xOTEyMzEyMzAwMDBaGA8yMTE5\nMTIwNzIzMDAwMFowUDELMAkGA1UEBhMCRVMxEzARBgNVBAoTClNoaW0gVGVzdHMx\nEDAOBgNVBAsTB1Rlc3RpbmcxGjAYBgNVBAMTEVNoaW0gVGVzdCBSb290IENBMIIB\nIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzDLZ7v4gR+7leAX7MwmINCcT\n/7lkBqdmPG3SJcbIiYYFnja3FJw4jYMEx3ef+Ec5ElZxeN9E2twUQ+8akp8hp79i\nSb4/dp9dXP5i4dq2J0OHeNxrSNAQ+jS7wRLHdxZWb45xUCw66z0jvr6ualSuZ2nv\nf2TpzCMjaeIYVSucs1qgTmDMJbWFgEclBP0Ll+gw6dFpXqniGoS1c++t4DQoqer4\nORMyl25vCoijvLrlh3+x6MhxXvnHoEI8QxjVNvf30D01o6gjW4p3Te5fEHAJ7lg+\nsUB/40f+TtxtmJ9uVnDknFJtvl2JjubnAJQX+Wuw84boiVRvEbpHZCB74SsKXwID\nAQABo0UwQzAdBgNVHQ4EFgQUD3MsE4j9K6q0ay5XGZu0gKpLFPIwDgYDVR0PAQH/\nBAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQEwDQYJKoZIhvcNAQELBQADggEBADAT\nXvaXg1LSq1CItA0nhDnAsHWdf3DSmeXVXuA65UMeetEfAl8dUrqbY2RQH77Rr9KR\n9GLvMeL82WWzgnYaQqZbufIHmi2Du0OBtjMTjdTyXfU1YsQA/4pLL/WDo9cTVHzB\nRgpR9wkqhpMLFM9BV0/beQmJG/tsl+sHTEt3DBUdSB4tMoiR84HVwCvJOOnqoSs0\nWaNToJqDH5PRPICqahXOanuT90k8m0F+Ykw/zoPfZSLfTVvufxxSYFfYU+/KG4WR\nYSOIkGQPadPSzZjpqdjDBQ2kLMxvGr4Jy6uboHnbYr68U3lrfHz3Hck8RErm5oW9\nhmK2UEMID4+eWxIuLTY=\n-----END CERTIFICATE-----\n";
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { Object r = b.get(); o = String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage() + (t.getCause() != null ? " <- " + t.getCause().getClass().getName() + ":" + t.getCause().getMessage() : ""); } System.out.println(n + "=" + o.replaceAll("@[0-9a-f]+", "@H")); }
  static SSLContext ctx(boolean keys, boolean trust) throws Exception {
    KeyManager[] kms = null; TrustManager[] tms = null;
    if (keys) { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(SERVER_P12)), "changeit".toCharArray()); KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); kmf.init(ks, "changeit".toCharArray()); kms = kmf.getKeyManagers(); }
    if (trust) { KeyStore ts = KeyStore.getInstance("PKCS12"); ts.load(null, null); ts.setCertificateEntry("root", CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(ROOT_PEM.getBytes()))); TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX"); tmf.init(ts); tms = tmf.getTrustManagers(); }
    SSLContext c = SSLContext.getInstance("TLS"); c.init(kms, tms, null); return c;
  }
  static String cn(java.security.cert.Certificate c) { return ((X509Certificate) c).getSubjectX500Principal().getName().replaceAll(",.*", ""); }

  static String engineRun(SSLEngine client, SSLEngine server) throws Exception {
    ByteBuffer cToS = ByteBuffer.allocate(40000), sToC = ByteBuffer.allocate(40000);
    ByteBuffer cApp = ByteBuffer.wrap("ping from client".getBytes()), sApp = ByteBuffer.wrap("pong from server".getBytes());
    ByteBuffer cIn = ByteBuffer.allocate(20000), sIn = ByteBuffer.allocate(20000);
    ByteBuffer empty = ByteBuffer.allocate(0);
    client.beginHandshake(); server.beginHandshake();
    boolean cDone = false, sDone = false; int rounds = 0; boolean sent = false;
    StringBuilder log = new StringBuilder();
    while (rounds++ < 200 && !(cIn.position() > 0 && sIn.position() > 0)) {
      SSLEngineResult r;
      while (true) { Runnable t = client.getDelegatedTask(); if (t == null) break; t.run(); }
      while (true) { Runnable t = server.getDelegatedTask(); if (t == null) break; t.run(); }
      r = client.wrap(client.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ? cApp : empty, cToS);
      cToS.flip(); cToS.compact();
      r = server.wrap(server.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING ? sApp : empty, sToC);
      cToS.flip();
      while (cToS.hasRemaining()) { r = server.unwrap(cToS, sIn); if (r.bytesConsumed() == 0 && r.bytesProduced() == 0) break; }
      cToS.compact();
      sToC.flip();
      while (sToC.hasRemaining()) { r = client.unwrap(sToC, cIn); if (r.bytesConsumed() == 0 && r.bytesProduced() == 0) break; }
      sToC.compact();
    }
    cIn.flip(); sIn.flip();
    return "client got '" + new String(cIn.array(), 0, cIn.limit()) + "' server got '" + new String(sIn.array(), 0, sIn.limit()) + "'";
  }

  static Object[] socketRun(SSLContext sc, SSLContext cc, java.util.function.Consumer<SSLServerSocket> srvCfg, java.util.function.Consumer<SSLSocket> cliCfg, String host) throws Exception {
    SSLServerSocket ss = (SSLServerSocket) sc.getServerSocketFactory().createServerSocket(0, 5, InetAddress.getLoopbackAddress());
    srvCfg.accept(ss);
    int port = ss.getLocalPort();
    final String[] serverResult = new String[1];
    Thread t = new Thread(() -> {
      try (SSLSocket s = (SSLSocket) ss.accept()) {
        BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line = r.readLine();
        SSLSession se = s.getSession();
        String peer; try { peer = cn(se.getPeerCertificates()[0]); } catch (SSLPeerUnverifiedException e) { peer = "none"; }
        s.getOutputStream().write(("echo:" + line + "\n").getBytes()); s.getOutputStream().flush();
        serverResult[0] = "server " + se.getProtocol() + " " + se.getCipherSuite() + " peer=" + peer + " alpn=" + s.getApplicationProtocol();
      } catch (Throwable e) { serverResult[0] = "server !" + e.getClass().getName() + ":" + e.getMessage(); }
    });
    t.start();
    String clientResult;
    try {
      SSLSocket s = (SSLSocket) cc.getSocketFactory().createSocket(new Socket(InetAddress.getLoopbackAddress(), port), host, port, true);
      cliCfg.accept(s);
      final boolean[] listened = new boolean[1];
      s.addHandshakeCompletedListener(e -> { listened[0] = true; });
      s.startHandshake();
      s.getOutputStream().write("hello\n".getBytes()); s.getOutputStream().flush();
      String line = new BufferedReader(new InputStreamReader(s.getInputStream())).readLine();
      SSLSession se = s.getSession();
      clientResult = "client " + se.getProtocol() + " " + se.getCipherSuite() + " peer=" + cn(se.getPeerCertificates()[0]) + " certs=" + se.getPeerCertificates().length + " line=" + line + " alpn=" + s.getApplicationProtocol() + " host=" + se.getPeerHost() + " local=" + (se.getLocalCertificates() != null);
      s.close();
      Thread.sleep(50);
      clientResult += " listener=" + listened[0];
    } catch (Throwable e) { clientResult = "client !" + e.getClass().getName() + ":" + e.getMessage(); }
    t.join(20000);
    ss.close();
    String srv = serverResult[0] == null ? "server none" : serverResult[0].startsWith("server !") ? "server failed" : serverResult[0];
    return new Object[]{clientResult, srv};
  }

  public static void main(String[] a) throws Exception {
    final SSLContext server = ctx(true, true), client = ctx(false, true), clientAuth = ctx(true, true), noTrust = ctx(false, false);
    p("engines default", () -> { SSLEngine c = client.createSSLEngine("shim.example.test", 443), s = server.createSSLEngine(); c.setUseClientMode(true); s.setUseClientMode(false); String r = engineRun(c, s); return r + " | " + c.getSession().getProtocol() + " " + c.getSession().getCipherSuite() + " " + cn(c.getSession().getPeerCertificates()[0]) + " | " + s.getSession().getProtocol() + " " + cn(s.getSession().getLocalCertificates()[0]) + " | " + c.getHandshakeStatus() + s.getHandshakeStatus(); });
    p("engines 1.2", () -> { SSLEngine c = client.createSSLEngine("shim.example.test", 443), s = server.createSSLEngine(); c.setUseClientMode(true); s.setUseClientMode(false); c.setEnabledProtocols(new String[]{"TLSv1.2"}); String r = engineRun(c, s); return r + " | " + c.getSession().getProtocol() + " " + c.getSession().getCipherSuite(); });
    p("engines mtls", () -> { SSLEngine c = clientAuth.createSSLEngine("shim.example.test", 443), s = server.createSSLEngine(); c.setUseClientMode(true); s.setUseClientMode(false); s.setNeedClientAuth(true); String r = engineRun(c, s); return r + " | " + cn(s.getSession().getPeerCertificates()[0]) + " | " + cn(c.getSession().getLocalCertificates()[0]); });
    p("engine hello size", () -> { SSLEngine c = client.createSSLEngine("shim.example.test", 443); c.setUseClientMode(true); ByteBuffer out = ByteBuffer.allocate(100); SSLEngineResult r = c.wrap(ByteBuffer.allocate(0), out); return r.getStatus() + " " + c.getHandshakeStatus() + " " + (r.bytesProduced() > 100) + " " + r.bytesProduced(); });
    p("engine close", () -> { SSLEngine c = client.createSSLEngine("shim.example.test", 443), s = server.createSSLEngine(); c.setUseClientMode(true); s.setUseClientMode(false); engineRun(c, s); c.closeOutbound(); ByteBuffer out = ByteBuffer.allocate(20000); SSLEngineResult r = c.wrap(ByteBuffer.allocate(0), out); out.flip(); SSLEngineResult r2 = s.unwrap(out, ByteBuffer.allocate(100)); return r.getStatus() + " " + c.isOutboundDone() + " " + r2.getStatus() + " " + s.isInboundDone(); });
    p("socket default", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> {}, "shim.example.test")));
    p("socket 1.2 only", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> s.setEnabledProtocols(new String[]{"TLSv1.2"}), "shim.example.test")));
    p("socket suite 128", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> s.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"}), "shim.example.test")));
    p("socket suite 1.2 ecdsa", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { s.setEnabledProtocols(new String[]{"TLSv1.2"}); s.setEnabledCipherSuites(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"}); }, "shim.example.test")));
    p("socket suite 1.2 rsa128", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { s.setEnabledProtocols(new String[]{"TLSv1.2"}); s.setEnabledCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"}); }, "shim.example.test")));
    p("socket hostname ok", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { SSLParameters sp = s.getSSLParameters(); sp.setEndpointIdentificationAlgorithm("HTTPS"); s.setSSLParameters(sp); }, "shim.example.test")));
    p("socket hostname wildcard", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { SSLParameters sp = s.getSSLParameters(); sp.setEndpointIdentificationAlgorithm("HTTPS"); s.setSSLParameters(sp); }, "api.shim.example.test")));
    p("socket hostname bad", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { SSLParameters sp = s.getSSLParameters(); sp.setEndpointIdentificationAlgorithm("HTTPS"); s.setSSLParameters(sp); }, "other.example.org")));
    p("socket hostname ip", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { SSLParameters sp = s.getSSLParameters(); sp.setEndpointIdentificationAlgorithm("HTTPS"); s.setSSLParameters(sp); }, "192.0.2.7")));
    p("socket hostname ip bad", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> { SSLParameters sp = s.getSSLParameters(); sp.setEndpointIdentificationAlgorithm("HTTPS"); s.setSSLParameters(sp); }, "192.0.2.8")));
    p("socket no check other host", () -> Arrays.toString(socketRun(server, client, s -> {}, s -> {}, "other.example.org")));
    p("socket untrusted", () -> Arrays.toString(socketRun(server, noTrust, s -> {}, s -> {}, "shim.example.test")));
    p("socket mtls need ok", () -> Arrays.toString(socketRun(server, clientAuth, s -> s.setNeedClientAuth(true), s -> {}, "shim.example.test")));
    p("socket mtls need missing", () -> Arrays.toString(socketRun(server, client, s -> s.setNeedClientAuth(true), s -> {}, "shim.example.test")));
    p("socket mtls want missing", () -> Arrays.toString(socketRun(server, client, s -> s.setWantClientAuth(true), s -> {}, "shim.example.test")));
    p("socket mtls want present 1.2", () -> Arrays.toString(socketRun(server, clientAuth, s -> s.setWantClientAuth(true), s -> s.setEnabledProtocols(new String[]{"TLSv1.2"}), "shim.example.test")));
    p("socket alpn", () -> Arrays.toString(socketRun(server, client, s -> { SSLParameters sp = s.getSSLParameters(); sp.setApplicationProtocols(new String[]{"h2", "http/1.1"}); s.setSSLParameters(sp); }, s -> { SSLParameters sp = s.getSSLParameters(); sp.setApplicationProtocols(new String[]{"http/1.1", "spdy"}); s.setSSLParameters(sp); }, "shim.example.test")));
    p("socket alpn none", () -> Arrays.toString(socketRun(server, client, s -> { SSLParameters sp = s.getSSLParameters(); sp.setApplicationProtocols(new String[]{"h2"}); s.setSSLParameters(sp); }, s -> { SSLParameters sp = s.getSSLParameters(); sp.setApplicationProtocols(new String[]{"http/1.1"}); s.setSSLParameters(sp); }, "shim.example.test")));
    p("socket protocol mismatch", () -> Arrays.toString(socketRun(server, client, s -> s.setEnabledProtocols(new String[]{"TLSv1.3"}), s -> s.setEnabledProtocols(new String[]{"TLSv1.2"}), "shim.example.test")));
    p("socket suite mismatch", () -> Arrays.toString(socketRun(server, client, s -> s.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"}), s -> s.setEnabledCipherSuites(new String[]{"TLS_AES_256_GCM_SHA384"}), "shim.example.test")));
    p("socket sni matcher bad", () -> Arrays.toString(socketRun(server, client, s -> { SSLParameters sp = s.getSSLParameters(); sp.setSNIMatchers(List.of(SNIHostName.createSNIMatcher("nomatch\\.test"))); s.setSSLParameters(sp); }, s -> {}, "shim.example.test")));
    p("socket sni matcher ok", () -> Arrays.toString(socketRun(server, client, s -> { SSLParameters sp = s.getSSLParameters(); sp.setSNIMatchers(List.of(SNIHostName.createSNIMatcher(".*\\.example\\.test"))); s.setSSLParameters(sp); }, s -> {}, "shim.example.test")));
  }
}
