/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;

public class HttpsH2WssModern {
  static final String SERVER_P12 = "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" +
      "BAj7wcMWKO5MswICCAA=";
  static final String ROOT_PEM = "-----BEGIN CERTIFICATE-----\nMIIDajCCAlKgAwIBAgIJAMrANhXiiL31MA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNV\nBAYTAkVTMRMwEQYDVQQKEwpTaGltIFRlc3RzMRAwDgYDVQQLEwdUZXN0aW5nMRow\nGAYDVQQDExFTaGltIFRlc3QgUm9vdCBDQTAgFw0xOTEyMzEyMzAwMDBaGA8yMTE5\nMTIwNzIzMDAwMFowUDELMAkGA1UEBhMCRVMxEzARBgNVBAoTClNoaW0gVGVzdHMx\nEDAOBgNVBAsTB1Rlc3RpbmcxGjAYBgNVBAMTEVNoaW0gVGVzdCBSb290IENBMIIB\nIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzDLZ7v4gR+7leAX7MwmINCcT\n/7lkBqdmPG3SJcbIiYYFnja3FJw4jYMEx3ef+Ec5ElZxeN9E2twUQ+8akp8hp79i\nSb4/dp9dXP5i4dq2J0OHeNxrSNAQ+jS7wRLHdxZWb45xUCw66z0jvr6ualSuZ2nv\nf2TpzCMjaeIYVSucs1qgTmDMJbWFgEclBP0Ll+gw6dFpXqniGoS1c++t4DQoqer4\nORMyl25vCoijvLrlh3+x6MhxXvnHoEI8QxjVNvf30D01o6gjW4p3Te5fEHAJ7lg+\nsUB/40f+TtxtmJ9uVnDknFJtvl2JjubnAJQX+Wuw84boiVRvEbpHZCB74SsKXwID\nAQABo0UwQzAdBgNVHQ4EFgQUD3MsE4j9K6q0ay5XGZu0gKpLFPIwDgYDVR0PAQH/\nBAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQEwDQYJKoZIhvcNAQELBQADggEBADAT\nXvaXg1LSq1CItA0nhDnAsHWdf3DSmeXVXuA65UMeetEfAl8dUrqbY2RQH77Rr9KR\n9GLvMeL82WWzgnYaQqZbufIHmi2Du0OBtjMTjdTyXfU1YsQA/4pLL/WDo9cTVHzB\nRgpR9wkqhpMLFM9BV0/beQmJG/tsl+sHTEt3DBUdSB4tMoiR84HVwCvJOOnqoSs0\nWaNToJqDH5PRPICqahXOanuT90k8m0F+Ykw/zoPfZSLfTVvufxxSYFfYU+/KG4WR\nYSOIkGQPadPSzZjpqdjDBQ2kLMxvGr4Jy6uboHnbYr68U3lrfHz3Hck8RErm5oW9\nhmK2UEMID4+eWxIuLTY=\n-----END CERTIFICATE-----\n";

