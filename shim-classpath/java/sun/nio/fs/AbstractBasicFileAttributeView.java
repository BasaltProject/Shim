/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

abstract class AbstractBasicFileAttributeView implements BasicFileAttributeView, DynamicFileAttributeView {
  private static final String kSizeName = "size";
  private static final String kCreationTimeName = "creationTime";
  private static final String kLastAccessTimeName = "lastAccessTime";
  private static final String kLastModifiedTimeName = "lastModifiedTime";
  private static final String kFileKeyName = "fileKey";
  private static final String kIsDirectoryName = "isDirectory";
  private static final String kIsRegularFileName = "isRegularFile";
  private static final String kIsSymbolicLinkName = "isSymbolicLink";
  private static final String kIsOtherName = "isOther";

  static final Set<String> basicAttributeNames = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
      kSizeName, kCreationTimeName, kLastAccessTimeName, kLastModifiedTimeName, kFileKeyName, kIsDirectoryName,
      kIsRegularFileName, kIsSymbolicLinkName, kIsOtherName)));

  protected AbstractBasicFileAttributeView() { }

  @Override
  public String name() {
    return "basic";
  }

  @Override
  public void setAttribute(String attribute, Object value) throws IOException {
    if (attribute.equals(kLastModifiedTimeName)) {
      setTimes((FileTime) value, null, null);
      return;
    }
    if (attribute.equals(kLastAccessTimeName)) {
      setTimes(null, (FileTime) value, null);
      return;
    }
    if (attribute.equals(kCreationTimeName)) {
      setTimes(null, null, (FileTime) value);
      return;
    }
    throw new IllegalArgumentException("'" + name() + ":" + attribute + "' not recognized");
  }

  static class AttributesBuilder {
    private final Set<String> names = new HashSet<String>();
    private final Map<String, Object> map = new HashMap<String, Object>();
    private final boolean copyAll;

    private AttributesBuilder(Set<String> allowed, String[] requested) {
      boolean all = false;
      for (String name : requested) {
        if (name.equals("*")) {
          all = true;
        } else {
          if (!allowed.contains(name)) {
            throw new IllegalArgumentException("'" + name + "' not recognized");
          }
          names.add(name);
        }
      }
      if (all) {
        names.clear();
        names.addAll(allowed);
      }
      this.copyAll = all;
    }

    static AttributesBuilder create(Set<String> allowed, String[] requested) {
      return new AttributesBuilder(allowed, requested);
    }

    boolean match(String name) {
      return names.contains(name);
    }

    void add(String name, Object value) {
      map.put(name, value);
    }

    Map<String, Object> unmodifiableMap() {
      return Collections.unmodifiableMap(map);
    }
  }

  final void addRequestedBasicAttributes(BasicFileAttributes attrs, AttributesBuilder builder) {
    if (builder.match(kSizeName)) {
      builder.add(kSizeName, attrs.size());
    }
    if (builder.match(kCreationTimeName)) {
      builder.add(kCreationTimeName, attrs.creationTime());
    }
    if (builder.match(kLastAccessTimeName)) {
      builder.add(kLastAccessTimeName, attrs.lastAccessTime());
    }
    if (builder.match(kLastModifiedTimeName)) {
      builder.add(kLastModifiedTimeName, attrs.lastModifiedTime());
    }
    if (builder.match(kFileKeyName)) {
      builder.add(kFileKeyName, attrs.fileKey());
    }
    if (builder.match(kIsDirectoryName)) {
      builder.add(kIsDirectoryName, attrs.isDirectory());
    }
    if (builder.match(kIsRegularFileName)) {
      builder.add(kIsRegularFileName, attrs.isRegularFile());
    }
    if (builder.match(kIsSymbolicLinkName)) {
      builder.add(kIsSymbolicLinkName, attrs.isSymbolicLink());
    }
    if (builder.match(kIsOtherName)) {
      builder.add(kIsOtherName, attrs.isOther());
    }
  }

  @Override
  public Map<String, Object> readAttributes(String[] requested) throws IOException {
    AttributesBuilder builder = AttributesBuilder.create(basicAttributeNames, requested);
    BasicFileAttributes attrs = readAttributes();
    addRequestedBasicAttributes(attrs, builder);
    return builder.unmodifiableMap();
  }
}
