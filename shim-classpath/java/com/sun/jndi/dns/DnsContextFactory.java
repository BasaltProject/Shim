/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.dns;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public final class DnsContextFactory implements InitialContextFactory {
  @Override
  public Context getInitialContext(Hashtable<?, ?> env) throws NamingException {
    @SuppressWarnings("unchecked")
    Hashtable<Object, Object> copy = env == null ? new Hashtable<Object, Object>()
        : (Hashtable<Object, Object>) env.clone();
    String url = (String) copy.get(Context.PROVIDER_URL);
    if (url == null) {
      url = "dns:";
    }
    return create(url, copy);
  }

  static Context create(String urls, Hashtable<Object, Object> env) throws NamingException {
    List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();
    String domain = null;
    for (String u : urls.trim().split("\\s+")) {
      if (u.indexOf(':') < 0) {
        throw new ConfigurationException("Invalid URI: " + u);
      }
      if (!u.regionMatches(true, 0, "dns:", 0, 4)) {
        throw new ConfigurationException(u + " is not a valid DNS pseudo-URL");
      }
      String rest = u.substring(4);
      String hostPort = "";
      String path = "";
      if (rest.startsWith("//")) {
        rest = rest.substring(2);
        int slash = rest.indexOf('/');
        hostPort = slash < 0 ? rest : rest.substring(0, slash);
        path = slash < 0 ? "" : rest.substring(slash + 1);
      } else {
        path = rest;
      }
      try {
        if (!path.isEmpty()) {
          path = new URI("dns", null, "x", -1, "/" + path, null, null).getPath().substring(1);
          path = java.net.URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        }
        if (path.contains(" ")) {
          throw new URISyntaxException(path, "space");
        }
      } catch (URISyntaxException e) {
        throw new ConfigurationException("Invalid URI: " + u.substring(u.lastIndexOf(' ') + 1));
      }
      if (domain == null) {
        domain = path;
      }
      if (!hostPort.isEmpty()) {
        servers.add(parseServer(hostPort));
      }
    }
    if (servers.isEmpty()) {
      servers.addAll(systemServers());
    }
    return new DnsContext(domain == null ? "" : domain, servers, env);
  }

  private static InetSocketAddress parseServer(String hostPort) throws NamingException {
    String host = hostPort;
    int port = 53;
    if (hostPort.startsWith("[")) {
      int end = hostPort.indexOf(']');
      host = hostPort.substring(1, end);
      if (hostPort.length() > end + 1 && hostPort.charAt(end + 1) == ':') {
        port = Integer.parseInt(hostPort.substring(end + 2));
      }
    } else {
      int colon = hostPort.lastIndexOf(':');
      if (colon >= 0) {
        host = hostPort.substring(0, colon);
        port = Integer.parseInt(hostPort.substring(colon + 1));
      }
    }
    return new InetSocketAddress(host, port);
  }

  private static List<InetSocketAddress> systemServers() {
    List<InetSocketAddress> out = new ArrayList<InetSocketAddress>();
    try {
      for (String line : Files.readAllLines(Paths.get("/etc/resolv.conf"))) {
        line = line.trim();
        if (line.startsWith("nameserver")) {
          String[] parts = line.split("\\s+");
          if (parts.length > 1) {
            out.add(new InetSocketAddress(parts[1], 53));
          }
        }
      }
    } catch (IOException | RuntimeException e) {
      // fall through to the loopback default
    }
    if (out.isEmpty()) {
      out.add(new InetSocketAddress("127.0.0.1", 53));
    }
    return out;
  }
}
