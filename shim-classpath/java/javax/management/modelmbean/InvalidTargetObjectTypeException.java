/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

public class InvalidTargetObjectTypeException extends Exception {
  private static final long serialVersionUID = 1190536278266811217L;

  private Exception exception;

  public InvalidTargetObjectTypeException() {
    super("InvalidTargetObjectTypeException: ");
    exception = null;
  }

  public InvalidTargetObjectTypeException(String s) {
    super("InvalidTargetObjectTypeException: " + s);
    exception = null;
  }

  public InvalidTargetObjectTypeException(Exception e, String s) {
    super("InvalidTargetObjectTypeException: " + s + (e != null ? ("\n\t\tTargetException is " + e.toString()) : ""));
    exception = e;
  }
}
