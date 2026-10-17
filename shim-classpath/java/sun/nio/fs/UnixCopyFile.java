/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import shim.FileNative;
import shim.NioNative;

class UnixCopyFile {
  private UnixCopyFile() { }

  private static class Flags {
    boolean replaceExisting;
    boolean atomicMove;
    boolean followLinks;
    boolean interruptible;
    boolean copyBasicAttributes;
    boolean copyPosixAttributes;
    boolean copyNonPosixAttributes;
    boolean failIfUnableToCopyBasic;
    boolean failIfUnableToCopyPosix;
    boolean failIfUnableToCopyNonPosix;

    static Flags fromCopyOptions(CopyOption... options) {
      Flags flags = new Flags();
      flags.followLinks = true;
      for (CopyOption option : options) {
        if (option == StandardCopyOption.REPLACE_EXISTING) {
          flags.replaceExisting = true;
          continue;
        }
        if (option == LinkOption.NOFOLLOW_LINKS) {
          flags.followLinks = false;
          continue;
        }
        if (option == StandardCopyOption.COPY_ATTRIBUTES) {
          flags.copyBasicAttributes = true;
          flags.copyPosixAttributes = true;
          flags.copyNonPosixAttributes = true;
          flags.failIfUnableToCopyBasic = true;
          continue;
        }
        if (option == null) {
          throw new NullPointerException();
        }
        throw new UnsupportedOperationException("Unsupported copy option: " + option);
      }
      return flags;
    }

    static Flags fromMoveOptions(CopyOption... options) {
      Flags flags = new Flags();
      for (CopyOption option : options) {
        if (option == StandardCopyOption.ATOMIC_MOVE) {
          flags.atomicMove = true;
          continue;
        }
        if (option == StandardCopyOption.REPLACE_EXISTING) {
          flags.replaceExisting = true;
          continue;
        }
        if (option == LinkOption.NOFOLLOW_LINKS) {
          continue;
        }
        if (option == null) {
          throw new NullPointerException();
        }
        throw new UnsupportedOperationException("Unsupported option: " + option);
      }
      flags.copyBasicAttributes = true;
      flags.copyPosixAttributes = true;
      flags.copyNonPosixAttributes = true;
      flags.failIfUnableToCopyBasic = true;
      return flags;
    }
  }

  private static void copyDirectory(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags)
      throws IOException {
    try {
      UnixNativeDispatcher.mkdir(target, attrs.mode());
    } catch (UnixException x) {
      x.rethrowAsIOException(target);
    }
    if (!flags.copyBasicAttributes && !flags.copyPosixAttributes && !flags.copyNonPosixAttributes) {
      return;
    }
    boolean done = false;
    try {
      if (flags.copyPosixAttributes) {
        try {
          UnixNativeDispatcher.chown(target, attrs.uid(), attrs.gid(), true);
        } catch (UnixException x) {
          if (flags.failIfUnableToCopyPosix) {
            x.rethrowAsIOException(target);
          }
        }
        try {
          UnixNativeDispatcher.chmod(target, attrs.mode());
        } catch (UnixException x) {
          if (flags.failIfUnableToCopyPosix) {
            x.rethrowAsIOException(target);
          }
        }
      }
      if (flags.copyBasicAttributes) {
        copyTimes(target, attrs, true);
      }
      done = true;
    } finally {
      if (!done) {
        try {
          UnixNativeDispatcher.rmdir(target);
        } catch (UnixException ignore) {
        }
      }
    }
  }

  private static void copyTimes(UnixPath target, UnixFileAttributes attrs, boolean follow) throws IOException {
    long ma = attrs.lastModifiedTime().to(TimeUnit.NANOSECONDS);
    long aa = attrs.lastAccessTime().to(TimeUnit.NANOSECONDS);
    try {
      UnixNativeDispatcher.utimens(target, Math.floorDiv(aa, 1000_000_000L), Math.floorMod(aa, 1000_000_000L),
          Math.floorDiv(ma, 1000_000_000L), Math.floorMod(ma, 1000_000_000L), follow);
    } catch (UnixException x) {
      x.rethrowAsIOException(target);
    }
  }

