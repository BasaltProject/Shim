/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.util.EventObject;

public class SSLSessionBindingEvent extends EventObject {
  private static final long serialVersionUID = 3989172637106345L;

  private final String name;

  public SSLSessionBindingEvent(SSLSession session, String name) {
    super(session);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public SSLSession getSession() {
    return (SSLSession) getSource();
  }
}
