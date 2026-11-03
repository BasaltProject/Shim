/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.sql.SQLException;

public class SerialException extends SQLException {
  private static final long serialVersionUID = -489794565168592690L;

  public SerialException() {
    super();
  }

  public SerialException(String msg) {
    super(msg);
  }
}
