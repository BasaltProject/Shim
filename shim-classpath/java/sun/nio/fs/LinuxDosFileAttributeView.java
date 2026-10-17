/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import shim.NioNative;

final class LinuxDosFileAttributeView extends UnixFileAttributeViews.Basic implements DosFileAttributeView {
  private static final String kReadonlyName = "readonly";
  private static final String kArchiveName = "archive";
  private static final String kSystemName = "system";
  private static final String kHiddenName = "hidden";

  private static final String kDosXattrName = "user.DOSATTRIB";

  private static final int kReadonly = 0x01;
  private static final int kHidden = 0x02;
  private static final int kSystem = 0x04;
  private static final int kArchive = 0x20;

  private static final Set<String> dosAttributeNames;

  static {
    Set<String> names = new HashSet<String>(AbstractBasicFileAttributeView.basicAttributeNames);
    names.addAll(Arrays.asList(kReadonlyName, kArchiveName, kSystemName, kHiddenName));
    dosAttributeNames = Collections.unmodifiableSet(names);
  }

  LinuxDosFileAttributeView(UnixPath file, boolean followLinks) {
    super(file, followLinks);
  }

  @Override
  public String name() {
    return "dos";
  }

  @Override
  public void setAttribute(String attribute, Object value) throws IOException {
    if (attribute.equals(kReadonlyName)) {
      setReadOnly((Boolean) value);
      return;
    }
    if (attribute.equals(kHiddenName)) {
      setHidden((Boolean) value);
      return;
    }
    if (attribute.equals(kArchiveName)) {
      setArchive((Boolean) value);
      return;
    }
    if (attribute.equals(kSystemName)) {
      setSystem((Boolean) value);
      return;
    }
    super.setAttribute(attribute, value);
  }

  @Override
  public Map<String, Object> readAttributes(String[] requested) throws IOException {
    AttributesBuilder builder = AttributesBuilder.create(dosAttributeNames, requested);
    DosFileAttributes attrs = readAttributes();
    addRequestedBasicAttributes(attrs, builder);
    if (builder.match(kReadonlyName)) {
      builder.add(kReadonlyName, attrs.isReadOnly());
    }
    if (builder.match(kArchiveName)) {
      builder.add(kArchiveName, attrs.isArchive());
    }
    if (builder.match(kSystemName)) {
      builder.add(kSystemName, attrs.isSystem());
    }
    if (builder.match(kHiddenName)) {
      builder.add(kHiddenName, attrs.isHidden());
    }
    return builder.unmodifiableMap();
  }

  private int getDosAttribute() throws IOException {
    byte[] buf = new byte[24];
    int r = NioNative.getxattr(file.getPathForSysCalls(), kDosXattrName, buf, 0, buf.length, followLinks);
    if (r < 0) {
      if (-r == UnixConstants.ENODATA) {
        return 0;
      }
      new UnixException(-r).rethrowAsIOException(file);
    }
    String value = new String(buf, 0, r, java.nio.charset.StandardCharsets.ISO_8859_1);
    if (value.startsWith("0x")) {
      try {
        return Integer.parseInt(value.substring(2), 16);
      } catch (NumberFormatException ignore) {
      }
    }
    return 0;
  }

  private void updateDosAttribute(int flag, boolean enable) throws IOException {
    int oldValue = getDosAttribute();
    int newValue = oldValue;
    if (enable) {
      newValue |= flag;
    } else {
      newValue &= ~flag;
    }
    if (newValue != oldValue) {
      byte[] data = ("0x" + Integer.toHexString(newValue)).getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
      int r = NioNative.setxattr(file.getPathForSysCalls(), kDosXattrName, data, 0, data.length);
      if (r < 0) {
        new UnixException(-r).rethrowAsIOException(file);
      }
    }
  }

  @Override
  public DosFileAttributes readAttributes() throws IOException {
    final UnixFileAttributes attrs;
    try {
      attrs = UnixFileAttributes.get(file, followLinks);
    } catch (UnixException x) {
      x.rethrowAsIOException(file);
      return null;
    }
    final int dosAttribute = getDosAttribute();
    return new DosFileAttributes() {
      @Override
      public FileTime lastModifiedTime() {
        return attrs.lastModifiedTime();
      }

      @Override
      public FileTime lastAccessTime() {
        return attrs.lastAccessTime();
      }

      @Override
      public FileTime creationTime() {
        return attrs.creationTime();
      }

      @Override
      public boolean isRegularFile() {
        return attrs.isRegularFile();
      }

      @Override
      public boolean isDirectory() {
        return attrs.isDirectory();
      }

      @Override
      public boolean isSymbolicLink() {
        return attrs.isSymbolicLink();
      }

      @Override
      public boolean isOther() {
        return attrs.isOther();
      }

      @Override
      public long size() {
        return attrs.size();
      }

      @Override
      public Object fileKey() {
        return attrs.fileKey();
      }

      @Override
      public boolean isReadOnly() {
        return (dosAttribute & kReadonly) != 0;
      }

      @Override
      public boolean isHidden() {
        return (dosAttribute & kHidden) != 0;
      }

      @Override
      public boolean isArchive() {
        return (dosAttribute & kArchive) != 0;
      }

      @Override
      public boolean isSystem() {
        return (dosAttribute & kSystem) != 0;
      }
    };
  }

  @Override
  public void setReadOnly(boolean value) throws IOException {
    updateDosAttribute(kReadonly, value);
  }

  @Override
  public void setHidden(boolean value) throws IOException {
    updateDosAttribute(kHidden, value);
  }

  @Override
  public void setArchive(boolean value) throws IOException {
    updateDosAttribute(kArchive, value);
  }

  @Override
  public void setSystem(boolean value) throws IOException {
    updateDosAttribute(kSystem, value);
  }
}
