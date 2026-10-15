/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class SimpleFileVisitor<T> implements FileVisitor<T> {
  protected SimpleFileVisitor() {}

  @Override
  public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
    Objects.requireNonNull(dir, "dir");
    Objects.requireNonNull(attrs, "attrs");
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(attrs, "attrs");
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
    Objects.requireNonNull(file, "file");
    throw exc;
  }

  @Override
  public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
    Objects.requireNonNull(dir, "dir");
    if (exc != null) {
      throw exc;
    }
    return FileVisitResult.CONTINUE;
  }
}
