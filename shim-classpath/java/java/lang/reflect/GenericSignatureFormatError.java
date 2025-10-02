/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public class GenericSignatureFormatError extends ClassFormatError {
  private static final long serialVersionUID = 6709919147137911034L;

  public GenericSignatureFormatError() {
    super();
  }

  public GenericSignatureFormatError(String message) {
    super(message);
  }
}
