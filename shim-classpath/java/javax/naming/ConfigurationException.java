/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class ConfigurationException extends NamingException {
  private static final long serialVersionUID = 809000649L;

  public ConfigurationException(String explanation) {
    super(explanation);
  }

  public ConfigurationException() {
    super();
  }
}
