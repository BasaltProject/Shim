/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.access;

import java.io.FileDescriptor;
import java.io.IOException;

public interface JavaIOFileDescriptorAccess {
  void set(FileDescriptor fdo, int fd);

  int get(FileDescriptor fdo);

  void close(FileDescriptor fdo) throws IOException;
}
