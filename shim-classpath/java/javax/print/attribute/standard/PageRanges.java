/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class PageRanges extends javax.print.attribute.SetOfIntegerSyntax implements javax.print.attribute.DocAttribute, javax.print.attribute.PrintRequestAttribute, javax.print.attribute.PrintJobAttribute {
  public PageRanges(int p0) {
    super(p0);
  }
  public PageRanges(int p0, int p1) {
    super(p0, p1);
  }
  public PageRanges(int[][] p0) {
    super(p0);
  }
  public PageRanges(java.lang.String p0) {
    super(p0);
  }
  public final java.lang.String getName() { return "page-ranges"; }
  public boolean equals(java.lang.Object p0) { return super.equals(p0) && p0 instanceof PageRanges; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return PageRanges.class; }
}
