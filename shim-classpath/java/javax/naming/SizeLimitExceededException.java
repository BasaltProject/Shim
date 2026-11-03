/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class SizeLimitExceededException extends javax.naming.LimitExceededException {
  private static final long serialVersionUID = 7129289564879168579L;


  public SizeLimitExceededException() {
  }
  public SizeLimitExceededException(java.lang.String string) {
    super(string);
  }

}
