/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public class MalformedParametersException extends RuntimeException {
  private static final long serialVersionUID = 2079070076719158762L;

  public MalformedParametersException() {
    super();
  }

  public MalformedParametersException(String reason) {
    super(reason);
  }
}
