/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class ProviderException extends RuntimeException {
  private static final long serialVersionUID = 5256023526693665674L;

  public ProviderException() { }
  public ProviderException(String message) { super(message); }
  public ProviderException(Throwable cause) { super(cause); }
  public ProviderException(String message, Throwable cause) { super(message, cause); }
}
