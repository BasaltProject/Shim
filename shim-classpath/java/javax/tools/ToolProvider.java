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
    return new com.sun.tools.javac.api.JavacTool();
  }

  public static DocumentationTool getSystemDocumentationTool() {
    return new shim.javadoc.JavadocTool();
  }

  @Deprecated
  public static ClassLoader getSystemToolClassLoader() {
    return null;
  }
}
