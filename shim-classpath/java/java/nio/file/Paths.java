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
  private Paths() { }

  public static Path get(String first, String... more) {
    return Path.of(first, more);
  }

  public static Path get(URI uri) {
    return Path.of(uri);
  }
}
