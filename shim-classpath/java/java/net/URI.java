/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public final class URI implements Comparable<URI>, Serializable {
  private static final long serialVersionUID = -6052424284110960213L;

  private transient String scheme;
  private transient String fragment;
  private transient String authority;
  private transient String userInfo;
  private transient String host;
  private transient int port = -1;
  private transient String path;
  private transient String query;
  private volatile transient String schemeSpecificPart;
  private volatile transient int hash;
  private volatile transient String decodedUserInfo;
  private volatile transient String decodedAuthority;
  private volatile transient String decodedPath;
  private volatile transient String decodedQuery;
  private volatile transient String decodedFragment;
  private volatile transient String decodedSchemeSpecificPart;
  private volatile String string;

  private URI() { }

  public URI(String str) throws URISyntaxException {
    new Parser(str).parse(false);
  }

  public URI(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
      throws URISyntaxException {
    String s = toString(scheme, null, null, userInfo, host, port, path, query, fragment);
    checkPath(s, scheme, path);
    new Parser(s).parse(true);
  }

  public URI(String scheme, String authority, String path, String query, String fragment)
      throws URISyntaxException {
    String s = toString(scheme, null, authority, null, null, -1, path, query, fragment);
    checkPath(s, scheme, path);
    new Parser(s).parse(false);
  }

  public URI(String scheme, String host, String path, String fragment) throws URISyntaxException {
    this(scheme, null, host, -1, path, null, fragment);
  }

  public URI(String scheme, String ssp, String fragment) throws URISyntaxException {
    new Parser(toString(scheme, ssp, null, null, null, -1, null, null, fragment)).parse(false);
  }

  public static URI create(String str) {
    try {
      return new URI(str);
    } catch (URISyntaxException x) {
      throw new IllegalArgumentException(x.getMessage(), x);
    }
  }

  public URI parseServerAuthority() throws URISyntaxException {
    if (host != null || authority == null) {
      return this;
    }
    defineString();
    new Parser(string).parse(true);
    return this;
  }

  public URI normalize() {
    return normalize(this);
  }

  public URI resolve(URI uri) {
    return resolve(this, uri);
  }

  public URI resolve(String str) {
    return resolve(URI.create(str));
  }

  public URI relativize(URI uri) {
    return relativize(this, uri);
  }

  public URL toURL() throws MalformedURLException {
    if (!isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute");
    }
    return new URL(toString());
  }

  public String getScheme() {
    return scheme;
  }

  public boolean isAbsolute() {
    return scheme != null;
  }

  public boolean isOpaque() {
    return path == null;
  }

  public String getRawSchemeSpecificPart() {
    defineSchemeSpecificPart();
    return schemeSpecificPart;
  }

  public String getSchemeSpecificPart() {
    if (decodedSchemeSpecificPart == null) {
      decodedSchemeSpecificPart = decode(getRawSchemeSpecificPart(), true);
    }
    return decodedSchemeSpecificPart;
  }

  public String getRawAuthority() {
    return authority;
  }

  public String getAuthority() {
    if (decodedAuthority == null) {
      decodedAuthority = decode(authority, true);
    }
    return decodedAuthority;
  }

  public String getRawUserInfo() {
    return userInfo;
  }

  public String getUserInfo() {
    if (decodedUserInfo == null && userInfo != null) {
      decodedUserInfo = decode(userInfo);
    }
    return decodedUserInfo;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getRawPath() {
    return path;
  }

  public String getPath() {
    if (decodedPath == null && path != null) {
      decodedPath = decode(path);
    }
    return decodedPath;
  }

  public String getRawQuery() {
    return query;
  }

  public String getQuery() {
    if (decodedQuery == null && query != null) {
      decodedQuery = decode(query);
    }
    return decodedQuery;
  }

  public String getRawFragment() {
    return fragment;
  }

  public String getFragment() {
    if (decodedFragment == null && fragment != null) {
      decodedFragment = decode(fragment);
    }
    return decodedFragment;
  }

  @Override
  public boolean equals(Object ob) {
    if (ob == this) {
      return true;
    }
    if (!(ob instanceof URI)) {
      return false;
    }
    URI that = (URI) ob;
    if (this.isOpaque() != that.isOpaque()) {
      return false;
    }
    if (!equalIgnoringCase(this.scheme, that.scheme)) {
      return false;
    }
    if (!equal(this.fragment, that.fragment)) {
      return false;
    }
    if (this.isOpaque()) {
      return equal(this.schemeSpecificPart, that.schemeSpecificPart);
    }
    if (!equal(this.path, that.path)) {
      return false;
    }
    if (!equal(this.query, that.query)) {
      return false;
    }
    if (this.authority == that.authority) {
      return true;
    }
    if (this.host != null) {
      if (!equal(this.userInfo, that.userInfo)) {
        return false;
      }
      if (!equalIgnoringCase(this.host, that.host)) {
        return false;
      }
      return this.port == that.port;
    } else if (this.authority != null) {
      return that.authority != null && this.authority.equals(that.authority);
    }
    return this.authority == that.authority;
  }

  @Override
  public int hashCode() {
    if (hash != 0) {
      return hash;
    }
    int h = hashIgnoringCase(0, scheme);
    h = hash(h, fragment);
    if (isOpaque()) {
      h = hash(h, schemeSpecificPart);
    } else {
      h = hash(h, path);
      h = hash(h, query);
      if (host != null) {
        h = hash(h, userInfo);
        h = hashIgnoringCase(h, host);
        h += 1949 * port;
      } else {
        h = hash(h, authority);
      }
    }
    hash = h;
    return h;
  }

  @Override
  public int compareTo(URI that) {
    int c;
    if ((c = compareIgnoringCase(this.scheme, that.scheme)) != 0) {
      return c;
    }
    if (this.isOpaque()) {
      if (that.isOpaque()) {
        if ((c = compare(this.schemeSpecificPart, that.schemeSpecificPart)) != 0) {
          return c;
        }
        return compare(this.fragment, that.fragment);
      }
      return +1;
    } else if (that.isOpaque()) {
      return -1;
    }
    if (this.host != null && that.host != null) {
      if ((c = compare(this.userInfo, that.userInfo)) != 0) {
        return c;
      }
      if ((c = compareIgnoringCase(this.host, that.host)) != 0) {
        return c;
      }
      if ((c = this.port - that.port) != 0) {
        return c;
      }
    } else if ((c = compare(this.authority, that.authority)) != 0) {
      return c;
    }
    if ((c = compare(this.path, that.path)) != 0) {
      return c;
    }
    if ((c = compare(this.query, that.query)) != 0) {
      return c;
    }
    return compare(this.fragment, that.fragment);
  }

  @Override
  public String toString() {
    defineString();
    return string;
  }

  public String toASCIIString() {
    defineString();
    return encode(string);
  }

  private void writeObject(ObjectOutputStream os) throws IOException {
    defineString();
    os.defaultWriteObject();
  }

  private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
    port = -1;
    is.defaultReadObject();
    try {
      new Parser(string).parse(false);
    } catch (URISyntaxException x) {
      IOException y = new InvalidObjectException("Invalid URI");
      y.initCause(x);
      throw y;
    }
  }

  private static boolean equal(String s, String t) {
    if (s == t) {
      return true;
    }
    if (s != null && t != null) {
      if (s.length() != t.length()) {
        return false;
      }
      if (s.indexOf('%') < 0) {
        return s.equals(t);
      }
      int n = s.length();
      for (int i = 0; i < n;) {
        char c = s.charAt(i);
        char d = t.charAt(i);
        if (c != '%') {
          if (c != d) {
            return false;
          }
          i++;
          continue;
        }
        if (d != '%') {
          return false;
        }
        i++;
        if (toLower(s.charAt(i)) != toLower(t.charAt(i))) {
          return false;
        }
        i++;
        if (toLower(s.charAt(i)) != toLower(t.charAt(i))) {
          return false;
        }
        i++;
      }
      return true;
    }
    return false;
  }

  private static boolean equalIgnoringCase(String s, String t) {
    if (s == t) {
      return true;
    }
    if (s != null && t != null) {
      int n = s.length();
      if (t.length() != n) {
        return false;
      }
      for (int i = 0; i < n; i++) {
        if (toLower(s.charAt(i)) != toLower(t.charAt(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static int hash(int hash, String s) {
    if (s == null) {
      return hash;
    }
    return s.indexOf('%') < 0 ? hash * 127 + s.hashCode() : normalizedHash(hash, s);
  }

  private static int normalizedHash(int hash, String s) {
    int h = 0;
    for (int index = 0; index < s.length(); index++) {
      char ch = s.charAt(index);
      h = 31 * h + ch;
      if (ch == '%') {
        for (int i = index + 1; i < index + 3; i++) {
          h = 31 * h + toUpper(s.charAt(i));
        }
        index += 2;
      }
    }
    return hash * 127 + h;
  }

  private static int hashIgnoringCase(int hash, String s) {
    if (s == null) {
      return hash;
    }
    int h = hash;
    int n = s.length();
    for (int i = 0; i < n; i++) {
      h = 31 * h + toLower(s.charAt(i));
    }
    return h;
  }

  private static int compare(String s, String t) {
    if (s == t) {
      return 0;
    }
    if (s != null) {
      if (t != null) {
        return upperEscapes(s).compareTo(upperEscapes(t));
      } else {
        return +1;
      }
    } else {
      return -1;
    }
  }

  private static String upperEscapes(String s) {
    if (s.indexOf('%') < 0) {
      return s;
    }
    char[] c = s.toCharArray();
    for (int i = 0; i + 2 < c.length + 0 && i < c.length; i++) {
      if (c[i] == '%' && i + 2 < c.length + 0) {
        c[i + 1] = (char) toUpper(c[i + 1]);
        c[i + 2] = (char) toUpper(c[i + 2]);
        i += 2;
      }
    }
    return new String(c);
  }

  private static int compareIgnoringCase(String s, String t) {
    if (s == t) {
      return 0;
    }
    if (s != null) {
      if (t != null) {
        int sn = s.length();
        int tn = t.length();
        int n = sn < tn ? sn : tn;
        for (int i = 0; i < n; i++) {
          int c = toLower(s.charAt(i)) - toLower(t.charAt(i));
          if (c != 0) {
            return c;
          }
        }
        return sn - tn;
      } else {
        return +1;
      }
    } else {
      return -1;
    }
  }

  private static int toLower(char c) {
    if (c >= 'A' && c <= 'Z') {
      return c + ('a' - 'A');
    }
    return c;
  }

  private static int toUpper(char c) {
    if (c >= 'a' && c <= 'z') {
      return c - ('a' - 'A');
    }
    return c;
  }

  private void appendAuthority(StringBuilder sb, String authority, String userInfo, String host, int port) {
    if (host != null) {
      sb.append("//");
      if (userInfo != null) {
        sb.append(quote(userInfo, kLUserinfo, kHUserinfo));
        sb.append('@');
      }
      boolean needBrackets = host.indexOf(':') >= 0 && !host.startsWith("[") && !host.endsWith("]");
      if (needBrackets) {
        sb.append('[');
      }
      sb.append(host);
      if (needBrackets) {
        sb.append(']');
      }
      if (port != -1) {
        sb.append(':');
        sb.append(port);
      }
    } else if (authority != null) {
      sb.append("//");
      if (authority.startsWith("[")) {
        int end = authority.indexOf(']');
        String doquote = authority;
        String dontquote = "";
        if (end != -1 && authority.indexOf(':') != -1) {
          if (end == authority.length()) {
            dontquote = authority;
            doquote = "";
          } else {
            dontquote = authority.substring(0, end + 1);
            doquote = authority.substring(end + 1);
          }
        }
        sb.append(dontquote);
        sb.append(quote(doquote, kLRegName | kLServer, kHRegName | kHServer));
      } else {
        sb.append(quote(authority, kLRegName | kLServer, kHRegName | kHServer));
      }
    }
  }

  private void appendSchemeSpecificPart(StringBuilder sb, String opaquePart, String authority, String userInfo,
      String host, int port, String path, String query) {
    if (opaquePart != null) {
      if (opaquePart.startsWith("//[")) {
        int end = opaquePart.indexOf(']');
        if (end != -1 && opaquePart.indexOf(':') != -1) {
          String doquote;
          String dontquote;
          if (end == opaquePart.length()) {
            dontquote = opaquePart;
            doquote = "";
          } else {
            dontquote = opaquePart.substring(0, end + 1);
            doquote = opaquePart.substring(end + 1);
          }
          sb.append(dontquote);
          sb.append(quote(doquote, kLUric, kHUric));
        }
      } else {
        sb.append(quote(opaquePart, kLUric, kHUric));
      }
    } else {
      appendAuthority(sb, authority, userInfo, host, port);
      if (path != null) {
        sb.append(quote(path, kLPath, kHPath));
      }
      if (query != null) {
        sb.append('?');
        sb.append(quote(query, kLUric, kHUric));
      }
    }
  }

  private void appendFragment(StringBuilder sb, String fragment) {
    if (fragment != null) {
      sb.append('#');
      sb.append(quote(fragment, kLUric, kHUric));
    }
  }

  private String toString(String scheme, String opaquePart, String authority, String userInfo, String host,
      int port, String path, String query, String fragment) {
    StringBuilder sb = new StringBuilder();
    if (scheme != null) {
      sb.append(scheme);
      sb.append(':');
    }
    appendSchemeSpecificPart(sb, opaquePart, authority, userInfo, host, port, path, query);
    appendFragment(sb, fragment);
    return sb.toString();
  }

  private void defineSchemeSpecificPart() {
    if (schemeSpecificPart != null) {
      return;
    }
    String part = authority == null ? "" : "//" + authority;
    if (path != null) {
      part += path;
    }
    if (query != null) {
      part += "?" + query;
    }
    schemeSpecificPart = part;
  }

  private void defineString() {
    if (string != null) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    if (scheme != null) {
      sb.append(scheme);
      sb.append(':');
    }
    if (isOpaque()) {
      sb.append(schemeSpecificPart);
    } else {
      if (host != null) {
        sb.append("//");
        if (userInfo != null) {
          sb.append(userInfo);
          sb.append('@');
        }
        boolean needBrackets = host.indexOf(':') >= 0 && !host.startsWith("[") && !host.endsWith("]");
        if (needBrackets) {
          sb.append('[');
        }
        sb.append(host);
        if (needBrackets) {
          sb.append(']');
        }
        if (port != -1) {
          sb.append(':');
          sb.append(port);
        }
      } else if (authority != null) {
        sb.append("//");
        sb.append(authority);
      }
      if (path != null) {
        sb.append(path);
      }
      if (query != null) {
        sb.append('?');
        sb.append(query);
      }
    }
    if (fragment != null) {
      sb.append('#');
      sb.append(fragment);
    }
    string = sb.toString();
  }

  private static void checkPath(String s, String scheme, String path) throws URISyntaxException {
    if (scheme != null) {
      if (path != null && !path.isEmpty() && path.charAt(0) != '/') {
        throw new URISyntaxException(s, "Relative path in absolute URI");
      }
    }
  }


  private static int needsNormalization(String path) {
    boolean normal = true;
    int ns = 0;
    int end = path.length() - 1;
    int p = 0;
    while (p <= end) {
      if (path.charAt(p) != '/') {
        break;
      }
      p++;
    }
    if (p > 1) {
      normal = false;
    }
    query:
    while (p <= end) {
      if (path.charAt(p) == '.' && (p == end || path.charAt(p + 1) == '/'
          || (path.charAt(p + 1) == '.' && (p + 1 == end || path.charAt(p + 2) == '/')))) {
        normal = false;
      }
      ns++;
      while (p <= end) {
        if (path.charAt(p++) != '/') {
          continue;
        }
        while (p <= end) {
          if (path.charAt(p) != '/') {
            break;
          }
          normal = false;
          p++;
        }
        continue query;
      }
    }
    return normal ? -1 : ns;
  }

  private static void split(char[] path, int[] segs) {
    int end = path.length - 1;
    int p = 0;
    int i = 0;
    while (p <= end) {
      if (path[p] != '/') {
        break;
      }
      path[p] = '\0';
      p++;
    }
    while (p <= end) {
      segs[i++] = p++;
      while (p <= end) {
        if (path[p++] != '/') {
          continue;
        }
        path[p - 1] = '\0';
        while (p <= end) {
          if (path[p] != '/') {
            break;
          }
          path[p++] = '\0';
        }
        break;
      }
    }
    if (i != segs.length) {
      throw new InternalError();
    }
  }

  private static int join(char[] path, int[] segs) {
    int ns = segs.length;
    int end = path.length - 1;
    int p = 0;
    if (path[p] == '\0') {
      path[p++] = '/';
    }
    for (int i = 0; i < ns; i++) {
      int q = segs[i];
      if (q == -1) {
        continue;
      }
      if (p == q) {
        while (p <= end && path[p] != '\0') {
          p++;
        }
        if (p <= end) {
          path[p++] = '/';
        }
      } else if (p < q) {
        while (q <= end && path[q] != '\0') {
          path[p++] = path[q++];
        }
        if (q <= end) {
          path[p++] = '/';
        }
      } else {
        throw new InternalError();
      }
    }
    return p;
  }

  private static void removeDots(char[] path, int[] segs) {
    int ns = segs.length;
    int end = path.length - 1;
    for (int i = 0; i < ns; i++) {
      int dots = 0;
      do {
        int p = segs[i];
        if (path[p] == '.') {
          if (p == end) {
            dots = 1;
            break;
          } else if (path[p + 1] == '\0') {
            dots = 1;
            break;
          } else if (path[p + 1] == '.' && (p + 1 == end || path[p + 2] == '\0')) {
            dots = 2;
            break;
          }
        }
        i++;
      } while (i < ns);
      if (i > ns || dots == 0) {
        break;
      }
      if (dots == 1) {
        segs[i] = -1;
      } else {
        int j;
        for (j = i - 1; j >= 0; j--) {
          if (segs[j] != -1) {
            break;
          }
        }
        if (j >= 0) {
          int q = segs[j];
          if (!(path[q] == '.' && path[q + 1] == '.' && path[q + 2] == '\0')) {
            segs[i] = -1;
            segs[j] = -1;
          }
        }
      }
    }
  }

  private static void maybeAddLeadingDot(char[] path, int[] segs) {
    if (path[0] == '\0') {
      return;
    }
    int ns = segs.length;
    int f = 0;
    while (f < ns) {
      if (segs[f] >= 0) {
        break;
      }
      f++;
    }
    if (f >= ns || f == 0) {
      return;
    }
    int p = segs[f];
    while (p < path.length && path[p] != ':' && path[p] != '\0') {
      p++;
    }
    if (p >= path.length || path[p] == '\0') {
      return;
    }
    path[0] = '.';
    path[1] = '\0';
    segs[0] = 0;
  }

  private static String normalize(String ps) {
    int ns = needsNormalization(ps);
    if (ns < 0) {
      return ps;
    }
    char[] path = ps.toCharArray();
    int[] segs = new int[ns];
    split(path, segs);
    removeDots(path, segs);
    maybeAddLeadingDot(path, segs);
    String s = new String(path, 0, join(path, segs));
    if (s.equals(ps)) {
      return ps;
    }
    return s;
  }

  private static URI normalize(URI u) {
    if (u.isOpaque() || u.path == null || u.path.isEmpty()) {
      return u;
    }
    String np = normalize(u.path);
    if (np == u.path) {
      return u;
    }
    URI v = new URI();
    v.scheme = u.scheme;
    v.fragment = u.fragment;
    v.authority = u.authority;
    v.userInfo = u.userInfo;
    v.host = u.host;
    v.port = u.port;
    v.path = np;
    v.query = u.query;
    return v;
  }

  private static String resolvePath(String base, String child, boolean absolute) {
    int i = base.lastIndexOf('/');
    int cn = child.length();
    String path = "";
    if (cn == 0) {
      if (i >= 0) {
        path = base.substring(0, i + 1);
      }
    } else {
      if (i >= 0 || !absolute) {
        path = base.substring(0, i + 1).concat(child);
      } else {
        path = "/".concat(child);
      }
    }
    return normalize(path);
  }

  private static URI resolve(URI base, URI child) {
    if (child.isOpaque() || base.isOpaque()) {
      return child;
    }
    if (child.scheme == null && child.authority == null && child.path.isEmpty() && child.fragment != null
        && child.query == null) {
      if (base.fragment != null && child.fragment.equals(base.fragment)) {
        return base;
      }
      URI ru = new URI();
      ru.scheme = base.scheme;
      ru.authority = base.authority;
      ru.userInfo = base.userInfo;
      ru.host = base.host;
      ru.port = base.port;
      ru.path = base.path;
      ru.fragment = child.fragment;
      ru.query = base.query;
      return ru;
    }
    if (child.scheme != null) {
      return child;
    }
    URI ru = new URI();
    ru.scheme = base.scheme;
    ru.query = child.query;
    ru.fragment = child.fragment;
    if (child.authority == null) {
      ru.authority = base.authority;
      ru.host = base.host;
      ru.userInfo = base.userInfo;
      ru.port = base.port;
      String cp = child.path == null ? "" : child.path;
      if (!cp.isEmpty() && cp.charAt(0) == '/') {
        ru.path = child.path;
      } else {
        ru.path = resolvePath(base.path, cp, base.isAbsolute());
      }
    } else {
      ru.authority = child.authority;
      ru.host = child.host;
      ru.userInfo = child.userInfo;
      ru.host = child.host;
      ru.port = child.port;
      ru.path = child.path;
    }
    return ru;
  }

  private static URI relativize(URI base, URI child) {
    if (child.isOpaque() || base.isOpaque()) {
      return child;
    }
    if (!equalIgnoringCase(base.scheme, child.scheme) || !equal(base.authority, child.authority)) {
      return child;
    }
    String bp = normalize(base.path);
    String cp = normalize(child.path);
    if (!bp.equals(cp)) {
      if (!bp.endsWith("/")) {
        bp = bp + "/";
      }
      if (!cp.startsWith(bp)) {
        return child;
      }
    }
    URI v = new URI();
    v.path = cp.substring(bp.length());
    v.query = child.query;
    v.fragment = child.fragment;
    return v;
  }


  private static final String kLowerHex = "0123456789abcdef";

  private static String quote(String s, long lowMask, long highMask) {
    int n = s.length();
    StringBuilder sb = null;
    boolean allowNonASCII = (lowMask & kLEscaped) != 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < '\u0080') {
        if (!match(c, lowMask, highMask)) {
          if (sb == null) {
            sb = new StringBuilder();
            sb.append(s, 0, i);
          }
          appendEscape(sb, (byte) c);
        } else if (sb != null) {
          sb.append(c);
        }
      } else if (allowNonASCII && (Character.isSpaceChar(c) || Character.isISOControl(c))) {
        if (sb == null) {
          sb = new StringBuilder();
          sb.append(s, 0, i);
        }
        appendEncoded(sb, c);
      } else if (sb != null) {
        sb.append(c);
      }
    }
    return sb == null ? s : sb.toString();
  }

  private static void appendEscape(StringBuilder sb, byte b) {
    sb.append('%');
    sb.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0x0f, 16)));
    sb.append(Character.toUpperCase(Character.forDigit(b & 0x0f, 16)));
  }

  private static void appendEncoded(StringBuilder sb, char c) {
    ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap("" + c));
    while (bb.hasRemaining()) {
      int b = bb.get() & 0xff;
      if (b >= 0x80) {
        appendEscape(sb, (byte) b);
      } else {
        sb.append((char) b);
      }
    }
  }

  private static String encode(String s) {
    int n = s.length();
    if (n == 0) {
      return s;
    }
    for (int i = 0;;) {
      if (s.charAt(i) >= '\u0080') {
        break;
      }
      if (++i >= n) {
        return s;
      }
    }
    String ns = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFC);
    ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(ns));
    StringBuilder sb = new StringBuilder();
    while (bb.hasRemaining()) {
      int b = bb.get() & 0xff;
      if (b >= 0x80) {
        appendEscape(sb, (byte) b);
      } else {
        sb.append((char) b);
      }
    }
    return sb.toString();
  }

  private static int decode(char c) {
    if (c >= '0' && c <= '9') {
      return c - '0';
    }
    if (c >= 'a' && c <= 'f') {
      return c - 'a' + 10;
    }
    if (c >= 'A' && c <= 'F') {
      return c - 'A' + 10;
    }
    return -1;
  }

  private static byte decode(char c1, char c2) {
    return (byte) (((decode(c1) & 0xf) << 4) | ((decode(c2) & 0xf) << 0));
  }

  private static String decode(String s) {
    return decode(s, false);
  }

  private static String decode(String s, boolean ignorePercentInBrackets) {
    if (s == null) {
      return s;
    }
    int n = s.length();
    if (n == 0) {
      return s;
    }
    if (s.indexOf('%') < 0) {
      return s;
    }
    StringBuilder sb = new StringBuilder(n);
    ByteBuffer bb = ByteBuffer.allocate(n);
    CharBuffer cb = CharBuffer.allocate(n);
    CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    char c = s.charAt(0);
    boolean betweenBrackets = false;
    for (int i = 0; i < n;) {
      if (c == '[') {
        betweenBrackets = true;
      } else if (betweenBrackets && c == ']') {
        betweenBrackets = false;
      }
      if (c != '%' || (betweenBrackets && ignorePercentInBrackets)) {
        sb.append(c);
        if (++i >= n) {
          break;
        }
        c = s.charAt(i);
        continue;
      }
      bb.clear();
      int ui = i;
      for (;;) {
        bb.put(decode(s.charAt(i + 1), s.charAt(i + 2)));
        i += 3;
        if (i >= n) {
          break;
        }
        c = s.charAt(i);
        if (c != '%') {
          break;
        }
      }
      bb.flip();
      cb.clear();
      dec.reset();
      CoderResult cr = dec.decode(bb, cb, true);
      cr = dec.flush(cb);
      sb.append(cb.flip().toString());
    }
    return sb.toString();
  }


  private static long lowMask(String chars) {
    int n = chars.length();
    long m = 0;
    for (int i = 0; i < n; i++) {
      char c = chars.charAt(i);
      if (c < 64) {
        m |= (1L << c);
      }
    }
    return m;
  }

  private static long highMask(String chars) {
    int n = chars.length();
    long m = 0;
    for (int i = 0; i < n; i++) {
      char c = chars.charAt(i);
      if (c >= 64 && c < 128) {
        m |= (1L << (c - 64));
      }
    }
    return m;
  }

  private static long lowMask(char first, char last) {
    long m = 0;
    int f = Math.max(Math.min(first, 63), 0);
    int l = Math.max(Math.min(last, 63), 0);
    for (int i = f; i <= l; i++) {
      m |= 1L << i;
    }
    return m;
  }

  private static long highMask(char first, char last) {
    long m = 0;
    int f = Math.max(Math.min(first, 127), 64) - 64;
    int l = Math.max(Math.min(last, 127), 64) - 64;
    for (int i = f; i <= l; i++) {
      m |= 1L << i;
    }
    return m;
  }

  private static boolean match(char c, long lowMask, long highMask) {
    if (c == 0) {
      return false;
    }
    if (c < 64) {
      return ((1L << c) & lowMask) != 0;
    }
    if (c < 128) {
      return ((1L << (c - 64)) & highMask) != 0;
    }
    return false;
  }

  private static final long kLEscaped = 1L;
  private static final long kLDigit = lowMask('0', '9');
  private static final long kHDigit = 0L;
  private static final long kLUpalpha = 0L;
  private static final long kHUpalpha = highMask('A', 'Z');
  private static final long kLLowalpha = 0L;
  private static final long kHLowalpha = highMask('a', 'z');
  private static final long kLAlpha = kLLowalpha | kLUpalpha;
  private static final long kHAlpha = kHLowalpha | kHUpalpha;
  private static final long kLAlphanum = kLAlpha | kLDigit;
  private static final long kHAlphanum = kHAlpha | kHDigit;
  private static final long kLHex = kLDigit;
  private static final long kHHex = highMask('A', 'F') | highMask('a', 'f');
  private static final long kLMark = lowMask("-_.!~*'()");
  private static final long kHMark = highMask("-_.!~*'()");
  private static final long kLUnreserved = kLAlphanum | kLMark;
  private static final long kHUnreserved = kHAlphanum | kHMark;
  private static final long kLReserved = lowMask(";/?:@&=+$,[]");
  private static final long kHReserved = highMask(";/?:@&=+$,[]");
  private static final long kLUric = kLReserved | kLUnreserved | kLEscaped;
  private static final long kHUric = kHReserved | kHUnreserved;
  private static final long kLPchar = kLUnreserved | kLEscaped | lowMask(":@&=+$,");
  private static final long kHPchar = kHUnreserved | highMask(":@&=+$,");
  private static final long kLPath = kLPchar | lowMask(";/");
  private static final long kHPath = kHPchar | highMask(";/");
  private static final long kLDash = lowMask("-");
  private static final long kHDash = highMask("-");
  private static final long kLDot = lowMask(".");
  private static final long kHDot = highMask(".");
  private static final long kLUserinfo = kLUnreserved | kLEscaped | lowMask(";:&=+$,");
  private static final long kHUserinfo = kHUnreserved | highMask(";:&=+$,");
  private static final long kLScheme = kLAlpha | kLDigit | lowMask("+-.");
  private static final long kHScheme = kHAlpha | kHDigit | highMask("+-.");
  private static final long kLRegName = kLUnreserved | kLEscaped | lowMask("$,;:@&=+");
  private static final long kHRegName = kHUnreserved | highMask("$,;:@&=+");
  private static final long kLServer = kLUserinfo | kLAlphanum | kLDash | lowMask(".:@[]");
  private static final long kHServer = kHUserinfo | kHAlphanum | kHDash | highMask(".:@[]");
  private static final long kLServerPercent = kLServer | lowMask("%");
  private static final long kHServerPercent = kHServer | highMask("%");


  private final class Parser {
    private final String input;
    private boolean requireServerAuthority = false;

    Parser(String s) {
      input = s;
      string = s;
    }

    private void fail(String reason) throws URISyntaxException {
      throw new URISyntaxException(input, reason);
    }

    private void fail(String reason, int p) throws URISyntaxException {
      throw new URISyntaxException(input, reason, p);
    }

    private void failExpecting(String expected, int p) throws URISyntaxException {
      fail("Expected " + expected, p);
    }

    private void failExpecting(String expected, String prior, int p) throws URISyntaxException {
      fail("Expected " + expected + " following " + prior, p);
    }

    private String substring(int start, int end) {
      return input.substring(start, end);
    }

    private char charAt(int p) {
      return input.charAt(p);
    }

    private boolean at(int start, int end, char c) {
      return (start < end) && (charAt(start) == c);
    }

    private boolean at(int start, int end, String s) {
      int p = start;
      int sn = s.length();
      if (sn > end - p) {
        return false;
      }
      int i = 0;
      while (i < sn) {
        if (charAt(p++) != s.charAt(i)) {
          break;
        }
        i++;
      }
      return i == sn;
    }

    private int scan(int start, int end, char c) {
      if (start < end && charAt(start) == c) {
        return start + 1;
      }
      return start;
    }

    private int scan(int start, int end, String err, String stop) {
      int p = start;
      while (p < end) {
        char c = charAt(p);
        if (err.indexOf(c) >= 0) {
          return -1;
        }
        if (stop.indexOf(c) >= 0) {
          break;
        }
        p++;
      }
      return p;
    }

    private int scan(int start, int end, String stop) {
      int p = start;
      while (p < end) {
        char c = charAt(p);
        if (stop.indexOf(c) >= 0) {
          break;
        }
        p++;
      }
      return p;
    }

    private int scanEscape(int start, int n, char first) throws URISyntaxException {
      int p = start;
      char c = first;
      if (c == '%') {
        if (p + 3 <= n && match(charAt(p + 1), kLHex, kHHex) && match(charAt(p + 2), kLHex, kHHex)) {
          return p + 3;
        }
        fail("Malformed escape pair", p);
      } else if (c > 128 && !Character.isSpaceChar(c) && !Character.isISOControl(c)) {
        return p + 1;
      }
      return p;
    }

    private int scan(int start, int n, long lowMask, long highMask) throws URISyntaxException {
      int p = start;
      while (p < n) {
        char c = charAt(p);
        if (match(c, lowMask, highMask)) {
          p++;
          continue;
        }
        if ((lowMask & kLEscaped) != 0) {
          int q = scanEscape(p, n, c);
          if (q > p) {
            p = q;
            continue;
          }
        }
        break;
      }
      return p;
    }

    private void checkChars(int start, int end, long lowMask, long highMask, String what)
        throws URISyntaxException {
      int p = scan(start, end, lowMask, highMask);
      if (p < end) {
        fail("Illegal character in " + what, p);
      }
    }

    private void checkChar(int p, long lowMask, long highMask, String what) throws URISyntaxException {
      checkChars(p, p + 1, lowMask, highMask, what);
    }

    void parse(boolean rsa) throws URISyntaxException {
      requireServerAuthority = rsa;
      int n = input.length();
      int p = scan(0, n, "/?#", ":");
      if (p >= 0 && at(p, n, ':')) {
        if (p == 0) {
          failExpecting("scheme name", 0);
        }
        checkChar(0, kLAlpha, kHAlpha, "scheme name");
        checkChars(1, p, kLScheme, kHScheme, "scheme name");
        scheme = substring(0, p);
        p++;
        int sspStart = p;
        if (at(p, n, '/')) {
          p = parseHierarchical(p, n);
        } else {
          int q = scan(p, n, "#");
          if (q <= p) {
            failExpecting("scheme-specific part", p);
          }
          checkChars(p, q, kLUric, kHUric, "opaque part");
          p = q;
        }
        schemeSpecificPart = substring(sspStart, p);
      } else {
        p = parseHierarchical(0, n);
        schemeSpecificPart = substring(0, p);
      }
      if (at(p, n, '#')) {
        checkChars(p + 1, n, kLUric, kHUric, "fragment");
        fragment = substring(p + 1, n);
        p = n;
      }
      if (p < n) {
        fail("end of URI", p);
      }
    }

    private int parseHierarchical(int start, int n) throws URISyntaxException {
      int p = start;
      if (at(p, n, '/') && at(p + 1, n, '/')) {
        p += 2;
        int q = scan(p, n, "/?#");
        if (q > p) {
          p = parseAuthority(p, q);
        } else if (q < n) {
          // empty authority before a path, query or fragment
        } else {
          failExpecting("authority", p);
        }
      }
      int q = scan(p, n, "?#");
      checkChars(p, q, kLPath, kHPath, "path");
      path = substring(p, q);
      p = q;
      if (at(p, n, '?')) {
        p++;
        q = scan(p, n, "#");
        checkChars(p, q, kLUric, kHUric, "query");
        query = substring(p, q);
        p = q;
      }
      return p;
    }

    private int parseAuthority(int start, int n) throws URISyntaxException {
      int p = start;
      int q = p;
      URISyntaxException ex = null;
      boolean serverChars;
      boolean regChars;
      if (scan(p, n, "]") > p) {
        q = scan(p, n, kLServerPercent, kHServerPercent);
      } else {
        q = scan(p, n, kLServer, kHServer);
      }
      serverChars = (q == n);
      regChars = (scan(p, n, kLRegName, kHRegName) == n);
      q = Math.max(q, scan(p, n, kLRegName, kHRegName));
      if (regChars && !serverChars) {
        authority = substring(p, n);
        return n;
      }
      if (serverChars) {
        try {
          q = parseServer(p, n);
          if (q < n) {
            failExpecting("end of authority", q);
          }
          authority = substring(p, n);
        } catch (URISyntaxException x) {
          userInfo = null;
          host = null;
          port = -1;
          if (requireServerAuthority) {
            throw x;
          } else {
            ex = x;
            q = p;
          }
        }
      }
      if (q < n) {
        if (regChars) {
          authority = substring(p, n);
        } else if (ex != null) {
          throw ex;
        } else {
          fail("Illegal character in authority", q);
        }
      }
      return n;
    }

    private int parseServer(int start, int n) throws URISyntaxException {
      int p = start;
      int q;
      q = scan(p, n, "/?#", "@");
      if (q >= p && at(q, n, '@')) {
        checkChars(p, q, kLUserinfo, kHUserinfo, "user info");
        userInfo = substring(p, q);
        p = q + 1;
      }
      if (at(p, n, '[')) {
        p++;
        q = scan(p, n, "/?#", "]");
        if (q > p && at(q, n, ']')) {
          int r = scan(p, q, "%");
          if (r > p) {
            parseIPv6Reference(p, r);
            if (r + 1 == q) {
              fail("scope id expected");
            }
            checkChars(r + 1, q, kLAlphanum, kHAlphanum, "scope id");
          } else {
            parseIPv6Reference(p, q);
          }
          host = substring(p - 1, q + 1);
          p = q + 1;
        } else {
          failExpecting("closing bracket for IPv6 address", q);
        }
      } else {
        q = parseIPv4Address(p, n);
        if (q <= p) {
          q = parseHostname(p, n);
        }
        p = q;
      }
      if (at(p, n, ':')) {
        p++;
        q = scan(p, n, "/");
        if (q > p) {
          checkChars(p, q, kLDigit, kHDigit, "port number");
          try {
            port = Integer.parseInt(input.substring(p, q));
          } catch (NumberFormatException x) {
            fail("Malformed port number", p);
          }
          p = q;
        }
      }
      if (p < n) {
        failExpecting("port number", p);
      }
      return p;
    }

    private int scanByte(int start, int n) throws URISyntaxException {
      int p = start;
      int q = scan(p, n, kLDigit, kHDigit);
      if (q <= p) {
        return q;
      }
      if (Integer.parseInt(input.substring(p, q)) > 255) {
        return p;
      }
      return q;
    }

    private int scanIPv4Address(int start, int n, boolean strict) throws URISyntaxException {
      int p = start;
      int q;
      int m = scan(p, n, kLDigit | lowMask("."), kHDigit | highMask("."));
      if (m <= p || (strict && m != n)) {
        return -1;
      }
      for (;;) {
        if ((q = scanByte(p, m)) <= p) {
          break;
        }
        p = q;
        if ((q = scan(p, m, '.')) <= p) {
          break;
        }
        p = q;
        if ((q = scanByte(p, m)) <= p) {
          break;
        }
        p = q;
        if ((q = scan(p, m, '.')) <= p) {
          break;
        }
        p = q;
        if ((q = scanByte(p, m)) <= p) {
          break;
        }
        p = q;
        if ((q = scan(p, m, '.')) <= p) {
          break;
        }
        p = q;
        if ((q = scanByte(p, m)) <= p) {
          break;
        }
        p = q;
        if (q < m) {
          break;
        }
        return q;
      }
      fail("Malformed IPv4 address", q);
      return -1;
    }

    private int takeIPv4Address(int start, int n, String expected) throws URISyntaxException {
      int p = scanIPv4Address(start, n, true);
      if (p <= start) {
        failExpecting(expected, start);
      }
      return p;
    }

    private int parseIPv4Address(int start, int n) {
      int p;
      try {
        p = scanIPv4Address(start, n, false);
      } catch (URISyntaxException x) {
        return -1;
      } catch (NumberFormatException nfe) {
        return -1;
      }
      if (p > start && p < n) {
        if (charAt(p) != ':') {
          p = -1;
        }
      }
      if (p > start) {
        host = substring(start, p);
      }
      return p;
    }

    private int parseHostname(int start, int n) throws URISyntaxException {
      int p = start;
      int q;
      int l = -1;
      do {
        q = scan(p, n, kLAlphanum, kHAlphanum);
        if (q <= p) {
          break;
        }
        l = p;
        if (q > p) {
          p = q;
          q = scan(p, n, kLAlphanum | kLDash, kHAlphanum | kHDash);
          if (q > p) {
            if (charAt(q - 1) == '-') {
              fail("Illegal character in hostname", q - 1);
            }
            p = q;
          }
        }
        q = scan(p, n, '.');
        if (q <= p) {
          break;
        }
        p = q;
      } while (p < n);
      if (p < n && !at(p, n, ':')) {
        fail("Illegal character in hostname", p);
      }
      if (l < 0) {
        failExpecting("hostname", start);
      }
      if (l > start && !match(charAt(l), kLAlpha, kHAlpha)) {
        fail("Illegal character in hostname", l);
      }
      host = substring(start, p);
      return p;
    }

    private int parseIPv6Reference(int start, int n) throws URISyntaxException {
      int p = start;
      int q;
      boolean compressedZeros = false;
      q = scanHexSeq(p, n);
      if (q > p) {
        p = q;
        if (at(p, n, "::")) {
          compressedZeros = true;
          p = scanHexPost(p + 2, n);
        } else if (at(p, n, ':')) {
          p = takeIPv4Address(p + 1, n, "IPv4 address");
          ipv6byteCount += 4;
        }
      } else if (at(p, n, "::")) {
        compressedZeros = true;
        p = scanHexPost(p + 2, n);
      }
      if (p < n) {
        fail("Malformed IPv6 address", start);
      }
      if (ipv6byteCount > 16) {
        fail("IPv6 address too long", start);
      }
      if (!compressedZeros && ipv6byteCount < 16) {
        fail("IPv6 address too short", start);
      }
      if (compressedZeros && ipv6byteCount == 16) {
        fail("Malformed IPv6 address", start);
      }
      return p;
    }

    private int ipv6byteCount = 0;

    private int scanHexPost(int start, int n) throws URISyntaxException {
      int p = start;
      int q;
      if (p == n) {
        return p;
      }
      q = scanHexSeq(p, n);
      if (q > p) {
        p = q;
        if (at(p, n, ':')) {
          p++;
          p = takeIPv4Address(p, n, "hex digits or IPv4 address");
          ipv6byteCount += 4;
        }
      } else {
        p = takeIPv4Address(p, n, "hex digits or IPv4 address");
        ipv6byteCount += 4;
      }
      return p;
    }

    private int scanHexSeq(int start, int n) throws URISyntaxException {
      int p = start;
      int q;
      q = scan(p, n, kLHex, kHHex);
      if (q <= p) {
        return -1;
      }
      if (at(q, n, '.')) {
        return -1;
      }
      if (q > p + 4) {
        fail("IPv6 hexadecimal digit sequence too long", p);
      }
      ipv6byteCount += 2;
      p = q;
      while (p < n) {
        if (!at(p, n, ':')) {
          break;
        }
        if (at(p + 1, n, ':')) {
          break;
        }
        p++;
        q = scan(p, n, kLHex, kHHex);
        if (q <= p) {
          failExpecting("digits for an IPv6 address", p);
        }
        if (at(q, n, '.')) {
          p--;
          break;
        }
        if (q > p + 4) {
          fail("IPv6 hexadecimal digit sequence too long", p);
        }
        ipv6byteCount += 2;
        p = q;
      }
      return p;
    }
  }
}
