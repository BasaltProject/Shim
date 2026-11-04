/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class SnippetEvent {
  private final Snippet snippet;
  private final Snippet.Status previousStatus;
  private final Snippet.Status status;
  private final boolean signatureChange;
  private final Snippet cause;
  private final String value;
  private final JShellException exception;

  SnippetEvent(Snippet snippet, Snippet.Status previousStatus, Snippet.Status status, boolean signatureChange,
      Snippet cause, String value, JShellException exception) {
    this.snippet = snippet;
    this.previousStatus = previousStatus;
    this.status = status;
    this.signatureChange = signatureChange;
    this.cause = cause;
    this.value = value;
    this.exception = exception;
  }

  public Snippet snippet() {
    return snippet;
  }

  public Snippet.Status previousStatus() {
    return previousStatus;
  }

  public Snippet.Status status() {
    return status;
  }

  public boolean isSignatureChange() {
    return signatureChange;
  }

  public Snippet causeSnippet() {
    return cause;
  }

  public String value() {
    return value;
  }

  public JShellException exception() {
    return exception;
  }

  public String toString() {
    return "SnippetEvent(snippet=" + snippet + ", previousStatus=" + previousStatus + ", status=" + status
        + ", isSignatureChange=" + signatureChange + ", causeSnippet=" + cause + ", value=" + value
        + ", exception=" + exception + ")";
  }
}
