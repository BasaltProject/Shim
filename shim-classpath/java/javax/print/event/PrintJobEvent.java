/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.event;

import javax.print.DocPrintJob;

public class PrintJobEvent extends PrintEvent {
  private static final long serialVersionUID = -1711656903622072997L;

  public static final int JOB_CANCELED = 101;
  public static final int JOB_COMPLETE = 102;
  public static final int JOB_FAILED = 103;
  public static final int REQUIRES_ATTENTION = 104;
  public static final int NO_MORE_EVENTS = 105;
  public static final int DATA_TRANSFER_COMPLETE = 106;

  private final int reason;

  public PrintJobEvent(DocPrintJob source, int reason) {
    super(source);
    this.reason = reason;
  }

  public int getPrintEventType() { return reason; }

  public DocPrintJob getPrintJob() { return (DocPrintJob) getSource(); }
}
