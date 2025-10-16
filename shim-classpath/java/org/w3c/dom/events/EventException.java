/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.events;

public class EventException extends RuntimeException {
  private static final long serialVersionUID = 242753408332692061L;

  public short code;

  public static final short UNSPECIFIED_EVENT_TYPE_ERR = 0;

  public EventException(short code, String message) {
    super(message);
    this.code = code;
  }
}
