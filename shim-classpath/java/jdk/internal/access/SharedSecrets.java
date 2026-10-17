/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.internal.access;

import java.io.FileDescriptor;

public class SharedSecrets {
  private static JavaIOFileDescriptorAccess javaIOFileDescriptorAccess;

  private SharedSecrets() { }

  public static void setJavaIOFileDescriptorAccess(JavaIOFileDescriptorAccess jiofda) {
    javaIOFileDescriptorAccess = jiofda;
  }

  public static JavaIOFileDescriptorAccess getJavaIOFileDescriptorAccess() {
    if (javaIOFileDescriptorAccess == null) {
      ensureClassInitialized(FileDescriptor.class);
    }
    return javaIOFileDescriptorAccess;
  }

  private static void ensureClassInitialized(Class<?> c) {
    try {
      Class.forName(c.getName(), true, c.getClassLoader());
    } catch (ClassNotFoundException e) {
    }
  }
}
