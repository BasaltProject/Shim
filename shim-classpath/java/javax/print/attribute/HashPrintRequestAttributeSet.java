/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

import java.io.Serializable;

public class HashPrintRequestAttributeSet extends HashAttributeSet implements PrintRequestAttributeSet, Serializable {
  private static final long serialVersionUID = 2364756266107751933L;

  public HashPrintRequestAttributeSet() { super(PrintRequestAttribute.class); }

  public HashPrintRequestAttributeSet(PrintRequestAttribute attribute) { super(attribute, PrintRequestAttribute.class); }

  public HashPrintRequestAttributeSet(PrintRequestAttribute[] attributes) { super(attributes, PrintRequestAttribute.class); }

  public HashPrintRequestAttributeSet(PrintRequestAttributeSet attributes) { super(attributes, PrintRequestAttribute.class); }
}
