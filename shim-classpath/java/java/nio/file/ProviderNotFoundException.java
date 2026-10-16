/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class ProviderNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -1880012509822920354L;

  public ProviderNotFoundException() { }

  public ProviderNotFoundException(String msg) {
    super(msg);
  }
}
