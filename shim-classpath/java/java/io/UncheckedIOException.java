/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

import java.util.Objects;

public class UncheckedIOException extends RuntimeException {
  private static final long serialVersionUID = -8134305061645241065L;

  public UncheckedIOException(String message, IOException cause) {
    super(message, Objects.requireNonNull(cause, "cause"));
  }

  public UncheckedIOException(IOException cause) {
    super(Objects.requireNonNull(cause, "cause"));
  }

  @Override
  public IOException getCause() {
    return (IOException) super.getCause();
  }
}
