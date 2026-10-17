/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public class LSException extends RuntimeException {
  private static final long serialVersionUID = 5371691160978884690L;

  public short code;

  public static final short PARSE_ERR = 81;
  public static final short SERIALIZE_ERR = 82;

  public LSException(short code, String message) {
    super(message);
    this.code = code;
  }
}
