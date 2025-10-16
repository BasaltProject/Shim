/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ranges;

public class RangeException extends RuntimeException {
  private static final long serialVersionUID = 2427563372446661889L;

  public short code;

  public static final short BAD_BOUNDARYPOINTS_ERR = 1;
  public static final short INVALID_NODE_TYPE_ERR = 2;

  public RangeException(short code, String message) {
    super(message);
    this.code = code;
  }
}
