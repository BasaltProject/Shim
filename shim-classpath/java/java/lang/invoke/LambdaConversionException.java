/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2016, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2016, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.invoke;

public class LambdaConversionException extends java.lang.Exception {
  private static final long serialVersionUID = 300L;

  public LambdaConversionException() { throw new RuntimeException(); }
  public LambdaConversionException(String s) { throw new RuntimeException(); }
  public LambdaConversionException(String s, Throwable th) { throw new RuntimeException(); }
  public LambdaConversionException(Throwable th) { throw new RuntimeException(); }
  public LambdaConversionException(String s, Throwable th, boolean b, boolean b2) { throw new RuntimeException(); }
}
