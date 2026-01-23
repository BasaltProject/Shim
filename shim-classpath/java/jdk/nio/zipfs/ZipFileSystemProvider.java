/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio.zipfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipError;
import java.util.zip.ZipException;

public class ZipFileSystemProvider extends FileSystemProvider {
  private final Map<Path, ZipFileSystem> filesystems = new HashMap<Path, ZipFileSystem>();

  public ZipFileSystemProvider() { }

  @Override
  public String getScheme() {
    return "jar";
  }

  protected Path uriToPath(URI uri) {
    String scheme = uri.getScheme();
    if ((scheme == null) || !scheme.equalsIgnoreCase(getScheme())) {
      throw new IllegalArgumentException("URI scheme is not '" + getScheme() + "'");
    }
    try {
      String spec = uri.getRawSchemeSpecificPart();
      int sep = spec.indexOf("!/");
      if (sep != -1) {
        spec = spec.substring(0, sep);
      }
      return Paths.get(new URI(spec)).toAbsolutePath();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  private boolean ensureFile(Path path) {
    try {
      BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
      if (!attrs.isRegularFile()) {
        throw new UnsupportedOperationException();
      }
      return true;
    } catch (IOException ioe) {
      return false;
    }
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    Path path = uriToPath(uri);
    synchronized (filesystems) {
      Path realPath = null;
      if (ensureFile(path)) {
        realPath = path.toRealPath();
        if (filesystems.containsKey(realPath)) {
          throw new FileSystemAlreadyExistsException();
        }
      }
      ZipFileSystem zipfs = null;
      try {
        zipfs = new ZipFileSystem(this, path, env);
      } catch (ZipException ze) {
        String pname = path.toString();
        if (pname.endsWith(".zip") || pname.endsWith(".jar")) {
          throw ze;
        }
        throw new UnsupportedOperationException(ze);
      }
      filesystems.put(realPath != null ? realPath : path.toRealPath(), zipfs);
      return zipfs;
    }
  }

  @Override
  public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
    if (path.getFileSystem() != FileSystems.getDefault()) {
      throw new UnsupportedOperationException();
    }
    ensureFile(path);
    try {
      return new ZipFileSystem(this, path, env);
    } catch (ZipException ze) {
      String pname = path.toString();
      if (pname.endsWith(".zip") || pname.endsWith(".jar")) {
        throw ze;
      }
      throw new UnsupportedOperationException(ze);
    }
  }

  @Override
  public Path getPath(URI uri) {
    String spec = uri.getSchemeSpecificPart();
    int sep = spec.indexOf("!/");
    if (sep == -1) {
      throw new IllegalArgumentException("URI: " + uri + " does not contain path info ex. jar:file:/c:/foo.zip!/BAR");
    }
    return getFileSystem(uri).getPath(spec.substring(sep + 1));
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    synchronized (filesystems) {
      ZipFileSystem zipfs = null;
      try {
        zipfs = filesystems.get(uriToPath(uri).toRealPath());
      } catch (IOException x) {
        // ignore the ioe from toRealPath(), return FSNFE instead
      }
      if (zipfs == null) {
        throw new FileSystemNotFoundException();
      }
      return zipfs;
    }
  }

  void removeFileSystem(Path zfpath, ZipFileSystem zfs) throws IOException {
    synchronized (filesystems) {
      Path tempPath = zfpath;
      try {
        tempPath = zfpath.toRealPath();
      } catch (IOException e) {
        // fall through with the original path
      }
      if (filesystems.get(tempPath) == zfs) {
        filesystems.remove(tempPath);
      }
    }
  }

  @Override
  public boolean isHidden(Path path) {
    toZipPath(path);
    return false;
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    toZipPath(path).getFileSystem().checkAccess(toZipPath(path), modes);
  }

  @Override
  public void copy(Path src, Path target, CopyOption... options) throws IOException {
    if (src instanceof ZipPath && target instanceof ZipPath && toZipPath(src).getFileSystem() == toZipPath(target)
        .getFileSystem()) {
      toZipPath(src).getFileSystem().copyFile(false, toZipPath(src), toZipPath(target), options);
      return;
    }
    copyAcross(src, target, options);
  }

  private static void copyAcross(Path src, Path target, CopyOption... options) throws IOException {
    boolean replace = false;
    for (CopyOption o : options) {
      if (o == java.nio.file.StandardCopyOption.REPLACE_EXISTING) {
        replace = true;
      }
    }
    BasicFileAttributes attrs = Files.readAttributes(src, BasicFileAttributes.class);
    if (replace) {
      Files.deleteIfExists(target);
    }
    if (attrs.isDirectory()) {
      Files.createDirectory(target);
      return;
    }
    try (InputStream in = Files.newInputStream(src)) {
      try (OutputStream out = Files.newOutputStream(target, java.nio.file.StandardOpenOption.CREATE_NEW,
          java.nio.file.StandardOpenOption.WRITE)) {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
          out.write(buf, 0, n);
        }
      }
    }
  }

