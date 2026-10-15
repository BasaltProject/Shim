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

public interface FileVisitor<T> {
  FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException;

  FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException;

  FileVisitResult visitFileFailed(T file, IOException exc) throws IOException;

  FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException;
}
