/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import jdk.internal.access.JavaIOFileDescriptorAccess;
import jdk.internal.access.SharedSecrets;
import shim.FileNative;

public class UnixFileSystemProvider extends FileSystemProvider {
  private static final String kUserDir = "user.dir";
  private final UnixFileSystem theFileSystem;

  public UnixFileSystemProvider() {
    String userDir = System.getProperty(kUserDir);
    theFileSystem = new UnixFileSystem(this, userDir);
  }

  UnixFileSystem theFileSystem() {
    return theFileSystem;
  }

  @Override
  public final String getScheme() {
    return "file";
  }

  private void checkUri(URI uri) {
    if (!uri.getScheme().equalsIgnoreCase(getScheme())) {
      throw new IllegalArgumentException("URI does not match this provider");
    }
    if (uri.getRawAuthority() != null) {
      throw new IllegalArgumentException("Authority component present");
    }
    String path = uri.getPath();
    if (path == null) {
      throw new IllegalArgumentException("Path component is undefined");
    }
    if (!path.equals("/")) {
      throw new IllegalArgumentException("Path component should be '/'");
    }
    if (uri.getRawQuery() != null) {
      throw new IllegalArgumentException("Query component present");
    }
    if (uri.getRawFragment() != null) {
      throw new IllegalArgumentException("Fragment component present");
    }
  }

