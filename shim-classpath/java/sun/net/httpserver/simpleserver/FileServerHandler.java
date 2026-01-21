/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver.simpleserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FileServerHandler implements HttpHandler {
  private static final DateTimeFormatter kHttpDate =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
  private static final String kNotResolved = "could not resolve request URI path";

  private final Path root;

  public FileServerHandler(Path root) {
    this.root = root.normalize();
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try (InputStream in = exchange.getRequestBody()) {
      in.readAllBytes();
    }
    String method = exchange.getRequestMethod();
    URI uri = exchange.getRequestURI();
    Path path = mapToPath(exchange, uri.getPath());
    exchange.setAttribute("request-path", path == null ? kNotResolved : path.toString());
    if (!method.equals("GET") && !method.equals("HEAD")) {
      exchange.getResponseHeaders().set("Allow", "HEAD, GET");
      exchange.sendResponseHeaders(405, -1);
      exchange.close();
      return;
    }
    if (path == null) {
      notFound(exchange, uri.getPath());
      return;
    }
    if (Files.isDirectory(path)) {
      handleDirectory(exchange, uri, path);
    } else if (Files.isRegularFile(path)) {
      if (uri.getPath().endsWith("/")) {
        notFound(exchange, uri.getPath());
        return;
      }
      serveFile(exchange, path, false);
    } else {
      notFound(exchange, uri.getPath());
    }
  }

  private Path mapToPath(HttpExchange exchange, String requestPath) {
    if (requestPath == null || requestPath.isEmpty()) {
      return null;
    }
    String trimmed = requestPath;
    while (trimmed.startsWith("/")) {
      trimmed = trimmed.substring(1);
    }
    if (trimmed.indexOf('\u0000') >= 0) {
      return null;
    }
    for (String segment : trimmed.split("/")) {
      if (segment.equals("..") || segment.equals(".")) {
        return null;
      }
    }
    Path resolved;
    try {
      resolved = root.resolve(trimmed).normalize();
    } catch (RuntimeException e) {
      return null;
    }
    if (!resolved.startsWith(root)) {
      return null;
    }
    if (!Files.exists(resolved, java.nio.file.LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(resolved)
        || isHidden(resolved) || !Files.isReadable(resolved)) {
      return null;
    }
    for (Path p = resolved; p != null && !p.equals(root); p = p.getParent()) {
      if (isHidden(p) || Files.isSymbolicLink(p)) {
        return null;
      }
    }
    return resolved;
  }

  private static boolean isHidden(Path p) {
    Path name = p.getFileName();
    return name != null && name.toString().startsWith(".");
  }

  private void handleDirectory(HttpExchange exchange, URI uri, Path dir) throws IOException {
    if (!uri.getPath().endsWith("/")) {
      String location = uri.getRawPath() + "/";
      exchange.getResponseHeaders().set("Location", location);
      exchange.sendResponseHeaders(301, -1);
      exchange.close();
      return;
    }
    Path index = dir.resolve("index.html");
    if (!Files.exists(index)) {
      index = dir.resolve("index.htm");
    }
    if (Files.exists(index)) {
      if (!Files.isRegularFile(index)) {
        throw new IOException("index is not a regular file");
      }
      serveFile(exchange, index, true);
      return;
    }
    StringBuilder body = new StringBuilder();
    body.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\"/>\n</head>\n<body>\n");
    body.append("<h1>Directory listing for ").append(escape(uri.getPath())).append("</h1>\n<ul>\n");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (Path entry : stream) {
        if (isHidden(entry) || !Files.isReadable(entry) || Files.isSymbolicLink(entry)) {
          continue;
        }
        String name = entry.getFileName().toString();
        boolean isDir = Files.isDirectory(entry);
        String shown = isDir ? name + "/" : name;
        String href = encode(name) + (isDir ? "/" : "");
        body.append("<li><a href=\"").append(href).append("\">").append(escape(shown)).append("</a></li>\n");
      }
    }
    body.append("</ul>\n</body>\n</html>\n");
    byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
    Headers headers = exchange.getResponseHeaders();
    headers.set("Last-modified", httpDate(Files.getLastModifiedTime(dir).toMillis()));
    headers.set("Content-type", "text/html; charset=UTF-8");
    respond(exchange, 200, bytes);
  }

  private static String encode(String name) {
    try {
      return new URI(null, null, name, null).toASCIIString();
    } catch (URISyntaxException e) {
      return name;
    }
  }

  private static String httpDate(long millis) {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC).format(kHttpDate);
  }

  private void serveFile(HttpExchange exchange, Path file, boolean index) throws IOException {
    Headers headers = exchange.getResponseHeaders();
    headers.set("Last-modified", httpDate(Files.getLastModifiedTime(file).toMillis()));
    String type = URLConnection.guessContentTypeFromName(file.getFileName().toString());
    headers.set("Content-type", type == null ? "application/octet-stream" : type);
    long length = Files.size(file);
    if (exchange.getRequestMethod().equals("HEAD")) {
      headers.set("Content-length", Long.toString(length));
      exchange.sendResponseHeaders(200, -1);
      exchange.close();
      return;
    }
    exchange.sendResponseHeaders(200, length);
    try (OutputStream out = exchange.getResponseBody(); InputStream in = Files.newInputStream(file)) {
      in.transferTo(out);
    }
  }

  private void notFound(HttpExchange exchange, String requestPath) throws IOException {
    String body = "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\"/>\n</head>\n<body>\n"
        + "<h1>File not found</h1>\n<p>" + escape(requestPath) + "</p>\n</body>\n</html>\n";
    exchange.getResponseHeaders().set("Content-type", "text/html; charset=UTF-8");
    respond(exchange, 404, body.getBytes(StandardCharsets.UTF_8));
  }

  private void respond(HttpExchange exchange, int code, byte[] body) throws IOException {
    if (exchange.getRequestMethod().equals("HEAD")) {
      exchange.getResponseHeaders().set("Content-length", Integer.toString(body.length));
      exchange.sendResponseHeaders(code, -1);
      exchange.close();
      return;
    }
    exchange.sendResponseHeaders(code, body.length);
    try (OutputStream out = exchange.getResponseBody()) {
      out.write(body);
    }
  }

  private static String escape(String s) {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
      case '&': sb.append("&amp;"); break;
      case '<': sb.append("&lt;"); break;
      case '>': sb.append("&gt;"); break;
      case '"': sb.append("&quot;"); break;
      case '\'': sb.append("&#x27;"); break;
      case '/': sb.append("&#x2F;"); break;
      default: sb.append(c);
      }
    }
    return sb.toString();
  }
}
