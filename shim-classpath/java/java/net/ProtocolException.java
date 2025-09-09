/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;

public class ProtocolException extends IOException {
  public ProtocolException() {
    super();
  }

  public ProtocolException(String message) {
    super(message);
  }
}
