/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import javax.management.JMException;

public class OpenDataException extends JMException {
  private static final long serialVersionUID = 8346311255433349870L;

  public OpenDataException() {
    super();
  }

  public OpenDataException(String msg) {
    super(msg);
  }
}
