/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class LinkLoopException extends javax.naming.LinkException {
  private static final long serialVersionUID = -3119189944325198009L;


  public LinkLoopException(java.lang.String string) {
    super(string);
  }
  public LinkLoopException() {
  }

}
