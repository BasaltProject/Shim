/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public abstract class SecureRandomSpi implements java.io.Serializable {
  private static final long serialVersionUID = -2991854161009191830L;

  public SecureRandomSpi() {
  }

  protected SecureRandomSpi(SecureRandomParameters params) {
  }

  protected abstract void engineSetSeed(byte[] seed);

  protected abstract void engineNextBytes(byte[] bytes);

  protected void engineNextBytes(byte[] bytes, SecureRandomParameters params) {
    throw new UnsupportedOperationException();
  }

  protected abstract byte[] engineGenerateSeed(int numBytes);

  protected void engineReseed(SecureRandomParameters params) {
    throw new UnsupportedOperationException();
  }

  protected SecureRandomParameters engineGetParameters() {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
