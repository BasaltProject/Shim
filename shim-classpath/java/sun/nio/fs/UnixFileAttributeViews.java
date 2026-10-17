/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import shim.NioNative;

class UnixFileAttributeViews {
  private UnixFileAttributeViews() { }

  static class Basic extends AbstractBasicFileAttributeView {
    protected final UnixPath file;
    protected final boolean followLinks;

    Basic(UnixPath file, boolean followLinks) {
      this.file = file;
      this.followLinks = followLinks;
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
      try {
        UnixFileAttributes attrs = UnixFileAttributes.get(file, followLinks);
        return attrs.asBasic();
      } catch (UnixException x) {
        x.rethrowAsIOException(file);
        return null;
      }
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
      if (lastModifiedTime == null && lastAccessTime == null) {
        return;
      }
      if (!followLinks) {
        try {
          if (!UnixFileAttributes.get(file, false).isSymbolicLink()) {
            // fall through to regular update
          }
        } catch (UnixException x) {
          x.rethrowAsIOException(file);
        }
      }
      long atimeSec = 0;
      long atimeNsec = -1;
      long mtimeSec = 0;
      long mtimeNsec = -1;
      if (lastAccessTime != null) {
        long ns = lastAccessTime.to(TimeUnit.NANOSECONDS);
        atimeSec = ns / 1000_000_000L;
        atimeNsec = ns % 1000_000_000L;
        if (atimeNsec < 0) {
          atimeSec = 0;
          atimeNsec = 0;
        }
      }
      if (lastModifiedTime != null) {
        long ns = lastModifiedTime.to(TimeUnit.NANOSECONDS);
        mtimeSec = ns / 1000_000_000L;
        mtimeNsec = ns % 1000_000_000L;
        if (mtimeNsec < 0) {
          mtimeSec = 0;
          mtimeNsec = 0;
        }
      }
      int r = NioNative.utimens(file.getPathForSysCalls(), atimeSec, atimeNsec, mtimeSec, mtimeNsec, followLinks);
      if (r < 0) {
        new UnixException(-r).rethrowAsIOException(file);
      }
    }
  }

  static class Posix extends Basic implements PosixFileAttributeView {
    private static final String kPermissionsName = "permissions";
    private static final String kOwnerName = "owner";
    private static final String kGroupName = "group";

    static final Set<String> posixAttributeNames;

    static {
      Set<String> names = new HashSet<String>(AbstractBasicFileAttributeView.basicAttributeNames);
      names.addAll(Arrays.asList(kPermissionsName, kOwnerName, kGroupName));
      posixAttributeNames = Collections.unmodifiableSet(names);
    }

    Posix(UnixPath file, boolean followLinks) {
      super(file, followLinks);
    }

    @Override
    public String name() {
      return "posix";
    }

    @Override
    public void setAttribute(String attribute, Object value) throws IOException {
      if (attribute.equals(kPermissionsName)) {
        @SuppressWarnings("unchecked")
        Set<PosixFilePermission> perms = (Set<PosixFilePermission>) value;
        setPermissions(perms);
        return;
      }
      if (attribute.equals(kOwnerName)) {
        setOwner((UserPrincipal) value);
        return;
      }
      if (attribute.equals(kGroupName)) {
        setGroup((GroupPrincipal) value);
        return;
      }
      super.setAttribute(attribute, value);
    }

    final void addRequestedPosixAttributes(PosixFileAttributes attrs, AttributesBuilder builder) {
      addRequestedBasicAttributes(attrs, builder);
      if (builder.match(kPermissionsName)) {
        builder.add(kPermissionsName, attrs.permissions());
      }
      if (builder.match(kOwnerName)) {
        builder.add(kOwnerName, attrs.owner());
      }
      if (builder.match(kGroupName)) {
        builder.add(kGroupName, attrs.group());
      }
    }

    @Override
    public Map<String, Object> readAttributes(String[] requested) throws IOException {
      AttributesBuilder builder = AttributesBuilder.create(posixAttributeNames, requested);
      PosixFileAttributes attrs = readAttributes();
      addRequestedPosixAttributes(attrs, builder);
      return builder.unmodifiableMap();
    }

    @Override
    public UnixFileAttributes readAttributes() throws IOException {
      try {
        return UnixFileAttributes.get(file, followLinks);
      } catch (UnixException x) {
        x.rethrowAsIOException(file);
        return null;
      }
    }

    final void setMode(int mode) throws IOException {
      if (!followLinks) {
        try {
          if (UnixFileAttributes.get(file, false).isSymbolicLink()) {
            throw new UnixException(UnixConstants.ELOOP);
          }
        } catch (UnixException x) {
          x.rethrowAsIOException(file);
        }
      }
      int r = NioNative.chmod(file.getPathForSysCalls(), mode);
      if (r < 0) {
        new UnixException(-r).rethrowAsIOException(file);
      }
    }

