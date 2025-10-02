/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class InvalidObjectException extends ObjectStreamException {
  private static final long serialVersionUID = 3233174318281839583L;

  public InvalidObjectException(String reason) {
    super(reason);
  }
}
