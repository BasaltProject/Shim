/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.net.URI;
import java.util.Objects;

public final class Paths {
  private Paths() {}

  public static Path get(String first, String... more) {
    return FileSystems.getDefault().getPath(first, more);
  }

  public static Path get(URI uri) {
    Objects.requireNonNull(uri, "uri");
    String scheme = uri.getScheme();
    if (scheme == null || !scheme.equalsIgnoreCase("file")) {
      throw new IllegalArgumentException("URI scheme must be 'file': " + uri);
    }
    String path = uri.getPath();
    if (path == null) {
      throw new IllegalArgumentException("URI path cannot be null: " + uri);
    }
    return get(path);
  }
}
