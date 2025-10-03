/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

public class ClosedByInterruptException extends AsynchronousCloseException {
  private static final long serialVersionUID = -4488191543534286750L;

  public ClosedByInterruptException() {
    super();
  }
}
