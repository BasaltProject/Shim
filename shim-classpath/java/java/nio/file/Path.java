/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public interface Path extends Comparable<Path>, Iterable<Path>, Watchable {
  public static Path of(String first, String... more) {
    return FileSystems.getDefault().getPath(first, more);
  }

  public static Path of(URI uri) {
    String scheme = uri.getScheme();
    if (scheme == null) {
      throw new IllegalArgumentException("Missing scheme");
    }
    if (scheme.equalsIgnoreCase("file")) {
      return FileSystems.getDefault().provider().getPath(uri);
    }
    for (java.nio.file.spi.FileSystemProvider provider : java.nio.file.spi.FileSystemProvider.installedProviders()) {
      if (provider.getScheme().equalsIgnoreCase(scheme)) {
        return provider.getPath(uri);
      }
    }
    throw new FileSystemNotFoundException("Provider \"" + scheme + "\" not installed");
  }

  FileSystem getFileSystem();

  boolean isAbsolute();

  Path getRoot();

  Path getFileName();

  Path getParent();

  int getNameCount();

  Path getName(int index);

  Path subpath(int beginIndex, int endIndex);

  boolean startsWith(Path other);

  default boolean startsWith(String other) {
    return startsWith(getFileSystem().getPath(other));
  }

  boolean endsWith(Path other);

  default boolean endsWith(String other) {
    return endsWith(getFileSystem().getPath(other));
  }

  Path normalize();

  Path resolve(Path other);

  default Path resolve(String other) {
    return resolve(getFileSystem().getPath(other));
  }

  default Path resolveSibling(Path other) {
    Objects.requireNonNull(other, "other");
    Path parent = getParent();
    return (parent == null) ? other : parent.resolve(other);
  }

  default Path resolveSibling(String other) {
    return resolveSibling(getFileSystem().getPath(other));
  }

  Path relativize(Path other);

  URI toUri();

  Path toAbsolutePath();

  Path toRealPath(LinkOption... options) throws IOException;

  default File toFile() {
    if (getFileSystem() == FileSystems.getDefault()) {
      return new File(toString());
    } else {
      throw new UnsupportedOperationException("Path not associated with default file system.");
    }
  }

  @Override
  int compareTo(Path other);

  @Override
  boolean equals(Object other);

  @Override
  int hashCode();

  @Override
  String toString();

  @Override
  default Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < getNameCount();
      }

      @Override
      public Path next() {
        if (i < getNameCount()) {
          Path result = getName(i);
          i++;
          return result;
        }
        throw new NoSuchElementException();
      }
    };
  }

  @Override
  WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers)
      throws IOException;

  @Override
  default WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
    return register(watcher, events, new WatchEvent.Modifier[0]);
  }

  default Path resolve(String first, String... more) {
    Path result = resolve(first);
    for (String segment : more) {
      result = result.resolve(segment);
    }
    return result;
  }

  default Path resolve(Path first, Path... more) {
    Path result = resolve(first);
    for (Path segment : more) {
      result = result.resolve(segment);
    }
    return result;
  }
}
