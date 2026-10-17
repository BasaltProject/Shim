/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.net.ssl.*; import java.security.*; import java.security.cert.*; import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.util.*; import java.util.concurrent.*; import java.util.zip.*;
public class HttpsUrlConnectionModern {
  static final String SERVER_P12 = "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" +
      "BAj7wcMWKO5MswICCAA=";
  static final String ROOT_PEM = "-----BEGIN CERTIFICATE-----\nMIIDajCCAlKgAwIBAgIJAMrANhXiiL31MA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNV\nBAYTAkVTMRMwEQYDVQQKEwpTaGltIFRlc3RzMRAwDgYDVQQLEwdUZXN0aW5nMRow\nGAYDVQQDExFTaGltIFRlc3QgUm9vdCBDQTAgFw0xOTEyMzEyMzAwMDBaGA8yMTE5\nMTIwNzIzMDAwMFowUDELMAkGA1UEBhMCRVMxEzARBgNVBAoTClNoaW0gVGVzdHMx\nEDAOBgNVBAsTB1Rlc3RpbmcxGjAYBgNVBAMTEVNoaW0gVGVzdCBSb290IENBMIIB\nIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzDLZ7v4gR+7leAX7MwmINCcT\n/7lkBqdmPG3SJcbIiYYFnja3FJw4jYMEx3ef+Ec5ElZxeN9E2twUQ+8akp8hp79i\nSb4/dp9dXP5i4dq2J0OHeNxrSNAQ+jS7wRLHdxZWb45xUCw66z0jvr6ualSuZ2nv\nf2TpzCMjaeIYVSucs1qgTmDMJbWFgEclBP0Ll+gw6dFpXqniGoS1c++t4DQoqer4\nORMyl25vCoijvLrlh3+x6MhxXvnHoEI8QxjVNvf30D01o6gjW4p3Te5fEHAJ7lg+\nsUB/40f+TtxtmJ9uVnDknFJtvl2JjubnAJQX+Wuw84boiVRvEbpHZCB74SsKXwID\nAQABo0UwQzAdBgNVHQ4EFgQUD3MsE4j9K6q0ay5XGZu0gKpLFPIwDgYDVR0PAQH/\nBAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQEwDQYJKoZIhvcNAQELBQADggEBADAT\nXvaXg1LSq1CItA0nhDnAsHWdf3DSmeXVXuA65UMeetEfAl8dUrqbY2RQH77Rr9KR\n9GLvMeL82WWzgnYaQqZbufIHmi2Du0OBtjMTjdTyXfU1YsQA/4pLL/WDo9cTVHzB\nRgpR9wkqhpMLFM9BV0/beQmJG/tsl+sHTEt3DBUdSB4tMoiR84HVwCvJOOnqoSs0\nWaNToJqDH5PRPICqahXOanuT90k8m0F+Ykw/zoPfZSLfTVvufxxSYFfYU+/KG4WR\nYSOIkGQPadPSzZjpqdjDBQ2kLMxvGr4Jy6uboHnbYr68U3lrfHz3Hck8RErm5oW9\nhmK2UEMID4+eWxIuLTY=\n-----END CERTIFICATE-----\n";
  static SSLContext ctx(boolean keys, boolean trust) throws Exception {
    KeyManager[] kms = null; TrustManager[] tms = null;
    if (keys) { KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(SERVER_P12)), "changeit".toCharArray()); KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); kmf.init(ks, "changeit".toCharArray()); kms = kmf.getKeyManagers(); }
    if (trust) { KeyStore ts = KeyStore.getInstance("PKCS12"); ts.load(null, null); ts.setCertificateEntry("root", CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(ROOT_PEM.getBytes()))); TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX"); tmf.init(ts); tms = tmf.getTrustManagers(); }
    SSLContext c = SSLContext.getInstance("TLS"); c.init(kms, tms, null); return c;
  }
  static final List<String> log = Collections.synchronizedList(new ArrayList<>());
  static volatile int port;
  static String readLine(InputStream in) throws IOException { StringBuilder sb = new StringBuilder(); int c; while ((c = in.read()) != -1) { if (c == '\n') break; if (c != '\r') sb.append((char) c); } return c == -1 && sb.length() == 0 ? null : sb.toString(); }
  static void serve(Socket s) {
    try (s) {
      InputStream in = new BufferedInputStream(s.getInputStream()); OutputStream out = s.getOutputStream();
      while (true) {
        String line = readLine(in); if (line == null || line.isEmpty()) return;
        List<String> hs = new ArrayList<>(); String h; Map<String, String> hm = new HashMap<>();
        while ((h = readLine(in)) != null && !h.isEmpty()) { hs.add(h); int i = h.indexOf(':'); hm.put(h.substring(0, i).toLowerCase(), h.substring(i + 1).trim()); }
        byte[] body = new byte[0];
        if (hm.containsKey("content-length")) { body = in.readNBytes(Integer.parseInt(hm.get("content-length"))); }
        else if ("chunked".equalsIgnoreCase(hm.get("transfer-encoding"))) { ByteArrayOutputStream bo = new ByteArrayOutputStream(); while (true) { int n = Integer.parseInt(readLine(in).trim(), 16); if (n == 0) { readLine(in); break; } bo.write(in.readNBytes(n)); readLine(in); } body = bo.toByteArray(); }
        StringBuilder rec = new StringBuilder("REQ " + line.replaceAll("127\\.0\\.0\\.1:\\d+", "H"));
        for (String x : hs) { String lx = x.toLowerCase(); if (lx.startsWith("user-agent:")) x = "User-Agent: <ua>"; rec.append(" | ").append(x.replaceAll("127\\.0\\.0\\.1:\\d+", "H")); }
        if (body.length > 0) rec.append(" | body=").append(new String(body, StandardCharsets.UTF_8));
        log.add(rec.toString());
        String[] parts = line.split(" "); String method = parts[0], target = parts[1]; String path = target.contains("?") ? target.substring(0, target.indexOf('?')) : target;
        boolean close = "close".equalsIgnoreCase(hm.get("connection")) || parts[2].equals("HTTP/1.0");
        String eol = "\r\n";
        switch (path) {
          case "/plain": reply(out, "200 OK", List.of("Content-Type: text/plain; charset=utf-8", "X-Multi: a", "X-Multi: b", "Set-Cookie: k=v"), "hello".getBytes(), method.equals("HEAD")); break;
          case "/404": reply(out, "404 Not Found", List.of("Content-Type: text/html"), "<h1>nope</h1>".getBytes(), false); break;
          case "/500": reply(out, "500 Internal Server Error", List.of(), "boom".getBytes(), false); break;
          case "/redirect": reply(out, "302 Found", List.of("Location: /plain"), new byte[0], false); break;
          case "/redirect307": reply(out, "307 Temporary Redirect", List.of("Location: /echo"), new byte[0], false); break;
          case "/redirect303": reply(out, "303 See Other", List.of("Location: /echo"), new byte[0], false); break;
          case "/redirect-abs": reply(out, "301 Moved Permanently", List.of("Location: http://127.0.0.1:" + port + "/plain"), new byte[0], false); break;
          case "/redirect-https": reply(out, "302 Found", List.of("Location: https://127.0.0.1:1/x"), new byte[0], false); break;
          case "/loop": reply(out, "302 Found", List.of("Location: /loop"), new byte[0], false); break;
          case "/chunked": { out.write(("HTTP/1.1 200 OK" + eol + "Transfer-Encoding: chunked" + eol + "Content-Type: text/plain" + eol + eol + "5" + eol + "hello" + eol + "6" + eol + " world" + eol + "0" + eol + eol).getBytes()); out.flush(); break; }
          case "/echo": reply(out, "200 OK", List.of("Content-Type: text/plain"), (method + ":" + new String(body)).getBytes(), method.equals("HEAD")); break;
          case "/nocontent": out.write(("HTTP/1.1 204 No Content" + eol + "X-A: 1" + eol + eol).getBytes()); out.flush(); break;
          case "/notmodified": out.write(("HTTP/1.1 304 Not Modified" + eol + "X-A: 1" + eol + eol).getBytes()); out.flush(); break;
          case "/close": { out.write(("HTTP/1.0 200 OK" + eol + "Content-Type: text/plain" + eol + eol + "until-close").getBytes()); out.flush(); return; }
          case "/auth": { String az = hm.get("authorization"); if ("Basic dXNlcjpwYXNz".equals(az)) reply(out, "200 OK", List.of(), "authed".getBytes(), false); else reply(out, "401 Unauthorized", List.of("WWW-Authenticate: Basic realm=\"r\""), "denied".getBytes(), false); break; }
          case "/cookie": reply(out, "200 OK", List.of("Set-Cookie: sid=abc; Path=/"), ("cookie:" + hm.get("cookie")).getBytes(), false); break;
          case "/slow": { try { Thread.sleep(1500); } catch (InterruptedException e) { } reply(out, "200 OK", List.of(), "late".getBytes(), false); break; }
          case "/gzip": { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (GZIPOutputStream g = new GZIPOutputStream(bo)) { g.write("zipped".getBytes()); } reply(out, "200 OK", List.of("Content-Encoding: gzip"), bo.toByteArray(), false); break; }
          case "/big": { byte[] b = new byte[100000]; for (int i = 0; i < b.length; i++) b[i] = (byte) ('a' + i % 26); reply(out, "200 OK", List.of(), b, false); break; }
          case "/badstatus": out.write(("GARBAGE" + eol + eol).getBytes()); out.flush(); return;
          case "/eof": return;
          case "/nolength-keepalive": out.write(("HTTP/1.1 200 OK" + eol + "Content-Type: text/plain" + eol + eol + "abc").getBytes()); out.flush(); return;
          case "/status-only": out.write(("HTTP/1.1 200" + eol + "Content-Length: 2" + eol + eol + "ok").getBytes()); out.flush(); break;
          case "/range": reply(out, "206 Partial Content", List.of("Content-Range: bytes 0-1/10"), "ab".getBytes(), false); break;
          case "/ctype": reply(out, "200 OK", List.of("Content-Type: Application/JSON ; charset=UTF-8", "Content-Length: 2", "Date: Tue, 15 Nov 1994 08:12:31 GMT", "Last-Modified: Tue, 15 Nov 1994 08:12:31 GMT", "Expires: Wed, 16 Nov 1994 08:12:31 GMT"), "{}".getBytes(), false); break;
          default: reply(out, "404 Not Found", List.of(), "?".getBytes(), false);
        }
        if (close) return;
      }
    } catch (IOException e) { }
  }
  static void reply(OutputStream out, String status, List<String> headers, byte[] body, boolean head) throws IOException {
    StringBuilder sb = new StringBuilder("HTTP/1.1 " + status + "\r\n");
    boolean hasLen = false; for (String h : headers) { sb.append(h).append("\r\n"); if (h.toLowerCase().startsWith("content-length")) hasLen = true; }
    if (!hasLen) sb.append("Content-Length: " + body.length + "\r\n");
    sb.append("\r\n"); out.write(sb.toString().getBytes()); if (!head) out.write(body); out.flush();
  }
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) {
    log.clear(); String o;
    try { Object r = b.get(); o = String.valueOf(r); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage(); }
    try { Thread.sleep(30); } catch (InterruptedException e) { }
    System.out.println(n + "=" + o.replaceAll(":" + port, ":P"));
    for (String l : new ArrayList<>(log)) System.out.println("    " + l);
  }
  static String read(InputStream in) throws IOException { return in == null ? "null" : new String(in.readAllBytes(), StandardCharsets.UTF_8); }
  static HttpsURLConnection open(String path) throws IOException { return (HttpsURLConnection) new URL("https://127.0.0.1:" + port + path).openConnection(); }
  public static void main(String[] a) throws Exception {
    ServerSocket ss = ctx(true, false).getServerSocketFactory().createServerSocket(0, 50, InetAddress.getLoopbackAddress()); port = ss.getLocalPort();
    Thread acceptor = new Thread(() -> { try { while (true) { Socket s = ss.accept(); Thread t = new Thread(() -> serve(s)); t.setDaemon(true); t.start(); } } catch (IOException e) { } }); acceptor.setDaemon(true); acceptor.start();
    System.setProperty("http.keepAlive", "false");
    final javax.net.ssl.HostnameVerifier acceptAll = (h, sess) -> true;
    HttpsURLConnection.setDefaultSSLSocketFactory(ctx(false, true).getSocketFactory());
    p("default verifier mismatch", () -> { HttpsURLConnection c = open("/plain"); c.getResponseCode(); return "noex"; });
    p("accept all verifier", () -> { HttpsURLConnection c = open("/plain"); c.setHostnameVerifier(acceptAll); return c.getResponseCode() + " " + read(c.getInputStream()) + " " + c.getCipherSuite().startsWith("TLS_") + " " + c.getServerCertificates().length + " " + ((X509Certificate) c.getServerCertificates()[0]).getSubjectX500Principal() + " " + c.getLocalCertificates() + " " + c.getPeerPrincipal() + " " + c.getLocalPrincipal() + " " + c.getSSLSession().isPresent(); });
    p("reject verifier", () -> { HttpsURLConnection c = open("/plain"); c.setHostnameVerifier((h, sess) -> false); c.getResponseCode(); return "noex"; });
    p("verifier args", () -> { HttpsURLConnection c = open("/plain"); final String[] seen = new String[1]; c.setHostnameVerifier((h, sess) -> { seen[0] = h + " " + sess.getProtocol(); return true; }); c.getResponseCode(); return seen[0]; });
    p("untrusted", () -> { HttpsURLConnection c = open("/plain"); c.setHostnameVerifier(acceptAll); c.setSSLSocketFactory(ctx(false, false).getSocketFactory()); c.getResponseCode(); return "noex"; });
    p("getters before connect", () -> { HttpsURLConnection c = open("/plain"); StringBuilder sb = new StringBuilder(); try { c.getCipherSuite(); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } try { c.getServerCertificates(); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } try { c.getLocalCertificates(); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } return sb + " " + c.getSSLSession().isPresent(); });
    p("setters", () -> { HttpsURLConnection c = open("/plain"); StringBuilder sb = new StringBuilder(); try { c.setHostnameVerifier(null); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } try { c.setSSLSocketFactory(null); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } return sb + " " + (c.getSSLSocketFactory() != null) + " " + (c.getHostnameVerifier() != null); });
    p("https get", () -> { HttpsURLConnection c = open("/plain"); c.setHostnameVerifier(acceptAll); return c.getResponseCode() + " " + c.getResponseMessage() + " " + c.getContentType() + " " + c.getContentLength() + " " + read(c.getInputStream()) + " " + c.getRequestMethod() + " " + c.usingProxy(); });
    p("https headers", () -> { HttpsURLConnection c = open("/plain"); c.setHostnameVerifier(acceptAll); c.getResponseCode(); StringBuilder sb = new StringBuilder(); for (Map.Entry<String, List<String>> e : c.getHeaderFields().entrySet()) sb.append(e.getKey()).append("->").append(e.getValue()).append(" ; "); return sb + " " + c.getHeaderField(0) + " " + c.getHeaderFieldKey(1); });
    p("https 404", () -> { HttpsURLConnection c = open("/404"); c.setHostnameVerifier(acceptAll); int code = c.getResponseCode(); String err = read(c.getErrorStream()); String msg; try { c.getInputStream(); msg = "noex"; } catch (IOException e) { msg = e.getClass().getName() + ":" + e.getMessage(); } return code + " " + err + " " + msg; });
    p("https post", () -> { HttpsURLConnection c = open("/echo"); c.setHostnameVerifier(acceptAll); c.setRequestMethod("POST"); c.setDoOutput(true); c.getOutputStream().write("name=value".getBytes()); return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("https chunked streaming", () -> { HttpsURLConnection c = open("/echo"); c.setHostnameVerifier(acceptAll); c.setRequestMethod("POST"); c.setDoOutput(true); c.setChunkedStreamingMode(4); c.getOutputStream().write("hello world".getBytes()); return read(c.getInputStream()); });
    p("https redirect", () -> { HttpsURLConnection c = open("/redirect"); c.setHostnameVerifier(acceptAll); return c.getResponseCode() + " " + read(c.getInputStream()) + " " + c.getURL(); });
    p("https chunked response", () -> { HttpsURLConnection c = open("/chunked"); c.setHostnameVerifier(acceptAll); return read(c.getInputStream()); });
    p("https big", () -> { HttpsURLConnection c = open("/big"); c.setHostnameVerifier(acceptAll); return c.getInputStream().readAllBytes().length; });
    p("https timeout", () -> { HttpsURLConnection c = open("/slow"); c.setHostnameVerifier(acceptAll); c.setReadTimeout(300); try { c.getResponseCode(); return "noex"; } catch (SocketTimeoutException e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("https auth", () -> { Authenticator.setDefault(new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication("user", "pass".toCharArray()); } }); try { HttpsURLConnection c = open("/auth"); c.setHostnameVerifier(acceptAll); return c.getResponseCode() + " " + read(c.getInputStream()); } finally { Authenticator.setDefault(null); } });
    p("https toString", () -> open("/plain").toString().replaceAll(":\\d+", ":P"));
    p("https url stream", () -> { HttpsURLConnection.setDefaultHostnameVerifier(acceptAll); try { return read(new URL("https://127.0.0.1:" + port + "/plain").openStream()); } finally { HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() { public boolean verify(String h, SSLSession s) { return false; } }); } });
    p("https wrong protocol server", () -> { ServerSocket t = new ServerSocket(0, 5, InetAddress.getLoopbackAddress()); Thread th = new Thread(() -> { try (Socket s = t.accept()) { s.getOutputStream().write("HTTP/1.0 200 OK\r\n\r\nplain".getBytes()); s.getOutputStream().flush(); Thread.sleep(200); } catch (Exception e) { } }); th.start(); HttpsURLConnection c = (HttpsURLConnection) new URL("https://127.0.0.1:" + t.getLocalPort() + "/").openConnection(); c.setHostnameVerifier(acceptAll); try { c.getResponseCode(); return "noex"; } catch (IOException e) { return e.getClass().getName(); } finally { t.close(); } });
    System.exit(0);
  }
}
