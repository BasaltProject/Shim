/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;

public class HashPrintJobAttributeSet extends HashAttributeSet implements PrintJobAttributeSet, Serializable {
  private static final long serialVersionUID = -4204473656070350348L;

  public HashPrintJobAttributeSet() { super(PrintJobAttribute.class); }

  public HashPrintJobAttributeSet(PrintJobAttribute attribute) { super(attribute, PrintJobAttribute.class); }

  public HashPrintJobAttributeSet(PrintJobAttribute[] attributes) { super(attributes, PrintJobAttribute.class); }

  public HashPrintJobAttributeSet(PrintJobAttributeSet attributes) { super(attributes, PrintJobAttribute.class); }
}
