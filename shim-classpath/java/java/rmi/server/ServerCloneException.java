/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public class ServerCloneException extends CloneNotSupportedException {
  private static final long serialVersionUID = 6617456357664815945L;

  public Exception detail;

  public ServerCloneException(String s) {
    super(s);
    initCause(null);
  }

  public ServerCloneException(String s, Exception cause) {
    super(s);
    initCause(null);
    detail = cause;
  }

  public String getMessage() {
    if (detail == null) {
      return super.getMessage();
    } else {
      return super.getMessage() + "; nested exception is: \n\t" + detail.toString();
    }
  }

  public Throwable getCause() {
    return detail;
  }
}
