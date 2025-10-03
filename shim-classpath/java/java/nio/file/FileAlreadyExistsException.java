/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class FileAlreadyExistsException extends FileSystemException {
  private static final long serialVersionUID = 7579540934498831181L;

  public FileAlreadyExistsException(String file) {
    super(file);
  }

  public FileAlreadyExistsException(String file, String other, String reason) {
    super(file, other, reason);
  }
}
