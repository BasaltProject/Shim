/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public final class URL implements Serializable {
  private static final long serialVersionUID = -7627629688361524110L;

  private static volatile URLStreamHandlerFactory factory;

  private String protocol;
  private String host;
  private int port = -1;
  private String file;
  private String authority;
  private String ref;
  private int hashCode = -1;

  private transient String userInfo;
  private transient String path;
  private transient String query;
  transient URLStreamHandler handler;
  private transient InetAddress hostAddress;

  public URL(String protocol, String host, int port, String file) throws MalformedURLException {
    this(protocol, host, port, file, null);
  }

  public URL(String protocol, String host, String file) throws MalformedURLException {
    this(protocol, host, -1, file);
  }

  public URL(String protocol, String host, int port, String file, URLStreamHandler handler)
      throws MalformedURLException {
    if (protocol == null) {
      throw new NullPointerException();
    }
    String lowerCaseProtocol = protocol.toLowerCase(Locale.ROOT);
    this.protocol = lowerCaseProtocol;
    if (host != null) {
      if (host.indexOf(':') >= 0 && !host.startsWith("[")) {
        host = "[" + host + "]";
      }
      this.host = host;
      if (port < -1) {
        throw new MalformedURLException("Invalid port number :" + port);
      }
      this.port = port;
      authority = (port == -1) ? host : host + ":" + port;
    }
    String spec = file;
    int hash = spec.indexOf('#');
    if (hash >= 0) {
      ref = spec.substring(hash + 1);
      spec = spec.substring(0, hash);
    }
    int q = spec.lastIndexOf('?');
    if (q != -1) {
      query = spec.substring(q + 1);
      path = spec.substring(0, q);
    } else {
      path = spec;
    }
    this.file = query != null ? path + "?" + query : path;
    if (handler == null && (handler = findHandler(lowerCaseProtocol)) == null) {
      throw new MalformedURLException("unknown protocol: " + lowerCaseProtocol);
    }
    this.handler = handler;
  }

  public URL(String spec) throws MalformedURLException {
    this(null, spec);
  }

  public URL(URL context, String spec) throws MalformedURLException {
    this(context, spec, null);
  }

  public URL(URL context, String spec, URLStreamHandler handler) throws MalformedURLException {
    String original = spec;
    int i;
    int limit;
    int c;
    int start = 0;
    String newProtocol = null;
    boolean aRef = false;
    boolean isRelative = false;
    try {
      limit = spec.length();
      while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) {
        limit--;
      }
      while ((start < limit) && (spec.charAt(start) <= ' ')) {
        start++;
      }
      if (spec.regionMatches(true, start, "url:", 0, 4)) {
        start += 4;
      }
      if (start < spec.length() && spec.charAt(start) == '#') {
        aRef = true;
      }
      for (i = start; !aRef && (i < limit) && ((c = spec.charAt(i)) != '/'); i++) {
        if (c == ':') {
          String s = spec.substring(start, i).toLowerCase(Locale.ROOT);
          if (isValidProtocol(s)) {
            newProtocol = s;
            start = i + 1;
          }
          break;
        }
      }
      protocol = newProtocol;
      if ((context != null) && ((newProtocol == null) || newProtocol.equalsIgnoreCase(context.protocol))) {
        if (handler == null) {
          handler = context.handler;
        }
        if (context.path != null && context.path.startsWith("/")) {
          newProtocol = null;
        }
        if (newProtocol == null) {
          protocol = context.protocol;
          authority = context.authority;
          userInfo = context.userInfo;
          host = context.host;
          port = context.port;
          file = context.file;
          path = context.path;
          isRelative = true;
        }
      }
      if (protocol == null) {
        throw new MalformedURLException("no protocol: " + original);
      }
      if (handler == null && (handler = findHandler(protocol)) == null) {
        throw new MalformedURLException("unknown protocol: " + protocol);
      }
      this.handler = handler;
      i = spec.indexOf('#', start);
      if (i >= 0) {
        ref = spec.substring(i + 1, limit);
        limit = i;
      }
      if (isRelative && start == limit) {
        query = context.query;
        if (ref == null) {
          ref = context.ref;
        }
      }
      handler.parseURL(this, spec, start, limit);
    } catch (MalformedURLException e) {
      throw e;
    } catch (Exception e) {
      MalformedURLException exception = new MalformedURLException(e.getMessage());
      exception.initCause(e);
      throw exception;
    }
  }

  public static URL of(URI uri, URLStreamHandler handler) throws MalformedURLException {
    Objects.requireNonNull(uri, "uri");
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute");
    }
    return new URL(null, uri.toString(), handler);
  }

  private static boolean isValidProtocol(String protocol) {
    int len = protocol.length();
    if (len < 1) {
      return false;
    }
    char c = protocol.charAt(0);
    if (!Character.isLetter(c)) {
      return false;
    }
    for (int i = 1; i < len; i++) {
      c = protocol.charAt(i);
      if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-') {
        return false;
      }
    }
    return true;
  }

  void set(String protocol, String host, int port, String file, String ref) {
    synchronized (this) {
      this.protocol = protocol;
      this.host = host;
      authority = port == -1 ? host : host + ":" + port;
      this.port = port;
      this.file = file;
      this.ref = ref;
      hashCode = -1;
      hostAddress = null;
      int q = file.lastIndexOf('?');
      if (q != -1) {
        query = file.substring(q + 1);
        path = file.substring(0, q);
      } else {
        path = file;
      }
    }
  }

  void set(String protocol, String host, int port, String authority, String userInfo, String path, String query,
      String ref) {
    synchronized (this) {
      this.protocol = protocol;
      this.host = host;
      this.port = port;
      this.file = query == null ? path : path + "?" + query;
      this.userInfo = userInfo;
      this.path = path;
      this.ref = ref;
      hashCode = -1;
      hostAddress = null;
      this.query = query;
      this.authority = authority;
    }
  }

  public String getQuery() {
    return query;
  }

  public String getPath() {
    return path;
  }

  public String getUserInfo() {
    return userInfo;
  }

  public String getAuthority() {
    return authority;
  }

  public int getPort() {
    return port;
  }

  public int getDefaultPort() {
    return handler.getDefaultPort();
  }

  public String getProtocol() {
    return protocol;
  }

  public String getHost() {
    return host;
  }

  public String getFile() {
    return file;
  }

  public String getRef() {
    return ref;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof URL)) {
      return false;
    }
    URL u2 = (URL) obj;
    return handler.equals(this, u2);
  }

  @Override
  public synchronized int hashCode() {
    if (hashCode != -1) {
      return hashCode;
    }
    hashCode = handler.hashCode(this);
    return hashCode;
  }

  public boolean sameFile(URL other) {
    return handler.sameFile(this, other);
  }

  @Override
  public String toString() {
    return toExternalForm();
  }

  public String toExternalForm() {
    return handler.toExternalForm(this);
  }

  public URI toURI() throws URISyntaxException {
    return new URI(toString());
  }

  public URLConnection openConnection() throws IOException {
    return handler.openConnection(this);
  }

  public URLConnection openConnection(Proxy proxy) throws IOException {
    if (proxy == null) {
      throw new IllegalArgumentException("proxy can not be null");
    }
    return handler.openConnection(this, proxy);
  }

  public final InputStream openStream() throws IOException {
    return openConnection().getInputStream();
  }

  public final Object getContent() throws IOException {
    return openConnection().getContent();
  }

  public final Object getContent(Class<?>[] classes) throws IOException {
    return openConnection().getContent(classes);
  }

  public static void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
    synchronized (URL.class) {
      if (factory != null) {
        throw new Error("factory already defined");
      }
      factory = fac;
    }
  }

  InetAddress getHostAddress() {
    synchronized (this) {
      if (hostAddress != null) {
        return hostAddress;
      }
      if (host == null || host.isEmpty()) {
        return null;
      }
      try {
        hostAddress = InetAddress.getByName(host);
      } catch (UnknownHostException ex) {
        return null;
      }
      return hostAddress;
    }
  }

  private static URLStreamHandler findHandler(String protocol) {
    URLStreamHandlerFactory fac = factory;
    if (fac != null) {
      URLStreamHandler h = fac.createURLStreamHandler(protocol);
      if (h != null) {
        return h;
      }
    }
    if ("http".equals(protocol)) {
      return new shim.http.Handler();
    } else if ("https".equals(protocol)) {
      return new shim.https.Handler();
    } else if ("shimvmresource".equals(protocol)) {
      return new shim.shimvmresource.Handler();
    } else if ("file".equals(protocol)) {
      return new shim.file.Handler();
    } else if ("jar".equals(protocol)) {
      return new shim.jar.Handler();
    } else if ("ftp".equals(protocol)) {
      return new shim.ftp.Handler();
    } else if ("mailto".equals(protocol)) {
      return new shim.mailto.Handler();
    } else if ("jrt".equals(protocol)) {
      return new shim.jrt.Handler();
    }
    return null;
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    URLStreamHandler h = findHandler(protocol);
    if (h == null) {
      throw new IOException("unknown protocol: " + protocol);
    }
    handler = h;
    hashCode = -1;
    if (authority == null && ((host != null && !host.isEmpty()) || port != -1)) {
      authority = (host != null && port == -1) ? host : (host == null ? "" : host) + ":" + port;
    }
    if (authority != null) {
      int ind = authority.indexOf('@');
      if (ind != -1) {
        userInfo = authority.substring(0, ind);
      }
    }
    if (file != null) {
      int q = file.lastIndexOf('?');
      if (q != -1) {
        query = file.substring(q + 1);
        path = file.substring(0, q);
      } else {
        path = file;
      }
    }
  }
}
