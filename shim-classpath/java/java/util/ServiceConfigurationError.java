/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

public class ServiceConfigurationError extends Error {
  private static final long serialVersionUID = 7413926997481804442L;

  public ServiceConfigurationError(String msg) {
    super(msg);
  }

  public ServiceConfigurationError(String msg, Throwable cause) {
    super(msg, cause);
  }
}