  @Override
  public final FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    checkUri(uri);
    throw new FileSystemAlreadyExistsException();
  }

  @Override
  public final FileSystem getFileSystem(URI uri) {
    checkUri(uri);
    return theFileSystem;
  }

  @Override
  public Path getPath(URI uri) {
    return UnixUriUtils.fromUri(theFileSystem, uri);
  }

  static boolean followLinks(LinkOption... options) {
    boolean followLinks = true;
    for (int i = 0; i < options.length; i++) {
      LinkOption option = options[i];
      if (option == LinkOption.NOFOLLOW_LINKS) {
        followLinks = false;
        continue;
      }
      if (option == null) {
        throw new NullPointerException();
      }
      throw new AssertionError("Should not get here");
    }
    return followLinks;
  }

  private static class Flags {
    boolean read;
    boolean write;
    boolean append;
    boolean truncateExisting;
    boolean noFollowLinks;
    boolean create;
    boolean createNew;
    boolean deleteOnClose;
    boolean sync;
    boolean dsync;

    static Flags toFlags(Set<? extends OpenOption> options) {
      Flags flags = new Flags();
      for (OpenOption option : options) {
        if (option instanceof StandardOpenOption) {
          switch ((StandardOpenOption) option) {
            case READ:
              flags.read = true;
              break;
            case WRITE:
              flags.write = true;
              break;
            case APPEND:
              flags.append = true;
              break;
            case TRUNCATE_EXISTING:
              flags.truncateExisting = true;
              break;
            case CREATE:
              flags.create = true;
              break;
            case CREATE_NEW:
              flags.createNew = true;
              break;
            case DELETE_ON_CLOSE:
              flags.deleteOnClose = true;
              break;
            case SPARSE:
              break;
            case SYNC:
              flags.sync = true;
              break;
            case DSYNC:
              flags.dsync = true;
              break;
            default:
              throw new UnsupportedOperationException();
          }
          continue;
        }
        if (option == LinkOption.NOFOLLOW_LINKS) {
          flags.noFollowLinks = true;
          continue;
        }
        if (option == null) {
          throw new NullPointerException();
        }
        throw new UnsupportedOperationException(option + " not supported");
      }
      return flags;
    }
  }

  private FileChannel open(UnixPath path, Set<? extends OpenOption> options, int mode) throws IOException {
    Flags flags = Flags.toFlags(options);
    if (!flags.read && !flags.write) {
      if (flags.append) {
        flags.write = true;
      } else {
        flags.read = true;
      }
    }
    if (flags.read && flags.append) {
      throw new IllegalArgumentException("READ + APPEND not allowed");
    }
    if (flags.append && flags.truncateExisting) {
      throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
    }
    int oflags = 0;
    if (flags.read) {
      oflags |= FileNative.READ | FileNative.ALLOW_DIR;
    }
    if (flags.write) {
      oflags |= FileNative.WRITE;
      if (flags.truncateExisting) {
        oflags |= FileNative.TRUNCATE;
      }
      if (flags.append) {
        oflags |= FileNative.APPEND;
      }
      if (flags.createNew) {
        byte[] pathForSysCall = path.asByteArray();
        if ((pathForSysCall[pathForSysCall.length - 1] == '.') && (pathForSysCall.length == 1
            || (pathForSysCall[pathForSysCall.length - 2] == '/'))) {
          throw new FileAlreadyExistsException(path.getPathForExceptionMessage());
        }
        oflags |= FileNative.CREATE_NEW;
      } else if (flags.create) {
        oflags |= FileNative.CREATE;
      }
    }
    boolean followLinks = true;
    if (!flags.createNew && (flags.noFollowLinks || flags.deleteOnClose)) {
      followLinks = false;
      oflags |= FileNative.NOFOLLOW;
    }
    if (flags.dsync) {
      oflags |= FileNative.DSYNC;
    }
    if (flags.sync) {
      oflags |= FileNative.SYNC;
    }
    int fd = -1;
    try {
      fd = UnixNativeDispatcher.open(path, oflags, mode);
    } catch (UnixException x) {
      if (flags.createNew && (x.errno() == UnixConstants.EISDIR)) {
        x.setError(UnixConstants.EEXIST);
      }
      if (!followLinks && (x.errno() == UnixConstants.ELOOP)) {
        x = new UnixException(x.getMessage() + " (NOFOLLOW_LINKS specified)");
      }
      x.rethrowAsIOException(path);
    }
    if (flags.deleteOnClose) {
      try {
        UnixNativeDispatcher.unlink(path);
      } catch (UnixException ignore) {
      }
    }
    FileDescriptor fdObj = new FileDescriptor();
    JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();
    fdAccess.set(fdObj, fd);
    return sun.nio.ch.FileChannelImpl.open(fdObj, path.toString(), flags.read, flags.write, flags.append, null);
  }

  @Override
  public FileChannel newFileChannel(Path obj, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    UnixPath file = UnixPath.toUnixPath(obj);
    int mode = UnixFileAttributes.toUnixMode(0666, attrs);
    return open(file, options, mode);
  }

  @Override
  public AsynchronousFileChannel newAsynchronousFileChannel(Path obj, Set<? extends OpenOption> options,
      ExecutorService executor, FileAttribute<?>... attrs) throws IOException {
    UnixPath file = UnixPath.toUnixPath(obj);
    UnixFileAttributes.toUnixMode(0666, attrs);
    return AsynchronousFileChannel.open(file, options, executor, attrs);
  }

  @Override
  public SeekableByteChannel newByteChannel(Path obj, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    UnixPath file = UnixPath.toUnixPath(obj);
    int mode = UnixFileAttributes.toUnixMode(0666, attrs);
    return open(file, options, mode);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path obj, DirectoryStream.Filter<? super Path> filter)
      throws IOException {
    UnixPath dir = UnixPath.toUnixPath(obj);
    if (filter == null) {
      throw new NullPointerException();
    }
    return new UnixDirectoryStream(dir, filter);
  }

  @Override
  public void createDirectory(Path obj, FileAttribute<?>... attrs) throws IOException {
    UnixPath dir = UnixPath.toUnixPath(obj);
    int mode = UnixFileAttributes.toUnixMode(0777, attrs);
    try {
      UnixNativeDispatcher.mkdir(dir, mode);
    } catch (UnixException x) {
      if (x.errno() == UnixConstants.EISDIR) {
        throw new FileAlreadyExistsException(dir.toString());
      }
      x.rethrowAsIOException(dir);
    }
  }

  @Override
  public void createSymbolicLink(Path obj1, Path obj2, FileAttribute<?>... attrs) throws IOException {
    UnixPath link = UnixPath.toUnixPath(obj1);
    UnixPath target = UnixPath.toUnixPath(obj2);
    if (attrs.length > 0) {
      UnixFileAttributes.toUnixMode(0, attrs);
      throw new UnsupportedOperationException("Initial file attributes not supported when creating symbolic link");
    }
    try {
      UnixNativeDispatcher.symlink(target.toString(), link);
    } catch (UnixException x) {
      x.rethrowAsIOException(link);
    }
  }

  @Override
  public void createLink(Path obj1, Path obj2) throws IOException {
    UnixPath link = UnixPath.toUnixPath(obj1);
    UnixPath existing = UnixPath.toUnixPath(obj2);
    try {
      UnixNativeDispatcher.link(existing, link);
    } catch (UnixException x) {
      x.rethrowAsIOException(link, existing);
    }
  }

  @Override
  public Path readSymbolicLink(Path obj1) throws IOException {
    UnixPath link = UnixPath.toUnixPath(obj1);
    try {
      String target = UnixNativeDispatcher.readlink(link);
      return new UnixPath(link.getFileSystem(), target);
    } catch (UnixException x) {
      if (x.errno() == UnixConstants.EINVAL) {
        throw new NotLinkException(link.getPathForExceptionMessage());
      }
      x.rethrowAsIOException(link);
      return null;
    }
  }

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {
    UnixCopyFile.copy(UnixPath.toUnixPath(source), UnixPath.toUnixPath(target), options);
  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    UnixCopyFile.move(UnixPath.toUnixPath(source), UnixPath.toUnixPath(target), options);
  }

  @Override
  public void delete(Path path) throws IOException {
    implDelete(path, true);
  }

  @Override
  public boolean deleteIfExists(Path path) throws IOException {
    return implDelete(path, false);
  }

  private boolean implDelete(Path obj, boolean failIfNotExists) throws IOException {
    UnixPath file = UnixPath.toUnixPath(obj);
    UnixFileAttributes attrs = null;
    try {
      attrs = UnixFileAttributes.get(file, false);
      if (attrs.isDirectory()) {
        UnixNativeDispatcher.rmdir(file);
      } else {
        UnixNativeDispatcher.unlink(file);
      }
      return true;
    } catch (UnixException x) {
      if (!failIfNotExists && x.errno() == UnixConstants.ENOENT) {
        return false;
      }
      if (attrs != null && attrs.isDirectory() && (x.errno() == UnixConstants.EEXIST
          || x.errno() == UnixConstants.ENOTEMPTY)) {
        throw new DirectoryNotEmptyException(file.getPathForExceptionMessage());
      }
      x.rethrowAsIOException(file);
      return false;
    }
  }

  @Override
  public boolean isSameFile(Path obj1, Path obj2) throws IOException {
    UnixPath file1 = UnixPath.toUnixPath(obj1);
    if (file1.equals(obj2)) {
      return true;
    }
    if (obj2 == null) {
      throw new NullPointerException();
    }
    if (!(obj2 instanceof UnixPath)) {
      return false;
    }
    UnixPath file2 = (UnixPath) obj2;
    UnixFileAttributes attrs1;
    UnixFileAttributes attrs2;
    try {
      attrs1 = UnixFileAttributes.get(file1, true);
    } catch (UnixException x) {
      x.rethrowAsIOException(file1);
      return false;
    }
    try {
      attrs2 = UnixFileAttributes.get(file2, true);
    } catch (UnixException x) {
      x.rethrowAsIOException(file2);
      return false;
    }
    return attrs1.isSameFile(attrs2);
  }

  @Override
  public boolean isHidden(Path obj) {
    UnixPath file = UnixPath.toUnixPath(obj);
    UnixPath name = file.getFileName();
    if (name == null) {
      return false;
    }
    byte[] filename = name.asByteArray();
    return (filename.length > 0 && filename[0] == '.');
  }

  @Override
  public FileStore getFileStore(Path obj) throws IOException {
    UnixPath file = UnixPath.toUnixPath(obj);
    return new UnixFileStore(file);
  }

  @Override
  public void checkAccess(Path obj, AccessMode... modes) throws IOException {
    UnixPath file = UnixPath.toUnixPath(obj);
    boolean e = false;
    boolean r = false;
    boolean w = false;
    boolean x = false;
    if (modes.length == 0) {
      e = true;
    } else {
      for (AccessMode mode : modes) {
        switch (mode) {
          case READ:
            r = true;
            break;
          case WRITE:
            w = true;
            break;
          case EXECUTE:
            x = true;
            break;
          default:
            throw new AssertionError("Should not get here");
        }
      }
    }
    int mode = 0;
    if (e || r) {
      mode |= (r) ? FileNative.R_OK : FileNative.F_OK;
    }
    if (w) {
      mode |= FileNative.W_OK;
    }
    if (x) {
      mode |= FileNative.X_OK;
    }
    try {
      UnixNativeDispatcher.access(file, mode);
    } catch (UnixException exc) {
      exc.rethrowAsIOException(file);
    }
  }

  @Override
  public boolean exists(Path path, LinkOption... options) {
    if (followLinks(options)) {
      UnixPath file = UnixPath.toUnixPath(path);
      try {
        UnixNativeDispatcher.access(file, FileNative.F_OK);
        return true;
      } catch (UnixException x) {
        return false;
      }
    }
    return super.exists(path, options);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends FileAttributeView> V getFileAttributeView(Path obj, Class<V> type, LinkOption... options) {
    UnixPath file = UnixPath.toUnixPath(obj);
    boolean followLinks = followLinks(options);
    if (type == BasicFileAttributeView.class) {
      return (V) UnixFileAttributeViews.createBasicView(file, followLinks);
    }
    if (type == PosixFileAttributeView.class) {
      return (V) UnixFileAttributeViews.createPosixView(file, followLinks);
    }
    if (type == FileOwnerAttributeView.class) {
      return (V) UnixFileAttributeViews.createOwnerView(file, followLinks);
    }
    if (type == null) {
      throw new NullPointerException();
    }
    if (type == DosFileAttributeView.class) {
      return (V) new LinuxDosFileAttributeView(file, followLinks);
    }
    if (type == UserDefinedFileAttributeView.class) {
      return (V) new LinuxUserDefinedFileAttributeView(file, followLinks);
    }
    return (V) null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A extends BasicFileAttributes> A readAttributes(Path file, Class<A> type, LinkOption... options)
      throws IOException {
    Class<? extends BasicFileAttributeView> view;
    if (type == BasicFileAttributes.class) {
      view = BasicFileAttributeView.class;
    } else if (type == PosixFileAttributes.class) {
      view = PosixFileAttributeView.class;
    } else if (type == DosFileAttributes.class) {
      view = DosFileAttributeView.class;
    } else if (type == null) {
      throw new NullPointerException();
    } else {
      throw new UnsupportedOperationException();
    }
    return (A) getFileAttributeView(file, view, options).readAttributes();
  }

  private static String[] split(String attribute) {
    String[] s = new String[2];
    int pos = attribute.indexOf(':');
    if (pos == -1) {
      s[0] = "basic";
      s[1] = attribute;
    } else {
      s[0] = attribute.substring(0, pos++);
      s[1] = (pos == attribute.length()) ? "" : attribute.substring(pos);
    }
    return s;
  }

  private DynamicFileAttributeView getDynamicView(Path file, String name, LinkOption... options) {
    UnixPath path = UnixPath.toUnixPath(file);
    boolean followLinks = followLinks(options);
    if (name.equals("basic")) {
      return UnixFileAttributeViews.createBasicView(path, followLinks);
    }
    if (name.equals("posix")) {
      return UnixFileAttributeViews.createPosixView(path, followLinks);
    }
    if (name.equals("unix")) {
      return UnixFileAttributeViews.createUnixView(path, followLinks);
    }
    if (name.equals("owner")) {
      return (DynamicFileAttributeView) UnixFileAttributeViews.createOwnerView(path, followLinks);
    }
    if (name.equals("dos")) {
      return new LinuxDosFileAttributeView(path, followLinks);
    }
    if (name.equals("user")) {
      return new LinuxUserDefinedFileAttributeView(path, followLinks);
    }
    return null;
  }

  @Override
  public Map<String, Object> readAttributes(Path file, String attributes, LinkOption... options)
      throws IOException {
    String[] s = split(attributes);
    if (s[0].isEmpty()) {
      throw new IllegalArgumentException(attributes);
    }
    DynamicFileAttributeView view = getDynamicView(file, s[0], options);
    if (view == null) {
      throw new UnsupportedOperationException("View '" + s[0] + "' not available");
    }
    return view.readAttributes(s[1].split(","));
  }

  @Override
  public void setAttribute(Path file, String attribute, Object value, LinkOption... options) throws IOException {
    String[] s = split(attribute);
    if (s[0].isEmpty()) {
      throw new IllegalArgumentException(attribute);
    }
    DynamicFileAttributeView view = getDynamicView(file, s[0], options);
    if (view == null) {
      throw new UnsupportedOperationException("View '" + s[0] + "' not available");
    }
    view.setAttribute(s[1], value);
  }
}
