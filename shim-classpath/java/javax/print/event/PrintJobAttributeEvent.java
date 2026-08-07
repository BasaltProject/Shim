/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.event;

import javax.print.DocPrintJob;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.PrintJobAttributeSet;

public class PrintJobAttributeEvent extends PrintEvent {
  private static final long serialVersionUID = -6534469883874742101L;

  private final PrintJobAttributeSet attributes;

  public PrintJobAttributeEvent(DocPrintJob source, PrintJobAttributeSet attributes) {
    super(source);
    this.attributes = AttributeSetUtilities.unmodifiableView(attributes);
  }

  public DocPrintJob getPrintJob() { return (DocPrintJob) getSource(); }

  public PrintJobAttributeSet getAttributes() { return attributes; }
}
