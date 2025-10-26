/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class AttributeNotFoundException extends OperationsException {
  private static final long serialVersionUID = 6511584241791106926L;

  public AttributeNotFoundException() {
    super();
  }

  public AttributeNotFoundException(String message) {
    super(message);
  }
}
