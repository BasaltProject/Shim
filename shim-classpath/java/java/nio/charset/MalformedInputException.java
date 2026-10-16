/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

public class MalformedInputException extends CharacterCodingException {
  private static final long serialVersionUID = -3438823399834806194L;

  private final int inputLength;

  public MalformedInputException(int inputLength) {
    this.inputLength = inputLength;
  }

  public int getInputLength() {
    return inputLength;
  }

  @Override
  public String getMessage() {
    return "Input length = " + inputLength;
  }
}
