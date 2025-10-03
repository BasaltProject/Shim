/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;

public class ClosedChannelException extends IOException {
  private static final long serialVersionUID = 882777185433553857L;

  public ClosedChannelException() {
    super();
  }

  public ClosedChannelException(String message) {
    super(message);
  }
}
