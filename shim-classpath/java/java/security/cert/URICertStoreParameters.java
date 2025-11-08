/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.net.URI;
import java.util.Objects;

public final class URICertStoreParameters implements CertStoreParameters {
  private final URI uri;
  private final int myhash;

  public URICertStoreParameters(URI uri) {
    if (uri == null) {
      throw new NullPointerException();
    }
    this.uri = uri;
    this.myhash = 7 * uri.hashCode();
  }

  public URI getURI() {
    return uri;
  }

  @Override
  public URICertStoreParameters clone() {
    return this;
  }

  @Override
  public int hashCode() {
    return myhash;
  }

  @Override
  public boolean equals(Object p) {
    if (p == this) {
      return true;
    }
    if (!(p instanceof URICertStoreParameters)) {
      return false;
    }
    return uri.equals(((URICertStoreParameters) p).uri);
  }

  @Override
  public String toString() {
    return "URICertStoreParameters: " + uri.toString();
  }
}
