/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

public abstract class CRL {
  private final String type;

  protected CRL(String type) {
    this.type = type;
  }

  public final String getType() {
    return this.type;
  }

  @Override
  public abstract String toString();

  public abstract boolean isRevoked(Certificate cert);
}
