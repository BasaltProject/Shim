/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class UnresolvedReferenceException extends jdk.jshell.JShellException {
  private static final long serialVersionUID = -5596683305880965231L;


  UnresolvedReferenceException(jdk.jshell.DeclarationSnippet declarationSnippet, java.lang.StackTraceElement[] stackTraceElements) {
    super((java.lang.String) null);
  }

  public jdk.jshell.DeclarationSnippet getSnippet() {
    throw new UnsupportedOperationException();
  }
}
