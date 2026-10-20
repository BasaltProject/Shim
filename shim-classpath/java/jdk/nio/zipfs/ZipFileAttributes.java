/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio.zipfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

final class ZipFileAttributes implements BasicFileAttributes {
  private final ZipFileSystem.Node node;

  ZipFileAttributes(ZipFileSystem.Node node) {
    this.node = node;
  }

  @Override
  public FileTime creationTime() {
    return node.creationTime();
  }

  @Override
  public boolean isDirectory() {
    return node.dir;
  }

  @Override
  public boolean isOther() {
    return false;
  }

  @Override
  public boolean isRegularFile() {
    return !node.dir;
  }

  @Override
  public FileTime lastAccessTime() {
    return node.lastAccessTime();
  }

  @Override
  public FileTime lastModifiedTime() {
    return node.lastModifiedTime();
  }

  @Override
  public long size() {
    return node.dir ? 0 : node.size();
  }

  @Override
  public boolean isSymbolicLink() {
    return false;
  }

  @Override
  public Object fileKey() {
    return null;
  }

  public long compressedSize() {
    return node.dir ? 0 : node.compressedSize();
  }

  public long crc() {
    return node.dir ? 0 : node.crc();
  }

  public int method() {
    return node.dir ? -1 : node.method();
  }

  public byte[] extra() {
    return node.extra == null ? null : node.extra.clone();
  }

  public byte[] comment() {
    return node.comment == null ? null : node.comment.getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(1024);
    java.util.Formatter fm = new java.util.Formatter(sb);
    if (creationTime() != null) {
      fm.format("    creationTime    : %tc%n", creationTime().toMillis());
    } else {
      fm.format("    creationTime    : null%n");
    }
    if (lastAccessTime() != null) {
      fm.format("    lastAccessTime  : %tc%n", lastAccessTime().toMillis());
    } else {
      fm.format("    lastAccessTime  : null%n");
    }
    fm.format("    lastModifiedTime: %tc%n", lastModifiedTime().toMillis());
    fm.format("    isRegularFile   : %b%n", isRegularFile());
    fm.format("    isDirectory     : %b%n", isDirectory());
    fm.format("    isSymbolicLink  : %b%n", isSymbolicLink());
    fm.format("    isOther         : %b%n", isOther());
    fm.format("    fileKey         : %s%n", fileKey());
    fm.format("    size            : %d%n", size());
    fm.format("    compressedSize  : %d%n", compressedSize());
    fm.format("    crc             : %x%n", crc());
    fm.format("    method          : %d%n", method());
    fm.close();
    return sb.toString();
  }
}
