/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.util.*; import java.util.concurrent.*; import java.util.zip.*;
public class HttpUrlConnectionModern {
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
  static HttpURLConnection open(String path) throws IOException { return (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection(); }
  public static void main(String[] a) throws Exception {
    ServerSocket ss = new ServerSocket(0, 50, InetAddress.getLoopbackAddress()); port = ss.getLocalPort();
    Thread acceptor = new Thread(() -> { try { while (true) { Socket s = ss.accept(); Thread t = new Thread(() -> serve(s)); t.setDaemon(true); t.start(); } } catch (IOException e) { } }); acceptor.setDaemon(true); acceptor.start();
    System.setProperty("http.keepAlive", "false");
    p("get", () -> { HttpURLConnection c = open("/plain"); return c.getResponseCode() + " " + c.getResponseMessage() + " " + c.getContentType() + " " + c.getContentLength() + " " + read(c.getInputStream()) + " " + c.getRequestMethod() + " " + c.usingProxy(); });
    p("headers", () -> { HttpURLConnection c = open("/plain"); c.getResponseCode(); StringBuilder sb = new StringBuilder(); for (int i = 0; i < 8; i++) sb.append(i).append(':').append(c.getHeaderFieldKey(i)).append('=').append(c.getHeaderField(i)).append(';'); Map<String, List<String>> m = c.getHeaderFields(); List<String> keys = new ArrayList<>(); for (String k : m.keySet()) keys.add(String.valueOf(k)); Collections.sort(keys); StringBuilder mb = new StringBuilder(); for (String k : keys) mb.append(k).append('=').append(m.get(k.equals("null") ? null : k)).append(','); String mut; try { m.put("x", List.of()); mut = "mutable"; } catch (UnsupportedOperationException e) { mut = "immutable"; } return sb + " " + mb + " " + mut + " " + c.getHeaderField("x-multi") + " " + c.getHeaderField("X-MULTI") + " " + m.get("x-multi") + " " + m.get("X-Multi") + " " + m.keySet().iterator().next() + " " + c.getHeaderField(100) + " " + c.getHeaderFieldKey(-1); });
    p("headers order", () -> { HttpURLConnection c = open("/plain"); c.getResponseCode(); StringBuilder sb = new StringBuilder(); for (Map.Entry<String, List<String>> e : c.getHeaderFields().entrySet()) sb.append(e.getKey()).append("->").append(e.getValue()).append(" ; "); return sb + " " + c.getHeaderFields().getClass().getName(); });
    p("req dup props", () -> { HttpURLConnection c = open("/plain"); c.setRequestProperty("X", "1"); c.addRequestProperty("X", "2"); c.addRequestProperty("x", "3"); c.setRequestProperty("Y", "a"); c.setRequestProperty("y", "b"); String r = c.getRequestProperties() + " " + c.getRequestProperty("X") + " " + c.getRequestProperty("Y"); c.getResponseCode(); return r; });
    p("req props immutable", () -> { HttpURLConnection c = open("/plain"); c.setRequestProperty("X", "1"); Map<String, List<String>> m = c.getRequestProperties(); try { m.put("a", List.of()); return "mutable"; } catch (UnsupportedOperationException e) { try { m.get("X").add("z"); return "inner mutable"; } catch (UnsupportedOperationException e2) { return "immutable"; } } });
    p("connected props", () -> { HttpURLConnection c = open("/plain"); c.connect(); try { c.getRequestProperties(); return "ok"; } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("404", () -> { HttpURLConnection c = open("/404"); int code = c.getResponseCode(); String err = read(c.getErrorStream()); String msg; try { c.getInputStream(); msg = "noex"; } catch (IOException e) { msg = e.getClass().getName() + ":" + e.getMessage(); } return code + " " + err + " " + msg; });
    p("500", () -> { HttpURLConnection c = open("/500"); try { c.getInputStream(); return "noex"; } catch (IOException e) { return e.getClass().getName() + ":" + e.getMessage() + " err=" + read(c.getErrorStream()) + " " + c.getResponseCode(); } });
    p("redirect", () -> { HttpURLConnection c = open("/redirect"); return c.getResponseCode() + " " + read(c.getInputStream()) + " " + c.getURL(); });
    p("redirect no follow", () -> { HttpURLConnection c = open("/redirect"); c.setInstanceFollowRedirects(false); return c.getResponseCode() + " " + c.getHeaderField("Location") + " " + c.getInstanceFollowRedirects(); });
    p("redirect abs", () -> { HttpURLConnection c = open("/redirect-abs"); return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("redirect https", () -> { HttpURLConnection c = open("/redirect-https"); return c.getResponseCode() + " " + c.getHeaderField("Location"); });
    p("redirect loop", () -> { HttpURLConnection c = open("/loop"); return c.getResponseCode(); });
    p("redirect 307 post", () -> { HttpURLConnection c = open("/redirect307"); c.setRequestMethod("POST"); c.setDoOutput(true); c.getOutputStream().write("data".getBytes()); return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("redirect 303 post", () -> { HttpURLConnection c = open("/redirect303"); c.setRequestMethod("POST"); c.setDoOutput(true); c.getOutputStream().write("data".getBytes()); return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("chunked", () -> { HttpURLConnection c = open("/chunked"); return c.getResponseCode() + " " + c.getContentLength() + " " + c.getHeaderField("Transfer-Encoding") + " " + read(c.getInputStream()); });
    p("post", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.getOutputStream().write("name=value".getBytes()); return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("post content-type", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json"); c.setRequestProperty("X-Custom", "1"); c.addRequestProperty("X-Custom", "2"); try (OutputStream o = c.getOutputStream()) { o.write("{}".getBytes()); } return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("put", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("PUT"); c.setDoOutput(true); c.getOutputStream().write("x".getBytes()); return read(c.getInputStream()); });
    p("delete", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("DELETE"); return read(c.getInputStream()); });
    p("head", () -> { HttpURLConnection c = open("/plain"); c.setRequestMethod("HEAD"); return c.getResponseCode() + " " + c.getContentLength() + " " + read(c.getInputStream()).length(); });
    p("options", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("OPTIONS"); return read(c.getInputStream()); });
    p("patch", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("PATCH"); return "x"; });
    p("bad method", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("FOO"); return "x"; });
    p("method lower", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("get"); return "x"; });
    p("fixed length", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.setFixedLengthStreamingMode(5); c.getOutputStream().write("hello".getBytes()); return read(c.getInputStream()); });
    p("fixed length short", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.setFixedLengthStreamingMode(5); OutputStream o = c.getOutputStream(); o.write("hi".getBytes()); try { o.close(); return "noex"; } catch (IOException e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("fixed length long", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.setFixedLengthStreamingMode(2); OutputStream o = c.getOutputStream(); try { o.write("toolong".getBytes()); return "noex"; } catch (IOException e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("chunked streaming", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.setChunkedStreamingMode(4); c.getOutputStream().write("hello world".getBytes()); return read(c.getInputStream()); });
    p("chunked streaming default", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); c.setDoOutput(true); c.setChunkedStreamingMode(0); c.getOutputStream().write("abc".getBytes()); return read(c.getInputStream()); });
    p("streaming errors", () -> { HttpURLConnection c = open("/echo"); try { c.setFixedLengthStreamingMode(-1); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "noex"; });
    p("streaming after connect", () -> { HttpURLConnection c = open("/plain"); c.connect(); try { c.setChunkedStreamingMode(1); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "noex"; });
    p("streaming both", () -> { HttpURLConnection c = open("/plain"); c.setChunkedStreamingMode(1); try { c.setFixedLengthStreamingMode(1); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "noex"; });
    p("nocontent", () -> { HttpURLConnection c = open("/nocontent"); return c.getResponseCode() + " " + c.getResponseMessage() + " " + c.getContentLength() + " " + read(c.getInputStream()).length(); });
    p("notmodified", () -> { HttpURLConnection c = open("/notmodified"); return c.getResponseCode() + " " + read(c.getInputStream()).length(); });
    p("until close", () -> { HttpURLConnection c = open("/close"); return c.getResponseCode() + " " + c.getContentLength() + " " + read(c.getInputStream()); });
    p("auth none", () -> { HttpURLConnection c = open("/auth"); return c.getResponseCode() + " " + read(c.getErrorStream()); });
    p("auth authenticator", () -> { Authenticator.setDefault(new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new PasswordAuthentication("user", "pass".toCharArray()); } }); try { HttpURLConnection c = open("/auth"); return c.getResponseCode() + " " + read(c.getInputStream()); } finally { Authenticator.setDefault(null); } });
    p("auth header", () -> { HttpURLConnection c = open("/auth"); c.setRequestProperty("Authorization", "Basic dXNlcjpwYXNz"); return c.getResponseCode() + " " + read(c.getInputStream()); });
    p("cookies", () -> { CookieHandler old = CookieHandler.getDefault(); CookieHandler.setDefault(new CookieManager()); try { HttpURLConnection c = open("/cookie"); String first = read(c.getInputStream()); HttpURLConnection d = open("/cookie"); return first + " | " + read(d.getInputStream()); } finally { CookieHandler.setDefault(old); } });
    p("timeout", () -> { HttpURLConnection c = open("/slow"); c.setReadTimeout(300); try { c.getResponseCode(); return "noex"; } catch (SocketTimeoutException e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("gzip", () -> { HttpURLConnection c = open("/gzip"); return c.getContentEncoding() + " " + c.getContentLength() + " " + new GZIPInputStream(c.getInputStream()).readAllBytes().length; });
    p("big", () -> { HttpURLConnection c = open("/big"); byte[] b = c.getInputStream().readAllBytes(); return b.length + " " + (char) b[27] + " " + c.getContentLengthLong(); });
    p("bad status", () -> { HttpURLConnection c = open("/badstatus"); return c.getResponseCode(); });
    p("eof", () -> { HttpURLConnection c = open("/eof"); return c.getResponseCode(); });
    p("status only", () -> { HttpURLConnection c = open("/status-only"); return c.getResponseCode() + " [" + c.getResponseMessage() + "] " + read(c.getInputStream()); });
    p("range", () -> { HttpURLConnection c = open("/range"); c.setRequestProperty("Range", "bytes=0-1"); return c.getResponseCode() + " " + c.getHeaderField("content-range") + " " + read(c.getInputStream()); });
    p("ctype", () -> { HttpURLConnection c = open("/ctype"); c.getResponseCode(); return c.getContentType() + " " + c.getContentLength() + " " + c.getDate() + " " + c.getLastModified() + " " + c.getExpiration() + " " + c.getHeaderFieldDate("last-modified", 5) + " " + c.getHeaderFieldInt("content-length", 7) + " " + c.getHeaderFieldInt("x", 7) + " " + c.getHeaderFieldLong("content-length", 9) + " " + c.getContentEncoding(); });
    p("req props", () -> { HttpURLConnection c = open("/plain"); c.setRequestProperty("A", "1"); c.addRequestProperty("a", "2"); c.setRequestProperty("B", "x"); return c.getRequestProperty("a") + " " + c.getRequestProperties() + " " + c.getRequestProperty("zz"); });
    p("req props after connect", () -> { HttpURLConnection c = open("/plain"); c.connect(); try { c.setRequestProperty("A", "1"); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "noex"; });
    p("req props null key", () -> { HttpURLConnection c = open("/plain"); try { c.setRequestProperty(null, "1"); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "noex"; });
    p("req props get null", () -> { HttpURLConnection c = open("/plain"); try { c.getRequestProperty(null); return "null-ok"; } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("req restricted", () -> { HttpURLConnection c = open("/echo"); c.setRequestProperty("Host", "evil"); c.setRequestProperty("Connection", "close"); c.setRequestProperty("Content-Length", "99"); c.setRequestProperty("Upgrade", "x"); return read(c.getInputStream()); });
    p("accept etc", () -> { HttpURLConnection c = open("/plain"); c.setRequestProperty("Accept", "text/xml"); c.setRequestProperty("User-Agent", "Mine/1"); c.setRequestProperty("Accept-Encoding", "gzip"); return read(c.getInputStream()); });
    p("if-modified", () -> { HttpURLConnection c = open("/plain"); c.setIfModifiedSince(784111777000L); return read(c.getInputStream()); });
    p("dooutput get", () -> { HttpURLConnection c = open("/echo"); c.setDoOutput(true); c.getOutputStream().write("x".getBytes()); return c.getRequestMethod() + " " + read(c.getInputStream()); });
    p("input after output", () -> { HttpURLConnection c = open("/echo"); c.setRequestMethod("POST"); try { c.getOutputStream(); return "noex"; } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("setters after connect", () -> { HttpURLConnection c = open("/plain"); c.connect(); StringBuilder sb = new StringBuilder(); try { c.setRequestMethod("POST"); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } try { c.setDoOutput(true); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } try { c.setUseCaches(false); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } try { c.setIfModifiedSince(1); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + ";"); } return sb; });
    p("toString", () -> open("/plain").toString().replaceAll(":\\d+", ":P"));
    p("disconnect", () -> { HttpURLConnection c = open("/plain"); c.getResponseCode(); c.disconnect(); try { return read(c.getInputStream()); } catch (IOException e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("url stream", () -> read(new URL("http://127.0.0.1:" + port + "/plain").openStream()));
    p("url content", () -> new URL("http://127.0.0.1:" + port + "/plain").getContent().getClass().getName());
    p("connect refused", () -> { ServerSocket t = new ServerSocket(0); int cp = t.getLocalPort(); t.close(); HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + cp + "/").openConnection(); try { c.getResponseCode(); return "noex"; } catch (IOException e) { return e.getClass().getName(); } });
    p("unknown host", () -> { HttpURLConnection c = (HttpURLConnection) new URL("http://no-such-host.invalid/").openConnection(); try { c.getResponseCode(); return "noex"; } catch (IOException e) { return e.getClass().getName() + ":" + e.getMessage(); } });
    p("connect timeout", () -> { HttpURLConnection c = open("/plain"); c.setConnectTimeout(1000); c.setReadTimeout(1000); return c.getConnectTimeout() + " " + c.getReadTimeout() + " " + c.getResponseCode(); });
    p("timeouts neg", () -> { HttpURLConnection c = open("/plain"); try { c.setConnectTimeout(-1); } catch (Exception e) { return e.getClass().getName() + ":" + e.getMessage(); } return "noex"; });
    p("query", () -> { HttpURLConnection c = open("/echo?a=b&c=d%20e"); return read(c.getInputStream()); });
    p("user info", () -> { HttpURLConnection c = (HttpURLConnection) new URL("http://user:pass@127.0.0.1:" + port + "/auth").openConnection(); return c.getResponseCode(); });
    p("proxy direct", () -> { HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/plain").openConnection(Proxy.NO_PROXY); return c.usingProxy() + " " + c.getResponseCode(); });
    p("keepalive reuse", () -> { System.setProperty("http.keepAlive", "true"); try { String r = ""; for (int i = 0; i < 3; i++) { HttpURLConnection c = open("/plain"); r += read(c.getInputStream()) + ","; } return r; } finally { System.setProperty("http.keepAlive", "false"); } });
    System.exit(0);
  }
}
