/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public class RowSetEvent extends java.util.EventObject {
  private static final long serialVersionUID = -1875450876546332005L;

  public RowSetEvent(RowSet source) {
    super(source);
  }
}
