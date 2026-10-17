/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.IOException;
import java.net.URI;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class FileSystems {
  private FileSystems() { }

  private static class DefaultFileSystemHolder {
    static final FileSystem defaultFileSystem = defaultFileSystem();

    private static FileSystem defaultFileSystem() {
      FileSystemProvider provider = sun.nio.fs.DefaultFileSystemProvider.instance();
      return provider.getFileSystem(URI.create("file:///"));
    }
  }

  public static FileSystem getDefault() {
    return DefaultFileSystemHolder.defaultFileSystem;
  }

  public static FileSystem getFileSystem(URI uri) {
    String scheme = uri.getScheme();
    for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
      if (scheme.equalsIgnoreCase(provider.getScheme())) {
        try {
          return provider.getFileSystem(uri);
        } catch (FileSystemNotFoundException ignore) {
        }
      }
    }
    throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
  }

  public static FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    return newFileSystem(uri, env, null);
  }

  public static FileSystem newFileSystem(URI uri, Map<String, ?> env, ClassLoader loader) throws IOException {
    String scheme = uri.getScheme();
    for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
      if (scheme.equalsIgnoreCase(provider.getScheme())) {
        try {
          return provider.newFileSystem(uri, env);
        } catch (UnsupportedOperationException uoe) {
        }
      }
    }
    if (loader != null) {
      ServiceLoader<FileSystemProvider> sl = ServiceLoader.load(FileSystemProvider.class, loader);
      for (FileSystemProvider provider : sl) {
        if (scheme.equalsIgnoreCase(provider.getScheme())) {
          try {
            return provider.newFileSystem(uri, env);
          } catch (UnsupportedOperationException uoe) {
          }
        }
      }
    }
    throw new ProviderNotFoundException("Provider \"" + scheme + "\" not found");
  }

  public static FileSystem newFileSystem(Path path, ClassLoader loader) throws IOException {
    return newFileSystem(path, Collections.<String, Object>emptyMap(), loader);
  }

  public static FileSystem newFileSystem(Path path) throws IOException {
    return newFileSystem(path, (ClassLoader) null);
  }

  public static FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
    return newFileSystem(path, env, null);
  }

  public static FileSystem newFileSystem(Path path, Map<String, ?> env, ClassLoader loader) throws IOException {
    if (path == null) {
      throw new NullPointerException();
    }
    for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
      try {
        return provider.newFileSystem(path, env);
      } catch (UnsupportedOperationException uoe) {
      }
    }
    if (loader != null) {
      ServiceLoader<FileSystemProvider> sl = ServiceLoader.load(FileSystemProvider.class, loader);
      for (FileSystemProvider provider : sl) {
        try {
          return provider.newFileSystem(path, env);
        } catch (UnsupportedOperationException uoe) {
        }
      }
    }
    throw new ProviderNotFoundException("Provider not found");
  }
}
