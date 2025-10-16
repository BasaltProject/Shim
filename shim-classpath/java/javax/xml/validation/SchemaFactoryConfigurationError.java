/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.validation;

public final class SchemaFactoryConfigurationError extends Error {
  private static final long serialVersionUID = 3531438703147750126L;

  public SchemaFactoryConfigurationError() {
  }

  public SchemaFactoryConfigurationError(String message) {
    super(message);
  }

  public SchemaFactoryConfigurationError(Throwable cause) {
    super(cause);
  }

  public SchemaFactoryConfigurationError(String message, Throwable cause) {
    super(message, cause);
  }
}
