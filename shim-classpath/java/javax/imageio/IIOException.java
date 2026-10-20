/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio;

import java.io.IOException;

public class IIOException extends IOException {
  private static final long serialVersionUID = -3216210718638985251L;

  public IIOException(String message) {
    super(message);
  }

  public IIOException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }
}
