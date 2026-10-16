/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.net.*;
import java.util.function.Supplier;

public class IdnAndPermissionsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  public static void main(String[] args) {
    String[] hosts = {"example.com", "EXAMPLE.com", "münchen.de", "bücher.example", "例え.jp", "مثال.إختبار", "café.fr", "xn--mnchen-3ya.de", "a.b.c", "", "a..b", "x。y", "sub.ä.com", "☃.net", "a-b.com", "-a.com", "a_b.com", "example.com."};
    for (String h : hosts) {
      probe("ascii[" + h + "]", () -> IDN.toASCII(h));
      probe("ascii-std3[" + h + "]", () -> IDN.toASCII(h, IDN.USE_STD3_ASCII_RULES));
      probe("unicode[" + h + "]", () -> IDN.toUnicode(h));
      probe("roundtrip[" + h + "]", () -> IDN.toUnicode(IDN.toASCII(h)));
    }
    probe("constants", () -> IDN.ALLOW_UNASSIGNED + " " + IDN.USE_STD3_ASCII_RULES);
    probe("too-long", () -> IDN.toASCII("a".repeat(64) + ".com"));
    probe("socket-permission", () -> { SocketPermission p = new SocketPermission("localhost:1024-", "connect,accept"); return p.getActions() + " " + p.getName() + " " + p.implies(new SocketPermission("localhost:2000", "connect")) + " " + p.implies(new SocketPermission("localhost:80", "connect")) + " " + p.implies(new SocketPermission("localhost:2000", "listen")) + " " + p.equals(new SocketPermission("localhost:1024-", "accept,connect")); });
    probe("socket-permission-bad", () -> new SocketPermission("h", "fly"));
    probe("socket-permission-str", () -> new SocketPermission("localhost:80", "connect").toString());
    probe("url-permission", () -> { URLPermission p = new URLPermission("http://www.example.com/a/*", "GET:X-Foo"); return p.getActions() + " " + p.getName() + " " + p.implies(new URLPermission("http://www.example.com/a/b", "GET:X-Foo")) + " " + p.implies(new URLPermission("http://www.example.com/c", "GET:X-Foo")) + " " + p.equals(new URLPermission("http://www.example.com/a/*", "GET:X-Foo")); });
    probe("permission-to-string", () -> new NetPermission("setDefaultAuthenticator") + " " + new java.lang.reflect.ReflectPermission("suppressAccessChecks") + " " + new LinkPermissionHolder().toString());
  }

  static class LinkPermissionHolder {
    public String toString() { return new java.nio.file.LinkPermission("hard").toString(); }
  }
}