    final void setOwners(int uid, int gid) throws IOException {
      int r = NioNative.chown(file.getPathForSysCalls(), uid, gid, followLinks);
      if (r < 0) {
        new UnixException(-r).rethrowAsIOException(file);
      }
    }

    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
      setMode(UnixFileAttributes.toUnixMode(perms));
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
      if (owner == null) {
        throw new NullPointerException("'owner' is null");
      }
      if (!(owner instanceof UnixUserPrincipals.User)) {
        throw new java.nio.file.ProviderMismatchException();
      }
      if (owner instanceof UnixUserPrincipals.Group) {
        throw new IOException("'owner' parameter can't be a group");
      }
      setOwners(((UnixUserPrincipals.User) owner).uid(), -1);
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
      return readAttributes().owner();
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException {
      if (group == null) {
        throw new NullPointerException("'owner' is null");
      }
      if (!(group instanceof UnixUserPrincipals.Group)) {
        throw new java.nio.file.ProviderMismatchException();
      }
      setOwners(-1, ((UnixUserPrincipals.Group) group).gid());
    }
  }

  static class Unix extends Posix {
    private static final String kModeName = "mode";
    private static final String kInoName = "ino";
    private static final String kDevName = "dev";
    private static final String kRdevName = "rdev";
    private static final String kNlinkName = "nlink";
    private static final String kUidName = "uid";
    private static final String kGidName = "gid";
    private static final String kCtimeName = "ctime";

    static final Set<String> unixAttributeNames;

    static {
      Set<String> names = new HashSet<String>(Posix.posixAttributeNames);
      names.addAll(Arrays.asList(kModeName, kInoName, kDevName, kRdevName, kNlinkName, kUidName, kGidName,
          kCtimeName));
      unixAttributeNames = Collections.unmodifiableSet(names);
    }

    Unix(UnixPath file, boolean followLinks) {
      super(file, followLinks);
    }

    @Override
    public String name() {
      return "unix";
    }

    @Override
    public void setAttribute(String attribute, Object value) throws IOException {
      if (attribute.equals(kModeName)) {
        setMode((Integer) value);
        return;
      }
      if (attribute.equals(kUidName)) {
        setOwners((Integer) value, -1);
        return;
      }
      if (attribute.equals(kGidName)) {
        setOwners(-1, (Integer) value);
        return;
      }
      super.setAttribute(attribute, value);
    }

    @Override
    public Map<String, Object> readAttributes(String[] requested) throws IOException {
      AttributesBuilder builder = AttributesBuilder.create(unixAttributeNames, requested);
      UnixFileAttributes attrs = readAttributes();
      addRequestedPosixAttributes(attrs, builder);
      if (builder.match(kModeName)) {
        builder.add(kModeName, attrs.mode());
      }
      if (builder.match(kInoName)) {
        builder.add(kInoName, attrs.ino());
      }
      if (builder.match(kDevName)) {
        builder.add(kDevName, attrs.dev());
      }
      if (builder.match(kRdevName)) {
        builder.add(kRdevName, attrs.rdev());
      }
      if (builder.match(kNlinkName)) {
        builder.add(kNlinkName, attrs.nlink());
      }
      if (builder.match(kUidName)) {
        builder.add(kUidName, attrs.uid());
      }
      if (builder.match(kGidName)) {
        builder.add(kGidName, attrs.gid());
      }
      if (builder.match(kCtimeName)) {
        builder.add(kCtimeName, attrs.ctime());
      }
      return builder.unmodifiableMap();
    }
  }

  static class Owner implements java.nio.file.attribute.FileOwnerAttributeView, DynamicFileAttributeView {
    private final Posix view;

    Owner(UnixPath file, boolean followLinks) {
      this.view = new Posix(file, followLinks);
    }

    @Override
    public String name() {
      return "owner";
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
      return view.getOwner();
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
      view.setOwner(owner);
    }

    @Override
    public void setAttribute(String attribute, Object value) throws IOException {
      if (attribute.equals("owner")) {
        setOwner((UserPrincipal) value);
        return;
      }
      throw new IllegalArgumentException("'" + name() + ":" + attribute + "' not recognized");
    }

    @Override
    public Map<String, Object> readAttributes(String[] requested) throws IOException {
      AbstractBasicFileAttributeView.AttributesBuilder builder =
          AbstractBasicFileAttributeView.AttributesBuilder.create(Collections.singleton("owner"), requested);
      if (builder.match("owner")) {
        builder.add("owner", getOwner());
      }
      return builder.unmodifiableMap();
    }
  }

  static Basic createBasicView(UnixPath file, boolean followLinks) {
    return new Basic(file, followLinks);
  }

  static Posix createPosixView(UnixPath file, boolean followLinks) {
    return new Posix(file, followLinks);
  }

  static Unix createUnixView(UnixPath file, boolean followLinks) {
    return new Unix(file, followLinks);
  }

  static Owner createOwnerView(UnixPath file, boolean followLinks) {
    return new Owner(file, followLinks);
  }
}
