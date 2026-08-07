/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.event;

import java.util.EventObject;

public class PrintEvent extends EventObject {
  private static final long serialVersionUID = 2286914924430763847L;

  public PrintEvent(Object source) { super(source); }

  public String toString() { return "PrintEvent on " + getSource().toString(); }
}
