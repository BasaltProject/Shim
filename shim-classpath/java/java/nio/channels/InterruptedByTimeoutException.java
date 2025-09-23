/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;

public class InterruptedByTimeoutException extends IOException {
  private static final long serialVersionUID = -4268008601014042947L;

  public InterruptedByTimeoutException() { }
}
