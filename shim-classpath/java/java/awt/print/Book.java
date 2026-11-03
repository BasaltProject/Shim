/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.print;

import java.util.Vector;

public class Book implements Pageable {
  private final Vector<PageFormat> mPageFormats = new Vector<PageFormat>();
  private final Vector<Printable> mPainters = new Vector<Printable>();
  private final Vector<Integer> mPageCounts = new Vector<Integer>();

  public Book() { }

  public int getNumberOfPages() {
    int total = 0;
    for (int c : mPageCounts) {
      if (c == UNKNOWN_NUMBER_OF_PAGES) {
        return UNKNOWN_NUMBER_OF_PAGES;
      }
      total += c;
    }
    return total;
  }

  public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
    int[] at = locate(pageIndex);
    return mPageFormats.get(at[0]);
  }

  public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
    int[] at = locate(pageIndex);
    return mPainters.get(at[0]);
  }

  private int[] locate(int pageIndex) {
    int remaining = pageIndex;
    for (int i = 0; i < mPageCounts.size(); i++) {
      int count = mPageCounts.get(i);
      if (count == UNKNOWN_NUMBER_OF_PAGES || remaining < count) {
        return new int[] {i, remaining};
      }
      remaining -= count;
    }
    throw new IndexOutOfBoundsException("Invalid page index " + pageIndex);
  }

  public void setPage(int pageIndex, Printable painter, PageFormat page) throws IndexOutOfBoundsException {
    if (painter == null) {
      throw new NullPointerException("painter is null");
    }
    if (page == null) {
      throw new NullPointerException("page is null");
    }
    mPainters.setElementAt(painter, pageIndex);
    mPageFormats.setElementAt(page, pageIndex);
  }

  public void append(Printable painter, PageFormat page) {
    mPainters.addElement(painter);
    mPageFormats.addElement(page);
    mPageCounts.addElement(Integer.valueOf(1));
  }

  public void append(Printable painter, PageFormat page, int numPages) {
    mPainters.addElement(painter);
    mPageFormats.addElement(page);
    mPageCounts.addElement(Integer.valueOf(numPages));
  }
}
