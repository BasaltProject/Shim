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

/**
 * Used by <code>String</code> to signal that a given index is either less than
 * or greater than the allowed range.
 */
public class StringIndexOutOfBoundsException extends IndexOutOfBoundsException {
  private static final long serialVersionUID = -6762910422159637258L;

  public StringIndexOutOfBoundsException(int index) {
    super("String index out of range: "+index);
  }

  public StringIndexOutOfBoundsException(String message) {
	super(message);
  }

  public StringIndexOutOfBoundsException() {}
}
