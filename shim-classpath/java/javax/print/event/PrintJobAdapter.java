/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.event;

public abstract class PrintJobAdapter implements javax.print.event.PrintJobListener {
  protected PrintJobAdapter() {
  }
  public void printDataTransferCompleted(javax.print.event.PrintJobEvent p0) { }
  public void printJobCompleted(javax.print.event.PrintJobEvent p0) { }
  public void printJobFailed(javax.print.event.PrintJobEvent p0) { }
  public void printJobCanceled(javax.print.event.PrintJobEvent p0) { }
  public void printJobNoMoreEvents(javax.print.event.PrintJobEvent p0) { }
  public void printJobRequiresAttention(javax.print.event.PrintJobEvent p0) { }
}
