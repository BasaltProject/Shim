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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import sun.net.www.http.HeaderParser;

final class BasicAuthentication extends AuthenticationInfo {
  private static final long serialVersionUID = 100L;

  private final String auth;

  BasicAuthentication(boolean isProxy, URL url, String realm, PasswordAuthentication pw, String charsetName) {
    super(isProxy ? kProxyAuthentication : kServerAuthentication, "Basic", url, realm);
    this.auth = encode(pw, charsetName);
  }

  BasicAuthentication(boolean isProxy, String host, int port, String realm, PasswordAuthentication pw,
      String charsetName) {
    super(isProxy ? kProxyAuthentication : kServerAuthentication, "Basic", host, port, realm);
    this.auth = encode(pw, charsetName);
  }

  private static String encode(PasswordAuthentication pw, String charsetName) {
    Charset cs = StandardCharsets.ISO_8859_1;
    if (charsetName != null && charsetName.equalsIgnoreCase("UTF-8")) {
      cs = StandardCharsets.UTF_8;
    }
    char[] password = pw.getPassword();
    String userpass = pw.getUserName() + ":" + new String(password);
    return "Basic " + Base64.getEncoder().encodeToString(userpass.getBytes(cs));
  }

  @Override
  public boolean supportsPreemptiveAuthorization() {
    return true;
  }

  @Override
  public String getHeaderValue(URL url, String method) {
    return auth;
  }

  @Override
  public boolean isAuthorizationStale(String header) {
    return false;
  }

  @Override
  public boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
    conn.setAuthenticationProperty(getHeaderName(), auth);
    return true;
  }

  static String getRootPath(String npath, String opath) {
    int index = 0;
    int toindex;
    try {
      npath = new java.net.URI(npath).normalize().getPath();
      opath = new java.net.URI(opath).normalize().getPath();
    } catch (java.net.URISyntaxException e) {
      // use the paths unchanged
    }
    while (index < opath.length()) {
      toindex = opath.indexOf('/', index + 1);
      if (toindex != -1 && opath.regionMatches(0, npath, 0, toindex + 1)) {
        index = toindex;
      } else {
        return opath.substring(0, index + 1);
      }
    }
    return npath;
  }
}
