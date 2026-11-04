/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.tools.attach;

public class AgentInitializationException extends Exception {
  private static final long serialVersionUID = -1508756333332806353L;

  private int returnValue;

  public AgentInitializationException() {
    super();
    this.returnValue = 0;
  }

  public AgentInitializationException(String s) {
    super(s);
    this.returnValue = 0;
  }

  public AgentInitializationException(String s, int returnValue) {
    super(s);
    this.returnValue = returnValue;
  }

  public int returnValue() {
    return returnValue;
  }
}
