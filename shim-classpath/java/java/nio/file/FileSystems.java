/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public final class FileSystems {
  private FileSystems() {}

  public static FileSystem getDefault() {
    return DefaultFileSystem.INSTANCE;
  }

  public static FileSystem getFileSystem(URI uri) {
    Objects.requireNonNull(uri, "uri");
    if ("file".equalsIgnoreCase(uri.getScheme())) {
      return DefaultFileSystem.INSTANCE;
    }
    throw new IllegalArgumentException("Unsupported file system scheme: " + uri.getScheme());
  }

  public static FileSystem newFileSystem(URI uri, Map<String, ?> env) {
    throw new UnsupportedOperationException();
  }

  public static FileSystem newFileSystem(Path path, ClassLoader loader) {
    throw new UnsupportedOperationException();
  }
}
