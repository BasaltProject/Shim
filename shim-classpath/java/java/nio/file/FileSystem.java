/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public abstract class FileSystem implements Closeable {
  protected FileSystem() {}

  public abstract void close() throws IOException;

  public abstract boolean isOpen();

  public abstract boolean isReadOnly();

  public abstract String getSeparator();

  public abstract Iterable<Path> getRootDirectories();

  public abstract Path getPath(String first, String... more);

  public abstract PathMatcher getPathMatcher(String syntaxAndPattern);
}
