/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

@Deprecated
public class Operation {
  private String operation;

  @Deprecated
  public Operation(String op) {
    operation = op;
  }

  public String getOperation() {
    return operation;
  }

  public String toString() {
    return operation;
  }
}
