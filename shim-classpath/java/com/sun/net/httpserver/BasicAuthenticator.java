/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public abstract class BasicAuthenticator extends Authenticator {
  protected final String realm;
  private final Charset charset;
  private final boolean isUtf8;

  public BasicAuthenticator(String realm) {
    this(realm, StandardCharsets.UTF_8);
  }

  public BasicAuthenticator(String realm, Charset charset) {
    Objects.requireNonNull(charset);
    this.realm = Objects.requireNonNull(realm);
    this.charset = charset;
    this.isUtf8 = charset.equals(StandardCharsets.UTF_8);
  }

  public String getRealm() {
    return realm;
  }

  @Override
  public Result authenticate(HttpExchange t) {
    Headers rmap = t.getRequestHeaders();
    String auth = rmap.getFirst("Authorization");
    if (auth == null) {
      setAuthHeader(t);
      return new Authenticator.Retry(401);
    }
    int sp = auth.indexOf(' ');
    if (sp == -1 || !auth.substring(0, sp).equals("Basic")) {
      return new Authenticator.Failure(401);
    }
    byte[] b = Base64.getDecoder().decode(auth.substring(sp + 1));
    String userpass = new String(b, charset);
    int colon = userpass.indexOf(':');
    String uname = userpass.substring(0, colon);
    String pass = userpass.substring(colon + 1);
    if (checkCredentials(uname, pass)) {
      return new Authenticator.Success(new HttpPrincipal(uname, realm));
    }
    setAuthHeader(t);
    return new Authenticator.Failure(401);
  }

  private void setAuthHeader(HttpExchange t) {
    Headers map = t.getResponseHeaders();
    String charsetParam = isUtf8 ? ", charset=\"UTF-8\"" : "";
    map.set("WWW-Authenticate", "Basic realm=" + "\"" + realm + "\"" + charsetParam);
  }

  public abstract boolean checkCredentials(String username, String password);
}
