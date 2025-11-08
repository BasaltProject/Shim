/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class AssertionError extends Error {
  private static final long serialVersionUID = -5013299493970297370L;

  public AssertionError() {
  }

  private AssertionError(String detailMessage) {
    super(detailMessage);
  }

  public AssertionError(Object detailMessage) {
    this(String.valueOf(detailMessage));
    if (detailMessage instanceof Throwable) {
      initCause((Throwable) detailMessage);
    }
  }

  public AssertionError(boolean detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError(char detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError(int detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError(long detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError(float detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError(double detailMessage) {
    this(String.valueOf(detailMessage));
  }

  public AssertionError(String message, Throwable cause) {
    super(message, cause);
  }
}
