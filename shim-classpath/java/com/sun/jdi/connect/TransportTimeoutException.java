/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect;

public class TransportTimeoutException extends java.io.IOException {
  private static final long serialVersionUID = 4107035242623365074L;


  public TransportTimeoutException() {
  }
  public TransportTimeoutException(java.lang.String string) {
    super(string);
  }

}
