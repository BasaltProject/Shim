/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class InterruptedIOException extends IOException {
  private static final long serialVersionUID = 4020568460727500567L;

  public int bytesTransferred = 0;

  public InterruptedIOException() { }

  public InterruptedIOException(String s) {
    super(s);
  }
}
