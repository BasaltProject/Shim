/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class NotActiveException extends ObjectStreamException {
  private static final long serialVersionUID = -3893467273049808895L;

  public NotActiveException(String reason) {
    super(reason);
  }

  public NotActiveException() { }
}
