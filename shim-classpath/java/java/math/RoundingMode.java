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

package java.math;

public enum RoundingMode {

  UP         (0),
  DOWN       (1),
  CEILING    (2),
  FLOOR      (3),
  HALF_UP    (4),
  HALF_DOWN  (5),
  HALF_EVEN  (6),
  UNNECESSARY(7);

  RoundingMode(int oldMode) {
    this.oldMode = oldMode;
  }

  final int oldMode;

  public static RoundingMode valueOf(int rm) {
    switch (rm) {
      case 0:
        return UP;
      case 1:
        return DOWN;
      case 2:
        return CEILING;
      case 3:
        return FLOOR;
      case 4:
        return HALF_UP;
      case 5:
        return HALF_DOWN;
      case 6:
        return HALF_EVEN;
      case 7:
        return UNNECESSARY;
      default:
        throw new IllegalArgumentException("argument out of range");
    }
  }
}
