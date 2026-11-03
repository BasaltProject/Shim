/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Naming {
  private Naming() {
  }

  public static Remote lookup(String name) throws NotBoundException, MalformedURLException, RemoteException {
    ParsedNamingURL parsed = parseURL(name);
    Registry registry = getRegistry(parsed);
    if (parsed.name == null) {
      return registry;
    }
    return registry.lookup(parsed.name);
  }

  public static void bind(String name, Remote obj) throws AlreadyBoundException, MalformedURLException,
      RemoteException {
    ParsedNamingURL parsed = parseURL(name);
    Registry registry = getRegistry(parsed);
    if (obj == null) {
      throw new NullPointerException("cannot bind to null");
    }
    registry.bind(parsed.name, obj);
  }

  public static void unbind(String name) throws RemoteException, NotBoundException, MalformedURLException {
    ParsedNamingURL parsed = parseURL(name);
    Registry registry = getRegistry(parsed);
    registry.unbind(parsed.name);
  }

  public static void rebind(String name, Remote obj) throws RemoteException, MalformedURLException {
    ParsedNamingURL parsed = parseURL(name);
    Registry registry = getRegistry(parsed);
    if (obj == null) {
      throw new NullPointerException("cannot bind to null");
    }
    registry.rebind(parsed.name, obj);
  }

  public static String[] list(String name) throws RemoteException, MalformedURLException {
    ParsedNamingURL parsed = parseURL(name);
    Registry registry = getRegistry(parsed);
    String prefix = "";
    if (parsed.port > 0 || !parsed.host.equals("")) {
      prefix += "//" + parsed.host;
    }
    if (parsed.port > 0) {
      prefix += ":" + parsed.port;
    }
    prefix += "/";
    String[] names = registry.list();
    for (int i = 0; i < names.length; i++) {
      names[i] = prefix + names[i];
    }
    return names;
  }

  private static Registry getRegistry(ParsedNamingURL parsed) throws RemoteException {
    return LocateRegistry.getRegistry(parsed.host, parsed.port);
  }

  private static ParsedNamingURL parseURL(String str) throws MalformedURLException {
    try {
      return intParseURL(str);
    } catch (URISyntaxException ex) {
      MalformedURLException e = new MalformedURLException("invalid URL String: " + str);
      e.initCause(ex);
      int indexSchemeEnd = str.indexOf(':');
      int indexAuthorityBegin = str.indexOf("//");
      if (indexAuthorityBegin < 0) {
        throw e;
      }
      if ((indexAuthorityBegin == 0) || ((indexSchemeEnd > 0) && (indexAuthorityBegin == indexSchemeEnd + 1))) {
        int indexHostBegin = indexAuthorityBegin + 2;
        int indexPortBegin = str.indexOf(':', indexHostBegin);
        int indexNameBegin = str.indexOf('/', indexHostBegin);
        if (indexNameBegin < 0) {
          indexNameBegin = str.length();
        }
        if (indexPortBegin < 0 || indexPortBegin > indexNameBegin) {
          indexPortBegin = -1;
        }
        String host = str.substring(indexHostBegin, indexPortBegin >= 0 ? indexPortBegin : indexNameBegin);
        int port = -1;
        if (indexPortBegin >= 0) {
          try {
            port = Integer.parseInt(str.substring(indexPortBegin + 1, indexNameBegin));
          } catch (NumberFormatException nfe) {
            throw e;
          }
        }
        String name = indexNameBegin < str.length() ? str.substring(indexNameBegin + 1) : null;
        return new ParsedNamingURL(host, port, name);
      }
      throw e;
    }
  }

  private static ParsedNamingURL intParseURL(String str) throws MalformedURLException, URISyntaxException {
    URI uri = new URI(str);
    if (uri.isOpaque()) {
      throw new MalformedURLException("not a hierarchical URL: " + str);
    }
    if (uri.getFragment() != null) {
      throw new MalformedURLException("invalid character, '#', in URL name: " + str);
    } else if (uri.getQuery() != null) {
      throw new MalformedURLException("invalid character, '?', in URL name: " + str);
    } else if (uri.getUserInfo() != null) {
      throw new MalformedURLException("invalid character, '@', in URL host: " + str);
    }
    String scheme = uri.getScheme();
    if (scheme != null && !scheme.equals("rmi")) {
      throw new MalformedURLException("invalid URL scheme: " + str);
    }
    String name = uri.getPath();
    if (name != null) {
      if (name.startsWith("/")) {
        name = name.substring(1);
      }
      if (name.length() == 0) {
        name = null;
      }
    }
    String host = uri.getHost();
    if (host == null) {
      host = "";
      try {
        uri.parseServerAuthority();
      } catch (URISyntaxException e) {
        String authority = uri.getRawAuthority();
        if (authority != null && authority.startsWith(":")) {
          authority = "localhost" + authority;
          uri = new URI(null, authority, null, null, null);
          uri.parseServerAuthority();
          host = "";
        } else {
          throw e;
        }
      }
    }
    int port = uri.getPort();
    if (port == -1) {
      port = Registry.REGISTRY_PORT;
    }
    return new ParsedNamingURL(host, port, name);
  }

  private static final class ParsedNamingURL {
    final String host;
    final int port;
    final String name;

    ParsedNamingURL(String host, int port, String name) {
      this.host = host;
      this.port = port;
      this.name = name;
    }
  }
}
