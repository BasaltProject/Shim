/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import shim.NioNative;

class UnixFileStore extends FileStore {
  private final UnixPath file;
  private final long dev;
  private final UnixMountEntry entry;

  UnixFileStore(UnixPath file) throws IOException {
    this.file = file;
    try {
      UnixFileAttributes attrs = UnixFileAttributes.get(file, true);
      this.dev = attrs.dev();
    } catch (UnixException x) {
      x.rethrowAsIOException(file);
      throw new AssertionError();
    }
    this.entry = findMountEntry();
  }

  UnixFileStore(UnixFileSystem fs, UnixMountEntry entry) throws IOException {
    this.file = new UnixPath(fs, entry.dir());
    this.entry = entry;
    try {
      this.dev = UnixFileAttributes.get(file, true).dev();
    } catch (UnixException x) {
      x.rethrowAsIOException(file);
      throw new AssertionError();
    }
  }

  private UnixMountEntry findMountEntry() throws IOException {
    UnixPath path = file;
    UnixPath rp = (UnixPath) path.toRealPath();
    UnixPath parent = rp.getParent();
    while (parent != null) {
      UnixFileAttributes attrs = null;
      try {
        attrs = UnixFileAttributes.get(parent, true);
      } catch (UnixException x) {
        x.rethrowAsIOException(parent);
      }
      if (attrs.dev() != dev) {
        break;
      }
      rp = parent;
      parent = rp.getParent();
    }
    byte[] dir = rp.asByteArray();
    for (UnixMountEntry e : UnixMountEntry.readEntries()) {
      if (e.dir().equals(rp.toString())) {
        return e;
      }
    }
    throw new IOException("Mount point not found");
  }

  UnixPath file() {
    return file;
  }

  long dev() {
    return dev;
  }

  UnixMountEntry entry() {
    return entry;
  }

  @Override
  public String name() {
    return entry.name();
  }

  @Override
  public String type() {
    return entry.fstype();
  }

  @Override
  public boolean isReadOnly() {
    return entry.hasOption("ro");
  }

  private long[] info() throws IOException {
    long[] out = new long[5];
    int r = NioNative.fsinfo(file.getPathForSysCalls(), out);
    if (r < 0) {
      new UnixException(-r).rethrowAsIOException(file);
    }
    return out;
  }

  @Override
  public long getTotalSpace() throws IOException {
    return info()[1];
  }

  @Override
  public long getUsableSpace() throws IOException {
    return info()[3];
  }

  @Override
  public long getUnallocatedSpace() throws IOException {
    return info()[2];
  }

  @Override
  public long getBlockSize() throws IOException {
    return info()[0];
  }

  @Override
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> view) {
    if (view == null) {
      throw new NullPointerException();
    }
    return (V) null;
  }

  @Override
  public Object getAttribute(String attribute) throws IOException {
    if (attribute.equals("totalSpace")) {
      return getTotalSpace();
    }
    if (attribute.equals("usableSpace")) {
      return getUsableSpace();
    }
    if (attribute.equals("unallocatedSpace")) {
      return getUnallocatedSpace();
    }
    throw new UnsupportedOperationException("'" + attribute + "' not recognized");
  }

  @Override
  public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
    if (type == null) {
      throw new NullPointerException();
    }
    if (type == BasicFileAttributeView.class) {
      return true;
    }
    if (type == PosixFileAttributeView.class || type == FileOwnerAttributeView.class) {
      return true;
    }
    if (type == DosFileAttributeView.class || type == UserDefinedFileAttributeView.class) {
      return xattrSupported();
    }
    return false;
  }

  @Override
  public boolean supportsFileAttributeView(String name) {
    if (name.equals("basic") || name.equals("unix")) {
      return true;
    }
    if (name.equals("posix")) {
      return true;
    }
    if (name.equals("owner")) {
      return true;
    }
    if (name.equals("dos") || name.equals("user")) {
      return xattrSupported();
    }
    return false;
  }

  private boolean xattrSupported() {
    int r = NioNative.getxattr(file.getPathForSysCalls(), "user.java", null, 0, 0, true);
    return r != -UnixConstants.ENOTSUP;
  }

  @Override
  public boolean equals(Object ob) {
    if (ob == this) {
      return true;
    }
    if (!(ob instanceof UnixFileStore)) {
      return false;
    }
    UnixFileStore other = (UnixFileStore) ob;
    return (this.dev == other.dev) && this.entry.dir().equals(other.entry.dir())
        && this.entry.name().equals(other.entry.name());
  }

  @Override
  public int hashCode() {
    return (int) (dev ^ (dev >>> 32)) ^ entry.dir().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(entry.dir());
    sb.append(" (");
    sb.append(entry.name());
    sb.append(")");
    return sb.toString();
  }
}
