/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public abstract class FileSystemProvider {
  private static final Object kLock = new Object();
  private static volatile List<FileSystemProvider> installedProviders;
  private static boolean loadingProviders = false;

  protected FileSystemProvider() { }

  public static List<FileSystemProvider> installedProviders() {
    if (installedProviders == null) {
      FileSystemProvider defaultProvider = sun.nio.fs.DefaultFileSystemProvider.instance();
      synchronized (kLock) {
        if (installedProviders == null) {
          if (loadingProviders) {
            throw new Error("Circular loading of installed providers detected");
          }
          loadingProviders = true;
          List<FileSystemProvider> list = new ArrayList<FileSystemProvider>();
          try {
            for (FileSystemProvider provider : ServiceLoader.load(FileSystemProvider.class,
                ClassLoader.getSystemClassLoader())) {
              String scheme = provider.getScheme();
              if (!scheme.equalsIgnoreCase("file")) {
                boolean found = false;
                for (FileSystemProvider p : list) {
                  if (p.getScheme().equalsIgnoreCase(scheme)) {
                    found = true;
                    break;
                  }
                }
                if (!found) {
                  list.add(provider);
                }
              }
            }
          } catch (ServiceConfigurationError e) {
            throw e;
          } finally {
            loadingProviders = false;
          }
          list.add(0, defaultProvider);
          installedProviders = Collections.unmodifiableList(list);
        }
      }
    }
    return installedProviders;
  }

  public abstract String getScheme();

  public abstract FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException;

  public abstract FileSystem getFileSystem(URI uri);

  public abstract Path getPath(URI uri);

  public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
    throw new UnsupportedOperationException();
  }

  public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
    if (options.length > 0) {
      for (OpenOption opt : options) {
        if (opt == StandardOpenOption.APPEND || opt == StandardOpenOption.WRITE) {
          throw new UnsupportedOperationException("'" + opt + "' not allowed");
        }
      }
    }
    return Channels.newInputStream(byteChannelFor(path, options));
  }

  private SeekableByteChannel byteChannelFor(Path path, OpenOption[] options) throws IOException {
    Set<OpenOption> set = new HashSet<OpenOption>();
    Collections.addAll(set, options);
    return newByteChannel(path, set);
  }

  public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
    int len = options.length;
    Set<OpenOption> opts;
    if (len == 0) {
      opts = new HashSet<OpenOption>();
      opts.add(StandardOpenOption.CREATE);
      opts.add(StandardOpenOption.TRUNCATE_EXISTING);
    } else {
      opts = new HashSet<OpenOption>();
      for (OpenOption opt : options) {
        if (opt == StandardOpenOption.READ) {
          throw new IllegalArgumentException("READ not allowed");
        }
        opts.add(opt);
      }
    }
    opts.add(StandardOpenOption.WRITE);
    return Channels.newOutputStream(newByteChannel(path, opts));
  }

  public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options,
      ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
    throw new UnsupportedOperationException();
  }

  public abstract SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
      FileAttribute<?>... attrs) throws IOException;

  public abstract DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
      throws IOException;

  public abstract void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException;

  public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void createLink(Path link, Path existing) throws IOException {
    throw new UnsupportedOperationException();
  }

  public abstract void delete(Path path) throws IOException;

  public boolean deleteIfExists(Path path) throws IOException {
    try {
      delete(path);
      return true;
    } catch (NoSuchFileException ignore) {
      return false;
    }
  }

  public Path readSymbolicLink(Path link) throws IOException {
    throw new UnsupportedOperationException();
  }

  public abstract void copy(Path source, Path target, CopyOption... options) throws IOException;

  public abstract void move(Path source, Path target, CopyOption... options) throws IOException;

  public abstract boolean isSameFile(Path path, Path path2) throws IOException;

  public abstract boolean isHidden(Path path) throws IOException;

  public abstract FileStore getFileStore(Path path) throws IOException;

  public abstract void checkAccess(Path path, AccessMode... modes) throws IOException;

  public abstract <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type,
      LinkOption... options);

  public abstract <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
      throws IOException;

  public <A extends BasicFileAttributes> A readAttributesIfExists(Path path, Class<A> type, LinkOption... options)
      throws IOException {
    try {
      return readAttributes(path, type, options);
    } catch (NoSuchFileException ignore) {
      return null;
    }
  }

  public abstract Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
      throws IOException;

  public abstract void setAttribute(Path path, String attribute, Object value, LinkOption... options)
      throws IOException;

  public boolean exists(Path path, LinkOption... options) {
    try {
      if (followLinks(options)) {
        checkAccess(path);
      } else {
        readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  private static boolean followLinks(LinkOption... options) {
    boolean followLinks = true;
    for (LinkOption opt : options) {
      if (opt == LinkOption.NOFOLLOW_LINKS) {
        followLinks = false;
      } else if (opt == null) {
        throw new NullPointerException();
      }
    }
    return followLinks;
  }
}
