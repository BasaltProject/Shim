/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import shim.FileNative;

public final class FileDescriptor {
  int fd;

  public static final FileDescriptor in = new FileDescriptor(0);
  public static final FileDescriptor out = new FileDescriptor(1);
  public static final FileDescriptor err = new FileDescriptor(2);

  public FileDescriptor() {
    fd = -1;
  }

  public FileDescriptor(int fd) {
    this.fd = fd;
  }

  public boolean valid() {
    return fd != -1;
  }

  public void sync() throws SyncFailedException {
    if (fd == -1 || FileNative.sync(fd, true) < 0) {
      throw new SyncFailedException("sync failed");
    }
  }

  void closeAll() throws IOException {
    int old = fd;
    if (old == -1) {
      return;
    }
    fd = -1;
    int r = FileNative.close(old);
    if (r < 0) {
      throw new IOException(FileNative.message(r));
    }
  }
}
