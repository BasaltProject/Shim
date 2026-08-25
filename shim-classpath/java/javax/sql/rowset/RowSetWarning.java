/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

import java.sql.SQLException;

public class RowSetWarning extends SQLException {
  private static final long serialVersionUID = 6678332766434564774L;

  public RowSetWarning(String reason) {
    super(reason);
  }

  public RowSetWarning() {
    super();
  }

  public RowSetWarning(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public RowSetWarning(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public RowSetWarning getNextWarning() {
    SQLException warning = getNextException();
    if (warning == null || warning instanceof RowSetWarning) {
      return (RowSetWarning) warning;
    } else {
      throw new Error("RowSetWarning chain holds value that is not a RowSetWarning: ");
    }
  }

  public void setNextWarning(RowSetWarning warning) {
    setNextException(warning);
  }
}
