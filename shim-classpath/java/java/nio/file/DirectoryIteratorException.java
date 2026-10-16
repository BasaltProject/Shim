/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Objects;

public final class DirectoryIteratorException extends ConcurrentModificationException {
  private static final long serialVersionUID = 1L;

  public DirectoryIteratorException(IOException cause) {
    super(Objects.requireNonNull(cause));
  }

  @Override
  public IOException getCause() {
    return (IOException) super.getCause();
  }
}
