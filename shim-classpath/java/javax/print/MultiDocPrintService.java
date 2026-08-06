/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface MultiDocPrintService extends javax.print.PrintService {
  javax.print.MultiDocPrintJob createMultiDocPrintJob();
}
