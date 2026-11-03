/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.desktop;

import java.net.URI;

public final class OpenURIEvent extends AppEvent {
  private static final long serialVersionUID = 221209100935933476L;

  URI uri;

  public OpenURIEvent(URI uri) {
    super();
    this.uri = uri;
  }

  public URI getURI() { return uri; }
}
