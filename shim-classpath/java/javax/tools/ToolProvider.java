/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

public class ToolProvider {
  private ToolProvider() { }

  public static JavaCompiler getSystemJavaCompiler() {
    return null;
  }

  public static DocumentationTool getSystemDocumentationTool() {
    return null;
  }

  @Deprecated
  public static ClassLoader getSystemToolClassLoader() {
    return null;
  }
}
