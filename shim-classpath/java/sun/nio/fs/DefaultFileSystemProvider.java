/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.nio.file.spi.FileSystemProvider;

public final class DefaultFileSystemProvider {
  private static final UnixFileSystemProvider INSTANCE = new UnixFileSystemProvider();

  private DefaultFileSystemProvider() { }

  public static FileSystemProvider instance() {
    return INSTANCE;
  }

  public static java.nio.file.FileSystem theFileSystem() {
    return INSTANCE.theFileSystem();
  }
}
