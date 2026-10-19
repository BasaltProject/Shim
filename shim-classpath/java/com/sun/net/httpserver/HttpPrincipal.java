/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.security.Principal;
import java.util.Objects;

public class HttpPrincipal implements Principal {
  private final String username;
  private final String realm;

  public HttpPrincipal(String username, String realm) {
    this.username = Objects.requireNonNull(username);
    this.realm = Objects.requireNonNull(realm);
  }

  @Override
  public boolean equals(Object another) {
    return another instanceof HttpPrincipal && toString().equals(another.toString());
  }

  @Override
  public String getName() {
    return realm + ":" + username;
  }

  public String getUsername() {
    return username;
  }

  public String getRealm() {
    return realm;
  }

  @Override
  public int hashCode() {
    return (username + realm).hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }
}