  private static void copyFile(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags)
      throws IOException {
    int fi = -1;
    try {
      fi = UnixNativeDispatcher.open(source, FileNative.READ, 0);
    } catch (UnixException x) {
      x.rethrowAsIOException(source);
    }
    try {
      int fo = -1;
      try {
        fo = UnixNativeDispatcher.open(target, FileNative.WRITE | FileNative.CREATE_NEW, attrs.mode() & 0777);
      } catch (UnixException x) {
        x.rethrowAsIOException(target);
      }
      boolean complete = false;
      try {
        try {
          transfer(fo, fi);
        } catch (UnixException x) {
          if (x.errno() == 0 && x.getMessage() != null) {
            throw new IOException(x.getMessage());
          }
          x.rethrowAsIOException(source, target);
        }
        if (flags.copyPosixAttributes) {
          try {
            UnixNativeDispatcher.chown(target, attrs.uid(), attrs.gid(), true);
          } catch (UnixException x) {
            if (flags.failIfUnableToCopyPosix) {
              x.rethrowAsIOException(target);
            }
          }
          try {
            UnixNativeDispatcher.chmod(target, attrs.mode());
          } catch (UnixException x) {
            if (flags.failIfUnableToCopyPosix) {
              x.rethrowAsIOException(target);
            }
          }
        }
        if (flags.copyBasicAttributes) {
          copyTimes(target, attrs, true);
        }
        complete = true;
      } finally {
        UnixNativeDispatcher.close(fo);
        if (!complete) {
          try {
            UnixNativeDispatcher.unlink(target);
          } catch (UnixException ignore) {
          }
        }
      }
    } finally {
      UnixNativeDispatcher.close(fi);
    }
  }

  private static void transfer(int dst, int src) throws UnixException {
    byte[] buf = new byte[65536];
    for (;;) {
      int n = FileNative.read(src, buf, 0, buf.length);
      UnixNativeDispatcher.check(n);
      if (n == 0) {
        return;
      }
      int off = 0;
      while (off < n) {
        int w = FileNative.write(dst, buf, off, n - off);
        UnixNativeDispatcher.check(w);
        off += w;
      }
    }
  }

  private static void copyLink(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags)
      throws IOException {
    String linktarget = null;
    try {
      linktarget = UnixNativeDispatcher.readlink(source);
    } catch (UnixException x) {
      x.rethrowAsIOException(source);
    }
    try {
      UnixNativeDispatcher.symlink(linktarget, target);
      if (flags.copyPosixAttributes) {
        try {
          UnixNativeDispatcher.chown(target, attrs.uid(), attrs.gid(), false);
        } catch (UnixException x) {
        }
      }
    } catch (UnixException x) {
      x.rethrowAsIOException(target);
    }
  }

  static void copy(final UnixPath source, final UnixPath target, CopyOption... options) throws IOException {
    final Flags flags = Flags.fromCopyOptions(options);
    UnixFileAttributes sourceAttrs = null;
    UnixFileAttributes targetAttrs = null;
    try {
      sourceAttrs = UnixFileAttributes.get(source, flags.followLinks);
    } catch (UnixException x) {
      x.rethrowAsIOException(source);
    }
    try {
      targetAttrs = UnixFileAttributes.get(target, false);
    } catch (UnixException x) {
    }
    boolean targetExists = (targetAttrs != null);
    if (targetExists) {
      if (sourceAttrs.isSameFile(targetAttrs)) {
        return;
      }
      if (!flags.replaceExisting) {
        throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
      }
      try {
        if (targetAttrs.isDirectory()) {
          UnixNativeDispatcher.rmdir(target);
        } else {
          UnixNativeDispatcher.unlink(target);
        }
      } catch (UnixException x) {
        if (targetAttrs.isDirectory() && (x.errno() == UnixConstants.EEXIST || x.errno() == UnixConstants.ENOTEMPTY)) {
          throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
        }
        x.rethrowAsIOException(target);
      }
    }
    if (sourceAttrs.isDirectory()) {
      copyDirectory(source, sourceAttrs, target, flags);
      return;
    }
    if (sourceAttrs.isSymbolicLink()) {
      copyLink(source, sourceAttrs, target, flags);
      return;
    }
    copyFile(source, sourceAttrs, target, flags);
  }

