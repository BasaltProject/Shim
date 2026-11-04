/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.javadoc.doclet;

public interface Reporter {
  default java.io.PrintWriter getDiagnosticWriter() {
    throw new UnsupportedOperationException();
  }
  default java.io.PrintWriter getStandardWriter() {
    throw new UnsupportedOperationException();
  }
  void print(javax.tools.Diagnostic.Kind kind, com.sun.source.util.DocTreePath docTreePath, java.lang.String string);
  default void print(javax.tools.Diagnostic.Kind kind, com.sun.source.util.DocTreePath docTreePath, int i, int i2, int i3, java.lang.String string) {
    throw new UnsupportedOperationException();
  }
  void print(javax.tools.Diagnostic.Kind kind, java.lang.String string);
  void print(javax.tools.Diagnostic.Kind kind, javax.lang.model.element.Element element, java.lang.String string);
  default void print(javax.tools.Diagnostic.Kind kind, javax.tools.FileObject fileObject, int i, int i2, int i3, java.lang.String string) {
    throw new UnsupportedOperationException();
  }
}
