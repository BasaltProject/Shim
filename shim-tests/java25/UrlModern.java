/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class UrlModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out);
  }

  static String parts(URL u) {
    return u.getProtocol() + "|" + u.getHost() + "|" + u.getPort() + "|" + u.getDefaultPort() + "|" + u.getPath() + "|" + u.getQuery() + "|" + u.getFile() + "|" + u.getRef() + "|" + u.getUserInfo() + "|" + u.getAuthority() + "|" + u.toExternalForm() + "|" + u;
  }

  public static void main(String[] args) throws Exception {
    String[] specs = {"http://example.com", "http://example.com/", "http://user:pw@host.example.com:8080/a/b/c.html?x=1&y=2#frag", "https://example.com:443/path", "http://[::1]:8080/p", "http://[2001:db8::1]/", "ftp://ftp.example.com/rfc/rfc1808.txt", "file:/etc/passwd", "file:///etc/passwd", "file://localhost/etc/passwd", "file:relative/path", "jar:file:/a.jar!/b/c", "jar:http://h/a.jar!/", "mailto:a@b.com", "http://h/p?q", "http://h?q", "http://h#f", "http://h/p#", "http://h/p?#", "http://h:/p", "http://h:0/", "http://h:65535/", "http://h:65536/", "http://h:-1/", "http://h:abc/", "HTTP://EXAMPLE.COM/PATH", "http://h/a b", "http://h/a%20b", "http://h/%zz", "http:///path", "http://", "http:", "http:/", "http:host", "http:/host", "//host/path", "/path", "relative", "", " ", "  http://h/ ", "http://h/\u00e9", "foo://bar/baz", "foo:bar", "nope", ":x", "1http://x", "ht+tp://x", "HtTp://h", "http://h/a/./b/../c", "http://h/a//b", "http://h/?a?b#c#d", "http://@h/", "http://u@h/", "http://u:@h/", "http://:p@h/", "http://h:80:81/", "http://a b/", "http://h/p;params?q", "http://h/\\p", "http://h/p|q", "http://h/p\tq", "data:text/plain,hi", "jrt:/java.base/x", "ws://h/x", "unknown:thing", "http://h/#a#b", "http://h/?q#f?x", "http://H:80/", "https://h:80/", "ftp://u:p@h:21/d;type=a", "file:///C:/dir/f.txt", "file:C:/x", "file:////server/share"};
    for (String s : specs) {
      probe("url " + s, () -> { try { return parts(new URL(s)); } catch (MalformedURLException e) { return "!MUE:" + e.getMessage(); } });
    }
    String[][] rels = {{"http://a/b/c/d;p?q", "g:h"}, {"http://a/b/c/d;p?q", "g"}, {"http://a/b/c/d;p?q", "./g"}, {"http://a/b/c/d;p?q", "g/"}, {"http://a/b/c/d;p?q", "/g"}, {"http://a/b/c/d;p?q", "//g"}, {"http://a/b/c/d;p?q", "?y"}, {"http://a/b/c/d;p?q", "g?y"}, {"http://a/b/c/d;p?q", "#s"}, {"http://a/b/c/d;p?q", "g#s"}, {"http://a/b/c/d;p?q", "g?y#s"}, {"http://a/b/c/d;p?q", ";x"}, {"http://a/b/c/d;p?q", ""}, {"http://a/b/c/d;p?q", "."}, {"http://a/b/c/d;p?q", "./"}, {"http://a/b/c/d;p?q", ".."}, {"http://a/b/c/d;p?q", "../"}, {"http://a/b/c/d;p?q", "../g"}, {"http://a/b/c/d;p?q", "../.."}, {"http://a/b/c/d;p?q", "../../g"}, {"http://a/b/c/d;p?q", "../../../g"}, {"http://a/b/c/d;p?q", "/./g"}, {"http://a/b/c/d;p?q", "/../g"}, {"http://a/b/c/d;p?q", "g."}, {"http://a/b/c/d;p?q", "..g"}, {"http://a/b/c/d;p?q", "./../g"}, {"http://a/b/c/d;p?q", "g/./h"}, {"http://a/b/c/d;p?q", "g/../h"}, {"http://a/b/c/d;p?q", "http:g"}, {"http://a/b/c/d;p?q", "http://x/y"}, {"http://a", "b"}, {"http://a/", "b"}, {"file:/a/b", "c"}, {"file:/a/b/", "../c"}, {"jar:file:/a.jar!/b/c", "d"}, {"http://a/b#f", "#g"}, {"http://a/b?q", "?r"}, {"http://a/b?q#f", ""}, {"http://a:80/b", "//c:81/d"}, {"http://u@a/b", "c"}, {"mailto:a@b", "c"}, {"http://a/b/c", "mailto:x"}};
    for (String[] r : rels) {
      probe("rel " + r[0] + " + " + r[1], () -> { try { return new URL(new URL(r[0]), r[1]).toExternalForm(); } catch (MalformedURLException e) { return "!MUE:" + e.getMessage(); } });
    }
    probe("rel null ctx", () -> new URL((URL) null, "http://x/y").toExternalForm());
    probe("rel null ctx relative", () -> new URL((URL) null, "y"));
    
    probe("ctor4", () -> new URL("http", "example.com", 8080, "/a/b?c#d") + " " + new URL("http", "example.com", -1, "/a") + " " + new URL("http", "example.com", "/a") + " " + new URL("http", "::1", 80, "/x") + " " + new URL("http", "[::1]", 80, "/x") + " " + new URL("file", "", "/etc/x") + " " + new URL("file", null, "/etc/x"));
    probe("ctor4 parts", () -> parts(new URL("http", "example.com", 8080, "/a/b?c#d")));
    probe("ctor4 unknown", () -> new URL("nope", "h", "/x"));
    probe("ctor4 nullproto", () -> new URL(null, "h", "/x"));
    probe("ctor4 badport", () -> new URL("http", "h", -5, "/x"));
    probe("ctor4 nopathslash", () -> new URL("http", "h", 80, "x"));
    probe("tostring", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[] {"http://h/p?q#f", "file:/x", "mailto:a@b", "jar:file:/a.jar!/e"}) { URL u = new URL(s); sb.append(u.toString().equals(u.toExternalForm())).append(u.toString().equals(s)).append(' '); } return sb; });
    probe("touri", () -> new URL("http://h/a%20b?q#f").toURI() + " " + new URL("file:/a/b").toURI() + " " + new URL("mailto:a@b").toURI());
    probe("touri bad", () -> new URL("http://h/a b").toURI());
    probe("sameFile", () -> { StringBuilder sb = new StringBuilder(); String[][] p = {{"http://127.0.0.1/a#x", "http://127.0.0.1/a#y"}, {"http://127.0.0.1/a?q", "http://127.0.0.1/a"}, {"http://127.0.0.1:80/a", "http://127.0.0.1/a"}, {"http://127.0.0.1/a", "https://127.0.0.1/a"}, {"file:/a", "file:/a"}, {"file:/a", "file:/b"}, {"http://127.0.0.1/A", "http://127.0.0.1/a"}}; for (String[] x : p) sb.append(new URL(x[0]).sameFile(new URL(x[1]))).append(' '); return sb; });
    probe("equals", () -> { StringBuilder sb = new StringBuilder(); String[][] p = {{"http://127.0.0.1/a#x", "http://127.0.0.1/a#y"}, {"http://127.0.0.1/a?q", "http://127.0.0.1/a?q"}, {"http://127.0.0.1:80/a", "http://127.0.0.1/a"}, {"file:/a", "file:/a"}, {"file:/a", "file:/b"}, {"mailto:a@b", "mailto:a@b"}, {"jar:file:/a!/b", "jar:file:/a!/b"}}; for (String[] x : p) { URL a = new URL(x[0]); URL b = new URL(x[1]); sb.append(a.equals(b)).append(a.hashCode() == b.hashCode() ? 't' : 'f').append(' '); } sb.append(new URL("file:/a").equals(null)).append(new URL("file:/a").equals("file:/a")); return sb; });
    probe("openStream file", () -> { Path p = Files.createTempFile("urlm", ".txt"); Files.write(p, "hello url".getBytes()); try (InputStream in = p.toUri().toURL().openStream()) { return new String(in.readAllBytes()); } finally { Files.delete(p); } });
    probe("openStream missing", () -> { try { new URL("file:/definitely/not/here.txt").openStream(); return "none"; } catch (FileNotFoundException e) { return e.getMessage().substring(0, 27); } });
    probe("openConnection file", () -> { Path p = Files.createTempFile("urlm", ".txt"); Files.write(p, "hello url".getBytes()); try { URLConnection c = p.toUri().toURL().openConnection(); return c.getContentLength() + " " + c.getContentType() + " " + (c.getLastModified() > 0) + " " + c.getClass().getSimpleName().length(); } finally { Files.delete(p); } });
    probe("getContent", () -> { Path p = Files.createTempFile("urlm", ".txt"); Files.write(p, "hello url".getBytes()); try { Object o = p.toUri().toURL().getContent(); return o instanceof InputStream; } finally { Files.delete(p); } });
    probe("url.of", () -> URL.of(URI.create("http://h/a?b#c"), null) + " " + URL.of(URI.create("file:/x"), null));
    probe("url.of rel", () -> URL.of(URI.create("a/b"), null));
    probe("url.of opaque", () -> URL.of(URI.create("nope://h/x"), null));
    probe("url.of null", () -> URL.of(null, null));
    probe("handler", () -> { URLStreamHandler h = new URLStreamHandler() { protected URLConnection openConnection(URL u) { return null; } protected int getDefaultPort() { return 7; } }; URL u = new URL("custom", "h", -1, "/p", h); return u + " " + u.getDefaultPort() + " " + u.getPort(); });
    probe("handler parse", () -> { URLStreamHandler h = new URLStreamHandler() { protected URLConnection openConnection(URL u) { return null; } }; URL u = new URL(null, "custom://u@h:9/p/q?x#y", h); return parts(u); });
    probe("numbers", () -> { StringBuilder sb = new StringBuilder(); String[] in = {"", "+", "-", "abc", "12x", "99999999999", "-99999999999", "+5", "-0", "007", "2147483648", "-2147483649"}; for (String x : in) { try { sb.append(Integer.parseInt(x)); } catch (NumberFormatException e) { sb.append(e.getMessage()); } sb.append('|'); try { sb.append(Long.parseLong(x, 16)); } catch (NumberFormatException e) { sb.append(e.getMessage()); } sb.append('|'); try { sb.append(Integer.parseInt(x, 0, x.length(), 10)); } catch (NumberFormatException e) { sb.append(e.getMessage()); } sb.append('|'); try { sb.append(Long.parseLong(x, 0, x.length(), 16)); } catch (NumberFormatException e) { sb.append(e.getMessage()); } sb.append('\n'); } try { Integer.parseInt(null); } catch (NumberFormatException e) { sb.append(e.getMessage()); } try { Integer.parseInt("1", 99); } catch (NumberFormatException e) { sb.append(e.getMessage()); } try { Integer.parseInt("1", 1); } catch (NumberFormatException e) { sb.append(e.getMessage()); } return sb; });
    probe("urlencoder", () -> URLEncoder.encode("a b&c=d/é\u4e2d~*-_.!'()", StandardCharsets.UTF_8) + " " + URLEncoder.encode("a b", "ISO-8859-1") + " " + URLDecoder.decode("a+b%20c%C3%A9", StandardCharsets.UTF_8));
    probe("urlencoder bad", () -> URLEncoder.encode("a", "nope-charset"));
    probe("urldecoder bad", () -> URLDecoder.decode("%zz", StandardCharsets.UTF_8));
    probe("urldecoder short", () -> URLDecoder.decode("abc%2", StandardCharsets.UTF_8));
    probe("urldecoder lone", () -> URLDecoder.decode("%ff%fe", StandardCharsets.UTF_8).length());
    probe("urlencoder null", () -> URLEncoder.encode(null, StandardCharsets.UTF_8));
    probe("urlencoder surrogates", () -> URLEncoder.encode("\ud83d\ude00\ud83d", StandardCharsets.UTF_8));
    probe("urlconnection", () -> URLConnection.guessContentTypeFromName("a.html") + " " + URLConnection.guessContentTypeFromName("a.txt") + " " + URLConnection.guessContentTypeFromName("a.png") + " " + URLConnection.guessContentTypeFromName("a.xyz") + " " + URLConnection.guessContentTypeFromName("a.jpg") + " " + URLConnection.guessContentTypeFromName("a.json") + " " + URLConnection.guessContentTypeFromName("a.css") + " " + URLConnection.guessContentTypeFromName("a.js"));
    probe("guessStream", () -> URLConnection.guessContentTypeFromStream(new ByteArrayInputStream("<html>".getBytes())) + " " + URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10})) + " " + URLConnection.guessContentTypeFromStream(new ByteArrayInputStream("GIF89a".getBytes())) + " " + URLConnection.guessContentTypeFromStream(new ByteArrayInputStream("xyz".getBytes())));
  }
}
