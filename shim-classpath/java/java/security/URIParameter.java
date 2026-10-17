/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.net.URI;

public class URIParameter implements Policy.Parameters, javax.security.auth.login.Configuration.Parameters {
  private final URI uri;

  public URIParameter(URI uri) {
    if (uri == null) {
      throw new NullPointerException("invalid null URI");
    }
    this.uri = uri;
  }

  public URI getURI() {
    return uri;
  }
}
