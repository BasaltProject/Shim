/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.http;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import sun.net.www.http.HeaderParser;

final class DigestAuthentication extends AuthenticationInfo {
  private final PasswordAuthentication pw;
  private String algorithm;
  private String nonce;
  private String opaque;
  private String qop;
  private String cnonce;
  private int nonceCount;
  private final boolean userhash;

  DigestAuthentication(boolean isProxy, URL url, String realm, PasswordAuthentication pw, HeaderParser p) {
    super(isProxy ? kProxyAuthentication : kServerAuthentication, "Digest", url, realm);
    this.pw = pw;
    this.userhash = false;
    update(p);
  }

  DigestAuthentication(boolean isProxy, String host, int port, String realm, PasswordAuthentication pw,
      HeaderParser p) {
    super(isProxy ? kProxyAuthentication : kServerAuthentication, "Digest", host, port, realm);
    this.pw = pw;
    this.userhash = false;
    update(p);
  }

  private void update(HeaderParser p) {
    algorithm = p.findValue("algorithm", "MD5");
    nonce = p.findValue("nonce");
    opaque = p.findValue("opaque");
    String q = p.findValue("qop");
    qop = null;
    if (q != null) {
      for (String s : q.split(",")) {
        if (s.trim().equalsIgnoreCase("auth")) {
          qop = "auth";
        }
      }
    }
    nonceCount = 0;
    byte[] c = new byte[16];
    new SecureRandom().nextBytes(c);
    cnonce = HexFormat.of().formatHex(c);
  }

  @Override
  public boolean supportsPreemptiveAuthorization() {
    return true;
  }

  @Override
  public boolean isAuthorizationStale(String header) {
    HeaderParser p = new HeaderParser(header.substring(Math.min(header.length(), header.indexOf(' ') + 1)));
    String s = p.findValue("stale");
    if (s == null || !s.equalsIgnoreCase("true")) {
      return false;
    }
    String newNonce = p.findValue("nonce");
    if (newNonce == null || newNonce.isEmpty()) {
      return false;
    }
    nonce = newNonce;
    nonceCount = 0;
    return true;
  }

  @Override
  public boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
    update(p);
    conn.setAuthenticationProperty(getHeaderName(), getHeaderValue(conn.getURL(), conn.getMethod()));
    return true;
  }

  private String hash(String s) {
    String name = algorithm.equalsIgnoreCase("SHA-256") || algorithm.equalsIgnoreCase("SHA-256-sess") ? "SHA-256"
        : algorithm.equalsIgnoreCase("SHA-512-256") ? "SHA-512/256" : "MD5";
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance(name).digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public synchronized String getHeaderValue(URL url, String method) {
    String uri = url.getFile();
    if (uri == null || uri.isEmpty()) {
      uri = "/";
    }
    String user = pw.getUserName();
    String ha1 = hash(user + ":" + realm + ":" + new String(pw.getPassword()));
    if (algorithm.toLowerCase().endsWith("-sess")) {
      ha1 = hash(ha1 + ":" + nonce + ":" + cnonce);
    }
    String ha2 = hash(method + ":" + uri);
    StringBuilder sb = new StringBuilder("Digest username=\"").append(user).append("\", realm=\"").append(realm)
        .append("\", nonce=\"").append(nonce).append("\", uri=\"").append(uri).append("\"");
    String response;
    if (qop != null) {
      nonceCount++;
      String nc = String.format("%08x", nonceCount);
      response = hash(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);
      sb.append(", nc=").append(nc).append(", cnonce=\"").append(cnonce).append("\", qop=").append(qop);
    } else {
      response = hash(ha1 + ":" + nonce + ":" + ha2);
    }
    sb.append(", response=\"").append(response).append("\"");
    if (opaque != null) {
      sb.append(", opaque=\"").append(opaque).append("\"");
    }
    if (!algorithm.equalsIgnoreCase("MD5")) {
      sb.append(", algorithm=").append(algorithm);
    }
    return sb.toString();
  }
}
