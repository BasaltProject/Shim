/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;
import java.net.URI;

public abstract class URISyntax implements Serializable, Cloneable {
  private final URI uri;

  protected URISyntax(URI uri) { this.uri = verify(uri); }

  private static URI verify(URI uri) {
    if (uri == null) {
      throw new NullPointerException(" uri is null");
    }
    return uri;
  }

  public URI getURI() { return uri; }

  public int hashCode() { return uri.hashCode(); }

  public boolean equals(Object object) {
    return object != null && object instanceof URISyntax && uri.equals(((URISyntax) object).uri);
  }

  public String toString() { return uri.toString(); }
}
