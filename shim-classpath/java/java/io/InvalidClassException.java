/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class InvalidClassException extends ObjectStreamException {
  private static final long serialVersionUID = -4333316296251054416L;

  public String classname;

  public InvalidClassException(String reason) {
    super(reason);
  }

  public InvalidClassException(String cname, String reason) {
    super(reason);
    classname = cname;
  }

  public String getMessage() {
    if (classname == null) {
      return super.getMessage();
    } else {
      return classname + "; " + super.getMessage();
    }
  }
}
