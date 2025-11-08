/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
public class HttpCookieModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  static String d(HttpCookie c) { return c.getName() + "|" + c.getValue() + "|v" + c.getVersion() + "|d=" + c.getDomain() + "|p=" + c.getPath() + "|ma=" + (c.getMaxAge() > 100000 ? "big" : String.valueOf(c.getMaxAge())) + "|s=" + c.getSecure() + "|h=" + c.isHttpOnly() + "|c=" + c.getComment() + "|cu=" + c.getCommentURL() + "|dis=" + c.getDiscard() + "|port=" + c.getPortlist() + "|exp=" + c.hasExpired() + "|" + c; }
  static String p(String h) { try { List<HttpCookie> l = HttpCookie.parse(h); StringBuilder sb = new StringBuilder(); for (HttpCookie c : l) sb.append(d(c)).append(" ;; "); return sb.toString(); } catch (Throwable e) { return "!" + e.getClass().getName() + ":" + e.getMessage(); } }
  public static void main(String[] a) throws Exception {
    t("ctor", () -> d(new HttpCookie("n", "v")));
    t("ctorTrim", () -> d(new HttpCookie("  n  ", "v")));
    t("ctorEmpty", () -> new HttpCookie("", "v"));
    t("ctorDollar", () -> new HttpCookie("$x", "v"));
    t("ctorSpace", () -> new HttpCookie("a b", "v"));
    t("ctorComma", () -> new HttpCookie("a,b", "v"));
    t("ctorSemi", () -> new HttpCookie("a;b", "v"));
    t("ctorEq", () -> new HttpCookie("a=b", "v"));
    t("ctorNull", () -> new HttpCookie(null, "v"));
    t("ctorCtrl", () -> new HttpCookie("a\u0001b", "v"));
    t("ctorNullValue", () -> d(new HttpCookie("n", null)));
    for (String h : new String[] {"a=b", "a=b; Path=/; Domain=Example.COM; Secure; HttpOnly", "a=\"quoted value\"; Version=1", "Set-Cookie: a=b; Max-Age=100", "Set-Cookie2: a=b; Path=/x; Port=\"80,8080\"; Comment=hi; CommentURL=http://x/; Discard", "a=b; expires=Thu, 01-Jan-1970 00:00:10 GMT", "a=b; Expires=Wed, 09 Jun 2021 10:18:14 GMT", "a=b; Expires=Wed Jun 09 2021 10:18:14 GMT+0000", "a=b; Expires=Wed, 09-Jun-21 10:18:14 GMT", "a=b; Expires=garbage", "a=b; Max-Age=abc", "a=b; Max-Age=0", "a=b; Max-Age=-1", "a=b; version=2", "a=b; version=x", "a=b; Version=1; Path=/p; Path=/q; Domain=a.com; Domain=b.com", "a=b, c=d; Version=1", "a=b, c=d; Max-Age=5, e=\"x,y\"", "novalue", "=x", "", ";a=b", "a=b;;c", "a=b; port", "a=b; Port=\"\"", "a=b; Secure=yes; unknown=1; Discard=1", "a='single'; Max-Age=5", "a=\"\"; Max-Age=5", "a=\"x; Max-Age=5", "set-cookie2: a=b", "SET-COOKIE: a=b; expires=Thu, 01 Jan 1970 00:00:00 GMT", "a=b; version=0; Max-Age=5", "$Version=1; a=b", "a=b; Comment=one; Comment=two", "a=b ; c = d ; Path = /z", "a=b=c; Max-Age=3"})
      System.out.println("parse[" + h + "]: " + p(h));
    t("parseNull", () -> HttpCookie.parse(null));
    t("setVersion", () -> { HttpCookie c = new HttpCookie("a", "b"); c.setVersion(0); String r = d(c); c.setVersion(1); return r + " " + d(c); });
    t("setVersionBad", () -> { new HttpCookie("a", "b").setVersion(2); return 1; });
    t("setDomain", () -> { HttpCookie c = new HttpCookie("a", "b"); c.setDomain("EXAMPLE.Com"); String r = c.getDomain(); c.setDomain(null); return r + " " + c.getDomain(); });
    t("rfc", () -> { HttpCookie c = new HttpCookie("a", "b"); c.setPath("/p"); c.setDomain("x.com"); c.setPortlist("80"); return c.toString(); });
    t("netscape", () -> { HttpCookie c = new HttpCookie("a", "b"); c.setVersion(0); c.setPath("/p"); c.setDomain("x.com"); return c.toString(); });
    t("equals", () -> { HttpCookie c = new HttpCookie("A", "1"); c.setDomain("X.com"); c.setPath("/p"); HttpCookie e = new HttpCookie("a", "2"); e.setDomain("x.COM"); e.setPath("/p"); HttpCookie f = new HttpCookie("a", "2"); f.setDomain("x.com"); f.setPath("/P"); return c.equals(e) + " " + (c.hashCode() == e.hashCode()) + " " + c.equals(f) + " " + c.equals(null) + " " + c.equals("x") + " " + c.equals(c); });
    t("hash", () -> { HttpCookie c = new HttpCookie("Abc", "1"); c.setDomain("X.com"); c.setPath("/p"); return c.hashCode(); });
    t("hashNone", () -> new HttpCookie("Abc", "1").hashCode());
    t("clone", () -> { HttpCookie c = new HttpCookie("a", "b"); c.setPath("/x"); HttpCookie k = (HttpCookie) c.clone(); k.setPath("/y"); return c.getPath() + " " + k.getPath() + " " + k.getName(); });
    t("expiry", () -> { HttpCookie c = new HttpCookie("a", "b"); String r = c.hasExpired() + " "; c.setMaxAge(0); r += c.hasExpired() + " "; c.setMaxAge(100); r += c.hasExpired() + " "; c.setMaxAge(-5); r += c.hasExpired() + " " + c.getMaxAge(); return r; });
    String[][] dm = {{"example.com", "example.com"}, {".example.com", "www.example.com"}, {".example.com", "example.com"}, {"example.com", "www.example.com"}, {".example.com", "a.b.example.com"}, {"example.com", "EXAMPLE.COM"}, {".com", "example.com"}, {"com", "example.com"}, {".local", "host"}, {"host.local", "host"}, {".local", "host.x"}, {".example.com.", "x.example.com."}, {"", "host"}, {".", "host"}, {null, "h"}, {"x.com", null}, {".a.b", "a.b"}, {"a.b", ".a.b"}, {".example.com", "xexample.com"}, {".example.com", "x.example.com"}};
    for (String[] x : dm) System.out.println("dm[" + x[0] + "," + x[1] + "]: " + HttpCookie.domainMatches(x[0], x[1]));
  }
}
