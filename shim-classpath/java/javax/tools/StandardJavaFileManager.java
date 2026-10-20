/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface StandardJavaFileManager extends JavaFileManager {
  Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files);

  Iterable<? extends JavaFileObject> getJavaFileObjects(File... files);

  Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names);

  Iterable<? extends JavaFileObject> getJavaFileObjects(String... names);

  void setLocation(Location location, Iterable<? extends File> files) throws IOException;

  Iterable<? extends File> getLocation(Location location);

  default Iterable<? extends Path> getLocationAsPaths(Location location) {
    return null;
  }
}
