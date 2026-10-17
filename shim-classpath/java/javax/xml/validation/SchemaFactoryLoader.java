/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.validation;

public abstract class SchemaFactoryLoader {
  protected SchemaFactoryLoader() {
  }

  public abstract SchemaFactory newFactory(String schemaLanguage);
}