  @Override
  public void createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
    toZipPath(path).getFileSystem().createDirectory(toZipPath(path), attrs);
  }

  @Override
  public void delete(Path path) throws IOException {
    toZipPath(path).getFileSystem().deleteFile(toZipPath(path), true);
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    if (type == null) {
      throw new NullPointerException();
    }
    ZipPath zp = toZipPath(path);
    if (type == java.nio.file.attribute.BasicFileAttributeView.class || type == ZipFileAttributeView.class) {
      return type.cast(new ZipFileAttributeView(zp, false));
    }
    return null;
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    return new ZipFileStore(toZipPath(path));
  }

  @Override
  public boolean isSameFile(Path path, Path other) throws IOException {
    if (path.equals(other)) {
      return true;
    }
    if (other == null || path.getFileSystem() != other.getFileSystem()) {
      return false;
    }
    toZipPath(path).checkAccess();
    toZipPath(other).checkAccess();
    return toZipPath(path).toRealPath().equals(toZipPath(other).toRealPath());
  }

  @Override
  public void move(Path src, Path target, CopyOption... options) throws IOException {
    if (src instanceof ZipPath && target instanceof ZipPath && toZipPath(src).getFileSystem() == toZipPath(target)
        .getFileSystem()) {
      toZipPath(src).getFileSystem().copyFile(true, toZipPath(src), toZipPath(target), options);
      return;
    }
    copyAcross(src, target, options);
    Files.delete(src);
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    return toZipPath(path).getFileSystem().newByteChannel(toZipPath(path), options, attrs);
  }

  @Override
  public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
      throws IOException {
    final ZipPath zdir = toZipPath(dir);
    final Iterator<Path> it = zdir.getFileSystem().iteratorOf(zdir, filter);
    return new DirectoryStream<Path>() {
      private boolean isClosed;
      private boolean iterated;

      @Override
      public Iterator<Path> iterator() {
        if (isClosed) {
          throw new java.nio.file.ClosedDirectoryStreamException();
        }
        if (iterated) {
          throw new IllegalStateException("Iterator has already been returned");
        }
        iterated = true;
        return new Iterator<Path>() {
          @Override
          public boolean hasNext() {
            return !isClosed && it.hasNext();
          }

          @Override
          public Path next() {
            if (isClosed) {
              throw new java.util.NoSuchElementException();
            }
            return it.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }

      @Override
      public void close() throws IOException {
        isClosed = true;
      }
    };
  }

  @Override
  public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
    for (OpenOption o : options) {
      if (o == java.nio.file.StandardOpenOption.WRITE || o == java.nio.file.StandardOpenOption.APPEND) {
        throw new UnsupportedOperationException("'" + o + "' not allowed");
      }
    }
    return toZipPath(path).getFileSystem().newInputStream(toZipPath(path));
  }

  @Override
  public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
    return toZipPath(path).getFileSystem().newOutputStream(toZipPath(path), options);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
      throws IOException {
    if (type == BasicFileAttributes.class) {
      return (A) toZipPath(path).getFileSystem().getFileAttributes(toZipPath(path));
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
    ZipPath zp = toZipPath(path);
    String view = "basic";
    String attrs = attributes;
    int pos = attributes.indexOf(':');
    if (pos != -1) {
      view = attributes.substring(0, pos);
      attrs = attributes.substring(pos + 1);
    }
    if (view.equals("basic") || view.equals("zip")) {
      return new ZipFileAttributeView(zp, view.equals("zip")).readAttributes(attrs);
    }
    throw new UnsupportedOperationException("view <" + view + "> is not supported");
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
    ZipPath zp = toZipPath(path);
    String view = "basic";
    String name = attribute;
    int pos = attribute.indexOf(':');
    if (pos != -1) {
      view = attribute.substring(0, pos);
      name = attribute.substring(pos + 1);
    }
    if (view.equals("basic") || view.equals("zip")) {
      new ZipFileAttributeView(zp, view.equals("zip")).setAttribute(name, value);
      return;
    }
    throw new UnsupportedOperationException("view <" + view + "> is not supported");
  }

  private ZipPath toZipPath(Path path) {
    if (path == null) {
      throw new NullPointerException();
    }
    if (!(path instanceof ZipPath)) {
      throw new ProviderMismatchException();
    }
    return (ZipPath) path;
  }
}
