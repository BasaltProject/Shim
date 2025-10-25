/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.nio.ByteBuffer;

public class SecureClassLoader extends ClassLoader {
  protected SecureClassLoader(ClassLoader parent) {
    super(parent);
  }

  protected SecureClassLoader() {
    super();
  }

  protected SecureClassLoader(String name, ClassLoader parent) {
    super(name, parent);
  }

  protected final Class<?> defineClass(String name, byte[] b, int off, int len, CodeSource cs) {
    return defineClass(name, b, off, len, (ProtectionDomain) null);
  }

  protected final Class<?> defineClass(String name, ByteBuffer b, CodeSource cs) {
    byte[] bytes = new byte[b.remaining()];
    b.get(bytes);
    return defineClass(name, bytes, 0, bytes.length, (ProtectionDomain) null);
  }

  protected PermissionCollection getPermissions(CodeSource codesource) {
    return new Permissions();
  }
}
