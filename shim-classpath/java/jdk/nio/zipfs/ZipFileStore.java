/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio.zipfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

final class ZipFileStore extends FileStore {
  private final ZipPath zpath;

  ZipFileStore(ZipPath zpath) {
    this.zpath = zpath;
  }

  @Override
  public String name() {
    return zpath.getFileSystem() + "" + zpath.toString();
  }

  @Override
  public String type() {
    return "zipfs";
  }

  @Override
  public boolean isReadOnly() {
    return zpath.getFileSystem().isReadOnly();
  }

  @Override
  public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
    return (type == BasicFileAttributeView.class || type == ZipFileAttributeView.class);
  }

  @Override
  public boolean supportsFileAttributeView(String name) {
    return name.equals("basic") || name.equals("zip");
  }

  @Override
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
    if (type == null) {
      throw new NullPointerException();
    }
    return null;
  }

  @Override
  public long getTotalSpace() throws IOException {
    return new ZipFileStoreAttributes(this).totalSpace();
  }

  @Override
  public long getUsableSpace() throws IOException {
    return new ZipFileStoreAttributes(this).usableSpace();
  }

  @Override
  public long getUnallocatedSpace() throws IOException {
    return new ZipFileStoreAttributes(this).unallocatedSpace();
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
    throw new UnsupportedOperationException("does not support the given attribute");
  }

  private static final class ZipFileStoreAttributes {
    private final java.nio.file.FileStore fstore;

    ZipFileStoreAttributes(ZipFileStore fileStore) throws IOException {
      fstore = java.nio.file.Files.getFileStore(fileStore.zpath.getFileSystem().getZipFile());
    }

    long totalSpace() throws IOException {
      return fstore.getTotalSpace();
    }

    long usableSpace() throws IOException {
      return fstore.getUsableSpace();
    }

    long unallocatedSpace() throws IOException {
      return fstore.getUnallocatedSpace();
    }
  }
}
