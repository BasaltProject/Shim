/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public interface JavaFileManager extends Closeable, Flushable, OptionChecker {
  interface Location {
    String getName();

    boolean isOutputLocation();

    default boolean isModuleOrientedLocation() {
      return false;
    }
  }

  ClassLoader getClassLoader(Location location);

  Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
      boolean recurse) throws IOException;

  String inferBinaryName(Location location, JavaFileObject file);

  boolean isSameFile(FileObject a, FileObject b);

  boolean handleOption(String current, Iterator<String> remaining);

  boolean hasLocation(Location location);

  JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind)
      throws IOException;

  JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
      FileObject sibling) throws IOException;

  FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException;

  FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling)
      throws IOException;

  void flush() throws IOException;

  void close() throws IOException;
}
