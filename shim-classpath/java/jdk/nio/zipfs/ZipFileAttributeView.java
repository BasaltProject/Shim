/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio.zipfs;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.Map;

final class ZipFileAttributeView implements BasicFileAttributeView {
  private static enum AttrID {
    size,
    creationTime,
    lastAccessTime,
    lastModifiedTime,
    isDirectory,
    isRegularFile,
    isSymbolicLink,
    isOther,
    fileKey,
    compressedSize,
    crc,
    method,
    owner,
    group,
    permissions
  }

  private final ZipPath path;
  private final boolean isZipView;

  ZipFileAttributeView(ZipPath path, boolean isZipView) {
    this.path = path;
    this.isZipView = isZipView;
  }

  @Override
  public String name() {
    return isZipView ? "zip" : "basic";
  }

  @Override
  public ZipFileAttributes readAttributes() throws IOException {
    return path.getFileSystem().getFileAttributes(path);
  }

  @Override
  public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
    path.getFileSystem().setTimes(path, lastModifiedTime, lastAccessTime, createTime);
  }

  void setAttribute(String attribute, Object value) throws IOException {
    try {
      if (AttrID.valueOf(attribute) == AttrID.lastModifiedTime) {
        setTimes((FileTime) value, null, null);
      }
      if (AttrID.valueOf(attribute) == AttrID.lastAccessTime) {
        setTimes(null, (FileTime) value, null);
      }
      if (AttrID.valueOf(attribute) == AttrID.creationTime) {
        setTimes(null, null, (FileTime) value);
      }
    } catch (IllegalArgumentException x) {
      throw new UnsupportedOperationException("'" + attribute + "' is unknown or read-only attribute");
    }
  }

  Map<String, Object> readAttributes(String attributes) throws IOException {
    ZipFileAttributes zfas = readAttributes();
    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    if ("*".equals(attributes)) {
      for (AttrID id : AttrID.values()) {
        try {
          map.put(id.name(), attribute(id, zfas));
        } catch (IllegalArgumentException x) {
          continue;
        }
      }
    } else {
      String[] as = attributes.split(",");
      for (String a : as) {
        try {
          map.put(a, attribute(AttrID.valueOf(a), zfas));
        } catch (IllegalArgumentException x) {
          continue;
        }
      }
    }
    return map;
  }

  Object attribute(AttrID id, ZipFileAttributes zfas) {
    switch (id) {
    case size:
      return zfas.size();
    case creationTime:
      return zfas.creationTime();
    case lastAccessTime:
      return zfas.lastAccessTime();
    case lastModifiedTime:
      return zfas.lastModifiedTime();
    case isDirectory:
      return zfas.isDirectory();
    case isRegularFile:
      return zfas.isRegularFile();
    case isSymbolicLink:
      return zfas.isSymbolicLink();
    case isOther:
      return zfas.isOther();
    case fileKey:
      return zfas.fileKey();
    case compressedSize:
      if (isZipView) {
        return zfas.compressedSize();
      }
      break;
    case crc:
      if (isZipView) {
        return zfas.crc();
      }
      break;
    case method:
      if (isZipView) {
        return zfas.method();
      }
      break;
    default:
      break;
    }
    return null;
  }
}
