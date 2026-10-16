/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class UriModern {
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

  static String parts(URI u) {
    return u.getScheme() + "|" + u.getSchemeSpecificPart() + "|" + u.getRawSchemeSpecificPart() + "|" + u.getAuthority() + "|" + u.getRawAuthority() + "|" + u.getUserInfo() + "|" + u.getRawUserInfo() + "|" + u.getHost() + "|" + u.getPort() + "|" + u.getPath() + "|" + u.getRawPath() + "|" + u.getQuery() + "|" + u.getRawQuery() + "|" + u.getFragment() + "|" + u.getRawFragment() + "|" + u.isAbsolute() + "|" + u.isOpaque() + "|" + u + "|" + u.toASCIIString();
  }

  public static void main(String[] args) throws Exception {
    String[] uris = {"http://example.com", "http://example.com/", "http://user:pw@host.example.com:8080/a/b/c.html?x=1&y=2#frag", "https://[::1]:443/path", "http://[2001:db8::1]/", "http://[fe80::1%25eth0]/", "http://[::ffff:192.168.0.1]:80/", "ftp://ftp.is.co.za/rfc/rfc1808.txt", "mailto:java-net@java.sun.com", "news:comp.lang.java", "urn:isbn:096139210x", "file:///etc/passwd", "file:/etc/passwd", "file:relative/path", "/absolute/path", "relative/path", "../up/../x", "./a/./b", "?query", "#frag", "", "a:b", "x:/", "http://a b/", "http://example.com/a b", "http://example.com/%41%20b", "http://example.com/%zz", "http://example.com/%4", "http://ex ample.com", "http://exa_mple.com/", "http://-bad.com/", "http://bad-.com/", "http://1.2.3.4/", "http://1.2.3/", "http://256.1.1.1/", "http://a.b.c.d.e/", "http://host:abc/", "http://host:99999999999/", "http://:80/", "http://@host/", "http://user@/", "//host/path", "//host", "///path", "/path?q#f", "scheme://auth/path;param?q#f", "http://h/p?q?r#f#g", "http://h/p#", "http://h/p?", "http://h?q", "http://h#f", "tel:+1-816-555-1212", "jar:file:/a.jar!/b/c", "data:text/plain;base64,SGVsbG8=", "http://example.com/\u00e9\u00e8", "http://\u00e9xample.com/", "http://example.com/a?b=\u00e9#\u00e9", "HTTP://EXAMPLE.COM/PATH", "1http://x", "ht+tp://x", "ht tp://x", ":x", "http:", "http:/", "http://", "http:///", "http://h/p%2", "[::1]", "http://[::1", "http://[::1]x/", "http://[:::1]/", "http://[1:2:3:4:5:6:7:8:9]/", "http://[12345::1]/", "http://[1::2::3]/", "a:b#c#d", "\\\\server\\share", "http://h/a\\b", "http://h/a|b", "http://h/a^b", "http://h/a{b}", "http://h/a`b", "http://h/<a>", "http://h/a\"b", "http://h/a;b=c;d", "http://h/a,b", "http://h/a@b:c", "http://us%40er@h/", "http://h:/p", "http://h:0/", "http://h:65535/", "http://localhost", "http://LocalHost:8080/Path"};
    for (String s : uris) {
      probe("uri " + s, () -> { try { URI u = new URI(s); return parts(u); } catch (URISyntaxException e) { return "!USE:" + e.getMessage() + " idx=" + e.getIndex() + " reason=" + e.getReason() + " input=" + e.getInput(); } });
    }
    probe("create bad", () -> URI.create("http://a b"));
    probe("create null", () -> URI.create(null));
    probe("ctor null", () -> { try { return new URI(null); } catch (URISyntaxException e) { return "use"; } });
    String[][] bases = {{"http://a/b/c/d;p?q", "g:h"}, {"http://a/b/c/d;p?q", "g"}, {"http://a/b/c/d;p?q", "./g"}, {"http://a/b/c/d;p?q", "g/"}, {"http://a/b/c/d;p?q", "/g"}, {"http://a/b/c/d;p?q", "//g"}, {"http://a/b/c/d;p?q", "?y"}, {"http://a/b/c/d;p?q", "g?y"}, {"http://a/b/c/d;p?q", "#s"}, {"http://a/b/c/d;p?q", "g#s"}, {"http://a/b/c/d;p?q", "g?y#s"}, {"http://a/b/c/d;p?q", ";x"}, {"http://a/b/c/d;p?q", "g;x"}, {"http://a/b/c/d;p?q", ""}, {"http://a/b/c/d;p?q", "."}, {"http://a/b/c/d;p?q", "./"}, {"http://a/b/c/d;p?q", ".."}, {"http://a/b/c/d;p?q", "../"}, {"http://a/b/c/d;p?q", "../g"}, {"http://a/b/c/d;p?q", "../.."}, {"http://a/b/c/d;p?q", "../../"}, {"http://a/b/c/d;p?q", "../../g"}, {"http://a/b/c/d;p?q", "../../../g"}, {"http://a/b/c/d;p?q", "../../../../g"}, {"http://a/b/c/d;p?q", "/./g"}, {"http://a/b/c/d;p?q", "/../g"}, {"http://a/b/c/d;p?q", "g."}, {"http://a/b/c/d;p?q", ".g"}, {"http://a/b/c/d;p?q", "g.."}, {"http://a/b/c/d;p?q", "..g"}, {"http://a/b/c/d;p?q", "./../g"}, {"http://a/b/c/d;p?q", "./g/."}, {"http://a/b/c/d;p?q", "g/./h"}, {"http://a/b/c/d;p?q", "g/../h"}, {"http://a/b/c/d;p?q", "g;x=1/./y"}, {"http://a/b/c/d;p?q", "g;x=1/../y"}, {"http://a/b/c/d;p?q", "g?y/./x"}, {"http://a/b/c/d;p?q", "g#s/./x"}, {"http://a/b/c/d;p?q", "http:g"}, {"http://a", "b"}, {"http://a", "/b"}, {"http://a/", "b"}, {"http://a/x", "b"}, {"http://a/x/", "b"}, {"file:///a/b", "c"}, {"file:/a/b", "../c"}, {"/a/b", "c"}, {"a/b", "c"}, {"a/b", "../../c"}, {"a/b", "../../../c"}, {"mailto:a@b", "c"}, {"http://a/b", "mailto:x"}, {"http://a/b#f", "#f"}, {"http://a/b#f", "#g"}, {"http://a/b", "#"}, {"jar:file:/a.jar!/b", "c"}, {"http://a/b/c/d", "e:f"}, {"http://a/b/c/d", "./e:f"}, {"http://a/b/c/d", "//h:80/x"}, {"http://a/b/c", "..//d"}, {"http://a/b//c", "d"}, {"http://a/b/c/", ""}, {"http://a/b/c/", "?"}};
    for (String[] bc : bases) {
      probe("resolve " + bc[0] + " + " + bc[1], () -> URI.create(bc[0]).resolve(bc[1]));
    }
    String[] norm = {"http://a/b/../c", "http://a/./b/./c/.", "http://a/b/c/../../..", "http://a/b//c", "a/b/../c", "./a", "../a", "a/..", "a/../..", "/..", "/../a", "/a/..", "/a/../b/.", "a:b/../c", "a/./b:c", "./a:b", "x/../a:b", "http://a", "http://a/", "mailto:x@y", "//h/a/../b", "a//b", "/", "", ".", "..", "./", "../", "http://a/b/c/./../d", "http://a/%7Efoo/../bar", "http://a/b/%2e%2e/c", "a/b/c/../../../../d", "b/./.", "../../a/b/../c"};
    for (String s : norm) probe("normalize " + s, () -> URI.create(s).normalize());
    String[][] rel = {{"http://a/b/", "http://a/b/c/d"}, {"http://a/b", "http://a/b/c"}, {"http://a/b/", "http://a/b/"}, {"http://a/b/", "http://a/c"}, {"http://a/b/", "http://x/b/c"}, {"http://a/b/", "https://a/b/c"}, {"http://a/b/", "http://A/b/c"}, {"http://a/b", "http://a/b"}, {"http://a/b/c", "http://a/b/d"}, {"file:/a/", "file:/a/b/c.txt"}, {"/a/", "/a/b"}, {"a/", "a/b"}, {"http://a/b/", "http://a/b/c?q#f"}, {"mailto:a@b", "mailto:a@b"}, {"http://a/b/", "mailto:x"}, {"http://a/b/./c/", "http://a/b/c/d"}, {"http://a/b/c/../d/", "http://a/b/d/e"}, {"http://a/", "http://a/x/y"}, {"http://a", "http://a/x"}, {"http://a/b/", "http://a/b/c/../d"}, {"http://u@a/b/", "http://a/b/c"}, {"http://a:80/b/", "http://a/b/c"}};
    for (String[] bc : rel) probe("relativize " + bc[0] + " + " + bc[1], () -> URI.create(bc[0]).relativize(URI.create(bc[1])));
    probe("relativize null", () -> URI.create("http://a/").relativize(null));
    probe("resolve null", () -> URI.create("http://a/").resolve((URI) null));
    probe("resolve bad", () -> URI.create("http://a/").resolve("a b"));
    probe("ctor7", () -> new URI("http", "user info", "example.com", 8080, "/a b/c", "q=1 2", "f g"));
    probe("ctor7 parts", () -> parts(new URI("http", "u@x", "host", 81, "/p%q", "a=b&c%d", "é")));
    probe("ctor7 relpath", () -> new URI("http", null, "host", -1, "relative", null, null));
    probe("ctor7 nohost", () -> new URI("http", "ui", null, -1, "/p", null, null));
    probe("ctor7 ipv6", () -> new URI("http", null, "::1", 80, "/", null, null));
    probe("ctor7 badhost", () -> new URI("http", null, "bad host", -1, "/", null, null));
    probe("ctor7 noscheme", () -> new URI(null, null, "host", -1, "/p", null, null));
    probe("ctor7 nopath", () -> new URI("http", null, "host", -1, null, "q", null));
    probe("ctor5", () -> new URI("http", "user@host:80", "/a b", "x=y z", "frag ment"));
    probe("ctor5 reg", () -> new URI("http", "some_reg:auth", "/p", null, null));
    probe("ctor5 bad", () -> new URI("http", "host:abc", "/p", null, null));
    probe("ctor4", () -> new URI("http", "host", "/a/b c", "f"));
    probe("ctor3", () -> new URI("mailto", "a b@c", null));
    probe("ctor3 frag", () -> new URI("urn", "isbn:123 4", "f x"));
    probe("ctor3 empty", () -> new URI("a", "", null));
    probe("ctor3 noscheme", () -> new URI(null, "//h/p", "f"));
    probe("ctor3 relative ssp", () -> new URI("scheme", "ssp/rel", "f"));
    probe("ctor scheme bad", () -> new URI("1bad", "x", null));
    probe("ctor path scheme", () -> new URI("file", null, "relative", null));
    probe("server authority", () -> { URI u = new URI("http://some_reg:name@/p"); return u.getHost() + "|" + u.getAuthority(); });
    probe("server authority parse", () -> URI.create("http://under_score.example.com:80/").parseServerAuthority());
    probe("server authority ok", () -> { URI u = URI.create("http://user@h.example.com:81/").parseServerAuthority(); return u.getHost() + u.getPort() + u.getUserInfo(); });
    probe("server authority reg", () -> { URI u = URI.create("http://reg_name/").parseServerAuthority(); return u.getHost(); });
    probe("equals", () -> { String[][] pairs = {{"http://a/b", "HTTP://A/b"}, {"http://a/b", "http://a/B"}, {"http://a/%aa", "http://a/%AA"}, {"http://a/%aa", "http://a/\u00aa"}, {"http://a:80/", "http://a/"}, {"http://a/b#f", "http://a/b"}, {"mailto:a@b", "MAILTO:a@b"}, {"mailto:a@B", "mailto:a@b"}, {"a:b%aa", "a:b%AA"}, {"/a", "/a"}, {"a", "a"}, {"http://U@a/", "http://u@a/"}, {"http://a?x", "http://a?x"}, {"http://a/", "http://a"}}; StringBuilder sb = new StringBuilder(); for (String[] p : pairs) { URI x = URI.create(p[0]); URI y = URI.create(p[1]); sb.append(x.equals(y) ? 'T' : 'F').append(x.hashCode() == y.hashCode() ? 't' : 'f').append(Integer.signum(x.compareTo(y))); } return sb; });
    probe("hashes", () -> { long h = 0; for (String s : uris) { try { h = h * 31 + URI.create(s).hashCode(); } catch (IllegalArgumentException e) { h = h * 31 + 1; } } return h; });
    probe("compare", () -> { List<URI> list = new ArrayList<>(); for (String s : new String[] {"http://b/", "http://a/z", "http://a/y", "mailto:x", "a/b", "/a", "http://a/y?q", "http://a/y#f", "HTTP://a/y", "urn:a", "urn:B", "http://a:81/", "http://a:80/", "http://u@a/", "http://a/"}) list.add(URI.create(s)); Collections.sort(list); return list; });
    probe("compare mixed", () -> URI.create("a:b").compareTo(URI.create("a:/b")) + " " + URI.create("a:/b").compareTo(URI.create("a:b")) + " " + URI.create("a:b").compareTo(URI.create("a:b")) + " " + URI.create("b").compareTo(URI.create("a:b")));
    probe("tourl", () -> URI.create("http://example.com/a?b#c").toURL());
    probe("tourl rel", () -> URI.create("a/b").toURL());
    probe("tourl bad scheme", () -> URI.create("nope://x/y").toURL());
    probe("serialization", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); new ObjectOutputStream(bo).writeObject(URI.create("http://u@h:81/p%20q?x#f")); URI back = (URI) new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray())).readObject(); return back + " " + back.getHost() + back.getPort(); });
    probe("ascii", () -> URI.create("http://\u00e9.com/\u00fc?\u00e9#\u00e9").toASCIIString() + " " + URI.create("http://x/a%20b").toASCIIString() + " " + new URI("http", "x", "/\u4e2d\u6587", null).toASCIIString());
    probe("decode utf8", () -> URI.create("http://x/%e4%b8%ad%e6%96%87%f0%9f%98%80?q=%41%c3%a9").getPath() + "|" + URI.create("http://x/%e4%b8").getPath().length() + "|" + URI.create("http://x/%ff%fe").getPath().length() + "|" + URI.create("http://x/a%2fb").getPath() + "|" + URI.create("http://x/a%2fb").getRawPath());
    probe("scheme case", () -> URI.create("HtTp://x/").getScheme() + " " + URI.create("HtTp://x/").toString());
    probe("host case", () -> URI.create("http://ExAmPle.COM/").getHost());
    probe("hierarchical empty", () -> { URI u = URI.create("file:///x"); return u.getAuthority() + "|" + u.getHost() + "|" + u.getPath(); });
    probe("opaque", () -> { URI u = URI.create("mailto:a@b?subject=x#f"); return u.getPath() + "|" + u.getQuery() + "|" + u.getSchemeSpecificPart() + "|" + u.getFragment() + "|" + u.getAuthority() + "|" + u.getHost() + "|" + u.getPort(); });
    probe("file uri", () -> { File f = new File("/tmp/a b/c#d.txt"); URI u = f.toURI(); return u + " " + new File(u).getPath() + " " + u.getPath() + " " + u.getRawPath(); });
    probe("file uri dir", () -> new File("/tmp").toURI() + " " + new File("/nonexistent/x").toURI() + " " + new File("rel").toURI().toString().startsWith("file:/"));
    probe("file ctor uri", () -> new File(URI.create("file:/a/b/")).getPath() + " " + new File(URI.create("file:///a//b")).getPath() + " " + new File(URI.create("file:/a%20b")).getPath() + " " + new File(URI.create("file:/")).getPath());
    probe("path uri", () -> java.nio.file.Path.of("/tmp/x y").toUri() + " " + java.nio.file.Path.of(URI.create("file:///tmp/z")));
    probe("string ctor misc", () -> { StringBuilder sb = new StringBuilder(); for (String s : new String[] {"http://a/b", "x:y", "/z", ""}) { URI u = URI.create(s); sb.append(u.toString().equals(s)).append(u.toASCIIString().equals(s)).append(' '); } return sb; });
  }
}
