/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.print;

public interface Printable {
  int PAGE_EXISTS = 0;
  int NO_SUCH_PAGE = 1;
  int print(java.awt.Graphics p0, java.awt.print.PageFormat p1, int p2) throws java.awt.print.PrinterException;
}