  static void move(UnixPath source, UnixPath target, CopyOption... options) throws IOException {
    Flags flags = Flags.fromMoveOptions(options);
    if (flags.atomicMove) {
      try {
        UnixNativeDispatcher.rename(source, target);
      } catch (UnixException x) {
        if (x.errno() == UnixConstants.EXDEV) {
          throw new AtomicMoveNotSupportedException(source.getPathForExceptionMessage(),
              target.getPathForExceptionMessage(), x.errorString());
        }
        x.rethrowAsIOException(source, target);
      }
      return;
    }
    UnixFileAttributes sourceAttrs = null;
    UnixFileAttributes targetAttrs = null;
    try {
      sourceAttrs = UnixFileAttributes.get(source, false);
    } catch (UnixException x) {
      x.rethrowAsIOException(source);
    }
    try {
      targetAttrs = UnixFileAttributes.get(target, false);
    } catch (UnixException x) {
    }
    boolean targetExists = (targetAttrs != null);
    if (targetExists) {
      if (sourceAttrs.isSameFile(targetAttrs)) {
        return;
      }
      if (!flags.replaceExisting) {
        throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
      }
      try {
        if (targetAttrs.isDirectory()) {
          UnixNativeDispatcher.rmdir(target);
        } else {
          UnixNativeDispatcher.unlink(target);
        }
      } catch (UnixException x) {
        if (targetAttrs.isDirectory() && (x.errno() == UnixConstants.EEXIST || x.errno() == UnixConstants.ENOTEMPTY)) {
          throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
        }
        x.rethrowAsIOException(target);
      }
    }
    try {
      UnixNativeDispatcher.rename(source, target);
      return;
    } catch (UnixException x) {
      if (x.errno() != UnixConstants.EXDEV && x.errno() != UnixConstants.EISDIR) {
        x.rethrowAsIOException(source, target);
      }
    }
    if (sourceAttrs.isDirectory()) {
      ensureEmptyDir(source);
      copyDirectory(source, sourceAttrs, target, flags);
    } else {
      if (sourceAttrs.isSymbolicLink()) {
        copyLink(source, sourceAttrs, target, flags);
      } else {
        copyFile(source, sourceAttrs, target, flags);
      }
    }
    try {
      if (sourceAttrs.isDirectory()) {
        UnixNativeDispatcher.rmdir(source);
      } else {
        UnixNativeDispatcher.unlink(source);
      }
    } catch (UnixException x) {
      try {
        if (sourceAttrs.isDirectory()) {
          UnixNativeDispatcher.rmdir(target);
        } else {
          UnixNativeDispatcher.unlink(target);
        }
      } catch (UnixException ignore) {
      }
      if (sourceAttrs.isDirectory() && (x.errno() == UnixConstants.EEXIST || x.errno() == UnixConstants.ENOTEMPTY)) {
        throw new DirectoryNotEmptyException(source.getPathForExceptionMessage());
      }
      x.rethrowAsIOException(source);
    }
  }

  private static void ensureEmptyDir(UnixPath dir) throws IOException {
    int[] err = new int[1];
    String[] names = NioNative.listdir(dir.getPathForSysCalls(), err);
    if (names == null) {
      new UnixException(-err[0]).rethrowAsIOException(dir);
    }
    if (names.length > 0) {
      throw new DirectoryNotEmptyException(dir.getPathForExceptionMessage());
    }
  }
}
