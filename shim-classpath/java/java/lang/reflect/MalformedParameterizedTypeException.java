/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public class MalformedParameterizedTypeException extends RuntimeException {
  private static final long serialVersionUID = -569655778034777605L;

  public MalformedParameterizedTypeException() {
    super();
  }

  public MalformedParameterizedTypeException(String message) {
    super(message);
  }
}
