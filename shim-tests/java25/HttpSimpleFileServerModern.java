/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class HttpSimpleFileServerModern {
  static String root;

  static void t(String name, Callable<Object> c) {
    try {
      System.out.println(name + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName()
          + (e instanceof NullPointerException ? "" : ": " + String.valueOf(e.getMessage()).replace(root, "<root>")));
    }
  }

  static String raw(int port, String request) throws Exception {
    try (Socket s = new Socket(InetAddress.getLoopbackAddress(), port)) {
      s.setSoTimeout(2000);
      s.getOutputStream().write(request.getBytes(StandardCharsets.ISO_8859_1));
      s.getOutputStream().flush();
      ByteArrayOutputStream b = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      try {
        int n;
        while ((n = s.getInputStream().read(buf)) > 0) {
          b.write(buf, 0, n);
        }
      } catch (SocketTimeoutException e) {
        b.write("<timeout>".getBytes());
      }
      return b.toString("ISO-8859-1").replaceAll("Date: [^\r]*", "Date: <date>");
    }
  }

  static String get(int port, String method, String path, String... headers) throws Exception {
    StringBuilder sb = new StringBuilder(method + " " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n");
    for (String h : headers) {
      sb.append(h).append("\r\n");
    }
    return show(raw(port, sb.append("\r\n").toString()));
  }

  static String show(String response) {
    return response.replace("\r", "\\r").replace("\n", "\\n");
  }

  static String sortedListing(String response) {
    List<String> lines = new ArrayList<>(Arrays.asList(response.split("\n", -1)));
    List<String> items = new ArrayList<>();
    for (String l : lines) {
      if (l.startsWith("<li>")) {
        items.add(l);
      }
    }
    Collections.sort(items);
    lines.removeIf(l -> l.startsWith("<li>"));
    int at = lines.indexOf("<ul>");
    lines.addAll(at + 1, items);
    return show(String.join("\n", lines)).replaceAll("Last-modified: [^\\\\]*", "Last-modified: <lm>");
  }

  public static void main(String[] args) throws Exception {
    Path base = Files.createTempDirectory("sfroot");
    root = base.toString();
    FileTime fixed = FileTime.fromMillis(1700000000000L);
    Files.writeString(base.resolve("hello.txt"), "hello world\n");
    Files.writeString(base.resolve("page.html"), "<html>x</html>");
    Files.writeString(base.resolve("data.json"), "{}");
    Files.write(base.resolve("img.png"), new byte[] {1, 2, 3});
    Files.write(base.resolve("noext"), new byte[] {1});
    Files.write(base.resolve("a.unknownext"), new byte[] {1, 2});
    Files.write(base.resolve("zero.txt"), new byte[0]);
    Files.writeString(base.resolve(".hidden"), "h");
    Files.writeString(base.resolve("with space.txt"), "s");
    Files.writeString(base.resolve("\u00e9.txt"), "u");
    Files.writeString(base.resolve("a&b<c>.txt"), "x");
    Files.createDirectory(base.resolve("sub"));
    Files.writeString(base.resolve("sub/inner.txt"), "inner");
    Files.createDirectory(base.resolve("withindex"));
    Files.writeString(base.resolve("withindex/index.html"), "<b>index</b>");
    Files.createDirectory(base.resolve("htm"));
    Files.writeString(base.resolve("htm/index.htm"), "htm");
    Files.createDirectory(base.resolve("both"));
    Files.writeString(base.resolve("both/index.htm"), "htm");
    Files.writeString(base.resolve("both/index.html"), "html");
    Files.createDirectory(base.resolve("empty"));
    Files.createDirectory(base.resolve(".hdir"));
    Files.writeString(base.resolve(".hdir/f.txt"), "f");
    Files.createDirectory(base.resolve("idxdir"));
    Files.createDirectory(base.resolve("idxdir/index.html"));
    for (String name : new String[] {"hello.txt", "page.html", "data.json", "img.png", "noext", "a.unknownext",
        "zero.txt", "with space.txt", "\u00e9.txt", "a&b<c>.txt", "sub/inner.txt", "withindex/index.html",
        "htm/index.htm", "both/index.htm", "both/index.html"}) {
      Files.setLastModifiedTime(base.resolve(name), fixed);
    }
    Files.createSymbolicLink(base.resolve("link.txt"), base.resolve("hello.txt"));

    HttpServer server = SimpleFileServer.createFileServer(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0),
        base, SimpleFileServer.OutputLevel.NONE);
    server.start();
    int port = server.getAddress().getPort();
    String[][] cases = {{"GET", "/hello.txt"}, {"HEAD", "/hello.txt"}, {"GET", "/page.html"}, {"GET", "/data.json"},
        {"GET", "/img.png"}, {"GET", "/noext"}, {"GET", "/a.unknownext"}, {"GET", "/zero.txt"}, {"GET", "/sub"},
        {"GET", "/withindex/"}, {"GET", "/withindex"}, {"GET", "/htm/"}, {"GET", "/both/"}, {"GET", "/.hidden"},
        {"GET", "/.hdir/"}, {"GET", "/.hdir/f.txt"}, {"GET", "/nonexistent"}, {"GET", "/nonexistent/"},
        {"GET", "/with%20space.txt"}, {"GET", "/%C3%A9.txt"}, {"GET", "/../etc/passwd"},
        {"GET", "/%2e%2e/etc/passwd"}, {"GET", "/sub/../hello.txt"}, {"POST", "/hello.txt"}, {"PUT", "/hello.txt"},
        {"DELETE", "/hello.txt"}, {"OPTIONS", "/hello.txt"}, {"GET", "/hello.txt?x=1"}, {"GET", "/hello.txt/"},
        {"GET", "/sub//inner.txt"}, {"GET", "/hello.txt#f"}, {"GET", "/link.txt"}, {"GET", "/he%6clo.txt"},
        {"GET", "/%2Fhello.txt"}, {"GET", "//hello.txt"}, {"GET", "/a%ZZ"}, {"GET", "/idxdir/"},
        {"GET", "/hello.txt%00"}, {"GET", "/sub/x?y#z"}};
    for (String[] c : cases) {
      t(c[0] + " " + c[1], () -> get(port, c[0], c[1]).replaceAll("Last-modified: [A-Za-z]{3}, [^\\\\]*GMT",
          "Last-modified: <lm>"));
    }
    t("hello-last-modified", () -> get(port, "GET", "/hello.txt").contains(
        "Last-modified: Tue, 14 Nov 2023 22:13:20 GMT"));
    t("if-modified-since", () -> get(port, "GET", "/hello.txt", "If-Modified-Since: Sun, 14 Nov 2027 22:13:20 GMT")
        .startsWith("HTTP/1.1 200 OK"));
    t("range-ignored", () -> get(port, "GET", "/hello.txt", "Range: bytes=0-3").endsWith("hello world\\n"));
    t("listing-root", () -> sortedListing(raw(port, "GET / HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n")));
    t("listing-empty", () -> sortedListing(raw(port, "GET /empty/ HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n")));
    t("listing-sub", () -> sortedListing(raw(port, "GET /sub/ HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n")));
    t("head-listing", () -> get(port, "HEAD", "/sub/").replaceAll("Last-modified: [^\\\\]*", "Last-modified: <lm>"));
    server.stop(0);

    for (String ext : new String[] {"css", "js", "xml", "svg", "gif", "jpg", "jpeg", "pdf", "zip", "mp4", "mp3",
        "ico", "csv", "md", "htm", "woff2", "wasm", "txt", "java", "yaml", "bin", "class", "jar", "webp", "avif",
        "mjs", "tar", "gz", "json", "png", "html", "wav", "ogg", "webm", "ttf", "otf", "woff", "rtf", "doc", "xls",
        "ppt", "tif", "tiff", "bmp", "JSON", "Html", "Txt"}) {
      Files.write(base.resolve("f." + ext), new byte[] {65});
    }
    HttpServer types = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    types.createContext("/", SimpleFileServer.createFileHandler(base));
    types.start();
    int tport = types.getAddress().getPort();
    t("content-types", () -> {
      StringBuilder sb = new StringBuilder();
      for (String ext : new String[] {"css", "js", "xml", "svg", "gif", "jpg", "jpeg", "pdf", "zip", "mp4", "mp3",
          "ico", "csv", "md", "htm", "woff2", "wasm", "txt", "java", "yaml", "bin", "class", "jar", "webp", "avif",
          "mjs", "tar", "gz", "json", "png", "html", "wav", "ogg", "webm", "ttf", "otf", "woff", "rtf", "doc", "xls",
          "ppt", "tif", "tiff", "bmp", "JSON", "Html", "Txt"}) {
        String r = raw(tport, "GET /f." + ext + " HTTP/1.1\r\nHost: x\r\nConnection: close\r\n\r\n");
        for (String line : r.split("\r\n")) {
          if (line.startsWith("Content-type")) {
            sb.append(ext).append(" -> ").append(line).append("; ");
          }
        }
      }
      return sb;
    });
    types.stop(0);

    t("rel", () -> SimpleFileServer.createFileServer(new InetSocketAddress(0), Path.of("relative"),
        SimpleFileServer.OutputLevel.NONE));
    t("missing", () -> SimpleFileServer.createFileServer(new InetSocketAddress(0), base.resolve("nope"),
        SimpleFileServer.OutputLevel.NONE));
    t("notdir", () -> SimpleFileServer.createFileServer(new InetSocketAddress(0), base.resolve("hello.txt"),
        SimpleFileServer.OutputLevel.NONE));
    t("null-addr", () -> SimpleFileServer.createFileServer(null, base, SimpleFileServer.OutputLevel.NONE));
    t("null-root", () -> SimpleFileServer.createFileServer(new InetSocketAddress(0), null,
        SimpleFileServer.OutputLevel.NONE));
    t("null-level", () -> SimpleFileServer.createFileServer(new InetSocketAddress(0), base, null));
    t("handler-null", () -> SimpleFileServer.createFileHandler(null));
    t("handler-dotdir", () -> SimpleFileServer.createFileHandler(Path.of("sub-nonexistent/..")));
    t("filter-none", () -> SimpleFileServer.createOutputFilter(new ByteArrayOutputStream(),
        SimpleFileServer.OutputLevel.NONE));
    t("filter-info", () -> SimpleFileServer.createOutputFilter(new ByteArrayOutputStream(),
        SimpleFileServer.OutputLevel.INFO).description());
    t("filter-null", () -> SimpleFileServer.createOutputFilter(null, SimpleFileServer.OutputLevel.INFO));
    t("levels", () -> Arrays.toString(SimpleFileServer.OutputLevel.values()));

    for (SimpleFileServer.OutputLevel level : new SimpleFileServer.OutputLevel[] {SimpleFileServer.OutputLevel.INFO,
        SimpleFileServer.OutputLevel.VERBOSE}) {
      t("log-" + level, () -> {
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        HttpServer s = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        HttpContext c = s.createContext("/", SimpleFileServer.createFileHandler(base));
        c.getFilters().add(SimpleFileServer.createOutputFilter(log, level));
        s.start();
        int p = s.getAddress().getPort();
        get(p, "GET", "/hello.txt", "X-Req: 1");
        get(p, "POST", "/hello.txt");
        get(p, "GET", "/missing");
        get(p, "HEAD", "/hello.txt");
        Thread.sleep(300);
        s.stop(0);
        return "\n" + log.toString().replaceAll("\\[[^\\]]*\\] \"", "[<ts>] \"")
            .replaceAll("Date: [^\n]*", "Date: <date>").replaceAll("Last-modified: [^\n]*", "Last-modified: <lm>")
            .replace(root, "<root>");
      });
    }
    System.exit(0);
  }
}
