/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class CoderResult {
  private static final int TYPE_UNDERFLOW = 0;
  private static final int TYPE_OVERFLOW = 1;
  private static final int TYPE_MALFORMED = 2;
  private static final int TYPE_UNMAPPABLE = 3;

  public static final CoderResult UNDERFLOW = new CoderResult(TYPE_UNDERFLOW, 0);
  public static final CoderResult OVERFLOW = new CoderResult(TYPE_OVERFLOW, 0);

  private final int type;
  private final int length;

  private CoderResult(int type, int length) {
    this.type = type;
    this.length = length;
  }

  public static CoderResult malformedForLength(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("Non-positive length: " + length);
    }
    return new CoderResult(TYPE_MALFORMED, length);
  }

  public static CoderResult unmappableForLength(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("Non-positive length: " + length);
    }
    return new CoderResult(TYPE_UNMAPPABLE, length);
  }

  public boolean isUnderflow() {
    return type == TYPE_UNDERFLOW;
  }

  public boolean isOverflow() {
    return type == TYPE_OVERFLOW;
  }

  public boolean isError() {
    return type == TYPE_MALFORMED || type == TYPE_UNMAPPABLE;
  }

  public boolean isMalformed() {
    return type == TYPE_MALFORMED;
  }

  public boolean isUnmappable() {
    return type == TYPE_UNMAPPABLE;
  }

  public int length() {
    if (!isError()) {
      throw new UnsupportedOperationException();
    }
    return length;
  }

  public void throwException() throws CharacterCodingException {
    switch (type) {
      case TYPE_UNDERFLOW:
        throw new BufferUnderflowException();
      case TYPE_OVERFLOW:
        throw new BufferOverflowException();
      case TYPE_MALFORMED:
        throw new MalformedInputException(length);
      case TYPE_UNMAPPABLE:
        throw new UnmappableCharacterException(length);
      default:
        throw new CharacterCodingException();
    }
  }

  @Override
  public String toString() {
    switch (type) {
      case TYPE_UNDERFLOW:
        return "UNDERFLOW";
      case TYPE_OVERFLOW:
        return "OVERFLOW";
      case TYPE_MALFORMED:
        return "MALFORMED[" + length + "]";
      case TYPE_UNMAPPABLE:
        return "UNMAPPABLE[" + length + "]";
      default:
        return "CoderResult[" + type + "]";
    }
  }
}
