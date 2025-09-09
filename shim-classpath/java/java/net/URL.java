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
import java.util.Objects;

public final class URL {
  private final URLStreamHandler handler;
  private String protocol;
  private String host;
  private int port = -1;
  private String file;
  private String path;
  private String query;
  private String ref;
  private String authority;
  private String userInfo;

  public URL(String s) throws MalformedURLException {
    this(null, s);
  }

  public URL(URL context, String spec) throws MalformedURLException {
    this(context, spec, null);
  }

  public URL(URL context, String spec, URLStreamHandler handler) throws MalformedURLException {
    Objects.requireNonNull(spec, "spec");
    spec = spec.trim();
    int colon = spec.indexOf(':');
    int slash = spec.indexOf('/');
    if (colon > 0 && (slash < 0 || colon < slash)) {
      String proto = spec.substring(0, colon).toLowerCase();
      this.handler = handler != null ? handler : findHandler(proto);
      this.handler.parseURL(this, spec, colon + 1, spec.length());
    } else if (context != null) {
      this.handler = handler != null ? handler : context.handler;
      this.handler.parseURL(this, spec, 0, spec.length());
    } else {
      throw new MalformedURLException("no protocol: " + spec);
    }
  }

  public URL(String protocol, String host, int port, String file) throws MalformedURLException {
    this(protocol, host, port, file, null);
  }

  public URL(String protocol, String host, String file) throws MalformedURLException {
    this(protocol, host, -1, file, null);
  }

  public URL(String protocol, String host, int port, String file, URLStreamHandler handler)
    throws MalformedURLException
  {
    if (protocol == null) throw new MalformedURLException("null protocol");
    this.protocol = protocol.toLowerCase();
    this.host = host;
    this.port = port;
    this.file = file;
    this.handler = handler != null ? handler : findHandler(this.protocol);
    if (file != null) {
      int q = file.indexOf('?');
      if (q != -1) {
        this.query = file.substring(q + 1);
        this.path = file.substring(0, q);
      } else {
        this.path = file;
      }
    }
  }

  public static URL of(URI uri, URLStreamHandler handler) throws MalformedURLException {
    Objects.requireNonNull(uri, "uri");
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute: " + uri);
    }
    return new URL(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), handler);
  }

  public URI toURI() throws URISyntaxException {
    return new URI(toString());
  }

  public String getAuthority() {
    if (authority != null) return authority;
    if (host == null) return null;
    return port == -1 ? host : host + ":" + port;
  }

  public String getUserInfo() {
    return userInfo;
  }

  public int getDefaultPort() {
    if ("http".equals(protocol)) return 80;
    if ("https".equals(protocol)) return 443;
    if ("ftp".equals(protocol)) return 21;
    return handler != null ? handler.getDefaultPort() : -1;
  }

  public String toString() {
    return handler.toExternalForm(this);
  }

  public String toExternalForm() {
    return toString();
  }

  public String getProtocol() {
    return protocol;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getFile() {
    return file;
  }

  public String getRef() {
    return ref;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getQuery() {
    return query;
  }

  public URLConnection openConnection() throws IOException {
    return handler.openConnection(this);
  }

  public URLConnection openConnection(Proxy proxy) throws IOException {
    if (proxy == null) {
      throw new IllegalArgumentException("proxy == null");
    }
    return handler.openConnection(this, proxy);
  }

  public InputStream openStream() throws IOException {
    return openConnection().getInputStream();
  }

  public Object getContent() throws IOException {
    return openStream();
  }

  private static URLStreamHandler findHandler(String protocol)
    throws MalformedURLException
  {
    if ("http".equals(protocol) || "https".equals(protocol)) {
      return new shim.http.Handler();
    } else if ("shimvmresource".equals(protocol)) {
      return new shim.shimvmresource.Handler();
    } else if ("file".equals(protocol)) {
      return new shim.file.Handler();
    } else if ("jar".equals(protocol)) {
      return new shim.jar.Handler();
    } else {
      throw new MalformedURLException("unknown protocol: " + protocol);
    }
  }

  public void set(String protocol, String host, int port, String file,
                  String ref)
  {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.file = file;
    this.ref = ref;
    
    int q = file == null ? -1 : file.lastIndexOf('?');
    if (q != -1) {
      this.query = file.substring(q + 1);
      this.path = file.substring(0, q);
    } else {
      this.path = file;
    }
  }

  public void set(String protocol, String host, int port, String authority,
                  String userInfo, String path, String query, String ref)
  {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.authority = authority;
    this.userInfo = userInfo;
    this.path = path;
    this.query = query;
    this.ref = ref;
    this.file = (query == null) ? path : path + "?" + query;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof URL)) return false;
    URL other = (URL) obj;
    return handler.equals(this, other);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
