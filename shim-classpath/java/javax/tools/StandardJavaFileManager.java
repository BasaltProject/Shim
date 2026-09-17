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

  default Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(
      Collection<? extends Path> paths) {
    java.util.List<File> files = new java.util.ArrayList<File>();
    for (Path p : paths) {
      files.add(p.toFile());
    }
    return getJavaFileObjectsFromFiles(files);
  }

  @Deprecated(since = "13")
  default Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(Iterable<? extends Path> paths) {
    java.util.List<Path> list = new java.util.ArrayList<Path>();
    for (Path p : paths) {
      list.add(p);
    }
    return getJavaFileObjectsFromPaths(list);
  }

  default Iterable<? extends JavaFileObject> getJavaFileObjects(Path... paths) {
    return getJavaFileObjectsFromPaths(java.util.Arrays.asList(paths));
  }

  default void setLocationFromPaths(Location location, Collection<? extends Path> paths) throws IOException {
    java.util.List<File> files = new java.util.ArrayList<File>();
    for (Path p : paths) {
      files.add(p.toFile());
    }
    setLocation(location, files);
  }

  default void setLocationForModule(Location location, String moduleName, Collection<? extends Path> paths)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  default Path asPath(FileObject file) {
    throw new UnsupportedOperationException();
  }

  interface PathFactory {
    Path getPath(String first, String... more);
  }

  default void setPathFactory(PathFactory f) {
  }
}
