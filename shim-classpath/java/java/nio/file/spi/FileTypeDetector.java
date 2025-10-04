/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.spi;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FileTypeDetector {
  protected FileTypeDetector() { }

  public abstract String probeContentType(Path path) throws IOException;
}
