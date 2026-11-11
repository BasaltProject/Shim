/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

public class GSSExceptionImpl extends GSSException {
  private static final long serialVersionUID = 4251197939069005575L;

  private final String message;

  public GSSExceptionImpl(int majorCode, Oid mech) {
    super(majorCode);
    message = super.getMajorString() + ": " + mech;
  }

  public GSSExceptionImpl(int majorCode, String message) {
    super(majorCode);
    this.message = message;
  }

  public GSSExceptionImpl(int majorCode, Exception cause) {
    super(majorCode);
    initCause(cause);
    message = null;
  }

  public String getMessage() {
    return message != null ? message : super.getMessage();
  }
}
