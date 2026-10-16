/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class UnixDomainSocketAddress extends SocketAddress {
  private static final long serialVersionUID = 92902496589351288L;

  private final transient Path path;

  private UnixDomainSocketAddress(Path path) {
    this.path = path;
  }

  public static UnixDomainSocketAddress of(String pathname) {
    return of(Paths.get(pathname));
  }

  public static UnixDomainSocketAddress of(Path path) {
    if (path.getFileSystem() != Paths.get("").getFileSystem()) {
      throw new IllegalArgumentException("path not on default file system");
    }
    String s = path.toString();
    if (s.indexOf('\u0000') >= 0) {
      throw new IllegalArgumentException("path contains null character");
    }
    return new UnixDomainSocketAddress(path);
  }

  public Path getPath() {
    return path;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof UnixDomainSocketAddress && path.equals(((UnixDomainSocketAddress) o).path);
  }

  @Override
  public String toString() {
    return path.toString();
  }
}
