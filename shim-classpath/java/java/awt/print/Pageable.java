/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.print;

public interface Pageable {
  int UNKNOWN_NUMBER_OF_PAGES = -1;
  java.awt.print.PageFormat getPageFormat(int p0) throws java.lang.IndexOutOfBoundsException;
  java.awt.print.Printable getPrintable(int p0) throws java.lang.IndexOutOfBoundsException;
  int getNumberOfPages();
}
