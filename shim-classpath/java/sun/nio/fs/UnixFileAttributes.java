/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import shim.NioNative;

final class UnixFileAttributes implements PosixFileAttributes {
  private final long[] st;

  private volatile UserPrincipal userCache;
  private volatile GroupPrincipal groupCache;
  private volatile UnixFileKey key;

  private UnixFileAttributes(long[] st) {
    this.st = st;
  }

  static UnixFileAttributes get(UnixPath path, boolean followLinks) throws UnixException {
    long[] out = new long[16];
    int r = NioNative.statx(path.getPathForSysCalls(), out, followLinks);
    if (r < 0) {
      throw new UnixException(-r);
    }
    return new UnixFileAttributes(out);
  }

  static UnixFileAttributes get(int fd) throws UnixException {
    long[] out = new long[16];
    int r = NioNative.fstatx(fd, out);
    if (r < 0) {
      throw new UnixException(-r);
    }
    return new UnixFileAttributes(out);
  }

  static UnixFileAttributes getIfExists(UnixPath path) throws UnixException {
    long[] out = new long[16];
    int r = NioNative.statx(path.getPathForSysCalls(), out, true);
    if (r < 0) {
      if (-r == UnixConstants.ENOENT) {
        return null;
      }
      throw new UnixException(-r);
    }
    return new UnixFileAttributes(out);
  }

  java.nio.file.attribute.BasicFileAttributes asBasic() {
    final UnixFileAttributes a = this;
    return new java.nio.file.attribute.BasicFileAttributes() {
      public FileTime lastModifiedTime() {
        return a.lastModifiedTime();
      }

      public FileTime lastAccessTime() {
        return a.lastAccessTime();
      }

      public FileTime creationTime() {
        return a.creationTime();
      }

      public boolean isRegularFile() {
        return a.isRegularFile();
      }

      public boolean isDirectory() {
        return a.isDirectory();
      }

      public boolean isSymbolicLink() {
        return a.isSymbolicLink();
      }

      public boolean isOther() {
        return a.isOther();
      }

      public long size() {
        return a.size();
      }

      public Object fileKey() {
        return a.fileKey();
      }
    };
  }

  boolean isSameFile(UnixFileAttributes attrs) {
    return ((st[NioNative.kIno] == attrs.st[NioNative.kIno]) && (st[NioNative.kDev] == attrs.st[NioNative.kDev]));
  }

  int mode() {
    return (int) st[NioNative.kMode];
  }

  long ino() {
    return st[NioNative.kIno];
  }

  long dev() {
    return st[NioNative.kDev];
  }

  long rdev() {
    return st[NioNative.kRdev];
  }

  int nlink() {
    return (int) st[NioNative.kNlink];
  }

  int uid() {
    return (int) st[NioNative.kUid];
  }

  int gid() {
    return (int) st[NioNative.kGid];
  }

  private static FileTime toFileTime(long sec, long nsec) {
    if (nsec == 0) {
      return FileTime.from(sec, TimeUnit.SECONDS);
    } else {
      try {
        long nanos = Math.addExact(nsec, Math.multiplyExact(sec, 1000_000_000L));
        return FileTime.from(nanos, TimeUnit.NANOSECONDS);
      } catch (ArithmeticException ignore) {
        long micro = sec * 1000_000L + nsec / 1000L;
        return FileTime.from(micro, TimeUnit.MICROSECONDS);
      }
    }
  }

  FileTime ctime() {
    return toFileTime(st[NioNative.kCtimeSec], st[NioNative.kCtimeNsec]);
  }

  boolean isDevice() {
    int type = mode() & UnixConstants.S_IFMT;
    return (type == UnixConstants.S_IFCHR || type == UnixConstants.S_IFBLK || type == UnixConstants.S_IFIFO);
  }

  @Override
  public FileTime lastModifiedTime() {
    return toFileTime(st[NioNative.kMtimeSec], st[NioNative.kMtimeNsec]);
  }

  @Override
  public FileTime lastAccessTime() {
    return toFileTime(st[NioNative.kAtimeSec], st[NioNative.kAtimeNsec]);
  }

  @Override
  public FileTime creationTime() {
    if (st[NioNative.kBirthNsec] >= 0) {
      return toFileTime(st[NioNative.kBirthSec], st[NioNative.kBirthNsec]);
    }
    return lastModifiedTime();
  }

  @Override
  public boolean isRegularFile() {
    return ((mode() & UnixConstants.S_IFMT) == UnixConstants.S_IFREG);
  }

