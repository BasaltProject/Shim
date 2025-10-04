/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

import java.io.IOException;

public class UserPrincipalNotFoundException extends IOException {
  static final long serialVersionUID = -5369283889045833024L;

  private String name;

  public UserPrincipalNotFoundException(String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
