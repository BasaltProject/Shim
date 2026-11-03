/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.event;

public interface PrintJobListener {
  void printDataTransferCompleted(javax.print.event.PrintJobEvent p0);
  void printJobCompleted(javax.print.event.PrintJobEvent p0);
  void printJobFailed(javax.print.event.PrintJobEvent p0);
  void printJobCanceled(javax.print.event.PrintJobEvent p0);
  void printJobNoMoreEvents(javax.print.event.PrintJobEvent p0);
  void printJobRequiresAttention(javax.print.event.PrintJobEvent p0);
}