  @Override
  public boolean isDirectory() {
    return ((mode() & UnixConstants.S_IFMT) == UnixConstants.S_IFDIR);
  }

  @Override
  public boolean isSymbolicLink() {
    return ((mode() & UnixConstants.S_IFMT) == UnixConstants.S_IFLNK);
  }

  @Override
  public boolean isOther() {
    int type = mode() & UnixConstants.S_IFMT;
    return (type != UnixConstants.S_IFREG && type != UnixConstants.S_IFDIR && type != UnixConstants.S_IFLNK);
  }

  @Override
  public long size() {
    return st[NioNative.kSize];
  }

  @Override
  public UnixFileKey fileKey() {
    if (key == null) {
      synchronized (this) {
        if (key == null) {
          key = new UnixFileKey(st[NioNative.kDev], st[NioNative.kIno]);
        }
      }
    }
    return key;
  }

  @Override
  public UserPrincipal owner() {
    if (userCache == null) {
      userCache = UnixUserPrincipals.fromUid(uid());
    }
    return userCache;
  }

  @Override
  public GroupPrincipal group() {
    if (groupCache == null) {
      groupCache = UnixUserPrincipals.fromGid(gid());
    }
    return groupCache;
  }

  @Override
  public Set<PosixFilePermission> permissions() {
    int bits = (mode() & 0777);
    java.util.EnumSet<PosixFilePermission> perms = java.util.EnumSet.noneOf(PosixFilePermission.class);
    if ((bits & UnixConstants.S_IRUSR) > 0) {
      perms.add(PosixFilePermission.OWNER_READ);
    }
    if ((bits & UnixConstants.S_IWUSR) > 0) {
      perms.add(PosixFilePermission.OWNER_WRITE);
    }
    if ((bits & UnixConstants.S_IXUSR) > 0) {
      perms.add(PosixFilePermission.OWNER_EXECUTE);
    }
    if ((bits & UnixConstants.S_IRGRP) > 0) {
      perms.add(PosixFilePermission.GROUP_READ);
    }
    if ((bits & UnixConstants.S_IWGRP) > 0) {
      perms.add(PosixFilePermission.GROUP_WRITE);
    }
    if ((bits & UnixConstants.S_IXGRP) > 0) {
      perms.add(PosixFilePermission.GROUP_EXECUTE);
    }
    if ((bits & UnixConstants.S_IROTH) > 0) {
      perms.add(PosixFilePermission.OTHERS_READ);
    }
    if ((bits & UnixConstants.S_IWOTH) > 0) {
      perms.add(PosixFilePermission.OTHERS_WRITE);
    }
    if ((bits & UnixConstants.S_IXOTH) > 0) {
      perms.add(PosixFilePermission.OTHERS_EXECUTE);
    }
    return perms;
  }

  static int toUnixMode(Set<PosixFilePermission> perms) {
    int mode = 0;
    for (PosixFilePermission perm : perms) {
      if (perm == null) {
        throw new NullPointerException();
      }
      switch (perm) {
        case OWNER_READ:
          mode |= UnixConstants.S_IRUSR;
          break;
        case OWNER_WRITE:
          mode |= UnixConstants.S_IWUSR;
          break;
        case OWNER_EXECUTE:
          mode |= UnixConstants.S_IXUSR;
          break;
        case GROUP_READ:
          mode |= UnixConstants.S_IRGRP;
          break;
        case GROUP_WRITE:
          mode |= UnixConstants.S_IWGRP;
          break;
        case GROUP_EXECUTE:
          mode |= UnixConstants.S_IXGRP;
          break;
        case OTHERS_READ:
          mode |= UnixConstants.S_IROTH;
          break;
        case OTHERS_WRITE:
          mode |= UnixConstants.S_IWOTH;
          break;
        case OTHERS_EXECUTE:
          mode |= UnixConstants.S_IXOTH;
          break;
        default:
          break;
      }
    }
    return mode;
  }

  static int toUnixMode(int defaultMode, java.nio.file.attribute.FileAttribute<?>... attrs) {
    int mode = defaultMode;
    for (java.nio.file.attribute.FileAttribute<?> attr : attrs) {
      String name = attr.name();
      if (!name.equals("posix:permissions") && !name.equals("unix:permissions")) {
        throw new UnsupportedOperationException("'" + attr.name() + "' not supported as initial attribute");
      }
      @SuppressWarnings("unchecked")
      Set<PosixFilePermission> perms = (Set<PosixFilePermission>) attr.value();
      mode = toUnixMode(perms);
    }
    return mode;
  }
}
