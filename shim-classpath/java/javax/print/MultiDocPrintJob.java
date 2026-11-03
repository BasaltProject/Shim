/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface MultiDocPrintJob extends javax.print.DocPrintJob {
  void print(javax.print.MultiDoc p0, javax.print.attribute.PrintRequestAttributeSet p1) throws javax.print.PrintException;
}
