/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

final class UnixFileSystem extends FileSystem {
  private final UnixFileSystemProvider provider;
  private final byte[] defaultDirectory;
  private final UnixPath rootDirectory;

  UnixFileSystem(UnixFileSystemProvider provider, String dir) {
    this.provider = provider;
    this.defaultDirectory = new UnixPath(this, UnixPath.normalizeAndCheck(dir)).asByteArray();
    if (this.defaultDirectory[0] != '/') {
      throw new RuntimeException("default directory must be absolute");
    }
    this.rootDirectory = new UnixPath(this, "/");
  }

  byte[] defaultDirectory() {
    return defaultDirectory;
  }

  UnixPath rootDirectory() {
    return rootDirectory;
  }

  @Override
  public FileSystemProvider provider() {
    return provider;
  }

  @Override
  public String getSeparator() {
    return "/";
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void close() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    final List<Path> allowedList = Collections.unmodifiableList(Arrays.asList((Path) rootDirectory));
    return allowedList;
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return new Iterable<FileStore>() {
      @Override
      public Iterator<FileStore> iterator() {
        return new FileStoreIterator();
      }
    };
  }

  private final class FileStoreIterator implements Iterator<FileStore> {
    private final Iterator<UnixMountEntry> entries = UnixMountEntry.readEntries().iterator();
    private FileStore next;

    private FileStore readNext() {
      while (entries.hasNext()) {
        UnixMountEntry entry = entries.next();
        if (entry.isIgnored()) {
          continue;
        }
        try {
          return new UnixFileStore(UnixFileSystem.this, entry);
        } catch (IOException ignore) {
        }
      }
      return null;
    }

    @Override
    public synchronized boolean hasNext() {
      if (next != null) {
        return true;
      }
      next = readNext();
      return next != null;
    }

    @Override
    public synchronized FileStore next() {
      if (next == null) {
        next = readNext();
      }
      if (next == null) {
        throw new NoSuchElementException();
      } else {
        FileStore result = next;
        next = null;
        return result;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return SupportedFileFileAttributeViewsHolder.supportedFileAttributeViews;
  }

  private static class SupportedFileFileAttributeViewsHolder {
    static final Set<String> supportedFileAttributeViews = supportedFileAttributeViews();

    private static Set<String> supportedFileAttributeViews() {
      Set<String> result = new HashSet<String>();
      result.addAll(Arrays.asList("basic", "posix", "unix", "owner"));
      result.add("dos");
      result.add("user");
      return Collections.unmodifiableSet(result);
    }
  }

  @Override
  public UnixPath getPath(String first, String... more) {
    Objects.requireNonNull(first);
    String path;
    if (more.length == 0) {
      path = first;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(first);
      for (String segment : more) {
        if (!segment.isEmpty()) {
          if (sb.length() > 0) {
            sb.append('/');
          }
          sb.append(segment);
        }
      }
      path = sb.toString();
    }
    return new UnixPath(this, path);
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndInput) {
    int pos = syntaxAndInput.indexOf(':');
    if (pos <= 0) {
      throw new IllegalArgumentException();
    }
    String syntax = syntaxAndInput.substring(0, pos);
    String input = syntaxAndInput.substring(pos + 1);
    String expr;
    if (syntax.equalsIgnoreCase("glob")) {
      expr = Globs.toUnixRegexPattern(input);
    } else {
      if (syntax.equalsIgnoreCase("regex")) {
        expr = input;
      } else {
        throw new UnsupportedOperationException("Syntax '" + syntax + "' not recognized");
      }
    }
    final Pattern pattern = Pattern.compile(expr);
    return new PathMatcher() {
      @Override
      public boolean matches(Path path) {
        return pattern.matcher(path.toString()).matches();
      }
    };
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return UnixUserPrincipals.LOOKUP_SERVICE;
  }

  @Override
  public WatchService newWatchService() throws IOException {
    if (System.getProperty("os.name").equals("Linux")) {
      return new LinuxWatchService();
    }
    return new PollingWatchService();
  }
}