  static SSLContext ctx(boolean keys) throws Exception {
    KeyManager[] kms = null;
    if (keys) {
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(SERVER_P12)), "changeit".toCharArray());
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ks, "changeit".toCharArray());
      kms = kmf.getKeyManagers();
    }
    KeyStore ts = KeyStore.getInstance("PKCS12");
    ts.load(null, null);
    ts.setCertificateEntry("root", CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(ROOT_PEM.getBytes())));
    TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
    tmf.init(ts);
    SSLContext c = SSLContext.getInstance("TLS");
    c.init(kms, tmf.getTrustManagers(), null);
    return c;
  }

  static void frame(OutputStream o, int type, int flags, int stream, byte[] payload) throws IOException {
    o.write(new byte[] {(byte) (payload.length >> 16), (byte) (payload.length >> 8), (byte) payload.length, (byte) type, (byte) flags,
        (byte) (stream >> 24), (byte) (stream >> 16), (byte) (stream >> 8), (byte) stream});
    o.write(payload);
    o.flush();
  }

  static void h2Server(SSLServerSocket ss, List<String> log, int requests) {
    try (SSLSocket s = (SSLSocket) ss.accept()) {
      s.startHandshake();
      log.add("server alpn=" + s.getApplicationProtocol());
      DataInputStream in = new DataInputStream(s.getInputStream());
      OutputStream out = s.getOutputStream();
      in.readFully(new byte[24]);
      frame(out, 4, 0, 0, new byte[0]);
      int served = 0;
      ByteArrayOutputStream body = new ByteArrayOutputStream();
      while (served < requests) {
        byte[] h = new byte[9];
        in.readFully(h);
        int len = (h[0] & 0xff) << 16 | (h[1] & 0xff) << 8 | (h[2] & 0xff);
        int type = h[3] & 0xff, flags = h[4] & 0xff;
        int stream = ((h[5] & 0x7f) << 24) | ((h[6] & 0xff) << 16) | ((h[7] & 0xff) << 8) | (h[8] & 0xff);
        byte[] payload = new byte[len];
        in.readFully(payload);
        if (type == 4 && (flags & 1) == 0) {
          frame(out, 4, 1, 0, new byte[0]);
        } else if (type == 6 && (flags & 1) == 0) {
          frame(out, 6, 1, 0, payload);
        } else if (type == 0) {
          body.write(payload);
        }
        if ((type == 0 || type == 1) && (flags & 1) != 0) {
          byte[] reply = ("echo:" + body.toString(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
          body.reset();
          byte[] hdr = {(byte) 0x88, 0x00, 6, 'x', '-', 't', 'e', 's', 't', 2, 'o', 'k'};
          frame(out, 1, 4, stream, hdr);
          frame(out, 0, 1, stream, reply);
          served++;
        }
      }
    } catch (Throwable e) {
      log.add("server !" + e.getClass().getName());
    }
  }

  static byte[] readFrame(InputStream in) throws IOException {
    int b0 = in.read(), b1 = in.read();
    if (b0 < 0) throw new EOFException();
    int op = b0 & 0x0f;
    boolean masked = (b1 & 0x80) != 0;
    long len = b1 & 0x7f;
    if (len == 126) len = in.read() << 8 | in.read();
    byte[] mask = new byte[4];
    if (masked) new DataInputStream(in).readFully(mask);
    byte[] p = new byte[(int) len];
    new DataInputStream(in).readFully(p);
    for (int i = 0; i < p.length; i++) if (masked) p[i] ^= mask[i % 4];
    byte[] r = new byte[p.length + 2];
    r[0] = (byte) op;
    r[1] = (byte) (b0 >> 7);
    System.arraycopy(p, 0, r, 2, p.length);
    return r;
  }

  static void wsFrame(OutputStream o, int op, byte[] p) throws IOException {
    o.write(0x80 | op);
    o.write(p.length);
    o.write(p);
    o.flush();
  }

  static void wsServer(SSLServerSocket ss, List<String> log) {
    try (SSLSocket s = (SSLSocket) ss.accept()) {
      InputStream in = s.getInputStream();
      OutputStream out = s.getOutputStream();
      StringBuilder head = new StringBuilder();
      while (!head.toString().endsWith("\r\n\r\n")) head.append((char) in.read());
      String key = null, proto = null;
      for (String line : head.toString().split("\r\n")) {
        if (line.toLowerCase().startsWith("sec-websocket-key:")) key = line.substring(18).trim();
        if (line.toLowerCase().startsWith("sec-websocket-protocol:")) proto = line.substring(23).trim().split(",")[0].trim();
      }
      log.add("server request " + head.toString().split("\r\n")[0]);
      String accept = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
      out.write(("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: " + accept + "\r\n"
          + (proto != null ? "Sec-WebSocket-Protocol: " + proto + "\r\n" : "") + "\r\n").getBytes());
      out.flush();
      while (true) {
        byte[] f = readFrame(in);
        byte[] p = Arrays.copyOfRange(f, 2, f.length);
        if (f[0] == 1) {
          wsFrame(out, 1, ("echo " + new String(p, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
        } else if (f[0] == 2) {
          wsFrame(out, 2, p);
        } else if (f[0] == 9) {
          wsFrame(out, 10, p);
        } else if (f[0] == 8) {
          wsFrame(out, 8, p);
          break;
        }
      }
    } catch (Throwable e) {
      log.add("server !" + e.getClass().getName());
    }
  }

  public static void main(String[] args) throws Exception {
    System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
    SSLContext server = ctx(true), client = ctx(false);
    List<String> log = new CopyOnWriteArrayList<String>();
    SSLServerSocket ss = (SSLServerSocket) server.getServerSocketFactory().createServerSocket(0, 5, InetAddress.getLoopbackAddress());
    SSLParameters sp = ss.getSSLParameters();
    sp.setApplicationProtocols(new String[] {"h2"});
    ss.setSSLParameters(sp);
    Thread t = new Thread(() -> h2Server(ss, log, 2));
    t.start();
    HttpClient hc = HttpClient.newBuilder().sslContext(client).version(HttpClient.Version.HTTP_2).build();
    String base = "https://localhost:" + ss.getLocalPort();
    HttpResponse<String> r = hc.send(HttpRequest.newBuilder(URI.create(base + "/a")).GET().build(), HttpResponse.BodyHandlers.ofString());
    System.out.println("get " + r.version() + " " + r.statusCode() + " " + r.body() + " " + r.headers().firstValue("x-test").orElse("none"));
    r = hc.send(HttpRequest.newBuilder(URI.create(base + "/b")).POST(HttpRequest.BodyPublishers.ofString("payload")).build(), HttpResponse.BodyHandlers.ofString());
    System.out.println("post " + r.version() + " " + r.statusCode() + " " + r.body() + " " + r.sslSession().isPresent());
    t.join(10000);
    ss.close();
    log.stream().sorted().forEach(System.out::println);

    log.clear();
    SSLServerSocket ws = (SSLServerSocket) server.getServerSocketFactory().createServerSocket(0, 5, InetAddress.getLoopbackAddress());
    Thread wt = new Thread(() -> wsServer(ws, log));
    wt.start();
    List<String> events = new CopyOnWriteArrayList<String>();
    CountDownLatch closed = new CountDownLatch(1);
    WebSocket.Listener listener = new WebSocket.Listener() {
      StringBuilder text = new StringBuilder();
      public void onOpen(WebSocket w) { events.add("open"); w.request(1); }
      public CompletionStage<?> onText(WebSocket w, CharSequence d, boolean last) { text.append(d); if (last) { events.add("text " + text); text.setLength(0); } w.request(1); return null; }
      public CompletionStage<?> onBinary(WebSocket w, ByteBuffer d, boolean last) { events.add("binary " + d.remaining()); w.request(1); return null; }
      public CompletionStage<?> onPong(WebSocket w, ByteBuffer d) { events.add("pong " + d.remaining()); w.request(1); return null; }
      public CompletionStage<?> onClose(WebSocket w, int code, String reason) { events.add("close " + code + " " + reason); closed.countDown(); return null; }
      public void onError(WebSocket w, Throwable e) { events.add("error " + e.getClass().getSimpleName()); closed.countDown(); }
    };
    WebSocket w = HttpClient.newBuilder().sslContext(client).build().newWebSocketBuilder().subprotocols("chat")
        .buildAsync(URI.create("wss://localhost:" + ws.getLocalPort() + "/sock"), listener).get(20, TimeUnit.SECONDS);
    System.out.println("subprotocol " + w.getSubprotocol());
    w.sendText("hello", true).get(10, TimeUnit.SECONDS);
    Thread.sleep(300);
    w.sendBinary(ByteBuffer.wrap(new byte[] {1, 2, 3}), true).get(10, TimeUnit.SECONDS);
    Thread.sleep(300);
    w.sendPing(ByteBuffer.wrap(new byte[] {9})).get(10, TimeUnit.SECONDS);
    Thread.sleep(300);
    w.sendClose(WebSocket.NORMAL_CLOSURE, "bye").get(10, TimeUnit.SECONDS);
    closed.await(10, TimeUnit.SECONDS);
    wt.join(10000);
    ws.close();
    events.forEach(e -> System.out.println("event " + e));
    log.stream().sorted().forEach(System.out::println);
  }
}
