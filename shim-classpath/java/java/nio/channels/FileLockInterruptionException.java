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

public class FileLockInterruptionException extends IOException {
  private static final long serialVersionUID = 7104080643653532383L;

  public FileLockInterruptionException() {
    super();
  }
}
