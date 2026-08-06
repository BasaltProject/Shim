/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface DocPrintJob {
  void print(javax.print.Doc p0, javax.print.attribute.PrintRequestAttributeSet p1) throws javax.print.PrintException;
  javax.print.attribute.PrintJobAttributeSet getAttributes();
  javax.print.PrintService getPrintService();
  void addPrintJobListener(javax.print.event.PrintJobListener p0);
  void removePrintJobListener(javax.print.event.PrintJobListener p0);
  void addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener p0, javax.print.attribute.PrintJobAttributeSet p1);
  void removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener p0);
}
